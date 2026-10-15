from datetime import date

from sqlalchemy import delete, func, select, update
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock
from app.core import rules
from app.core.errors import ApiError
from app.core.schemas import ClassroomRef
from app.core.security import CurrentUser, ensure_classroom_access
from app.modules.classrooms.centers import center_today, get_active_classroom
from app.modules.classrooms.models import Classroom
from app.modules.reports.models import Report

from .models import MomentStatus, RoutineMoment, RoutineTemplate, RoutineTemplateMoment
from .schemas import (
    MomentOut,
    RoutineOut,
    TemplateAssign,
    TemplateAssignOut,
    TemplateIn,
    TemplateMomentOut,
    TemplateOut,
)


async def _template_for(db: AsyncSession, classroom: Classroom) -> RoutineTemplate | None:
    if classroom.routine_template_id is not None:
        return await db.get(RoutineTemplate, classroom.routine_template_id)
    return await db.scalar(
        select(RoutineTemplate).order_by(RoutineTemplate.is_default.desc(), RoutineTemplate.id).limit(1)
    )


async def ensure_day(db: AsyncSession, classroom_id: int, day: date) -> None:
    """Arranca el día de la sala: copia los momentos de su plantilla, con el primero en curso."""
    already = await db.scalar(
        select(RoutineMoment.id).where(RoutineMoment.classroom_id == classroom_id, RoutineMoment.date == day).limit(1)
    )
    if already is not None:
        return
    classroom = await db.get(Classroom, classroom_id)
    template = await _template_for(db, classroom)
    if template is None:
        return
    moments = (
        await db.scalars(
            select(RoutineTemplateMoment)
            .where(RoutineTemplateMoment.template_id == template.id)
            .order_by(RoutineTemplateMoment.number)
        )
    ).all()
    if not moments:
        return
    now = clock.now()
    values = [
        {
            "classroom_id": classroom_id,
            "template_id": template.id,
            "date": day,
            "number": moment.number,
            "name": moment.name,
            "category": moment.category,
            "status": MomentStatus.ACTIVE if index == 0 else MomentStatus.PENDING,
            "started_at": now if index == 0 else None,
        }
        for index, moment in enumerate(moments)
    ]
    # si dos docentes de la sala llegan al mismo tiempo, la segunda no duplica el día.
    await db.execute(insert(RoutineMoment).values(values).on_conflict_do_nothing())
    await db.commit()


async def routine_out(db: AsyncSession, classroom_id: int, day: date) -> RoutineOut:
    moments = (
        await db.scalars(
            select(RoutineMoment)
            .where(RoutineMoment.classroom_id == classroom_id, RoutineMoment.date == day)
            .order_by(RoutineMoment.number)
        )
    ).all()
    counts = dict(
        (
            await db.execute(
                select(Report.routine_moment_id, func.count())
                .where(Report.routine_moment_id.in_([moment.id for moment in moments]))
                .group_by(Report.routine_moment_id)
            )
        ).all()
    )
    template = await db.get(RoutineTemplate, moments[0].template_id) if moments and moments[0].template_id else None
    active = next((moment for moment in moments if moment.status == MomentStatus.ACTIVE), None)
    return RoutineOut(
        classroom_id=classroom_id,
        date=day,
        name=template.name if template else None,
        moments=[
            MomentOut(
                id=moment.id,
                number=moment.number,
                name=moment.name,
                category=moment.category,
                status=moment.status,
                started_at=moment.started_at,
                confirmed_at=moment.confirmed_at,
                description=moment.description,
                report_count=counts.get(moment.id, 0),
            )
            for moment in moments
        ],
        current_step=sum(1 for moment in moments if moment.status != MomentStatus.PENDING),
        total_steps=len(moments),
        active_moment_id=active.id if active else None,
    )


async def get_routine(db: AsyncSession, current: CurrentUser, classroom_id: int, day: date | None) -> RoutineOut:
    ensure_classroom_access(current, classroom_id)
    await get_active_classroom(db, classroom_id)
    today = await center_today(db)
    day = day or today
    if day == today:
        await ensure_day(db, classroom_id, day)
    return await routine_out(db, classroom_id, day)


async def get_moment_in_scope(
    db: AsyncSession, current: CurrentUser, moment_id: int, *, for_update: bool = False
) -> RoutineMoment:
    query = select(RoutineMoment).where(RoutineMoment.id == moment_id)
    if for_update:
        query = query.with_for_update()
    moment = await db.scalar(query)
    if moment is None:
        raise ApiError(404, "MOMENT_NOT_FOUND", "No existe ese momento")
    try:
        ensure_classroom_access(current, moment.classroom_id)
    except ApiError:
        raise ApiError(404, "MOMENT_NOT_FOUND", "No existe ese momento")
    return moment


async def confirm(db: AsyncSession, current: CurrentUser, moment_id: int) -> RoutineOut:
    """Cierra el momento activo y arranca el siguiente (RoutineRules.confirm)."""
    # la fila queda bloqueada: dos asistentes de la sala no pueden confirmar el mismo momento a la vez.
    moment = await get_moment_in_scope(db, current, moment_id, for_update=True)
    if moment.status != MomentStatus.ACTIVE:
        raise ApiError(409, "MOMENT_NOT_ACTIVE", "Solo se puede confirmar el momento en curso")
    now = clock.now()
    moment.status = MomentStatus.DONE
    moment.confirmed_at = now
    moment.confirmed_by = current.id
    await db.flush()
    following = await db.scalar(
        select(RoutineMoment)
        .where(
            RoutineMoment.classroom_id == moment.classroom_id,
            RoutineMoment.date == moment.date,
            RoutineMoment.number > moment.number,
            RoutineMoment.status == MomentStatus.PENDING,
        )
        .order_by(RoutineMoment.number)
        .limit(1)
        .with_for_update()
    )
    if following is not None:
        following.status = MomentStatus.ACTIVE
        following.started_at = now
    await db.commit()
    return await routine_out(db, moment.classroom_id, moment.date)


# --- plantillas de rutina (administrador) ---
# Cambiar una plantilla no toca los días ya empezados: cada día guarda su propia copia de los momentos.


async def _templates_out(db: AsyncSession, template_id: int | None = None) -> list[TemplateOut]:
    query = select(RoutineTemplate).order_by(RoutineTemplate.is_default.desc(), RoutineTemplate.name)
    if template_id is not None:
        query = query.where(RoutineTemplate.id == template_id)
    templates = (await db.scalars(query)).all()
    ids = [template.id for template in templates]
    moments = (
        await db.scalars(
            select(RoutineTemplateMoment)
            .where(RoutineTemplateMoment.template_id.in_(ids))
            .order_by(RoutineTemplateMoment.template_id, RoutineTemplateMoment.number)
        )
    ).all()
    classrooms = (
        await db.scalars(
            select(Classroom)
            .where(Classroom.routine_template_id.in_(ids), Classroom.deleted_at.is_(None))
            .order_by(Classroom.name)
        )
    ).all()
    return [
        TemplateOut(
            id=template.id,
            name=template.name,
            is_default=template.is_default,
            moments=[TemplateMomentOut(number=m.number, name=m.name, category=m.category) for m in moments if m.template_id == template.id],
            classrooms=[ClassroomRef.model_validate(c) for c in classrooms if c.routine_template_id == template.id],
        )
        for template in templates
    ]


def _check_template(data: TemplateIn) -> str:
    name = rules.require_name(data.name)
    if not data.moments:
        raise ApiError(422, "MOMENTS_REQUIRED", "La plantilla necesita al menos un momento")
    if any(not moment.name.strip() for moment in data.moments):
        raise ApiError(422, "NAME_REQUIRED", "Cada momento necesita un nombre")
    return name


async def _set_moments(db: AsyncSession, template_id: int, data: TemplateIn) -> None:
    await db.execute(delete(RoutineTemplateMoment).where(RoutineTemplateMoment.template_id == template_id))
    db.add_all(
        RoutineTemplateMoment(template_id=template_id, number=number, name=moment.name.strip(), category=moment.category)
        for number, moment in enumerate(data.moments, start=1)
    )


async def _only_default(db: AsyncSession, template_id: int) -> None:
    await db.execute(update(RoutineTemplate).where(RoutineTemplate.id != template_id).values(is_default=False))


async def list_templates(db: AsyncSession) -> list[TemplateOut]:
    return await _templates_out(db)


async def create_template(db: AsyncSession, data: TemplateIn) -> TemplateOut:
    name = _check_template(data)
    template = RoutineTemplate(name=name, is_default=data.is_default)
    db.add(template)
    await db.flush()
    await _set_moments(db, template.id, data)
    if data.is_default:
        await _only_default(db, template.id)
    await db.commit()
    return (await _templates_out(db, template.id))[0]


async def update_template(db: AsyncSession, template_id: int, data: TemplateIn) -> TemplateOut:
    template = await db.get(RoutineTemplate, template_id)
    if template is None:
        raise ApiError(404, "TEMPLATE_NOT_FOUND", "No existe esa plantilla")
    template.name = _check_template(data)
    # la default no se puede quitar editándola: otra plantilla tiene que tomar su lugar.
    if data.is_default:
        await _only_default(db, template.id)
        template.is_default = True
    elif template.is_default:
        raise ApiError(409, "DEFAULT_REQUIRED", "Marca otra plantilla como la default para quitarle esta")
    await _set_moments(db, template.id, data)
    await db.commit()
    return (await _templates_out(db, template.id))[0]


async def assign_template(db: AsyncSession, classroom_id: int, data: TemplateAssign) -> TemplateAssignOut:
    classroom = await get_active_classroom(db, classroom_id)
    if data.template_id is not None and await db.get(RoutineTemplate, data.template_id) is None:
        raise ApiError(404, "TEMPLATE_NOT_FOUND", "No existe esa plantilla")
    classroom.routine_template_id = data.template_id
    await db.commit()
    return TemplateAssignOut(classroom=ClassroomRef.model_validate(classroom), template_id=classroom.routine_template_id)
