import uuid

from fastapi import APIRouter, Query

from app.core.db import Db
from app.core.pagination import Page
from app.core.security import AdminUser

from . import service
from .schemas import AuditOut

router = APIRouter(tags=["auditoría"])


@router.get("/audit-log", response_model=Page[AuditOut])
async def list_audit_log(
    db: Db,
    _: AdminUser,
    action: str | None = None,
    actor_id: uuid.UUID | None = None,
    limit: int = Query(50, ge=1, le=200),
    cursor: str | None = None,
) -> Page[AuditOut]:
    """La bitácora de quién hizo qué, lo más nuevo primero. Solo el administrador la ve.
    Filtra con ?action= (por ejemplo LOGIN_FAILED) y ?actor_id=; pagina con ?cursor=."""
    return await service.list_entries(db, action=action, actor_id=actor_id, limit=limit, cursor=cursor)
