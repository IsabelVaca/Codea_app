from datetime import UTC, date, datetime
from zoneinfo import ZoneInfo


def now() -> datetime:
    """La hora del servidor en UTC. Las pruebas la reemplazan para fijar el reloj."""
    return datetime.now(UTC)


def local_now(tz: str) -> datetime:
    """La hora en la zona del centro: el checador y "hoy" se calculan con ella."""
    return now().astimezone(ZoneInfo(tz))


def local_today(tz: str) -> date:
    return local_now(tz).date()
