import asyncio
import logging
from contextlib import asynccontextmanager

from fastapi import APIRouter, Depends, FastAPI
from fastapi.concurrency import run_in_threadpool
from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncSession

from .core import errors
from .core.config import settings
from .core.db import get_db
from .core.errors import ApiError
from .core.storage import get_storage
from .modules.admins.router import router as admins
from .modules.attendance.router import router as attendance
from .modules.auth.router import router as auth
from .modules.children.router import router as children
from .modules.classrooms.router import router as classrooms
from .modules.documents.router import router as documents
from .modules.media.router import router as media
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
    if settings.auth_mode == "firebase":
        import firebase_admin
        from firebase_admin import credentials

        credential = credentials.Certificate(settings.firebase_credentials) if settings.firebase_credentials else None
        firebase_admin.initialize_app(credential, {"projectId": settings.firebase_project_id})
    bucket = asyncio.create_task(_ensure_bucket()) if settings.env == "dev" else None
    yield
    if bucket is not None:
        bucket.cancel()


app = FastAPI(title="API Guardería", lifespan=lifespan)
errors.install(app)

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
    messaging,
):
    api.include_router(router)

app.include_router(api)
