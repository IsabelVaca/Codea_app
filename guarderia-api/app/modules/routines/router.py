from datetime import date

from fastapi import APIRouter, status

from app.core.db import Db
from app.core.security import AdminUser, StaffUser, TeacherUser

from . import service
from .schemas import RoutineOut, TemplateAssign, TemplateAssignOut, TemplateIn, TemplateOut

router = APIRouter(tags=["rutina"])


@router.get("/classrooms/{classroom_id}/routine", response_model=RoutineOut)
async def get_routine(db: Db, current: StaffUser, classroom_id: int, date: date | None = None) -> RoutineOut:
    """La rutina del día con sus momentos y el estado de cada uno: DONE, ACTIVE o PENDING."""
    return await service.get_routine(db, current, classroom_id, date)


@router.post("/routine-moments/{moment_id}/confirm", response_model=RoutineOut)
async def confirm_moment(db: Db, current: TeacherUser, moment_id: int) -> RoutineOut:
    """Cierra el momento activo y arranca el siguiente. 409 MOMENT_NOT_ACTIVE si ese momento no es el activo."""
    return await service.confirm(db, current, moment_id)


@router.get("/routine-templates", response_model=list[TemplateOut])
async def list_templates(db: Db, _: AdminUser) -> list[TemplateOut]:
    """Las plantillas de rutina con sus momentos y las salas que las usan."""
    return await service.list_templates(db)


@router.post("/routine-templates", response_model=TemplateOut, status_code=status.HTTP_201_CREATED)
async def create_template(db: Db, _: AdminUser, data: TemplateIn) -> TemplateOut:
    """Crea una plantilla. 422 NAME_REQUIRED o MOMENTS_REQUIRED."""
    return await service.create_template(db, data)


@router.put("/routine-templates/{template_id}", response_model=TemplateOut)
async def update_template(db: Db, _: AdminUser, template_id: int, data: TemplateIn) -> TemplateOut:
    """Reemplaza el nombre y los momentos. No cambia los días que ya empezaron."""
    return await service.update_template(db, template_id, data)


@router.put("/classrooms/{classroom_id}/routine-template", response_model=TemplateAssignOut)
async def assign_template(db: Db, _: AdminUser, classroom_id: int, data: TemplateAssign) -> TemplateAssignOut:
    """Elige la plantilla que usa la sala. template_id = null vuelve a la default."""
    return await service.assign_template(db, classroom_id, data)
