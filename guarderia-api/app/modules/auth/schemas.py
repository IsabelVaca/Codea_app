import uuid
from datetime import datetime
from enum import StrEnum

from pydantic import BaseModel

from app.core.schemas import AccessCodeOut, ClassroomRef
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
    # solo al crear la invitación: el código se muestra una vez y en la base queda su hash.
    access_code: AccessCodeOut | None = None


class LoginIn(BaseModel):
    email: str
    password: str


class ActivateIn(BaseModel):
    """Primer acceso: el correo, el código que dio el administrador y la contraseña nueva."""

    email: str
    code: str
    password: str


class RefreshIn(BaseModel):
    refresh_token: str


class ChangePasswordIn(BaseModel):
    current_password: str
    new_password: str


class TokenOut(BaseModel):
    """Lo que regresan login, activar, refrescar y cambiar contraseña. `me` viene en todos menos en refrescar."""

    access_token: str
    token_type: str = "bearer"
    expires_in: int
    refresh_token: str
    refresh_expires_in: int
    me: MeOut | None = None


class SessionsOut(BaseModel):
    revoked: int
