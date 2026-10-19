import uuid
from typing import Any

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.pagination import Page, parse_cursor
from app.modules.auth.models import AppUser

from .models import AuditLog
from .schemas import AuditOut

# las acciones que se registran. Un solo lugar para que la app y las pruebas usen los mismos nombres.
LOGIN_OK = "LOGIN_OK"
LOGIN_FAILED = "LOGIN_FAILED"
LOGIN_LOCKED = "LOGIN_LOCKED"
ACCOUNT_ACTIVATED = "ACCOUNT_ACTIVATED"
ACTIVATION_FAILED = "ACTIVATION_FAILED"
PASSWORD_CHANGED = "PASSWORD_CHANGED"
SESSIONS_CLOSED = "SESSIONS_CLOSED"
TOKEN_REUSE_DETECTED = "TOKEN_REUSE_DETECTED"
ACCESS_CODE_ISSUED = "ACCESS_CODE_ISSUED"
USER_INVITED = "USER_INVITED"
USER_CREATED = "USER_CREATED"
USER_DEACTIVATED = "USER_DEACTIVATED"
USER_REACTIVATED = "USER_REACTIVATED"
USER_UPDATED = "USER_UPDATED"
CENTER_UPDATED = "CENTER_UPDATED"
MEDIA_DELETION_APPROVED = "MEDIA_DELETION_APPROVED"
MEDIA_DELETION_REJECTED = "MEDIA_DELETION_REJECTED"
CHILD_ENROLLED = "CHILD_ENROLLED"
CHILD_UPDATED = "CHILD_UPDATED"
CHILD_DEACTIVATED = "CHILD_DEACTIVATED"
CHILD_REACTIVATED = "CHILD_REACTIVATED"
CHILD_HEALTH_UPDATED = "CHILD_HEALTH_UPDATED"
PICKUP_ADDED = "PICKUP_ADDED"
PICKUP_REMOVED = "PICKUP_REMOVED"
CHILD_DEPARTED = "CHILD_DEPARTED"
CHILD_DEPARTURE_UNDONE = "CHILD_DEPARTURE_UNDONE"


def record(
    db: AsyncSession,
    action: str,
    *,
    actor_id: uuid.UUID | None = None,
    actor_email: str | None = None,
    entity_type: str | None = None,
    entity_id: Any = None,
    detail: dict | None = None,
    ip: str | None = None,
) -> None:
    """Agrega una fila a la bitácora. No hace commit: se guarda junto con lo que la causó.
    Si el hecho termina en un error (un login fallido), quien llama debe hacer commit antes de lanzarlo."""
    db.add(
        AuditLog(
            actor_id=actor_id,
            actor_email=actor_email,
            action=action,
            entity_type=entity_type,
            entity_id=None if entity_id is None else str(entity_id),
            detail=detail,
            ip=ip,
        )
    )


async def list_entries(
    db: AsyncSession, *, action: str | None, actor_id: uuid.UUID | None, limit: int, cursor: str | None
) -> Page[AuditOut]:
    """Lo más nuevo primero. El cursor es el id de la última fila de la página anterior."""
    query = (
        select(AuditLog, AppUser)
        .outerjoin(AppUser, AppUser.id == AuditLog.actor_id)
        .order_by(AuditLog.id.desc())
        .limit(limit + 1)
    )
    if action:
        query = query.where(AuditLog.action == action)
    if actor_id:
        query = query.where(AuditLog.actor_id == actor_id)
    before = parse_cursor(cursor)
    if before is not None:
        query = query.where(AuditLog.id < before)

    rows = (await db.execute(query)).all()
    has_more = len(rows) > limit
    items = [
        AuditOut(
            id=entry.id,
            created_at=entry.created_at,
            action=entry.action,
            actor_id=entry.actor_id,
            actor_name=user.full_name if user else None,
            actor_email=entry.actor_email or (user.email if user else None),
            entity_type=entry.entity_type,
            entity_id=entry.entity_id,
            detail=entry.detail,
            ip=entry.ip,
        )
        for entry, user in rows[:limit]
    ]
    return Page(items=items, next_cursor=str(items[-1].id) if has_more and items else None)
