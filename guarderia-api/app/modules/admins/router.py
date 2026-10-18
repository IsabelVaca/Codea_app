from fastapi import APIRouter

from app.core.db import Db
from app.core.security import AdminUser
from app.modules.classrooms.schemas import CenterOut

from . import service
from .schemas import CenterUpdate

router = APIRouter(tags=["centro"])


@router.get("/center", response_model=CenterOut)
async def get_center(db: Db, current: AdminUser) -> CenterOut:
    """Los datos del centro: nombre, ubicación, radio, zona horaria y el horario (entrada, salida, cierre y días)."""
    return await service.get_center(db, current)


@router.patch("/center", response_model=CenterOut)
async def update_center(db: Db, current: AdminUser, data: CenterUpdate) -> CenterOut:
    """Cambia el centro: ubicación, radio, zona horaria y el horario (entrada, salida, cierre automático y días de trabajo).
    422 LOCATION_INVALID, LOCATION_INCOMPLETE, RADIUS_INVALID, TIMEZONE_INVALID, SCHEDULE_INVALID o WORKDAYS_INVALID."""
    return await service.update_center(db, current, data)
