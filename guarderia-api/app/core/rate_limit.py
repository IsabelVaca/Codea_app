"""Límite genérico de peticiones por IP para toda la API (rúbrica: rate limiting / control de
concurrencia en endpoints críticos). El límite fino de intentos fallidos de login y activación
vive en auth/throttle.py; este es el piso general contra abuso y denegación de servicio en el
resto de endpoints (incluido /auth/refresh, que throttle.py no cubre).

Ventana deslizante en memoria: alcanza para un solo proceso (la API corre en una sola Mac según
docs/ENTREGA-3.md). Si algún día hay varios procesos, habría que pasar esto a Postgres como
throttle.py."""

import time
from collections import defaultdict, deque

from app.core.config import settings

_hits: dict[str, deque[float]] = defaultdict(deque)


def check(ip: str | None) -> int | None:
    """Cuenta esta petición para `ip`. Regresa los segundos que faltan para que se libere el
    límite, o None si todavía no se excede."""
    if ip is None:
        return None
    now = time.monotonic()
    window = _hits[ip]
    since = now - settings.rate_limit_window_s
    while window and window[0] <= since:
        window.popleft()
    if len(window) >= settings.rate_limit_max_requests:
        return max(1, int(window[0] + settings.rate_limit_window_s - now) + 1)
    window.append(now)
    return None


def reset() -> None:
    """Solo para las pruebas: limpia el estado entre una prueba y otra."""
    _hits.clear()
