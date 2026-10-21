import asyncio
import logging
from contextlib import asynccontextmanager

from fastapi import APIRouter, Depends, FastAPI, Request
from fastapi.concurrency import run_in_threadpool
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncSession

from .core import errors, rate_limit
from .core.config import settings
from .core.db import get_db
from .core.errors import ApiError
from .core.storage import get_storage
from .modules.admins.router import router as admins
from .modules.audit.router import router as audit
from .modules.attendance.router import router as attendance
from .modules.auth import invite_page
from .modules.auth.router import router as auth
from .modules.children.router import router as children
from .modules.classrooms.router import router as classrooms
from .modules.departures.router import router as departures
from .modules.documents.router import router as documents
from .modules.media.router import router as media
from .modules.menus.router import router as menus
from .modules.messaging.router import router as messaging
from .modules.parents.router import router as parents
from .modules.reports.router import router as reports
from .modules.routines.router import router as routines
from .modules.staff_attendance.router import router as staff_attendance
from .modules.teachers.router import router as teachers

log = logging.getLogger(__name__)


async def _ensure_bucket() -> None:
    """En desarrollo crea el bucket. El almacén puede tardar en prender, así que reintenta."""
    for _ in range(30):
        try:
            await run_in_threadpool(get_storage().ensure_bucket)
            return
        except Exception:
            await asyncio.sleep(1)
    log.warning("no se pudo crear el bucket; ¿está prendido el almacén (servicio storage)?")


@asynccontextmanager
async def lifespan(_: FastAPI):
    bucket = asyncio.create_task(_ensure_bucket()) if settings.env == "dev" else None
    yield
    if bucket is not None:
        bucket.cancel()


app = FastAPI(title="API Guardería", lifespan=lifespan)
errors.install(app)

_RATE_LIMIT_EXEMPT = ("/api/v1/health/live", "/api/v1/health/ready")


@app.middleware("http")
async def rate_limiting(request: Request, call_next):
    """429 cuando una misma IP pasa el límite genérico (rúbrica: rate limiting / control de
    concurrencia). Va antes que la ruta: nada de tocar la base si ya se excedió."""
    path = request.url.path
    if path.startswith("/api/") and path not in _RATE_LIMIT_EXEMPT:
        wait = rate_limit.check(request.client.host if request.client else None)
        if wait is not None:
            return JSONResponse(
                {
                    "type": "about:blank",
                    "title": "Demasiadas peticiones; espera unos segundos y vuelve a intentar",
                    "status": 429,
                    "code": "TOO_MANY_REQUESTS",
                    "retry_after_s": wait,
                },
                status_code=429,
                media_type=errors.PROBLEM_JSON,
                headers={"Retry-After": str(wait)},
            )
    return await call_next(request)


# la app Android no usa CORS; solo se abre para los orígenes web que se listen en CORS_ORIGINS (por omisión, ninguno).
_cors_origins = [origin.strip() for origin in settings.cors_origins.split(",") if origin.strip()]
if _cors_origins:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=_cors_origins,
        allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE"],
        allow_headers=["Authorization", "Content-Type"],
        max_age=600,
    )

# Swagger necesita cargar scripts de otro sitio, así que la política de contenido estricta no va en su página.
_DOC_PATHS = ("/docs", "/redoc", "/openapi.json")


@app.middleware("http")
async def security_headers(request: Request, call_next):
    """Cabeceras de seguridad en todas las respuestas, también en los errores."""
    response = await call_next(request)
    headers = response.headers
    headers["X-Content-Type-Options"] = "nosniff"
    headers["X-Frame-Options"] = "DENY"
    headers["Referrer-Policy"] = "no-referrer"
    headers["Permissions-Policy"] = "camera=(), microphone=(), geolocation=()"
    path = request.url.path
    if path == "/invitar":
        # la página del enlace de invitación solo lleva estilos propios en la misma página; nada de scripts.
        headers["Content-Security-Policy"] = (
            "default-src 'none'; style-src 'unsafe-inline'; base-uri 'none'; form-action 'none'; frame-ancestors 'none'"
        )
    elif not path.startswith(_DOC_PATHS):
        headers["Content-Security-Policy"] = "default-src 'none'; frame-ancestors 'none'"
    if path.startswith("/api/"):
        # son datos de menores y tokens: nada de guardarlos en cachés del camino.
        headers["Cache-Control"] = "no-store"
    if settings.env != "dev":
        # con HTTPS (producción) el navegador ya no vuelve a usar HTTP con este servidor.
        headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
    return response

# Todas las rutas viven bajo /api/v1. Los routers de cada módulo se agregan aquí.
api = APIRouter(prefix="/api/v1")


@api.get("/health/live", tags=["sistema"])
async def live() -> dict:
    """¿El servidor está prendido?"""
    return {"status": "ok"}


@api.get("/health/ready", tags=["sistema"])
async def ready(db: AsyncSession = Depends(get_db)) -> dict:
    """¿El servidor puede hablar con la base de datos?"""
    try:
        await db.execute(text("SELECT 1"))
    except Exception:
        raise ApiError(503, "DB_UNAVAILABLE", "La base de datos no responde")
    return {"status": "ok", "database": "ok"}


for router in (
    auth,
    audit,
    admins,
    classrooms,
    teachers,
    children,
    parents,
    staff_attendance,
    attendance,
    routines,
    reports,
    media,
    documents,
    departures,
    menus,
    messaging,
):
    api.include_router(router)

app.include_router(api)
# fuera de /api/v1: son direcciones que abre una persona o Android, no la app con su token.
app.include_router(invite_page.router)
