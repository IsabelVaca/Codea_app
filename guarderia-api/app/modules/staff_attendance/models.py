import uuid
from datetime import date, datetime
from enum import StrEnum

from sqlalchemy import Boolean, DateTime, ForeignKey, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, str_enum


class CheckInStatus(StrEnum):
    """Lo que se guarda de una checada: a tiempo o tarde según el turno de la docente."""

    ON_TIME = "ON_TIME"
    LATE = "LATE"


class StaffCheckIn(IdMixin, Base):
    """La entrada de una docente (CheckInRecord en la app). Una por docente y día."""

    __tablename__ = "staff_check_in"
    __table_args__ = (UniqueConstraint("teacher_id", "date"),)

    teacher_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("teacher.user_id"))
    date: Mapped[date]
    checked_in_at: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    status: Mapped[CheckInStatus] = mapped_column(str_enum(CheckInStatus))
    minutes_late: Mapped[int] = mapped_column(default=0)
    late_reason: Mapped[str | None] = mapped_column(Text)
    # la salida; vacía hasta que la docente la registra o el cierre automático de las 9 pm la pone.
    checked_out_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    # true cuando la salida la puso el cierre automático y no la docente.
    auto_closed: Mapped[bool] = mapped_column(Boolean, default=False, server_default="false")


class CoordinationReason(StrEnum):
    LATE = "LATE"


class CoordinationNotice(IdMixin, Base):
    """El aviso a coordinación de que la docente llegará tarde."""

    __tablename__ = "coordination_notice"

    teacher_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("teacher.user_id"))
    date: Mapped[date]
    reason: Mapped[CoordinationReason] = mapped_column(str_enum(CoordinationReason))
    message: Mapped[str | None] = mapped_column(Text)
