import uuid
from datetime import date

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock, rules
from app.core.errors import ApiError
from app.core.security import CurrentUser, ensure_classroom_access
from app.modules.attendance.models import Attendance, AttendanceStatus
from app.modules.auth.models import AppUser
from app.modules.children.models import Child, ChildStatus
from app.modules.messaging import service as messaging
from app.modules.teachers.models import Teacher

from .centers import center_today, get_active_classroom
from .models import Classroom
from .schemas import (
    ClassroomChild,
    ClassroomCreate,
    ClassroomDetail,
    ClassroomOut,
    ClassroomUpdate,
    RoomChild,
    StaffMember,
    StaffUpdate,
)


async def _staff(db: AsyncSession, classroom_ids: list[int]) -> dict[int, list[StaffMember]]:
    rows = (
        await db.execute(
            select(Teacher, AppUser)
            .join(AppUser, AppUser.id == Teacher.user_id)
            .where(Teacher.classroom_id.in_(classroom_ids))
            .order_by(Teacher.is_primary.desc(), AppUser.name)
        )
    ).all()
    staff: dict[int, list[StaffMember]] = {}
    for teacher, user in rows:
        staff.setdefault(teacher.classroom_id, []).append(
            StaffMember(
                id=user.id,
                name=user.name,
                surname=user.surname,
                full_name=user.full_name,
                is_primary=teacher.is_primary,
            )
        )
    return staff


async def _enrolled(db: AsyncSession, classroom_ids: list[int]) -> dict[int, int]:
    rows = await db.execute(
        select(Child.classroom_id, func.count())
        .where(Child.classroom_id.in_(classroom_ids), Child.status == ChildStatus.ACTIVE)
        .group_by(Child.classroom_id)
    )
    return dict(rows.all())


def _to_out(classroom: Classroom, enrolled: int, staff: list[StaffMember]) -> ClassroomOut:
    return ClassroomOut(
        id=classroom.id,
        name=classroom.name,
        school_grade=classroom.school_grade,
        capacity=classroom.capacity,
        enrolled=enrolled,
        available=max(classroom.capacity - enrolled, 0),
        primary_teacher=next((member for member in staff if member.is_primary), None),
    )


async def list_classrooms(db: AsyncSession, center_id: int) -> list[ClassroomOut]:
    classrooms = (
        await db.scalars(
            select(Classroom)
            .where(Classroom.daycare_center_id == center_id, Classroom.deleted_at.is_(None))
            .order_by(Classroom.name)
        )
    ).all()
    ids = [classroom.id for classroom in classrooms]
    enrolled = await _enrolled(db, ids)
    staff = await _staff(db, ids)
    return [_to_out(c, enrolled.get(c.id, 0), staff.get(c.id, [])) for c in classrooms]


async def get_detail(db: AsyncSession, classroom_id: int) -> ClassroomDetail:
    classroom = await get_active_classroom(db, classroom_id)
    staff = (await _staff(db, [classroom.id])).get(classroom.id, [])
    children = (
        await db.scalars(
            select(Child)
            .where(Child.classroom_id == classroom.id, Child.status == ChildStatus.ACTIVE)
            .order_by(Child.name, Child.surname)
        )
    ).all()
    today = await center_today(db)
    out = _to_out(classroom, len(children), staff)
    return ClassroomDetail(
        **out.model_dump(),
        children=[
            ClassroomChild(
                id=child.id,
                name=child.name,
                surname=child.surname,
                full_name=child.full_name,
                birth_date=child.birth_date,
                age=rules.age_on(child.birth_date, today) if child.birth_date else None,
                status=child.status,
            )
            for child in children
        ],
        teachers=staff,
    )


async def _get_teacher(db: AsyncSession, teacher_id: uuid.UUID) -> Teacher:
    teacher = await db.get(Teacher, teacher_id)
    if teacher is None:
        raise ApiError(404, "TEACHER_NOT_FOUND", "No existe esa docente")
    return teacher


async def create_classroom(db: AsyncSession, current: CurrentUser, data: ClassroomCreate) -> ClassroomDetail:
    name = rules.require_name(data.name)
    capacity = rules.check_capacity(data.capacity)
    teacher = await _get_teacher(db, data.teacher_id) if data.teacher_id else None

    classroom = Classroom(
        daycare_center_id=current.admin.daycare_center_id,
        name=name,
        capacity=capacity,
        school_grade=data.school_grade,
    )
    db.add(classroom)
    await db.flush()
    if teacher is not None:
        # si era la principal de otra sala, deja de serlo antes de pasar a esta.
        teacher.is_primary = False
        await db.flush()
        teacher.classroom_id = classroom.id
        teacher.is_primary = True
    await db.commit()
    return await get_detail(db, classroom.id)


async def update_classroom(db: AsyncSession, classroom_id: int, data: ClassroomUpdate) -> ClassroomDetail:
    classroom = await get_active_classroom(db, classroom_id)
    if data.name is not None:
        classroom.name = rules.require_name(data.name)
    if data.capacity is not None:
        classroom.capacity = rules.check_capacity(data.capacity)
    if "school_grade" in data.model_fields_set:
        classroom.school_grade = data.school_grade
    await db.commit()
    return await get_detail(db, classroom.id)


async def set_staff(db: AsyncSession, classroom_id: int, data: StaffUpdate) -> ClassroomDetail:
    """Deja en la sala exactamente a estas docentes, con una principal (o ninguna)."""
    classroom = await get_active_classroom(db, classroom_id)
    ids = set(data.teacher_ids)
    if data.primary_teacher_id is not None and data.primary_teacher_id not in ids:
        raise ApiError(422, "PRIMARY_NOT_IN_STAFF", "La asistente principal debe estar entre las asistentes de la sala")
    chosen = (await db.scalars(select(Teacher).where(Teacher.user_id.in_(ids)))).all()
    if len(chosen) != len(ids):
        raise ApiError(404, "TEACHER_NOT_FOUND", "No existe esa docente")
    current_staff = (await db.scalars(select(Teacher).where(Teacher.classroom_id == classroom.id))).all()

    # primero nadie es principal, para que el índice de "una principal por sala" nunca vea dos.
    for teacher in [*current_staff, *chosen]:
        teacher.is_primary = False
    await db.flush()
    for teacher in current_staff:
        if teacher.user_id not in ids:
            teacher.classroom_id = None
    for teacher in chosen:
        teacher.classroom_id = classroom.id
        teacher.is_primary = teacher.user_id == data.primary_teacher_id
    await db.commit()
    return await get_detail(db, classroom.id)


async def delete_classroom(db: AsyncSession, classroom_id: int) -> None:
    classroom = await get_active_classroom(db, classroom_id)
    active_children = await db.scalar(
        select(func.count()).where(Child.classroom_id == classroom.id, Child.status == ChildStatus.ACTIVE)
    )
    if active_children:
        raise ApiError(
            409,
            "CLASSROOM_NOT_EMPTY",
            "La sala todavía tiene niños inscritos",
            "Cámbialos de sala o sácalos antes de eliminarla",
        )
    for teacher in (await db.scalars(select(Teacher).where(Teacher.classroom_id == classroom.id))).all():
        teacher.is_primary = False
        teacher.classroom_id = None
    for child in (await db.scalars(select(Child).where(Child.classroom_id == classroom.id))).all():
        child.classroom_id = None
    classroom.deleted_at = clock.now()
    await db.commit()


async def room_children(
    db: AsyncSession, current: CurrentUser, classroom_id: int, day: date | None
) -> list[RoomChild]:
    ensure_classroom_access(current, classroom_id)
    await get_active_classroom(db, classroom_id)
    day = day or await center_today(db)
    children = (
        await db.scalars(
            select(Child)
            .where(Child.classroom_id == classroom_id, Child.status == ChildStatus.ACTIVE)
            .order_by(Child.name, Child.surname)
        )
    ).all()
    ids = [child.id for child in children]
    absent = set(
        (
            await db.scalars(
                select(Attendance.child_id).where(
                    Attendance.child_id.in_(ids),
                    Attendance.date == day,
                    Attendance.status == AttendanceStatus.ABSENT,
                )
            )
        ).all()
    )
    chats = await messaging.unread_by_child(db, current.id, ids)
    result = []
    for child in children:
        conversation_id, unread = chats.get(child.id, (None, 0))
        result.append(
            RoomChild(
                id=child.id,
                name=child.name,
                surname=child.surname,
                full_name=child.full_name,
                attendance=AttendanceStatus.ABSENT if child.id in absent else AttendanceStatus.PRESENT,
                conversation_id=conversation_id,
                has_unread_chat=unread > 0,
                unread_count=unread,
            )
        )
    return result
