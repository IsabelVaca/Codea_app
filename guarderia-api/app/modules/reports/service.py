from datetime import date

from sqlalchemy import select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.errors import ApiError
from app.core.schemas import ClassroomRef
from app.core.security import CurrentUser, get_child_in_scope
from app.modules.attendance.models import Attendance, AttendanceStatus
from app.modules.children.models import Child, ChildStatus
from app.modules.classrooms.centers import center_of_classroom, center_today
from app.modules.classrooms.models import Classroom
from app.modules.media.models import Media, MediaChild, MediaKind, MediaStatus
from app.modules.messaging.models import ParentNote
from app.modules.messaging.schemas import NoteOut
from app.modules.routines import service as routines
from app.modules.routines.models import MomentCategory, MomentStatus, RoutineMoment

from .models import DailySummary, Report, ReportChild
from .schemas import (
    DailyEntryIn,
    DailyEntryOut,
    DayChild,
    DayOut,
    ReportIn,
    ReportOut,
    TimelineEvent,
    TimelineType,
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
    absent = (
        await db.scalars(
            select(Attendance.child_id).where(
                Attendance.child_id.in_(child_ids),
                Attendance.date == moment.date,
                Attendance.status == AttendanceStatus.ABSENT,
            )
        )
    ).all()
    if absent:
        raise ApiError(409, "CHILD_ABSENT", "Un niño que faltó hoy no puede estar en un reporte", child_ids=sorted(absent))

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
    attendance = marked or AttendanceStatus.PRESENT
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
