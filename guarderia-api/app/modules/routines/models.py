import uuid
from datetime import date, datetime
from enum import StrEnum

from sqlalchemy import DateTime, ForeignKey, Index, String, Text, UniqueConstraint, text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, str_enum


class MomentCategory(StrEnum):
    """Qué tipo de momento es. Con esto se arma la línea de tiempo y la categoría de las fotos."""

    ARRIVAL = "ARRIVAL"
    MEAL = "MEAL"
    ACTIVITY = "ACTIVITY"
    NAP = "NAP"
    HYGIENE = "HYGIENE"
    PICKUP = "PICKUP"


class MomentStatus(StrEnum):
    """SubprocessStatus de la app."""

    DONE = "DONE"
    ACTIVE = "ACTIVE"
    PENDING = "PENDING"


class RoutineTemplate(IdMixin, Base):
    """Una rutina tipo, por ejemplo "Día completo · Preescolar"."""

    __tablename__ = "routine_template"

    name: Mapped[str] = mapped_column(String(80))
    is_default: Mapped[bool] = mapped_column(default=False, server_default=text("false"))


class RoutineTemplateMoment(IdMixin, Base):
    __tablename__ = "routine_template_moment"
    __table_args__ = (UniqueConstraint("template_id", "number"),)

    template_id: Mapped[int] = mapped_column(ForeignKey("routine_template.id"))
    number: Mapped[int]
    name: Mapped[str] = mapped_column(String(80))
    category: Mapped[MomentCategory] = mapped_column(str_enum(MomentCategory))


class RoutineMoment(IdMixin, Base):
    """Un momento de la rutina de una sala en un día (Subprocess en la app)."""

    __tablename__ = "routine_moment"
    __table_args__ = (
        UniqueConstraint("classroom_id", "date", "number"),
        # un solo momento en curso por sala y día.
        Index(
            "uq_routine_moment_one_active",
            "classroom_id",
            "date",
            unique=True,
            postgresql_where=text("status = 'ACTIVE'"),
        ),
    )

    classroom_id: Mapped[int] = mapped_column(ForeignKey("classroom.id"))
    template_id: Mapped[int | None] = mapped_column(ForeignKey("routine_template.id"))
    date: Mapped[date]
    number: Mapped[int]
    name: Mapped[str] = mapped_column(String(80))
    category: Mapped[MomentCategory] = mapped_column(str_enum(MomentCategory))
    status: Mapped[MomentStatus] = mapped_column(str_enum(MomentStatus))
    started_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    confirmed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    confirmed_by: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("app_user.id"))
    description: Mapped[str | None] = mapped_column(Text)
