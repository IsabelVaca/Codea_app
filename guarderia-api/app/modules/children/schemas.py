import uuid
from datetime import date

from pydantic import BaseModel

from app.core.schemas import AccessCodeOut, ClassroomRef
from app.modules.parents.models import Relationship

from .models import ChildStatus


class GuardianIn(BaseModel):
    name: str
    surname: str = ""
    relationship: Relationship
    phone: str
    email: str


class ChildCreate(BaseModel):
    name: str
    surname: str = ""
    birth_date: date
    allergies: str | None = None
    classroom_id: int | None = None
    guardian: GuardianIn | None = None


class ChildUpdate(BaseModel):
    """classroom_id = null deja al niño sin sala ("Sacar de la sala")."""

    classroom_id: int | None


class GuardianOut(BaseModel):
    id: uuid.UUID
    name: str
    surname: str
    full_name: str
    relationship: Relationship
    phone: str | None
    email: str | None
    is_primary: bool
    has_account: bool


class ChildOut(BaseModel):
    """Un infante (Infante en la app). La edad sale de birth_date."""

    id: int
    name: str
    surname: str
    full_name: str
    birth_date: date | None
    age: int | None
    allergies: str | None
    classroom: ClassroomRef | None
    status: ChildStatus
    active: bool
    # el tutor principal: el que se registró al inscribirlo.
    guardian: GuardianOut | None


class ChildDetail(ChildOut):
    guardians: list[GuardianOut]
    # solo al inscribir con tutor: el código para que el tutor active su cuenta (se muestra una vez).
    access_code: AccessCodeOut | None = None
