from datetime import date

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock
from app.core.errors import ApiError
from app.core.security import CurrentUser, ensure_classroom_access, get_child_in_scope
from app.modules.attendance.queries import present_child_ids
from app.modules.audit import service as audit
from app.modules.auth.models import AppUser
from app.modules.children.models import Child, ChildStatus
from app.modules.classrooms.centers import center_today, get_active_classroom
from app.modules.documents.models import AuthorizedPickup
from app.modules.parents.models import ParentChildRel

from .models import ChildDeparture
from .schemas import DepartureChild, DepartureIn, DepartureOut, DepartureSheet, PickupChoice, PickupKind


def _out(departure: ChildDeparture) -> DepartureOut:
    return DepartureOut(
        child_id=departure.child_id,
        date=departure.date,
        departed_at=departure.departed_at,
        picked_up_by=departure.picked_up_by,
    )


async def _choices(db: AsyncSession, child_ids: list[int]) -> dict[int, list[PickupChoice]]:
    """Las personas autorizadas que registró el admin y, además, los tutores del niño."""
    choices: dict[int, list[PickupChoice]] = {child_id: [] for child_id in child_ids}
    for pickup in (
        await db.scalars(
            select(AuthorizedPickup).where(AuthorizedPickup.child_id.in_(child_ids)).order_by(AuthorizedPickup.full_name)
        )
    ).all():
        choices[pickup.child_id].append(
            PickupChoice(
                kind=PickupKind.AUTHORIZED, id=str(pickup.id), full_name=pickup.full_name, relationship=pickup.relationship
            )
        )
    rows = (
        await db.execute(
            select(ParentChildRel, AppUser)
            .join(AppUser, AppUser.id == ParentChildRel.parent_id)
            .where(ParentChildRel.child_id.in_(child_ids))
            .order_by(ParentChildRel.is_primary.desc(), ParentChildRel.id)
        )
    ).all()
    for link, user in rows:
        choices[link.child_id].append(
            PickupChoice(
                kind=PickupKind.GUARDIAN, id=str(user.id), full_name=user.full_name, relationship=link.relationship.value
            )
        )
    return choices


async def sheet(db: AsyncSession, current: CurrentUser, classroom_id: int, day: date | None) -> DepartureSheet:
    """Los niños presentes de la sala, ordenados por nombre, con su salida si ya se registró."""
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
    present = await present_child_ids(db, [child.id for child in children], day)
    children = [child for child in children if child.id in present]
    ids = [child.id for child in children]
    departures = (
        {d.child_id: d for d in (await db.scalars(select(ChildDeparture).where(ChildDeparture.child_id.in_(ids), ChildDeparture.date == day))).all()}
        if ids
        else {}
    )
    choices = await _choices(db, ids) if ids else {}
    items = [
        DepartureChild(
            child_id=child.id,
            name=child.name,
            full_name=child.full_name,
            choices=choices.get(child.id, []),
            departure=_out(departures[child.id]) if child.id in departures else None,
        )
        for child in children
    ]
    pending = sum(1 for item in items if item.departure is None)
    return DepartureSheet(date=day, children=items, pending=pending, all_done=pending == 0)


async def register(db: AsyncSession, current: CurrentUser, child_id: int, data: DepartureIn) -> DepartureOut:
    """Registra la hora de salida de un niño y con quién se fue. Solo si hoy estuvo presente."""
    child = await get_child_in_scope(db, current, child_id)
    day = await center_today(db)
    if child.id not in await present_child_ids(db, [child.id], day):
        raise ApiError(409, "CHILD_NOT_PRESENT", "Ese niño no está marcado como presente hoy")
    if (data.pickup_id is None) == (data.guardian_id is None):
        raise ApiError(422, "PICKUP_REQUIRED", "Elige quién recoge al niño")
    if await db.scalar(select(ChildDeparture.id).where(ChildDeparture.child_id == child.id, ChildDeparture.date == day)):
        raise ApiError(409, "ALREADY_DEPARTED", "La salida de este niño ya está registrada")

    pickup_id = guardian_id = None
    if data.pickup_id is not None:
        pickup = await db.get(AuthorizedPickup, data.pickup_id)
        if pickup is None or pickup.child_id != child.id:
            raise ApiError(404, "PICKUP_NOT_FOUND", "Esa persona no está autorizada para recoger a este niño")
        pickup_id, name = pickup.id, pickup.full_name
    else:
        link = await db.scalar(
            select(ParentChildRel).where(ParentChildRel.child_id == child.id, ParentChildRel.parent_id == data.guardian_id)
        )
        user = await db.get(AppUser, data.guardian_id)
        if link is None or user is None:
            raise ApiError(404, "PICKUP_NOT_FOUND", "Esa persona no es tutora de este niño")
        guardian_id, name = user.id, user.full_name

    departure = ChildDeparture(
        child_id=child.id,
        date=day,
        departed_at=clock.now(),
        pickup_id=pickup_id,
        guardian_id=guardian_id,
        picked_up_by=name,
        recorded_by=current.id,
    )
    db.add(departure)
    audit.record(
        db, audit.CHILD_DEPARTED, actor_id=current.id, entity_type="CHILD", entity_id=child.id,
        detail={"picked_up_by": name},
    )
    await db.commit()
    return _out(departure)


async def undo(db: AsyncSession, current: CurrentUser, child_id: int) -> None:
    """Borra la salida de hoy de un niño, por si la docente se equivocó de persona."""
    child = await get_child_in_scope(db, current, child_id)
    day = await center_today(db)
    departure = await db.scalar(
        select(ChildDeparture).where(ChildDeparture.child_id == child.id, ChildDeparture.date == day)
    )
    if departure is None:
        raise ApiError(404, "DEPARTURE_NOT_FOUND", "Ese niño todavía no tiene salida registrada hoy")
    await db.delete(departure)
    audit.record(db, audit.CHILD_DEPARTURE_UNDONE, actor_id=current.id, entity_type="CHILD", entity_id=child.id)
    await db.commit()
