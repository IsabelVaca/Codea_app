import datetime as dt
from datetime import date
from enum import StrEnum

from pydantic import AwareDatetime, BaseModel

from .models import AttendanceStatus


class AttendanceIn(BaseModel):
    status: AttendanceStatus
    # sin fecha es hoy.
    date: dt.date | None = None
    # cuándo llegó el niño, con zona horaria. Sin él, la hora del servidor. La app lo manda cuando la marca se hizo
    # sin conexión y llega después; si viene en el futuro se usa la hora del servidor.
    arrived_at: AwareDatetime | None = None


class AttendanceOut(BaseModel):
    child_id: int
    date: date
    status: AttendanceStatus
    # la hora de entrada; None si el niño está ausente.
    arrived_at: dt.datetime | None = None


class DayAttendanceStatus(StrEnum):
    """DayAttendanceStatus de la pantalla Calendario."""

    FULL_DAY = "FULL_DAY"
    WITH_NOTE = "WITH_NOTE"
    ABSENT = "ABSENT"
    NO_RECORD = "NO_RECORD"


class CalendarDay(BaseModel):
    day: int
    date: date
    status: DayAttendanceStatus


class CalendarSummary(BaseModel):
    full_days: int
    with_note: int
    absences: int


class CalendarOut(BaseModel):
    child_id: int
    month: str
    days: list[CalendarDay]
    summary: CalendarSummary
