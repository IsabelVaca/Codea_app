import uuid
from datetime import date, datetime
from enum import StrEnum

from sqlalchemy import DateTime, Float, ForeignKey, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, str_enum


class CheckInStatus(StrEnum):
    """Lo que se guarda de una checada. OUT_OF_RANGE nunca se guarda: fuera del radio no se registra."""

    ON_TIME = "ON_TIME"
    LATE = "LATE"


class StaffCheckIn(IdMixin, Base):
    """La entrada de una docente (CheckInRecord en la app). Una por docente y día."""

    __tablename__ = "staff_check_in"
    __table_args__ = (UniqueConstraint("teacher_id", "date"),)

    teacher_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("teacher.user_id"))
    date: Mapped[date]
    checked_in_at: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    # solo la ubicación del momento de checar, nunca un rastreo.
    latitude: Mapped[float] = mapped_column(Float)
    longitude: Mapped[float] = mapped_column(Float)
    distance_m: Mapped[int]
    status: Mapped[CheckInStatus] = mapped_column(str_enum(CheckInStatus))
    minutes_late: Mapped[int] = mapped_column(default=0)
    late_reason: Mapped[str | None] = mapped_column(Text)
    # la salida, con la misma regla de ubicación que la entrada; vacía hasta que la docente la registra.
    checked_out_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    out_latitude: Mapped[float | None] = mapped_column(Float)
    out_longitude: Mapped[float | None] = mapped_column(Float)
    out_distance_m: Mapped[int | None]


class CoordinationReason(StrEnum):
    LATE = "LATE"
    OUT_OF_RANGE = "OUT_OF_RANGE"


class CoordinationNotice(IdMixin, Base):
    """El aviso a coordinación de que la docente llegará tarde o está fuera del radio."""

    __tablename__ = "coordination_notice"

    teacher_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("teacher.user_id"))
    date: Mapped[date]
    reason: Mapped[CoordinationReason] = mapped_column(str_enum(CoordinationReason))
    message: Mapped[str | None] = mapped_column(Text)
