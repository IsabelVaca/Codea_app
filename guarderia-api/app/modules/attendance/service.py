import calendar
from datetime import date

from sqlalchemy import select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.errors import ApiError
from app.core.security import CurrentUser, get_child_in_scope
from app.modules.classrooms.centers import center_today
from app.modules.reports.models import DailySummary, Report, ReportChild
from app.modules.routines.models import RoutineMoment

from .models import Attendance, AttendanceStatus
from .schemas import AttendanceIn, AttendanceOut, CalendarDay, CalendarOut, CalendarSummary, DayAttendanceStatus


async def set_attendance(db: AsyncSession, current: CurrentUser, child_id: int, data: AttendanceIn) -> AttendanceOut:
    """Marca a un niño como presente o ausente. Sin registro, cuenta como presente."""
    child = await get_child_in_scope(db, current, child_id)
    day = data.date or await center_today(db)
    statement = insert(Attendance).values(child_id=child.id, date=day, status=data.status, marked_by=current.id)
    await db.execute(
        statement.on_conflict_do_update(
            index_elements=["child_id", "date"],
            set_={"status": statement.excluded.status, "marked_by": statement.excluded.marked_by},
        )
    )
    await db.commit()
    return AttendanceOut(child_id=child.id, date=day, status=data.status)


def _parse_month(month: str) -> tuple[date, date]:
    try:
        year, number = (int(part) for part in month.split("-"))
        first = date(year, number, 1)
    except ValueError:
        raise ApiError(422, "MONTH_INVALID", "El mes debe venir como AAAA-MM")
    return first, date(year, number, calendar.monthrange(year, number)[1])


async def month_calendar(db: AsyncSession, current: CurrentUser, child_id: int, month: str) -> CalendarOut:
    """El estado de cada día del mes para la pantalla Calendario.

    ABSENT si se marcó ausente; WITH_NOTE si ese día tuvo un reporte o una nota en su resumen;
    FULL_DAY si su sala tuvo rutina ese día; NO_RECORD en cualquier otro caso.
    """
    child = await get_child_in_scope(db, current, child_id)
    first, last = _parse_month(month)
    in_month = lambda column: column.between(first, last)  # noqa: E731

    absent = set(
        (
            await db.scalars(
                select(Attendance.date).where(
                    Attendance.child_id == child.id,
                    in_month(Attendance.date),
                    Attendance.status == AttendanceStatus.ABSENT,
                )
            )
        ).all()
    )
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
        elif day in absent:
            status = DayAttendanceStatus.ABSENT
        elif day in with_note:
            status = DayAttendanceStatus.WITH_NOTE
        elif day in routine_days:
            status = DayAttendanceStatus.FULL_DAY
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
