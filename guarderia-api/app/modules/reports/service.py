from datetime import date, timedelta

from sqlalchemy import func, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.errors import ApiError
from app.core.schemas import ClassroomRef
from app.core.security import CurrentUser, get_child_in_scope
from app.modules.attendance.models import Attendance, AttendanceStatus
from app.modules.attendance.queries import present_child_ids
from app.modules.children.models import Child, ChildStatus
from app.modules.classrooms.centers import center_of_classroom, center_today
from app.modules.classrooms.models import Classroom
from app.modules.media.models import Media, MediaChild, MediaKind, MediaStatus
from app.modules.messaging.models import ParentNote
from app.modules.messaging.schemas import NoteOut
from app.modules.routines import service as routines
from app.modules.routines.models import MomentCategory, MomentStatus, RoutineMoment

from .models import DailySummary, Report, ReportChild, ReportType
from .schemas import (
    DailyEntryIn,
    DailyEntryOut,
    DayChild,
    DayOut,
    ReportIn,
    ReportOut,
    TimelineEvent,
    TimelineType,
    WeekDayOut,
    WeekDayStatus,
    WeeklySummaryOut,
)

# ReportValidator.DESCRIPTION_MIN en la app.
DESCRIPTION_MIN = 10


def _to_out(report: Report, child_ids: list[int]) -> ReportOut:
    return ReportOut(
        id=report.id,
        routine_moment_id=report.routine_moment_id,
        type=report.type,
        method=report.method,
        description=report.description,
        child_ids=child_ids,
        media_id=report.media_id,
        has_photo=report.media_id is not None,
        created_by=report.created_by,
        created_at=report.created_at,
    )


async def create_report(db: AsyncSession, current: CurrentUser, data: ReportIn) -> ReportOut:
    """Las reglas de ReportValidator y RoutineRules, en el mismo orden que la app."""
    description = data.description.strip()
    if len(description) < DESCRIPTION_MIN:
        raise ApiError(422, "DESCRIPTION_TOO_SHORT", f"La descripción debe tener al menos {DESCRIPTION_MIN} caracteres")
    child_ids = sorted(set(data.child_ids))
    if not child_ids:
        raise ApiError(422, "CHILDREN_REQUIRED", "Elige al menos un niño")

    moment = await routines.get_moment_in_scope(db, current, data.routine_moment_id)
    if moment.status == MomentStatus.PENDING:
        raise ApiError(409, "MOMENT_NOT_STARTED", "Ese momento todavía no empieza")

    in_room = (
        await db.scalars(
            select(Child.id).where(
                Child.id.in_(child_ids),
                Child.classroom_id == moment.classroom_id,
                Child.status == ChildStatus.ACTIVE,
            )
        )
    ).all()
    if len(in_room) != len(child_ids):
        raise ApiError(404, "CHILD_NOT_FOUND", "No existe ese niño en la sala")
    present = await present_child_ids(db, child_ids, moment.date)
    absent = [child_id for child_id in child_ids if child_id not in present]
    if absent:
        raise ApiError(
            409,
            "CHILD_ABSENT",
            "Un niño que no está presente hoy no puede estar en un reporte",
            child_ids=sorted(absent),
        )

    if data.media_id is not None:
        media = await db.get(Media, data.media_id)
        if media is None or media.uploaded_by != current.id:
            raise ApiError(404, "MEDIA_NOT_FOUND", "No existe esa foto")
        if media.status != MediaStatus.READY or media.kind != MediaKind.PHOTO:
            raise ApiError(409, "MEDIA_NOT_READY", "La foto todavía no termina de subirse")

    report = Report(
        routine_moment_id=moment.id,
        type=data.type,
        method=data.method,
        description=description,
        media_id=data.media_id,
        created_by=current.id,
    )
    db.add(report)
    await db.flush()
    db.add_all([ReportChild(report_id=report.id, child_id=child_id) for child_id in child_ids])
    if data.media_id is not None:
        # la foto queda etiquetada con los niños del reporte: eso decide qué papás la ven.
        await db.execute(
            insert(MediaChild)
            .values([{"media_id": data.media_id, "child_id": child_id} for child_id in child_ids])
            .on_conflict_do_nothing()
        )
    await db.commit()
    await db.refresh(report)
    return _to_out(report, child_ids)


async def list_reports(db: AsyncSession, current: CurrentUser, moment_id: int) -> list[ReportOut]:
    moment = await routines.get_moment_in_scope(db, current, moment_id)
    reports = (
        await db.scalars(select(Report).where(Report.routine_moment_id == moment.id).order_by(Report.id))
    ).all()
    children: dict[int, list[int]] = {}
    for report_id, child_id in (
        await db.execute(
            select(ReportChild.report_id, ReportChild.child_id)
            .where(ReportChild.report_id.in_([report.id for report in reports]))
            .order_by(ReportChild.child_id)
        )
    ).all():
        children.setdefault(report_id, []).append(child_id)
    return [_to_out(report, children.get(report.id, [])) for report in reports]


def _summary_out(summary: DailySummary) -> DailyEntryOut:
    return DailyEntryOut(
        child_id=summary.child_id,
        date=summary.date,
        mood=summary.mood,
        meals=summary.meals,
        diaper_changes=summary.diaper_changes,
        nap_minutes=summary.nap_minutes,
        general_note=summary.general_note,
    )


async def save_daily_entry(db: AsyncSession, current: CurrentUser, child_id: int, data: DailyEntryIn) -> DailyEntryOut:
    """Guarda el resumen del día del niño. Hay uno por niño y día: si ya existe, se actualiza."""
    child = await get_child_in_scope(db, current, child_id)
    day = data.date or await center_today(db)
    fields = {name: getattr(data, name) for name in data.model_fields_set if name != "date"}
    if "general_note" in fields:
        fields["general_note"] = (fields["general_note"] or "").strip() or None
    statement = insert(DailySummary).values(child_id=child.id, date=day, recorded_by=current.id, **fields)
    await db.execute(
        statement.on_conflict_do_update(
            index_elements=["child_id", "date"],
            set_={**{name: statement.excluded[name] for name in fields}, "recorded_by": current.id},
        )
    )
    await db.commit()
    summary = await db.scalar(select(DailySummary).where(DailySummary.child_id == child.id, DailySummary.date == day))
    await db.refresh(summary)
    return _summary_out(summary)


_TIMELINE_TYPE = {
    MomentCategory.ARRIVAL: TimelineType.CHECK_IN,
    MomentCategory.MEAL: TimelineType.MEAL,
    MomentCategory.NAP: TimelineType.NAP,
}


async def child_day(db: AsyncSession, current: CurrentUser, child_id: int, day: date | None) -> DayOut:
    """La pantalla Hoy: los momentos que ya pasaron en su sala, con lo que la asistente reportó de él."""
    child = await get_child_in_scope(db, current, child_id)
    day = day or await center_today(db)
    classroom = await db.get(Classroom, child.classroom_id) if child.classroom_id else None
    center = await center_of_classroom(db, child.classroom_id)

    marked = await db.scalar(select(Attendance.status).where(Attendance.child_id == child.id, Attendance.date == day))
    attendance = marked or AttendanceStatus.ABSENT
    summary = await db.scalar(select(DailySummary).where(DailySummary.child_id == child.id, DailySummary.date == day))
    notes = (
        await db.scalars(
            select(ParentNote).where(ParentNote.child_id == child.id, ParentNote.date == day).order_by(ParentNote.id)
        )
    ).all()

    timeline: list[TimelineEvent] = []
    if attendance != AttendanceStatus.ABSENT and classroom is not None:
        moments = (
            await db.scalars(
                select(RoutineMoment)
                .where(
                    RoutineMoment.classroom_id == classroom.id,
                    RoutineMoment.date == day,
                    RoutineMoment.status != MomentStatus.PENDING,
                )
                .order_by(RoutineMoment.number)
            )
        ).all()
        reports = (
            await db.scalars(
                select(Report)
                .join(ReportChild, ReportChild.report_id == Report.id)
                .where(
                    ReportChild.child_id == child.id,
                    Report.routine_moment_id.in_([moment.id for moment in moments]),
                )
                .order_by(Report.id)
            )
        ).all()
        for moment in moments:
            mine = [report for report in reports if report.routine_moment_id == moment.id]
            timeline.append(
                TimelineEvent(
                    id=f"moment-{moment.id}",
                    type=_TIMELINE_TYPE.get(moment.category, TimelineType.ACTIVITY),
                    title=moment.name,
                    time=moment.confirmed_at or moment.started_at,
                    description=" ".join(report.description for report in mine) or moment.description or "",
                    report_types=[report.type for report in mine],
                )
            )

    return DayOut(
        child=DayChild(
            id=child.id,
            name=child.name,
            surname=child.surname,
            full_name=child.full_name,
            classroom=ClassroomRef.model_validate(classroom) if classroom else None,
            center_name=center.name,
        ),
        date=day,
        attendance=attendance,
        summary=_summary_out(summary) if summary else None,
        timeline=timeline,
        notes=[NoteOut.model_validate(note) for note in notes],
    )


async def weekly_summary(
    db: AsyncSession, current: CurrentUser, child_id: int, week_start: date | None
) -> WeeklySummaryOut:
    """El resumen de la semana (lunes a viernes) a partir de lo que ya se registró cada día.

    Cualquier fecha sirve como `week_start`: se toma el lunes de esa semana. Sin fecha, la semana de hoy.
    Un día cuenta como asistido si la docente lo pasó como presente, o si hay un resumen o un reporte suyo.
    Cuenta como ausente si se marcó ausente o si su sala tuvo rutina y no hay nada de lo anterior (sin registro es falta).
    """
    child = await get_child_in_scope(db, current, child_id)
    today = await center_today(db)
    monday = (week_start or today) - timedelta(days=(week_start or today).weekday())
    friday = monday + timedelta(days=4)
    week = [monday + timedelta(days=offset) for offset in range(5)]

    marks = dict(
        (
            await db.execute(
                select(Attendance.date, Attendance.status).where(
                    Attendance.child_id == child.id, Attendance.date.between(monday, friday)
                )
            )
        ).all()
    )
    present = {day for day, status in marks.items() if status == AttendanceStatus.PRESENT}
    absent = {day for day, status in marks.items() if status == AttendanceStatus.ABSENT}
    summaries = {
        row.date: row
        for row in (
            await db.scalars(
                select(DailySummary).where(DailySummary.child_id == child.id, DailySummary.date.between(monday, friday))
            )
        ).all()
    }
    routine_days: set[date] = set()
    if child.classroom_id is not None:
        routine_days = set(
            (
                await db.scalars(
                    select(RoutineMoment.date)
                    .where(RoutineMoment.classroom_id == child.classroom_id, RoutineMoment.date.between(monday, friday))
                    .distinct()
                )
            ).all()
        )
    reports = (
        await db.execute(
            select(RoutineMoment.date, Report.type, Report.description)
            .join(Report, Report.routine_moment_id == RoutineMoment.id)
            .join(ReportChild, ReportChild.report_id == Report.id)
            .where(ReportChild.child_id == child.id, RoutineMoment.date.between(monday, friday))
            .order_by(Report.id)
        )
    ).all()
    notes = dict(
        (
            await db.execute(
                select(ParentNote.date, func.count())
                .where(ParentNote.child_id == child.id, ParentNote.date.between(monday, friday))
                .group_by(ParentNote.date)
            )
        ).all()
    )

    days: list[WeekDayOut] = []
    for day in week:
        summary = summaries.get(day)
        reports_today = [report for report in reports if report.date == day]
        if day > today:
            status = WeekDayStatus.NO_RECORD
        elif day in present:
            status = WeekDayStatus.ATTENDED
        elif day in absent:
            status = WeekDayStatus.ABSENT
        elif summary is not None or reports_today:
            status = WeekDayStatus.ATTENDED
        elif day in routine_days:
            status = WeekDayStatus.ABSENT
        else:
            status = WeekDayStatus.NO_RECORD
        days.append(
            WeekDayOut(
                date=day,
                status=status,
                mood=summary.mood if summary else None,
                meals=summary.meals if summary else None,
                diaper_changes=summary.diaper_changes if summary else None,
                nap_minutes=summary.nap_minutes if summary else None,
                reports=len(reports_today),
                family_notes=notes.get(day, 0),
            )
        )

    def tally(values) -> dict:
        counts: dict = {}
        for value in values:
            if value is not None:
                counts[value] = counts.get(value, 0) + 1
        return counts

    naps = [day.nap_minutes for day in days if day.nap_minutes is not None and day.status == WeekDayStatus.ATTENDED]
    return WeeklySummaryOut(
        child_id=child.id,
        child_name=child.full_name,
        week_start=monday,
        week_end=friday,
        days_attended=sum(1 for day in days if day.status == WeekDayStatus.ATTENDED),
        days_absent=sum(1 for day in days if day.status == WeekDayStatus.ABSENT),
        moods=tally(day.mood for day in days),
        meals=tally(day.meals for day in days),
        nap_minutes_average=round(sum(naps) / len(naps)) if naps else None,
        diaper_changes_total=sum(day.diaper_changes or 0 for day in days),
        reports_by_type=tally(report.type for report in reports),
        achievements=[report.description for report in reports if report.type == ReportType.ACHIEVEMENT],
        family_notes=sum(day.family_notes for day in days),
        days=days,
    )

