import uuid
from datetime import datetime
from enum import StrEnum

from pydantic import BaseModel

from app.core.schemas import ClassroomRef
from app.modules.auth.models import Role
from app.modules.classrooms.schemas import CenterOut
from app.modules.parents.models import Relationship
from app.modules.teachers.schemas import ShiftOut


class MeAdmin(BaseModel):
    center: CenterOut


class MeTeacher(BaseModel):
    classroom: ClassroomRef | None
    is_primary: bool
    shift: ShiftOut | None
    # coordenadas y radio para el checador.
    center: CenterOut


class MeChild(BaseModel):
    id: int
    name: str
    surname: str
    full_name: str
    classroom: ClassroomRef | None
    relationship: Relationship


class MeParent(BaseModel):
    children: list[MeChild]


class MeOut(BaseModel):
    """El perfil de quien inició sesión. Con roles, la app decide qué tabs mostrar."""

    id: uuid.UUID
    name: str
    surname: str
    full_name: str
    email: str | None
    phone: str | None
    roles: list[Role]
    admin: MeAdmin | None
    teacher: MeTeacher | None
    parent: MeParent | None


class InvitationStatus(StrEnum):
    PENDING = "PENDING"
    ACCEPTED = "ACCEPTED"
    EXPIRED = "EXPIRED"


class InvitationCreate(BaseModel):
    """Invita a una persona nueva con el rol que se le dé."""

    name: str
    surname: str = ""
    phone: str
    email: str
    role: Role


class InvitationOut(BaseModel):
    id: int
    user_id: uuid.UUID
    name: str
    surname: str
    full_name: str
    email: str
    role: Role
    status: InvitationStatus
    expires_at: datetime
    accepted_at: datetime | None
    created_at: datetime
