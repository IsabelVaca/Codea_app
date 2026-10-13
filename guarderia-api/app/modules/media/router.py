from fastapi import APIRouter, Query, Response, status

from app.core.db import Db
from app.core.pagination import Page, parse_cursor
from app.core.security import AnyUser, ParentUser
from app.core.storage import StorageDep

from . import service
from .schemas import DeletionRequestIn, DeletionRequestOut, FeedItem, MediaOut, PhotoCategory, UploadIn, UploadOut

router = APIRouter(prefix="/media", tags=["fotos"])


@router.post("/uploads", response_model=UploadOut, status_code=status.HTTP_201_CREATED)
async def request_upload(db: Db, current: AnyUser, storage: StorageDep, data: UploadIn) -> UploadOut:
    """Pide permiso para subir una foto o un documento. Regresa una URL firmada que dura 5 minutos."""
    return await service.request_upload(db, current, storage, data)


@router.post("/{media_id}/complete", response_model=MediaOut)
async def complete(db: Db, current: AnyUser, storage: StorageDep, media_id: int) -> MediaOut:
    """Avisa que el archivo ya se subió, para poder ligarlo a un reporte o a un documento."""
    return await service.complete(db, current, storage, media_id)


@router.get("", response_model=Page[FeedItem])
async def feed(
    db: Db,
    current: AnyUser,
    storage: StorageDep,
    child_id: int,
    category: PhotoCategory | None = None,
    cursor: str | None = None,
    limit: int = Query(20, ge=1, le=50),
) -> Page[FeedItem]:
    """Pantalla Fotos: fotos paginadas y filtradas por categoría, solo donde aparece el niño."""
    return await service.feed(db, current, storage, child_id, category, parse_cursor(cursor), limit)


@router.post("/{media_id}/deletion-request", response_model=DeletionRequestOut, status_code=status.HTTP_201_CREATED)
async def request_deletion(
    db: Db, current: ParentUser, media_id: int, response: Response, data: DeletionRequestIn | None = None
) -> DeletionRequestOut:
    """Botón "Pedir que se borre". Si ya lo pidió, regresa la misma solicitud con 200."""
    request, created = await service.request_deletion(db, current, media_id, data or DeletionRequestIn())
    if not created:
        response.status_code = status.HTTP_200_OK
    return request
