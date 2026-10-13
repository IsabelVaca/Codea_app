import uuid

from fastapi import APIRouter, status

from app.core.db import Db
from app.core.security import AdminUser

from . import service
from .schemas import ParentCreate, ParentOut

router = APIRouter(tags=["padres"])


@router.get("/parents", response_model=list[ParentOut])
async def list_parents(db: Db, _: AdminUser) -> list[ParentOut]:
    """Padres con parentesco, hijos, teléfono, correo y si tienen acceso a la app."""
    return await service.list_parents(db)


@router.post("/parents", response_model=ParentOut, status_code=status.HTTP_201_CREATED)
async def create_parent(db: Db, _: AdminUser, data: ParentCreate) -> ParentOut:
    """Da de alta a un padre y le crea su invitación. Los hijos van por id."""
    return await service.create_parent(db, data)


@router.get("/parents/{parent_id}", response_model=ParentOut)
async def get_parent(db: Db, _: AdminUser, parent_id: uuid.UUID) -> ParentOut:
    return await service.get_parent(db, parent_id)


@router.post("/parents/{parent_id}/deactivate", response_model=ParentOut)
async def deactivate_parent(db: Db, _: AdminUser, parent_id: uuid.UUID) -> ParentOut:
    """Le quita el acceso a la app."""
    return await service.set_active(db, parent_id, active=False)


@router.post("/parents/{parent_id}/reactivate", response_model=ParentOut)
async def reactivate_parent(db: Db, _: AdminUser, parent_id: uuid.UUID) -> ParentOut:
    """Le devuelve el acceso."""
    return await service.set_active(db, parent_id, active=True)


@router.post("/parents/{parent_id}/resend-invitation", response_model=ParentOut)
async def resend_invitation(db: Db, _: AdminUser, parent_id: uuid.UUID) -> ParentOut:
    """Reenvía la invitación para entrar a la app (por ahora renueva su vigencia)."""
    return await service.resend_invitation(db, parent_id)
