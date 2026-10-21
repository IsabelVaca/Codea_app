import datetime as dt
import uuid
from datetime import date, datetime
from enum import StrEnum

from pydantic import BaseModel, Field

from app.core.schemas import ClassroomRef
from app.modules.attendance.models import AttendanceStatus
from app.modules.menus.schemas import MenuOut
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
    # URL firmada de 10 minutos para ver la foto completa; null si no tiene foto.
    photo_url: str | None = None
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
    # la entrada del niño: la hora en que la docente le pasó lista como presente.
    ARRIVAL = "ARRIVAL"
    MEAL = "MEAL"
    ACTIVITY = "ACTIVITY"
    NAP = "NAP"
    # la salida del niño (pase de salida): a qué hora se fue y con quién.
    DEPARTURE = "DEPARTURE"


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
    # lo que se come ese día en el centro (HN3); None si todavía no lo capturan.
    menu: MenuOut | None = None


class WeekDayStatus(StrEnum):
    """Cómo estuvo el niño ese día de la semana."""

    ATTENDED = "ATTENDED"
    ABSENT = "ABSENT"
    # el día no ha llegado, o ese día no hubo rutina ni registro suyo.
    NO_RECORD = "NO_RECORD"


class WeekDayOut(BaseModel):
    date: date
    status: WeekDayStatus
    mood: Mood | None
    meals: MealAmount | None
    diaper_changes: int | None
    nap_minutes: int | None
    reports: int
    family_notes: int


class WeeklySummaryOut(BaseModel):
    """El resumen semanal (RF-12): de lunes a viernes, lo que se vio cada día y los totales."""

    child_id: int
    child_name: str
    week_start: date
    week_end: date
    days_attended: int
    days_absent: int
    # cuántos días hubo cada ánimo y cada cantidad de comida (solo los que aparecieron).
    moods: dict[Mood, int]
    meals: dict[MealAmount, int]
    nap_minutes_average: int | None
    diaper_changes_total: int
    reports_by_type: dict[ReportType, int]
    # lo que la asistente escribió en reportes de logros, para que el papá lo lea de corrido.
    achievements: list[str]
    family_notes: int
    days: list[WeekDayOut]

