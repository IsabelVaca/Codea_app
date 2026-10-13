import uuid
from datetime import datetime, time

from pydantic import BaseModel

from app.core.schemas import ClassroomRef, Schema


class ShiftOut(Schema):
    id: int
    name: str
    start_time: time
    end_time: time


class TeacherOut(BaseModel):
    """Una docente en la tab Docentes (Docente en la app)."""

    id: uuid.UUID
    name: str
    surname: str
    full_name: str
    phone: str | None
    email: str | None
    classroom: ClassroomRef | None
    is_primary: bool
    shift: ShiftOut | None
    # la hora en que checó hoy; la escribe el checador y el administrador solo la lee.
    checked_in_at: datetime | None
    active: bool
    # ya entró a la app con su invitación.
    has_account: bool
    children_count: int


class TeacherCreate(BaseModel):
    name: str
    surname: str = ""
    phone: str
    email: str
    classroom_id: int | None = None
    shift_id: int | None = None


class TeacherUpdate(BaseModel):
    """Solo cambia lo que se manda. classroom_id = null deja a la docente sin sala."""

    classroom_id: int | None = None
    shift_id: int | None = None
