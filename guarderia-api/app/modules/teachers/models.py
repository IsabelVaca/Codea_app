import uuid
from datetime import time

from sqlalchemy import ForeignKey, Index, String, text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, TimestampMixin


class Shift(IdMixin, Base):
    """Un turno del centro (Matutino, Vespertino). El checador calcula el retardo con start_time."""

    __tablename__ = "shift"

    daycare_center_id: Mapped[int] = mapped_column(ForeignKey("daycare_center.id"))
    name: Mapped[str] = mapped_column(String(40))
    start_time: Mapped[time]
    end_time: Mapped[time]


class Teacher(TimestampMixin, Base):
    __tablename__ = "teacher"
    # una sola asistente principal por sala.
    __table_args__ = (
        Index(
            "uq_teacher_primary_per_classroom",
            "classroom_id",
            unique=True,
            postgresql_where=text("is_primary"),
        ),
    )

    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"), primary_key=True)
    # vacía cuando la docente todavía no tiene sala.
    classroom_id: Mapped[int | None] = mapped_column(ForeignKey("classroom.id"))
    is_primary: Mapped[bool] = mapped_column(default=False, server_default=text("false"))
    shift_id: Mapped[int | None] = mapped_column(ForeignKey("shift.id"))
