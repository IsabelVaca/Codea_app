import uuid

from sqlalchemy import case, select, update
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock
from app.core.errors import ApiError
from app.core.pagination import Page
from app.core.security import CurrentUser, get_child_in_scope
from app.core.storage import UPLOAD_SECONDS, Storage
from app.modules.audit import service as audit
from app.modules.auth.models import AppUser
from app.modules.children.models import Child
from app.modules.parents.models import ParentChildRel
from app.modules.reports.models import Report, ReportType
from app.modules.routines.models import MomentCategory, RoutineMoment

from .models import DeletionStatus, Media, MediaChild, MediaDeletionRequest, MediaKind, MediaStatus
from .schemas import (
    DeletionDecisionIn,
    DeletionRequestIn,
    DeletionRequestOut,
    DeletionReviewOut,
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


async def delete_own_photo(db: AsyncSession, current: CurrentUser, storage: Storage, media_id: int) -> None:
    """El botón "Borrar" de la familia: borra la foto del bucket y de todas las pantallas, sin pasar por el admin.

    Solo si todos los niños etiquetados son hijos de quien la borra. Si sale algún niño de otra familia, la foto
    no es solo suya: responde 409 y la decisión queda con la dirección.
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
    if tagged - mine:
        raise ApiError(
            409, "MEDIA_SHARED", "En esta foto salen niños de otras familias; pide a la dirección que decida si se borra"
        )
    # primero el bucket: si falla, no se marca nada como borrado.
    try:
        await storage.delete(media.storage_key)
    except Exception as error:
        raise ApiError(502, "STORAGE_UNAVAILABLE", "No se pudo borrar el archivo; intenta de nuevo") from error
    media.status = MediaStatus.DELETED
    # los reportes conservan su texto, pero ya no apuntan a una foto que no existe.
    await db.execute(update(Report).where(Report.media_id == media.id).values(media_id=None))
    # una solicitud de borrado anterior queda resuelta con ella.
    await db.execute(
        update(MediaDeletionRequest)
        .where(MediaDeletionRequest.media_id == media.id, MediaDeletionRequest.status == DeletionStatus.PENDING)
        .values(
            status=DeletionStatus.APPROVED,
            decided_by=current.id,
            decided_at=clock.now(),
            decision_note="La familia la borró",
        )
    )
    audit.record(
        db, audit.MEDIA_DELETED_BY_FAMILY, actor_id=current.id, entity_type="MEDIA", entity_id=media.id, detail=None
    )
    await db.commit()


# ---------------------------------------------------------------- revisión del admin


async def list_deletion_requests(
    db: AsyncSession, storage: Storage, status: DeletionStatus
) -> list[DeletionReviewOut]:
    """Las solicitudes de borrado con ese estado, de la más vieja a la más nueva (la que lleva más esperando, primero)."""
    return await _reviews(db, storage, MediaDeletionRequest.status == status)


async def _reviews(db: AsyncSession, storage: Storage, *conditions) -> list[DeletionReviewOut]:
    rows = (
        await db.execute(
            select(MediaDeletionRequest, Media, AppUser)
            .join(Media, Media.id == MediaDeletionRequest.media_id)
            .join(AppUser, AppUser.id == MediaDeletionRequest.requested_by)
            .where(*conditions)
            .order_by(MediaDeletionRequest.id)
        )
    ).all()
    names: dict[int, list[str]] = {}
    media_ids = [media.id for _, media, _ in rows]
    if media_ids:
        for media_id, child in (
            await db.execute(
                select(MediaChild.media_id, Child)
                .join(Child, Child.id == MediaChild.child_id)
                .where(MediaChild.media_id.in_(media_ids))
                .order_by(Child.name)
            )
        ).all():
            names.setdefault(media_id, []).append(child.full_name)
    return [
        DeletionReviewOut(
            id=request.id,
            media_id=media.id,
            status=request.status,
            reason=request.reason,
            requested_by_id=requester.id,
            requested_by_name=requester.full_name,
            child_names=names.get(media.id, []),
            photo_url=None if media.status == MediaStatus.DELETED else storage.presign_get(media.storage_key),
            created_at=request.created_at,
            decided_at=request.decided_at,
            decision_note=request.decision_note,
        )
        for request, media, requester in rows
    ]


async def decide_deletion(
    db: AsyncSession,
    current: CurrentUser,
    storage: Storage,
    request_id: int,
    approve: bool,
    data: DeletionDecisionIn,
) -> DeletionReviewOut:
    """Aprobar borra la foto del bucket y de todas las pantallas; rechazar la deja como estaba.
    Una solicitud ya resuelta no se vuelve a resolver (409)."""
    request = await db.scalar(
        select(MediaDeletionRequest).where(MediaDeletionRequest.id == request_id).with_for_update()
    )
    if request is None:
        raise ApiError(404, "DELETION_REQUEST_NOT_FOUND", "No existe esa solicitud")
    if request.status != DeletionStatus.PENDING:
        raise ApiError(409, "REQUEST_ALREADY_DECIDED", "Esa solicitud ya se resolvió")

    now = clock.now()
    note = (data.note or "").strip() or None
    new_status = DeletionStatus.APPROVED if approve else DeletionStatus.REJECTED
    media = await db.get(Media, request.media_id)
    if approve:
        # primero el bucket: si falla, no se marca nada como borrado.
        try:
            await storage.delete(media.storage_key)
        except Exception as error:
            raise ApiError(502, "STORAGE_UNAVAILABLE", "No se pudo borrar el archivo; intenta de nuevo") from error
        media.status = MediaStatus.DELETED
        # los reportes conservan su texto, pero ya no apuntan a una foto que no existe.
        await db.execute(update(Report).where(Report.media_id == media.id).values(media_id=None))
        # si otras familias pidieron borrar la misma foto, su solicitud queda resuelta con ella.
        await db.execute(
            update(MediaDeletionRequest)
            .where(
                MediaDeletionRequest.media_id == media.id,
                MediaDeletionRequest.status == DeletionStatus.PENDING,
            )
            .values(status=new_status, decided_by=current.id, decided_at=now, decision_note=note)
        )
    request.status = new_status
    request.decided_by = current.id
    request.decided_at = now
    request.decision_note = note
    audit.record(
        db,
        audit.MEDIA_DELETION_APPROVED if approve else audit.MEDIA_DELETION_REJECTED,
        actor_id=current.id,
        entity_type="MEDIA",
        entity_id=media.id,
        detail={"request_id": request.id},
    )
    await db.commit()
    return (await _reviews(db, storage, MediaDeletionRequest.id == request.id))[0]
