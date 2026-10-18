from fastapi import APIRouter, status

from app.core.db import Db
from app.core.security import AdminUser, AnyUser

from . import service
from .schemas import ChildCreate, ChildDetail, ChildOut, ChildUpdate

router = APIRouter(tags=["infantes"])


@router.get("/children", response_model=list[ChildOut])
async def list_children(db: Db, _: AdminUser) -> list[ChildOut]:
    """Infantes con edad, tutor, parentesco, correo del tutor, sala y si está de baja."""
    return await service.list_children(db)


@router.post("/children", response_model=ChildDetail, status_code=status.HTTP_201_CREATED)
async def create_child(db: Db, current: AdminUser, data: ChildCreate) -> ChildDetail:
    """Inscribe a un infante. El tutor es opcional; si viene, se registra y se le crea su invitación. 422 NAME_REQUIRED, AGE_INVALID, GUARDIAN_REQUIRED o PHONE_INVALID."""
    return await service.create_child(db, data, current.user.id)


@router.get("/children/{child_id}", response_model=ChildDetail)
async def get_child(db: Db, current: AnyUser, child_id: int) -> ChildDetail:
    """Expediente de un infante. El padre solo ve a sus hijos y la docente a los de su sala."""
    return await service.get_child(db, current, child_id)


@router.patch("/children/{child_id}", response_model=ChildDetail)
async def update_child(db: Db, current: AdminUser, child_id: int, data: ChildUpdate) -> ChildDetail:
    """Cambia al infante de sala. Con classroom_id = null queda sin sala."""
    return await service.update_child(db, child_id, data, current.user.id)


@router.post("/children/{child_id}/deactivate", response_model=ChildDetail)
async def deactivate_child(db: Db, current: AdminUser, child_id: int) -> ChildDetail:
    """Da de baja al infante."""
    return await service.set_active(db, child_id, active=False, actor_id=current.user.id)


@router.post("/children/{child_id}/reactivate", response_model=ChildDetail)
async def reactivate_child(db: Db, current: AdminUser, child_id: int) -> ChildDetail:
    return await service.set_active(db, child_id, active=True, actor_id=current.user.id)
