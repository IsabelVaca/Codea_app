from datetime import date, datetime
from enum import StrEnum

from pydantic import BaseModel

from app.core.schemas import Schema


class DocumentStatus(StrEnum):
    """DocumentStatus de la app: vigente o faltante."""

    CURRENT = "CURRENT"
    MISSING = "MISSING"


class DocumentOut(BaseModel):
    """Un documento pedido (RequestedDocument en la app)."""

    id: int
    title: str
    due_date: date | None
    status: DocumentStatus
    uploaded_at: datetime | None
    # URL firmada de 10 minutos para verlo, si ya se subió.
    url: str | None


class DocumentUpload(BaseModel):
    # el archivo que ya se subió con /media/uploads (kind = DOCUMENT) y /media/{id}/complete.
    media_id: int


class PickupIn(BaseModel):
    full_name: str
    relationship: str | None = None
    phone: str | None = None


class PickupOut(Schema):
    """Una persona autorizada para recoger al niño (AuthorizedPerson en la app)."""

    id: int
    full_name: str
    relationship: str | None
    phone: str | None
    created_at: datetime
