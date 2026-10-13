from fastapi import APIRouter, status

from app.core.db import Db
from app.core.security import AdminOrParent, AnyUser
from app.core.storage import StorageDep

from . import service
from .schemas import DocumentOut, DocumentUpload, PickupIn, PickupOut

router = APIRouter(prefix="/children/{child_id}", tags=["documentos"])


@router.get("/documents", response_model=list[DocumentOut])
async def list_documents(db: Db, current: AdminOrParent, storage: StorageDep, child_id: int) -> list[DocumentOut]:
    """Pantalla Documentos: los documentos pedidos, cada uno vigente o faltante."""
    return await service.list_documents(db, current, storage, child_id)


@router.put("/documents/{document_id}", response_model=DocumentOut)
async def upload_document(
    db: Db, current: AdminOrParent, storage: StorageDep, child_id: int, document_id: int, data: DocumentUpload
) -> DocumentOut:
    """Sube o reemplaza un documento, con el mismo permiso temporal que las fotos."""
    return await service.upload_document(db, current, storage, child_id, document_id, data)


@router.get("/authorized-pickups", response_model=list[PickupOut])
async def list_pickups(db: Db, current: AnyUser, child_id: int) -> list[PickupOut]:
    """Las personas autorizadas para recoger al niño."""
    return await service.list_pickups(db, current, child_id)


@router.post("/authorized-pickups", response_model=PickupOut, status_code=status.HTTP_201_CREATED)
async def add_pickup(db: Db, current: AdminOrParent, child_id: int, data: PickupIn) -> PickupOut:
    """Agrega una persona autorizada para recoger al niño."""
    return await service.add_pickup(db, current, child_id, data)


@router.delete("/authorized-pickups/{person_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_pickup(db: Db, current: AdminOrParent, child_id: int, person_id: int) -> None:
    """Quita a una persona autorizada."""
    await service.remove_pickup(db, current, child_id, person_id)
