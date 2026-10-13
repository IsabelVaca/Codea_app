from datetime import date

from fastapi import APIRouter, status

from app.core.db import Db
from app.core.security import AdminUser, StaffUser

from . import service
from .schemas import ClassroomCreate, ClassroomDetail, ClassroomOut, ClassroomUpdate, RoomChild, StaffUpdate

router = APIRouter(tags=["salas"])


@router.get("/classrooms", response_model=list[ClassroomOut])
async def list_classrooms(db: Db, current: AdminUser) -> list[ClassroomOut]:
    """Salas con cupo, niños inscritos, cupo disponible y asistente principal."""
    return await service.list_classrooms(db, current.admin.daycare_center_id)


@router.post("/classrooms", response_model=ClassroomDetail, status_code=status.HTTP_201_CREATED)
async def create_classroom(db: Db, current: AdminUser, data: ClassroomCreate) -> ClassroomDetail:
    """Crea una sala. 422 NAME_REQUIRED o CAPACITY_INVALID."""
    return await service.create_classroom(db, current, data)


@router.get("/classrooms/{classroom_id}", response_model=ClassroomDetail)
async def get_classroom(db: Db, _: AdminUser, classroom_id: int) -> ClassroomDetail:
    """Detalle de una sala, con sus niños y sus asistentes."""
    return await service.get_detail(db, classroom_id)


@router.patch("/classrooms/{classroom_id}", response_model=ClassroomDetail)
async def update_classroom(db: Db, _: AdminUser, classroom_id: int, data: ClassroomUpdate) -> ClassroomDetail:
    """Edita la sala, por ejemplo el cupo."""
    return await service.update_classroom(db, classroom_id, data)


@router.put("/classrooms/{classroom_id}/staff", response_model=ClassroomDetail)
async def set_staff(db: Db, _: AdminUser, classroom_id: int, data: StaffUpdate) -> ClassroomDetail:
    """Asigna las asistentes de la sala y cuál es la principal."""
    return await service.set_staff(db, classroom_id, data)


@router.delete("/classrooms/{classroom_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_classroom(db: Db, _: AdminUser, classroom_id: int) -> None:
    """Elimina la sala. 409 CLASSROOM_NOT_EMPTY si todavía tiene niños; sus asistentes quedan sin sala."""
    await service.delete_classroom(db, classroom_id)


@router.get("/classrooms/{classroom_id}/children", response_model=list[RoomChild])
async def room_children(db: Db, current: StaffUser, classroom_id: int, date: date | None = None) -> list[RoomChild]:
    """Mi sala: los niños con su asistencia del día y si tienen mensajes sin leer."""
    return await service.room_children(db, current, classroom_id, date)
