import uuid

from pydantic import BaseModel

from app.core.schemas import AccessCodeOut

from .models import Relationship


class ParentChild(BaseModel):
    id: int
    full_name: str
    relationship: Relationship


class ParentOut(BaseModel):
    """Un padre o tutor (Padre en la app). Los hijos van como lista, no como texto."""

    id: uuid.UUID
    name: str
    surname: str
    full_name: str
    phone: str | None
    email: str | None
    relationship: Relationship | None
    child_ids: list[int]
    children: list[ParentChild]
    # tiene acceso a la app (no está dado de baja).
    has_access: bool
    # ya entró con su invitación.
    has_account: bool
    # solo al dar de alta o reenviar: el código para activar la cuenta (se muestra una vez).
    access_code: AccessCodeOut | None = None


class ParentCreate(BaseModel):
    name: str
    surname: str = ""
    phone: str
    email: str
    relationship: Relationship
    child_ids: list[int]
