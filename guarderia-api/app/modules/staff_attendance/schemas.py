from datetime import date, datetime

from pydantic import BaseModel

from app.core.schemas import Schema

from .models import CheckInStatus, CoordinationReason


class CheckInIn(BaseModel):
    """La entrada no manda ubicación: la hora y el estado los calcula la API. Solo el motivo del retardo, si hubo."""

    late_reason: str | None = None


class CheckInOut(BaseModel):
    """La entrada registrada (CheckInRecord en la app)."""

    id: int
    date: date
    checked_in_at: datetime
    status: CheckInStatus
    minutes_late: int
    late_reason: str | None
    place_name: str
    # null hasta que la docente registra su salida (POST /staff-attendance/check-out) o llegan las 9 pm.
    checked_out_at: datetime | None = None
    # true si la salida la puso el cierre automático de las 9 pm.
    auto_closed: bool = False


class NotifyIn(BaseModel):
    reason: CoordinationReason
    message: str | None = None


class NotifyOut(Schema):
    id: int
    date: date
    reason: CoordinationReason
    message: str | None
    created_at: datetime
