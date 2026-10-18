"""Límite de intentos fallidos de entrada (rúbrica: rate limiting y bloqueo).

Dos reglas, las dos con ventana móvil de `auth_window_s` segundos:
- por correo: `auth_max_failures` fallos seguidos y ese correo queda bloqueado hasta que el más viejo salga de la ventana.
- por IP: `auth_max_failures_ip` fallos desde una misma dirección (frena el barrido de muchos correos).
Una entrada exitosa borra los fallos de ese correo. Se guarda en Postgres, así vale para varios procesos de la API.
Si la API va detrás de un proxy, hay que hacer que `request.client` traiga la IP real (ver README)."""

from datetime import timedelta

from sqlalchemy import delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock
from app.core.config import settings
from app.core.errors import ApiError

from .models import AuthAttempt

LOGIN = "LOGIN"
ACTIVATE = "ACTIVATE"


async def _unlock_in(db: AsyncSession, *conditions, limit: int) -> int | None:
    """Segundos que faltan para que se libere el bloqueo, o None si no hay bloqueo."""
    since = clock.now() - timedelta(seconds=settings.auth_window_s)
    times = (
        await db.scalars(
            select(AuthAttempt.attempted_at)
            .where(AuthAttempt.attempted_at > since, *conditions)
            .order_by(AuthAttempt.attempted_at.desc())
            .limit(limit)
        )
    ).all()
    if len(times) < limit:
        return None
    # el bloqueo se libera cuando el fallo número `limit` (contando desde el más nuevo) salga de la ventana.
    return max(1, int((times[-1] + timedelta(seconds=settings.auth_window_s) - clock.now()).total_seconds()))


async def check(db: AsyncSession, kind: str, email: str, ip: str | None) -> None:
    """Lanza 429 TOO_MANY_ATTEMPTS si ya se falló demasiado. Va ANTES de revisar la contraseña o el código."""
    waits = [
        await _unlock_in(db, AuthAttempt.kind == kind, AuthAttempt.email == email, limit=settings.auth_max_failures)
    ]
    if ip:
        waits.append(
            await _unlock_in(db, AuthAttempt.kind == kind, AuthAttempt.ip == ip, limit=settings.auth_max_failures_ip)
        )
    active = [wait for wait in waits if wait is not None]
    if active:
        raise ApiError(
            429,
            "TOO_MANY_ATTEMPTS",
            "Demasiados intentos; espera unos minutos y vuelve a intentar",
            retry_after_s=max(active),
        )


async def fail(db: AsyncSession, kind: str, email: str, ip: str | None) -> bool:
    """Anota un fallo (sin commit). Regresa True si con este fallo el correo quedó bloqueado."""
    now = clock.now()
    db.add(AuthAttempt(kind=kind, email=email, ip=ip, attempted_at=now))
    await db.flush()
    # los fallos de más de un día ya no sirven para nada.
    await db.execute(delete(AuthAttempt).where(AuthAttempt.attempted_at < now - timedelta(days=1)))
    since = now - timedelta(seconds=settings.auth_window_s)
    recent = await db.scalar(
        select(func.count())
        .select_from(AuthAttempt)
        .where(AuthAttempt.kind == kind, AuthAttempt.email == email, AuthAttempt.attempted_at > since)
    )
    return recent == settings.auth_max_failures


async def clear(db: AsyncSession, kind: str, email: str) -> None:
    """Entró bien: se borran los fallos de ese correo (sin commit)."""
    await db.execute(delete(AuthAttempt).where(AuthAttempt.kind == kind, AuthAttempt.email == email))
