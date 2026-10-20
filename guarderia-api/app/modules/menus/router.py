from datetime import date

from fastapi import APIRouter, status

from app.core.db import Db
from app.core.security import AdminUser, AnyUser

from . import service
from .schemas import MenuIn, MenuOut

router = APIRouter(tags=["menú del día"])


@router.get("/menus", response_model=list[MenuOut])
async def list_menus(db: Db, current: AnyUser, start: date | None = None, end: date | None = None) -> list[MenuOut]:
    """Los menús capturados entre `start` y `end` (por omisión, el de hoy). Los días sin menú no aparecen."""
    return await service.list_menus(db, current, start, end)


@router.put("/menus/{menu_date}", response_model=MenuOut)
async def put_menu(db: Db, current: AdminUser, menu_date: date, data: MenuIn) -> MenuOut:
    """Crea o reemplaza el menú de ese día. 422 MENU_EMPTY si no trae ninguna comida."""
    return await service.put_menu(db, current, menu_date, data)


@router.delete("/menus/{menu_date}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_menu(db: Db, current: AdminUser, menu_date: date) -> None:
    """Quita el menú de ese día. 404 MENU_NOT_FOUND."""
    await service.delete_menu(db, current, menu_date)
