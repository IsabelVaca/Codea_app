from datetime import datetime, time

from sqlalchemy import ARRAY, DateTime, Float, ForeignKey, Integer, String, text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin


class DaycareCenter(IdMixin, Base):
    __tablename__ = "daycare_center"

    name: Mapped[str] = mapped_column(String(50))
    # dónde está el centro y qué tan lejos puede checar la docente (CheckInAttempt en la app).
    latitude: Mapped[float | None] = mapped_column(Float)
    longitude: Mapped[float | None] = mapped_column(Float)
    check_in_radius_m: Mapped[int] = mapped_column(default=60, server_default="60")
    timezone: Mapped[str] = mapped_column(
        String(64), default="America/Monterrey", server_default="America/Monterrey"
    )
    # el horario de todo el centro (hora local del centro): a qué hora empieza el día, a qué hora termina
    # y a qué hora se cierran solas las entradas que siguen abiertas. work_days: 1 = lunes … 7 = domingo.
    entry_time: Mapped[time] = mapped_column(default=time(8), server_default="08:00")
    exit_time: Mapped[time] = mapped_column(default=time(16), server_default="16:00")
    auto_close_time: Mapped[time] = mapped_column(default=time(21), server_default="21:00")
    work_days: Mapped[list[int]] = mapped_column(
        ARRAY(Integer), default=[1, 2, 3, 4, 5], server_default=text("'{1,2,3,4,5}'")
    )


class Classroom(IdMixin, Base):
    __tablename__ = "classroom"

    daycare_center_id: Mapped[int] = mapped_column(ForeignKey("daycare_center.id"))
    name: Mapped[str] = mapped_column(String(40))
    school_grade: Mapped[str | None] = mapped_column(String(40))
    # el cupo máximo (Sala.cupoMaximo en la app).
    capacity: Mapped[int] = mapped_column(default=0, server_default="0")
    # la rutina que sigue la sala; vacía = la plantilla por defecto.
    routine_template_id: Mapped[int | None] = mapped_column(ForeignKey("routine_template.id"))
    deleted_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
