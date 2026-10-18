from datetime import date

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import rules
from app.core.errors import ApiError
from app.core.schemas import ClassroomRef
from app.core.security import CurrentUser, get_child_in_scope
from app.modules.auth.models import AppUser
from app.modules.classrooms.centers import center_today, get_active_classroom
from app.modules.classrooms.models import Classroom
from app.modules.parents import service as parents
from app.modules.parents.models import ParentChildRel

from .models import Child, ChildStatus
from .schemas import ChildCreate, ChildDetail, ChildOut, ChildUpdate, GuardianOut


async def _guardians(db: AsyncSession, child_ids: list[int]) -> dict[int, list[GuardianOut]]:
    rows = (
        await db.execute(
            select(ParentChildRel, AppUser)
            .join(AppUser, AppUser.id == ParentChildRel.parent_id)
            .where(ParentChildRel.child_id.in_(child_ids))
            .order_by(ParentChildRel.is_primary.desc(), ParentChildRel.id)
        )
    ).all()
    guardians: dict[int, list[GuardianOut]] = {}
    for link, user in rows:
        guardians.setdefault(link.child_id, []).append(
            GuardianOut(
                id=user.id,
                name=user.name,
                surname=user.surname,
                full_name=user.full_name,
                relationship=link.relationship,
                phone=user.phone,
                email=user.email,
                is_primary=link.is_primary,
                has_account=user.password_hash is not None,
            )
        )
    return guardians


def _to_out(child: Child, classroom: Classroom | None, guardians: list[GuardianOut], today: date) -> ChildOut:
    return ChildOut(
        id=child.id,
        name=child.name,
        surname=child.surname,
        full_name=child.full_name,
        birth_date=child.birth_date,
        age=rules.age_on(child.birth_date, today) if child.birth_date else None,
        allergies=child.allergies,
        classroom=ClassroomRef.model_validate(classroom) if classroom else None,
        status=child.status,
        active=child.status == ChildStatus.ACTIVE,
        guardian=guardians[0] if guardians else None,
    )


async def list_children(db: AsyncSession) -> list[ChildOut]:
    rows = (
        await db.execute(
            select(Child, Classroom)
            .outerjoin(Classroom, Classroom.id == Child.classroom_id)
            .order_by(Child.name, Child.surname)
        )
    ).all()
    guardians = await _guardians(db, [child.id for child, _ in rows])
    today = await center_today(db)
    return [_to_out(child, classroom, guardians.get(child.id, []), today) for child, classroom in rows]


async def get_detail(db: AsyncSession, child: Child) -> ChildDetail:
    classroom = await db.get(Classroom, child.classroom_id) if child.classroom_id else None
    guardians = (await _guardians(db, [child.id])).get(child.id, [])
    out = _to_out(child, classroom, guardians, await center_today(db))
    return ChildDetail(**out.model_dump(), guardians=guardians)


async def get_child(db: AsyncSession, current: CurrentUser, child_id: int) -> ChildDetail:
    return await get_detail(db, await get_child_in_scope(db, current, child_id))


async def create_child(db: AsyncSession, data: ChildCreate) -> ChildDetail:
    """Inscribe a un infante; si viene tutor, lo registra y le crea su invitación."""
    name = rules.require_name(data.name)
    rules.check_age(data.birth_date, await center_today(db))
    # el tutor es opcional: la pestaña Padres también registra a los papás y los liga a sus hijos.
    guardian = data.guardian
    phone = email = None
    if guardian is not None:
        if not guardian.name.strip():
            raise ApiError(422, "GUARDIAN_REQUIRED", "El tutor necesita un nombre")
        phone = rules.check_phone(guardian.phone)
        email = rules.check_email(guardian.email)
    if data.classroom_id is not None:
        await get_active_classroom(db, data.classroom_id)

    child = Child(
        name=name,
        surname=data.surname.strip(),
        birth_date=data.birth_date,
        allergies=(data.allergies or "").strip() or None,
        classroom_id=data.classroom_id,
    )
    db.add(child)
    await db.flush()
    access_code = None
    if guardian is not None:
        _, access_code = await parents.link_guardian(
            db,
            child,
            name=guardian.name.strip(),
            surname=guardian.surname.strip(),
            phone=phone,
            email=email,
            relationship=guardian.relationship,
            is_primary=True,
        )
    await db.commit()
    out = await get_detail(db, child)
    out.access_code = access_code
    return out


async def update_child(db: AsyncSession, child_id: int, data: ChildUpdate) -> ChildDetail:
    """Cambia al infante de sala. Sirve para "Cambiar de sala", "Agregar a esta sala" y "Sacar de la sala"."""
    child = await _get(db, child_id)
    if data.classroom_id is not None:
        await get_active_classroom(db, data.classroom_id)
    child.classroom_id = data.classroom_id
    await db.commit()
    return await get_detail(db, child)


async def set_active(db: AsyncSession, child_id: int, active: bool) -> ChildDetail:
    child = await _get(db, child_id)
    child.status = ChildStatus.ACTIVE if active else ChildStatus.INACTIVE
    await db.commit()
    return await get_detail(db, child)


async def _get(db: AsyncSession, child_id: int) -> Child:
    child = await db.get(Child, child_id)
    if child is None:
        raise ApiError(404, "CHILD_NOT_FOUND", "No existe ese niño")
    return child
