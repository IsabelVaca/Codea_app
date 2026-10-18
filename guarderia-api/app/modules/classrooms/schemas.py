import uuid
from datetime import date

from pydantic import BaseModel

from app.core.schemas import ClassroomRef, Schema
from app.modules.attendance.models import AttendanceStatus
from app.modules.children.models import ChildStatus


class CenterOut(Schema):
    id: int
    name: str
    latitude: float | None
    longitude: float | None
    check_in_radius_m: int
    timezone: str


class StaffMember(BaseModel):
    id: uuid.UUID
    name: str
    surname: str
    full_name: str
    is_primary: bool


class ClassroomOut(BaseModel):
    """Una sala con su ocupación ya calculada (Sala en la app)."""

    id: int
    name: str
    school_grade: str | None
    capacity: int
    enrolled: int
    available: int
    primary_teacher: StaffMember | None


class ClassroomChild(BaseModel):
    id: int
    name: str
    surname: str
    full_name: str
    birth_date: date | None
    age: int | None
    status: ChildStatus


class ClassroomDetail(ClassroomOut):
    children: list[ClassroomChild]
    teachers: list[StaffMember]


class ClassroomCreate(BaseModel):
    name: str
    capacity: int
    school_grade: str | None = None
    # la asistente principal; si no se manda, la sala queda "Sin asignar".
    teacher_id: uuid.UUID | None = None


class ClassroomUpdate(BaseModel):
    name: str | None = None
    capacity: int | None = None
    school_grade: str | None = None


class StaffUpdate(BaseModel):
    teacher_ids: list[uuid.UUID]
    primary_teacher_id: uuid.UUID | None = None


class RoomChild(BaseModel):
    """Un niño en Mi sala (Child en la app): su asistencia del día y si tiene chat sin leer."""

    id: int
    name: str
    surname: str
    full_name: str
    attendance: AttendanceStatus
    conversation_id: int | None
    has_unread_chat: bool
    unread_count: int
    # notas que el papá le dejó a la asistente hoy (POST /children/{id}/notes): marcan al niño en Mi sala.
    notes_today: int = 0
    has_note_today: bool = False
