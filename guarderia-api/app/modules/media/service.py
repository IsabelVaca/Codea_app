import uuid

from sqlalchemy import case, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock
from app.core.errors import ApiError
from app.core.pagination import Page
from app.core.security import CurrentUser, get_child_in_scope
from app.core.storage import UPLOAD_SECONDS, Storage
from app.modules.auth.models import AppUser
from app.modules.parents.models import ParentChildRel
from app.modules.reports.models import Report, ReportType
from app.modules.routines.models import MomentCategory, RoutineMoment

from .models import Media, MediaChild, MediaDeletionRequest, MediaKind, MediaStatus
from .schemas import DeletionRequestIn, DeletionRequestOut, FeedItem, MediaOut, PhotoCategory, UploadIn, UploadOut

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


async def request_deletion(
    db: AsyncSession, current: CurrentUser, media_id: int, data: DeletionRequestIn
) -> tuple[DeletionRequestOut, bool]:
    """El botón "Pedir que se borre": solo sobre fotos donde aparece un hijo del padre."""
    visible = await db.scalar(
        select(MediaChild.media_id)
        .join(ParentChildRel, ParentChildRel.child_id == MediaChild.child_id)
        .where(MediaChild.media_id == media_id, ParentChildRel.parent_id == current.id)
        .limit(1)
    )
    if visible is None:
        raise ApiError(404, "MEDIA_NOT_FOUND", "No existe esa foto")
    result = await db.execute(
        insert(MediaDeletionRequest)
        .values(media_id=media_id, requested_by=current.id, reason=(data.reason or "").strip() or None)
        .on_conflict_do_nothing()
        .returning(MediaDeletionRequest.id)
    )
    created = result.scalar() is not None
    await db.commit()
    request = await db.scalar(
        select(MediaDeletionRequest).where(
            MediaDeletionRequest.media_id == media_id, MediaDeletionRequest.requested_by == current.id
        )
    )
    return DeletionRequestOut.model_validate(request), created
