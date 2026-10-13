from fastapi import APIRouter, Query

from app.core.db import Db
from app.core.security import AnyUser, StaffUser

from . import service
from .schemas import AttendanceIn, AttendanceOut, CalendarOut

router = APIRouter(tags=["asistencia"])


@router.put("/attendance/{child_id}", response_model=AttendanceOut)
async def set_attendance(db: Db, current: StaffUser, child_id: int, data: AttendanceIn) -> AttendanceOut:
    """Marca a un niño como presente o ausente hoy."""
    return await service.set_attendance(db, current, child_id, data)


@router.get("/children/{child_id}/attendance", response_model=CalendarOut)
async def month_calendar(
    db: Db, current: AnyUser, child_id: int, month: str = Query(pattern=r"^\d{4}-\d{2}$")
) -> CalendarOut:
    """Calendario: el estado de cada día (FULL_DAY, WITH_NOTE, ABSENT o NO_RECORD)."""
    return await service.month_calendar(db, current, child_id, month)
