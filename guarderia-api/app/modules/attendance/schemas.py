import datetime as dt
from datetime import date
from enum import StrEnum

from pydantic import BaseModel

from .models import AttendanceStatus


class AttendanceIn(BaseModel):
    status: AttendanceStatus
    # sin fecha es hoy.
    date: dt.date | None = None


class AttendanceOut(BaseModel):
    child_id: int
    date: date
    status: AttendanceStatus


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
