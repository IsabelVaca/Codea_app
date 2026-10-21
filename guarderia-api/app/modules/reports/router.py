from datetime import date

from fastapi import APIRouter, status

from app.core.db import Db
from app.core.security import AnyUser, StaffUser, TeacherUser
from app.core.storage import StorageDep

from . import service
from .schemas import DailyEntryIn, DailyEntryOut, DayOut, ReportIn, ReportOut, WeeklySummaryOut

router = APIRouter(tags=["reportes"])


@router.post("/reports", response_model=ReportOut, status_code=status.HTTP_201_CREATED)
async def create_report(db: Db, current: TeacherUser, data: ReportIn) -> ReportOut:
    """Crea un reporte de un momento. 422 DESCRIPTION_TOO_SHORT o CHILDREN_REQUIRED; 409 MOMENT_NOT_STARTED o CHILD_ABSENT."""
    return await service.create_report(db, current, data)


@router.get("/reports", response_model=list[ReportOut])
async def list_reports(db: Db, current: StaffUser, storage: StorageDep, moment_id: int) -> list[ReportOut]:
    """Los reportes de un momento: el contador que aparece en Mi día, y ahora también para verlos completos."""
    return await service.list_reports(db, current, storage, moment_id)


@router.post("/children/{child_id}/daily-entries", response_model=DailyEntryOut)
async def save_daily_entry(db: Db, current: StaffUser, child_id: int, data: DailyEntryIn) -> DailyEntryOut:
    """Guarda el resumen del día de un niño: ánimo, comida, cambios y siesta."""
    return await service.save_daily_entry(db, current, child_id, data)


@router.get("/children/{child_id}/day", response_model=DayOut)
async def child_day(db: Db, current: AnyUser, child_id: int, date: date | None = None) -> DayOut:
    """Pantalla Hoy: resumen del día (ánimo, comida, cambios, siesta y nota) y la línea de tiempo."""
    return await service.child_day(db, current, child_id, date)


@router.get("/children/{child_id}/weekly-summary", response_model=WeeklySummaryOut)
async def weekly_summary(
    db: Db, current: AnyUser, child_id: int, week_start: date | None = None
) -> WeeklySummaryOut:
    """El resumen semanal (lunes a viernes) para el papá. `week_start` es cualquier fecha de esa semana; sin ella, la de hoy.
    El padre solo ve a sus hijos y la docente a los de su sala (404 si no)."""
    return await service.weekly_summary(db, current, child_id, week_start)

