from collections.abc import Iterable
from datetime import date

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from .models import Attendance, AttendanceStatus


async def present_child_ids(db: AsyncSession, child_ids: Iterable[int], day: date) -> set[int]:
    """Los niños a los que la docente pasó lista como presentes ese día.

    Sin registro el niño cuenta como ausente: quien no esté en este conjunto, faltó.
    """
    ids = list(child_ids)
    if not ids:
        return set()
    return set(
        (
            await db.scalars(
                select(Attendance.child_id).where(
                    Attendance.child_id.in_(ids),
                    Attendance.date == day,
                    Attendance.status == AttendanceStatus.PRESENT,
                )
            )
        ).all()
    )
