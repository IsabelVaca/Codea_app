import datetime as dt
import uuid
from datetime import date, datetime
from enum import StrEnum

from pydantic import BaseModel, Field

from app.core.schemas import ClassroomRef
from app.modules.attendance.models import AttendanceStatus
from app.modules.messaging.schemas import NoteOut

from .models import MealAmount, Mood, ReportMethod, ReportType


class ReportIn(BaseModel):
    routine_moment_id: int
    type: ReportType
    method: ReportMethod
    description: str
    child_ids: list[int]
    # la foto que ya se subió con /media/uploads y /media/{id}/complete.
    media_id: int | None = None


class ReportOut(BaseModel):
    """Un reporte de un momento (Report en la app)."""

    id: int
    routine_moment_id: int
    type: ReportType
    method: ReportMethod
    description: str
    child_ids: list[int]
    media_id: int | None
    has_photo: bool
    created_by: uuid.UUID
    created_at: datetime


class DailyEntryIn(BaseModel):
    """El resumen del día. Solo cambia lo que se manda; sin fecha es hoy."""

    date: dt.date | None = None
    mood: Mood | None = None
    meals: MealAmount | None = None
    diaper_changes: int | None = Field(None, ge=0)
    nap_minutes: int | None = Field(None, ge=0)
    general_note: str | None = None


class DailyEntryOut(BaseModel):
    child_id: int
    date: date
    mood: Mood | None
    meals: MealAmount | None
    diaper_changes: int | None
    nap_minutes: int | None
    general_note: str | None


class TimelineType(StrEnum):
    """TimelineEventType de la pantalla Hoy."""

    CHECK_IN = "CHECK_IN"
    MEAL = "MEAL"
    ACTIVITY = "ACTIVITY"
    NAP = "NAP"


class TimelineEvent(BaseModel):
    id: str
    type: TimelineType
    title: str
    time: datetime
    description: str
    report_types: list[ReportType]


class DayChild(BaseModel):
    id: int
    name: str
    surname: str
    full_name: str
    classroom: ClassroomRef | None
    center_name: str


class DayOut(BaseModel):
    """La pantalla Hoy del padre: resumen del día y línea de tiempo."""

    child: DayChild
    date: date
    attendance: AttendanceStatus
    summary: DailyEntryOut | None
    timeline: list[TimelineEvent]
    notes: list[NoteOut]
