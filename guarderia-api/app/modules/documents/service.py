from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock, rules
from app.core.errors import ApiError
from app.core.security import CurrentUser, get_child_in_scope
from app.core.storage import Storage
from app.modules.media import service as media_service
from app.modules.media.models import Media, MediaStatus

from .models import AuthorizedPickup, ChildDocument
from .schemas import DocumentOut, DocumentStatus, DocumentUpload, PickupIn, PickupOut


def _document_out(document: ChildDocument, media: Media | None, storage: Storage) -> DocumentOut:
    return DocumentOut(
        id=document.id,
        title=document.title,
        due_date=document.due_date,
        status=DocumentStatus.CURRENT if media is not None else DocumentStatus.MISSING,
        uploaded_at=document.uploaded_at,
        url=storage.presign_get(media.storage_key) if media is not None else None,
    )


async def list_documents(db: AsyncSession, current: CurrentUser, storage: Storage, child_id: int) -> list[DocumentOut]:
    child = await get_child_in_scope(db, current, child_id)
    rows = (
        await db.execute(
            select(ChildDocument, Media)
            .outerjoin(Media, Media.id == ChildDocument.media_id)
            .where(ChildDocument.child_id == child.id)
            .order_by(ChildDocument.due_date.nulls_last(), ChildDocument.id)
        )
    ).all()
    return [_document_out(document, media, storage) for document, media in rows]


async def upload_document(
    db: AsyncSession, current: CurrentUser, storage: Storage, child_id: int, document_id: int, data: DocumentUpload
) -> DocumentOut:
    """Sube o reemplaza el archivo de un documento pedido."""
    child = await get_child_in_scope(db, current, child_id)
    document = await db.get(ChildDocument, document_id)
    if document is None or document.child_id != child.id:
        raise ApiError(404, "DOCUMENT_NOT_FOUND", "No existe ese documento")
    media = await media_service.get_own_media(db, current, data.media_id)
    if media.status != MediaStatus.READY:
        raise ApiError(409, "MEDIA_NOT_READY", "El archivo todavía no termina de subirse")
    document.media_id = media.id
    document.uploaded_at = clock.now()
    await db.commit()
    return _document_out(document, media, storage)


async def list_pickups(db: AsyncSession, current: CurrentUser, child_id: int) -> list[PickupOut]:
    child = await get_child_in_scope(db, current, child_id)
    pickups = (
        await db.scalars(
            select(AuthorizedPickup).where(AuthorizedPickup.child_id == child.id).order_by(AuthorizedPickup.full_name)
        )
    ).all()
    return [PickupOut.model_validate(pickup) for pickup in pickups]


async def add_pickup(db: AsyncSession, current: CurrentUser, child_id: int, data: PickupIn) -> PickupOut:
    child = await get_child_in_scope(db, current, child_id)
    pickup = AuthorizedPickup(
        child_id=child.id,
        full_name=rules.require_name(data.full_name),
        relationship=(data.relationship or "").strip() or None,
        phone=rules.check_phone(data.phone) if data.phone else None,
        added_by=current.id,
    )
    db.add(pickup)
    await db.commit()
    await db.refresh(pickup)
    return PickupOut.model_validate(pickup)


async def remove_pickup(db: AsyncSession, current: CurrentUser, child_id: int, person_id: int) -> None:
    child = await get_child_in_scope(db, current, child_id)
    pickup = await db.get(AuthorizedPickup, person_id)
    if pickup is None or pickup.child_id != child.id:
        raise ApiError(404, "PICKUP_NOT_FOUND", "No existe esa persona autorizada")
    await db.delete(pickup)
    await db.commit()
