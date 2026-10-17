import uuid

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import rules
from app.core.errors import ApiError
from app.core.schemas import ClassroomRef
from app.core.security import revoke_all_sessions
from app.modules.auth import service as auth
from app.modules.auth.models import AppUser, Role, UserStatus
from app.modules.children.models import Child, ChildStatus
from app.modules.classrooms.centers import center_today, get_active_classroom
from app.modules.classrooms.models import Classroom
from app.modules.staff_attendance.models import StaffCheckIn

from .models import Shift, Teacher
from .schemas import ShiftOut, TeacherCreate, TeacherOut, TeacherUpdate


async def _rows(db: AsyncSession, teacher_id: uuid.UUID | None = None) -> list[TeacherOut]:
    today = await center_today(db)
    query = (
        select(Teacher, AppUser, Classroom, Shift, StaffCheckIn.checked_in_at)
        .join(AppUser, AppUser.id == Teacher.user_id)
        .outerjoin(Classroom, Classroom.id == Teacher.classroom_id)
        .outerjoin(Shift, Shift.id == Teacher.shift_id)
        .outerjoin(StaffCheckIn, (StaffCheckIn.teacher_id == Teacher.user_id) & (StaffCheckIn.date == today))
        .order_by(AppUser.name, AppUser.surname)
    )
    if teacher_id is not None:
        query = query.where(Teacher.user_id == teacher_id)
    rows = (await db.execute(query)).all()

    classroom_ids = [teacher.classroom_id for teacher, *_ in rows if teacher.classroom_id is not None]
    counts = dict(
        (
            await db.execute(
                select(Child.classroom_id, func.count())
                .where(Child.classroom_id.in_(classroom_ids), Child.status == ChildStatus.ACTIVE)
                .group_by(Child.classroom_id)
            )
        ).all()
    )
    return [
        TeacherOut(
            id=user.id,
            name=user.name,
            surname=user.surname,
            full_name=user.full_name,
            phone=user.phone,
            email=user.email,
            classroom=ClassroomRef.model_validate(classroom) if classroom else None,
            is_primary=teacher.is_primary,
            shift=ShiftOut.model_validate(shift) if shift else None,
            checked_in_at=checked_in_at,
            active=user.status == UserStatus.ACTIVE,
            has_account=user.password_hash is not None,
            children_count=counts.get(teacher.classroom_id, 0),
        )
        for teacher, user, classroom, shift, checked_in_at in rows
    ]


async def list_teachers(db: AsyncSession) -> list[TeacherOut]:
    return await _rows(db)


async def get_teacher(db: AsyncSession, teacher_id: uuid.UUID) -> TeacherOut:
    rows = await _rows(db, teacher_id)
    if not rows:
        raise ApiError(404, "TEACHER_NOT_FOUND", "No existe esa docente")
    return rows[0]


async def _get_shift(db: AsyncSession, shift_id: int) -> Shift:
    shift = await db.get(Shift, shift_id)
    if shift is None:
        raise ApiError(404, "SHIFT_NOT_FOUND", "No existe ese turno")
    return shift


async def create_teacher(db: AsyncSession, data: TeacherCreate) -> TeacherOut:
    """Da de alta a una docente y le crea su invitación para entrar a la app."""
    name = rules.require_name(data.name)
    phone = rules.check_phone(data.phone)
    email = rules.check_email(data.email)
    if data.classroom_id is not None:
        await get_active_classroom(db, data.classroom_id)
    if data.shift_id is not None:
        await _get_shift(db, data.shift_id)

    user = await db.scalar(select(AppUser).where(AppUser.email == email))
    if user is not None and await db.get(Teacher, user.id) is not None:
        raise ApiError(409, "EMAIL_TAKEN", "Ya hay una docente con ese correo")
    if user is None:
        user = AppUser(name=name, surname=data.surname.strip(), phone=phone, email=email)
        db.add(user)
        await db.flush()
    db.add(Teacher(user_id=user.id, classroom_id=data.classroom_id, shift_id=data.shift_id))
    created = await auth.create_invitation(db, user, Role.TEACHER)
    await db.commit()
    out = await get_teacher(db, user.id)
    out.access_code = created[1] if created else None
    return out


async def update_teacher(db: AsyncSession, teacher_id: uuid.UUID, data: TeacherUpdate) -> TeacherOut:
    """Cambia a la docente de sala o de turno."""
    teacher = await db.get(Teacher, teacher_id)
    if teacher is None:
        raise ApiError(404, "TEACHER_NOT_FOUND", "No existe esa docente")
    if "classroom_id" in data.model_fields_set and data.classroom_id != teacher.classroom_id:
        if data.classroom_id is not None:
            await get_active_classroom(db, data.classroom_id)
        teacher.classroom_id = data.classroom_id
        # la principal de una sala deja de serlo al cambiarse.
        teacher.is_primary = False
    if "shift_id" in data.model_fields_set:
        if data.shift_id is not None:
            await _get_shift(db, data.shift_id)
        teacher.shift_id = data.shift_id
    await db.commit()
    return await get_teacher(db, teacher_id)


async def set_active(db: AsyncSession, teacher_id: uuid.UUID, active: bool) -> TeacherOut:
    """Dar de baja quita el acceso y cierra sus sesiones; reactivar se lo devuelve."""
    if await db.get(Teacher, teacher_id) is None:
        raise ApiError(404, "TEACHER_NOT_FOUND", "No existe esa docente")
    user = await db.get(AppUser, teacher_id)
    user.status = UserStatus.ACTIVE if active else UserStatus.INACTIVE
    if not active:
        await revoke_all_sessions(db, user)
    await db.commit()
    return await get_teacher(db, teacher_id)


async def list_shifts(db: AsyncSession) -> list[ShiftOut]:
    shifts = (await db.scalars(select(Shift).order_by(Shift.start_time))).all()
    return [ShiftOut.model_validate(shift) for shift in shifts]
