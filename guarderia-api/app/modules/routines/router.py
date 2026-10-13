from datetime import date

from fastapi import APIRouter

from app.core.db import Db
from app.core.security import StaffUser, TeacherUser

from . import service
from .schemas import RoutineOut

router = APIRouter(tags=["rutina"])


@router.get("/classrooms/{classroom_id}/routine", response_model=RoutineOut)
async def get_routine(db: Db, current: StaffUser, classroom_id: int, date: date | None = None) -> RoutineOut:
    """La rutina del día con sus momentos y el estado de cada uno: DONE, ACTIVE o PENDING."""
    return await service.get_routine(db, current, classroom_id, date)


@router.post("/routine-moments/{moment_id}/confirm", response_model=RoutineOut)
async def confirm_moment(db: Db, current: TeacherUser, moment_id: int) -> RoutineOut:
    """Cierra el momento activo y arranca el siguiente. 409 MOMENT_NOT_ACTIVE si ese momento no es el activo."""
    return await service.confirm(db, current, moment_id)
