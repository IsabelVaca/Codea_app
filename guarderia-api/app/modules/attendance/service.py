import calendar
from datetime import date

from sqlalchemy import func, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock
from app.core.errors import ApiError
from app.core.security import CurrentUser, get_child_in_scope
from app.modules.classrooms.centers import center_today
from app.modules.reports.models import DailySummary, Report, ReportChild
from app.modules.routines.models import RoutineMoment

from .models import Attendance, AttendanceStatus
from .schemas import AttendanceIn, AttendanceOut, CalendarDay, CalendarOut, CalendarSummary, DayAttendanceStatus


async def set_attendance(db: AsyncSession, current: CurrentUser, child_id: int, data: AttendanceIn) -> AttendanceOut:
    """Marca a un niño como presente o ausente. Sin registro, cuenta como ausente.

    Al marcarlo presente se guarda la hora de entrada (la de la app si la marca se hizo sin conexión, si no la del
    servidor). Volver a marcarlo presente no la cambia; marcarlo ausente la borra.
    """
    child = await get_child_in_scope(db, current, child_id)
    day = data.date or await center_today(db)
    now = clock.now()
    present = data.status == AttendanceStatus.PRESENT
    arrived_at = (min(data.arrived_at, now) if data.arrived_at else now) if present else None
    statement = insert(Attendance).values(
        child_id=child.id, date=day, status=data.status, marked_by=current.id, arrived_at=arrived_at
    )
    # en el DO UPDATE, `Attendance.arrived_at` es la hora que ya estaba guardada.
    kept = func.coalesce(Attendance.arrived_at, statement.excluded.arrived_at) if present else None
    await db.execute(
        statement.on_conflict_do_update(
            index_elements=["child_id", "date"],
            set_={
                "status": statement.excluded.status,
                "marked_by": statement.excluded.marked_by,
                "arrived_at": kept,
            },
        )
    )
    await db.commit()
    stored = await db.scalar(
        select(Attendance.arrived_at).where(Attendance.child_id == child.id, Attendance.date == day)
    )
    return AttendanceOut(child_id=child.id, date=day, status=data.status, arrived_at=stored)


def _parse_month(month: str) -> tuple[date, date]:
    try:
        year, number = (int(part) for part in month.split("-"))
        first = date(year, number, 1)
    except ValueError:
        raise ApiError(422, "MONTH_INVALID", "El mes debe venir como AAAA-MM")
    return first, date(year, number, calendar.monthrange(year, number)[1])


async def month_calendar(db: AsyncSession, current: CurrentUser, child_id: int, month: str) -> CalendarOut:
    """El estado de cada día del mes para la pantalla Calendario.

    Con pase de lista (PRESENT): WITH_NOTE si ese día tuvo un reporte o una nota en su resumen, FULL_DAY si no.
    ABSENT si se marcó ausente o si su sala tuvo rutina y nadie lo pasó como presente (sin registro es falta).
    NO_RECORD en cualquier otro caso (fines de semana, días sin rutina, días que aún no llegan).
    """
    child = await get_child_in_scope(db, current, child_id)
    first, last = _parse_month(month)
    in_month = lambda column: column.between(first, last)  # noqa: E731

    marks = dict(
        (
            await db.execute(
                select(Attendance.date, Attendance.status).where(Attendance.child_id == child.id, in_month(Attendance.date))
            )
        ).all()
    )
    present = {day for day, status in marks.items() if status == AttendanceStatus.PRESENT}
    absent = {day for day, status in marks.items() if status == AttendanceStatus.ABSENT}
    with_note = set(
        (
            await db.scalars(
                select(RoutineMoment.date)
                .join(Report, Report.routine_moment_id == RoutineMoment.id)
                .join(ReportChild, ReportChild.report_id == Report.id)
                .where(ReportChild.child_id == child.id, in_month(RoutineMoment.date))
            )
        ).all()
    ) | set(
        (
            await db.scalars(
                select(DailySummary.date).where(
                    DailySummary.child_id == child.id,
                    in_month(DailySummary.date),
                    DailySummary.general_note.is_not(None),
                )
            )
        ).all()
    )
    routine_days = set()
    if child.classroom_id is not None:
        routine_days = set(
            (
                await db.scalars(
                    select(RoutineMoment.date)
                    .where(RoutineMoment.classroom_id == child.classroom_id, in_month(RoutineMoment.date))
                    .distinct()
                )
            ).all()
        )

    today = await center_today(db)
    days = []
    for number in range(1, last.day + 1):
        day = first.replace(day=number)
        if day > today:
            status = DayAttendanceStatus.NO_RECORD
        elif day in present:
            status = DayAttendanceStatus.WITH_NOTE if day in with_note else DayAttendanceStatus.FULL_DAY
        elif day in absent:
            status = DayAttendanceStatus.ABSENT
        elif day in with_note:
            # un reporte o una nota de ese día son prueba de que vino, aunque no haya pase de lista.
            status = DayAttendanceStatus.WITH_NOTE
        elif day in routine_days:
            status = DayAttendanceStatus.ABSENT
        else:
            status = DayAttendanceStatus.NO_RECORD
        days.append(CalendarDay(day=number, date=day, status=status))

    count = lambda status: sum(1 for day in days if day.status == status)  # noqa: E731
    return CalendarOut(
        child_id=child.id,
        month=f"{first.year:04d}-{first.month:02d}",
        days=days,
        summary=CalendarSummary(
            full_days=count(DayAttendanceStatus.FULL_DAY) + count(DayAttendanceStatus.WITH_NOTE),
            with_note=count(DayAttendanceStatus.WITH_NOTE),
            absences=count(DayAttendanceStatus.ABSENT),
        ),
    )
