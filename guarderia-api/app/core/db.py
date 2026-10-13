from collections.abc import AsyncIterator
from datetime import datetime

from sqlalchemy import DateTime, func
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column

from .config import settings

engine = create_async_engine(settings.database_url, pool_pre_ping=True)
SessionLocal = async_sessionmaker(engine, expire_on_commit=False)


class Base(DeclarativeBase):
    """Todas las tablas heredan de aquí. Alembic lee Base.metadata para saber qué tablas existen."""


class IdMixin:
    """Columnas que llevan todas las tablas: id, created_at y updated_at."""

    id: Mapped[int] = mapped_column(primary_key=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now()
    )


async def get_db() -> AsyncIterator[AsyncSession]:
    """Una sesión de base de datos por petición. FastAPI la abre al entrar y la cierra al salir."""
    async with SessionLocal() as session:
        yield session