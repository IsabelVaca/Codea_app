import uuid

from fastapi import APIRouter, status

from app.core.db import Db
from app.core.security import AdminUser

from . import service
from .schemas import ShiftOut, TeacherCreate, TeacherOut, TeacherUpdate

router = APIRouter(tags=["docentes"])


@router.get("/teachers", response_model=list[TeacherOut])
async def list_teachers(db: Db, _: AdminUser) -> list[TeacherOut]:
    """Docentes con su sala, turno, hora de checada de hoy, si está de baja y cuántos niños hay en su sala."""
    return await service.list_teachers(db)


@router.post("/teachers", response_model=TeacherOut, status_code=status.HTTP_201_CREATED)
async def create_teacher(db: Db, _: AdminUser, data: TeacherCreate) -> TeacherOut:
    """Da de alta a una docente y le crea su invitación. 422 NAME_REQUIRED, PHONE_INVALID o EMAIL_INVALID."""
    return await service.create_teacher(db, data)


@router.get("/teachers/{teacher_id}", response_model=TeacherOut)
async def get_teacher(db: Db, _: AdminUser, teacher_id: uuid.UUID) -> TeacherOut:
    return await service.get_teacher(db, teacher_id)


@router.patch("/teachers/{teacher_id}", response_model=TeacherOut)
async def update_teacher(db: Db, _: AdminUser, teacher_id: uuid.UUID, data: TeacherUpdate) -> TeacherOut:
    """Cambia a la docente de sala o de turno."""
    return await service.update_teacher(db, teacher_id, data)


@router.post("/teachers/{teacher_id}/deactivate", response_model=TeacherOut)
async def deactivate_teacher(db: Db, _: AdminUser, teacher_id: uuid.UUID) -> TeacherOut:
    """Da de baja a la docente: le quita el acceso y cierra sus sesiones."""
    return await service.set_active(db, teacher_id, active=False)


@router.post("/teachers/{teacher_id}/reactivate", response_model=TeacherOut)
async def reactivate_teacher(db: Db, _: AdminUser, teacher_id: uuid.UUID) -> TeacherOut:
    return await service.set_active(db, teacher_id, active=True)


@router.get("/shifts", response_model=list[ShiftOut])
async def list_shifts(db: Db, _: AdminUser) -> list[ShiftOut]:
    """Los turnos del centro, para el formulario de alta de asistente."""
    return await service.list_shifts(db)
