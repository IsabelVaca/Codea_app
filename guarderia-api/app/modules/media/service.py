import uuid

from sqlalchemy import case, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock
from app.core.errors import ApiError
from app.core.pagination import Page
from app.core.security import CurrentUser, get_child_in_scope
from app.core.storage import UPLOAD_SECONDS, Storage
from app.modules.audit import service as audit
from app.modules.auth.models import AppUser
from app.modules.parents.models import ParentChildRel
from app.modules.reports.models import Report, ReportType
from app.modules.routines.models import MomentCategory, RoutineMoment

from .models import Media, MediaChild, MediaKind, MediaStatus
from .schemas import (
    FeedItem,
    MediaOut,
    PhotoCategory,
    UploadIn,
    UploadOut,
)

MAX_BYTES = 10 * 1024 * 1024

_IMAGES = {"image/jpeg": "jpg", "image/png": "png", "image/webp": "webp"}
ALLOWED_TYPES = {
    MediaKind.PHOTO: _IMAGES,
    MediaKind.DOCUMENT: {**_IMAGES, "application/pdf": "pdf"},
}


async def request_upload(db: AsyncSession, current: CurrentUser, storage: Storage, data: UploadIn) -> UploadOut:
    """Pide permiso para subir un archivo. La foto no pasa por la API: va directo al bucket."""
    # las fotos las suben las docentes; los documentos, los papás. El administrador, ambos.
    can_upload = current.admin is not None or (
        current.teacher is not None if data.kind == MediaKind.PHOTO else current.parent is not None
    )
    if not can_upload:
        raise ApiError(403, "FORBIDDEN", "Tu rol no puede subir este tipo de archivo")
    extension = ALLOWED_TYPES[data.kind].get(data.content_type)
    if extension is None:
        raise ApiError(422, "CONTENT_TYPE_NOT_ALLOWED", "Ese tipo de archivo no se puede subir")
    if data.size_bytes > MAX_BYTES:
        raise ApiError(422, "FILE_TOO_LARGE", "El archivo pesa más de 10 MB")

    # la llave no lleva datos personales.
    key = f"media/{clock.now():%Y/%m}/{uuid.uuid4()}/original.{extension}"
    media = Media(
        kind=data.kind,
        storage_key=key,
        content_type=data.content_type,
        size_bytes=data.size_bytes,
        status=MediaStatus.PENDING,
        uploaded_by=current.id,
    )
    db.add(media)
    await db.commit()
    await db.refresh(media)
    post = storage.presign_upload(key, data.content_type, MAX_BYTES)
    return UploadOut(
        media_id=media.id, url=post["url"], fields=post["fields"], expires_in=UPLOAD_SECONDS, max_bytes=MAX_BYTES
    )


async def get_own_media(db: AsyncSession, current: CurrentUser, media_id: int) -> Media:
    media = await db.get(Media, media_id)
    if media is None or media.uploaded_by != current.id:
        raise ApiError(404, "MEDIA_NOT_FOUND", "No existe ese archivo")
    return media


async def complete(db: AsyncSession, current: CurrentUser, storage: Storage, media_id: int) -> MediaOut:
    """La app avisa que ya subió el archivo; la API revisa que sí esté en el bucket."""
    media = await get_own_media(db, current, media_id)
    if media.status != MediaStatus.READY:
        size = await storage.size_of(media.storage_key)
        if size is None:
            raise ApiError(409, "UPLOAD_MISSING", "El archivo todavía no llega al almacén")
        media.size_bytes = size
        media.status = MediaStatus.READY
        await db.commit()
        await db.refresh(media)
    return MediaOut.model_validate(media)


_category = case(
    (Report.type == ReportType.INCIDENT, PhotoCategory.INCIDENTS.value),
    (RoutineMoment.category == MomentCategory.MEAL, PhotoCategory.MEALS.value),
    (RoutineMoment.category == MomentCategory.NAP, PhotoCategory.NAP.value),
    else_=PhotoCategory.ACTIVITIES.value,
)


async def feed(
    db: AsyncSession,
    current: CurrentUser,
    storage: Storage,
    child_id: int,
    category: PhotoCategory | None,
    cursor: int | None,
    limit: int,
) -> Page[FeedItem]:
    """Las fotos donde aparece el niño, de la más nueva a la más vieja."""
    child = await get_child_in_scope(db, current, child_id)
    query = (
        select(Media, Report.description, AppUser, _category.label("category"))
        .join(MediaChild, (MediaChild.media_id == Media.id) & (MediaChild.child_id == child.id))
        .join(AppUser, AppUser.id == Media.uploaded_by)
        .outerjoin(Report, Report.media_id == Media.id)
        .outerjoin(RoutineMoment, RoutineMoment.id == Report.routine_moment_id)
        .where(Media.kind == MediaKind.PHOTO, Media.status == MediaStatus.READY)
    )
    if category is not None:
        query = query.where(_category == category.value)
    if cursor is not None:
        query = query.where(Media.id < cursor)
    rows = (await db.execute(query.order_by(Media.id.desc()).limit(limit + 1))).all()
    next_cursor = str(rows[limit - 1][0].id) if len(rows) > limit else None
    rows = rows[:limit]

    tagged: dict[int, list[int]] = {}
    for media_id, tagged_child in (
        await db.execute(
            select(MediaChild.media_id, MediaChild.child_id)
            .where(MediaChild.media_id.in_([media.id for media, *_ in rows]))
            .order_by(MediaChild.child_id)
        )
    ).all():
        tagged.setdefault(media_id, []).append(tagged_child)

    return Page(
        items=[
            FeedItem(
                id=media.id,
                url=storage.presign_get(media.storage_key),
                category=PhotoCategory(category_value),
                taken_at=media.created_at,
                teacher_name=author.full_name,
                note=description,
                child_ids=tagged.get(media.id, []),
            )
            for media, description, author, category_value in rows
        ],
        next_cursor=next_cursor,
    )


async def delete_own_photo(db: AsyncSession, current: CurrentUser, storage: Storage, media_id: int) -> None:
    """El botón "Borrar" de la familia: borra la foto del bucket y de todas las pantallas, sin pedirle permiso a nadie.

    Basta con que salga un hijo de quien la borra. Si en la foto salen también niños de otras familias, se borra
    igual: la decisión es de la familia que aparece en ella (cambio pedido el 19/oct).
    """
    media = await db.scalar(
        select(Media)
        .where(Media.id == media_id, Media.kind == MediaKind.PHOTO, Media.status != MediaStatus.DELETED)
        .with_for_update()
    )
    tagged = set((await db.scalars(select(MediaChild.child_id).where(MediaChild.media_id == media_id))).all())
    mine = set(
        (
            await db.scalars(
                select(ParentChildRel.child_id).where(
                    ParentChildRel.parent_id == current.id, ParentChildRel.child_id.in_(tagged or {0})
                )
            )
        ).all()
    )
    if media is None or not mine:
        raise ApiError(404, "MEDIA_NOT_FOUND", "No existe esa foto")
    # primero el bucket: si falla, no se marca nada como borrado.
    try:
        await storage.delete(media.storage_key)
    except Exception as error:
        raise ApiError(502, "STORAGE_UNAVAILABLE", "No se pudo borrar el archivo; intenta de nuevo") from error
    media.status = MediaStatus.DELETED
    # los reportes conservan su texto, pero ya no apuntan a una foto que no existe.
    await db.execute(update(Report).where(Report.media_id == media.id).values(media_id=None))
    audit.record(
        db,
        audit.MEDIA_DELETED_BY_FAMILY,
        actor_id=current.id,
        entity_type="MEDIA",
        entity_id=media.id,
        detail={"children": sorted(tagged)} if tagged - mine else None,
    )
    await db.commit()
