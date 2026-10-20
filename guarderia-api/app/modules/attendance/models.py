import uuid
from datetime import date, datetime
from enum import StrEnum

from sqlalchemy import DateTime, ForeignKey, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, str_enum


class AttendanceStatus(StrEnum):
    """AttendanceStatus de la app. Sin registro, el niño cuenta como ausente: la docente pasa lista en Mi día."""

    PRESENT = "PRESENT"
    ABSENT = "ABSENT"


class Attendance(IdMixin, Base):
    __tablename__ = "attendance"
    __table_args__ = (UniqueConstraint("child_id", "date"),)

    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"))
    date: Mapped[date]
    status: Mapped[AttendanceStatus] = mapped_column(str_enum(AttendanceStatus))
    marked_by: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))
    # la hora de entrada del niño (RF-03): cuándo se le pasó lista como presente. Vacía si está ausente, o en
    # las marcas anteriores a que existiera la columna.
    arrived_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
