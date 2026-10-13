from datetime import date

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock
from app.core.errors import ApiError

from .models import Classroom, DaycareCenter

# Hoy hay un solo centro. Cuando haya más sedes, cada usuario sacará el suyo
# de su sala (docente) o de su registro de admin, y esta función se va.


async def default_center(db: AsyncSession) -> DaycareCenter:
    center = await db.scalar(select(DaycareCenter).order_by(DaycareCenter.id).limit(1))
    if center is None:
        raise ApiError(409, "CENTER_MISSING", "Todavía no hay un centro registrado")
    return center


async def center_of_classroom(db: AsyncSession, classroom_id: int | None) -> DaycareCenter:
    if classroom_id is not None:
        classroom = await db.get(Classroom, classroom_id)
        if classroom is not None:
            return await db.get(DaycareCenter, classroom.daycare_center_id)
    return await default_center(db)


async def center_today(db: AsyncSession) -> date:
    """La fecha de hoy en la zona del centro."""
    center = await default_center(db)
    return clock.local_today(center.timezone)


async def get_active_classroom(db: AsyncSession, classroom_id: int) -> Classroom:
    classroom = await db.get(Classroom, classroom_id)
    if classroom is None or classroom.deleted_at is not None:
        raise ApiError(404, "CLASSROOM_NOT_FOUND", "No existe esa sala")
    return classroom
