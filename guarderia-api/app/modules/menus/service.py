from datetime import date, timedelta

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.errors import ApiError
from app.core.security import CurrentUser
from app.modules.audit import service as audit
from app.modules.classrooms.centers import center_today, default_center

from .models import DailyMenu
from .schemas import MenuIn, MenuOut

MAX_DAYS = 31


async def center_id_of(db: AsyncSession, current: CurrentUser) -> int:
    """El centro del admin; docente y papá ven el del centro (hoy hay uno solo)."""
    if current.admin is not None:
        return current.admin.daycare_center_id
    return (await default_center(db)).id


def _clean(value: str | None) -> str | None:
    return (value or "").strip() or None


async def menu_of(db: AsyncSession, center_id: int, day: date) -> MenuOut | None:
    """El menú de ese día, o None si todavía no lo capturaron."""
    menu = await db.scalar(select(DailyMenu).where(DailyMenu.daycare_center_id == center_id, DailyMenu.date == day))
    return MenuOut.model_validate(menu) if menu else None


async def list_menus(
    db: AsyncSession, current: CurrentUser, start: date | None, end: date | None
) -> list[MenuOut]:
    """Los menús capturados entre dos fechas (ambas incluidas), del día más viejo al más nuevo.
    Sin fechas, el de hoy."""
    start = start or await center_today(db)
    end = end or start
    if end < start or (end - start) > timedelta(days=MAX_DAYS):
        raise ApiError(422, "RANGE_INVALID", f"El rango debe ser de 0 a {MAX_DAYS} días y la fecha final no puede ser anterior")
    center_id = await center_id_of(db, current)
    rows = await db.scalars(
        select(DailyMenu)
        .where(DailyMenu.daycare_center_id == center_id, DailyMenu.date.between(start, end))
        .order_by(DailyMenu.date)
    )
    return [MenuOut.model_validate(row) for row in rows.all()]


async def put_menu(db: AsyncSession, current: CurrentUser, day: date, data: MenuIn) -> MenuOut:
    """Crea o reemplaza el menú de ese día. Si no trae ninguna comida, 422: para quitarlo se usa DELETE."""
    breakfast, lunch, snack = _clean(data.breakfast), _clean(data.lunch), _clean(data.snack)
    if breakfast is None and lunch is None and snack is None:
        raise ApiError(422, "MENU_EMPTY", "Escribe al menos una comida")
    center_id = await center_id_of(db, current)
    menu = await db.scalar(select(DailyMenu).where(DailyMenu.daycare_center_id == center_id, DailyMenu.date == day))
    if menu is None:
        menu = DailyMenu(daycare_center_id=center_id, date=day, updated_by=current.id)
        db.add(menu)
    menu.breakfast, menu.lunch, menu.snack, menu.updated_by = breakfast, lunch, snack, current.id
    audit.record(db, audit.MENU_SAVED, actor_id=current.id, entity_type="MENU", entity_id=None, detail={"date": day.isoformat()})
    await db.commit()
    await db.refresh(menu)
    return MenuOut.model_validate(menu)


async def delete_menu(db: AsyncSession, current: CurrentUser, day: date) -> None:
    center_id = await center_id_of(db, current)
    menu = await db.scalar(select(DailyMenu).where(DailyMenu.daycare_center_id == center_id, DailyMenu.date == day))
    if menu is None:
        raise ApiError(404, "MENU_NOT_FOUND", "Ese día no tiene menú")
    await db.delete(menu)
    audit.record(db, audit.MENU_DELETED, actor_id=current.id, entity_type="MENU", entity_id=None, detail={"date": day.isoformat()})
    await db.commit()
