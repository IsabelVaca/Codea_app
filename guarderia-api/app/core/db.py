from collections.abc import AsyncIterator
from datetime import datetime
from enum import Enum
from typing import Annotated

from fastapi import Depends
from sqlalchemy import DateTime, MetaData, func
from sqlalchemy import Enum as SAEnum
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column

from .config import settings

engine = create_async_engine(settings.database_url, pool_pre_ping=True)
SessionLocal = async_sessionmaker(engine, expire_on_commit=False)


class Base(DeclarativeBase):
    """Todas las tablas heredan de aquí. Alembic lee Base.metadata para saber qué tablas existen."""

    # los mismos nombres que pone Postgres, para que las migraciones puedan borrar cada constraint.
    metadata = MetaData(
        naming_convention={
            "pk": "%(table_name)s_pkey",
            "fk": "%(table_name)s_%(column_0_name)s_fkey",
            "uq": "%(table_name)s_%(column_0_N_name)s_key",
            "ix": "ix_%(column_0_N_label)s",
            "ck": "%(table_name)s_%(constraint_name)s_check",
        }
    )


class TimestampMixin:
    """Agrega created_at y updated_at. Lo usan las tablas cuya llave NO es un id int."""

    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), onupdate=func.now()
    )


class IdMixin(TimestampMixin):
    """Agrega id (int) + created_at + updated_at. Lo usan las tablas con PK id int."""

    id: Mapped[int] = mapped_column(primary_key=True)


def str_enum(enum_cls: type[Enum]) -> SAEnum:
    """Guarda un enum como texto (VARCHAR) con el mismo valor que en la app: "PRESENT", "LATE"…"""
    return SAEnum(
        enum_cls,
        native_enum=False,
        length=20,
        values_callable=lambda members: [m.value for m in members],
    )


async def get_db() -> AsyncIterator[AsyncSession]:
    """Una sesión de base de datos por petición. FastAPI la abre al entrar y la cierra al salir."""
    async with SessionLocal() as session:
        yield session


Db = Annotated[AsyncSession, Depends(get_db)]
