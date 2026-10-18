from fastapi import APIRouter, Response, status

from app.core.db import Db
from app.core.security import TeacherUser

from . import service
from .schemas import CheckInIn, CheckInOut, NotifyIn, NotifyOut

router = APIRouter(prefix="/staff-attendance", tags=["checador"])


@router.get("/today", response_model=CheckInOut | None)
async def today(db: Db, current: TeacherUser) -> CheckInOut | None:
    """Si la docente ya registró su entrada hoy, regresa el registro; si no, null."""
    return await service.today(db, current)


@router.post("/check-in", response_model=CheckInOut, status_code=status.HTTP_201_CREATED)
async def check_in(db: Db, current: TeacherUser, data: CheckInIn, response: Response) -> CheckInOut:
    """Registra la entrada, sin ubicación. 201 con status LATE si llegó tarde. Repetirla regresa la misma con 200."""
    record, created = await service.check_in(db, current, data)
    if not created:
        response.status_code = status.HTTP_200_OK
    return record


@router.post("/check-out", response_model=CheckInOut, status_code=status.HTTP_201_CREATED)
async def check_out(db: Db, current: TeacherUser, response: Response) -> CheckInOut:
    """Registra la salida del día. 409 NOT_CHECKED_IN si no hay entrada hoy.
    Repetirla (o llegar después del cierre de las 9 pm) regresa la misma salida con 200."""
    record, created = await service.check_out(db, current)
    if not created:
        response.status_code = status.HTTP_200_OK
    return record


@router.post("/notify-coordination", response_model=NotifyOut, status_code=status.HTTP_201_CREATED)
async def notify_coordination(db: Db, current: TeacherUser, data: NotifyIn) -> NotifyOut:
    """Avisa a coordinación que la docente llegará tarde."""
    return await service.notify_coordination(db, current, data)
