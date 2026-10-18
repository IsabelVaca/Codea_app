from datetime import date, datetime

from pydantic import BaseModel, Field

from app.core.schemas import Schema

from .models import CheckInStatus, CoordinationReason


class CheckInIn(BaseModel):
    """Solo la ubicación del teléfono (sirve igual para la entrada y la salida). La hora, la distancia y el estado los calcula la API."""

    latitude: float = Field(ge=-90, le=90)
    longitude: float = Field(ge=-180, le=180)
    late_reason: str | None = None


class CheckInOut(BaseModel):
    """La entrada registrada (CheckInRecord en la app)."""

    id: int
    date: date
    checked_in_at: datetime
    status: CheckInStatus
    minutes_late: int
    late_reason: str | None
    distance_m: int
    allowed_radius_m: int
    place_name: str
    # null hasta que la docente registra su salida (POST /staff-attendance/check-out).
    checked_out_at: datetime | None = None


class NotifyIn(BaseModel):
    reason: CoordinationReason
    message: str | None = None


class NotifyOut(Schema):
    id: int
    date: date
    reason: CoordinationReason
    message: str | None
    created_at: datetime
