from fastapi import APIRouter, Depends, FastAPI
from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncSession

from .core import errors
from .core.db import get_db
from .core.errors import ApiError

app = FastAPI(title="API Guardería")
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


app.include_router(api)