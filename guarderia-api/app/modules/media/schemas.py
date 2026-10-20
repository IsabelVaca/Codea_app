from datetime import datetime
from enum import StrEnum

from pydantic import BaseModel, Field

from app.core.schemas import Schema

from .models import MediaKind, MediaStatus


class UploadIn(BaseModel):
    content_type: str
    size_bytes: int = Field(gt=0)
    kind: MediaKind = MediaKind.PHOTO


class UploadOut(BaseModel):
    """El permiso para subir: la app manda un POST multipart a url con fields + el archivo (campo "file")."""

    media_id: int
    url: str
    fields: dict[str, str]
    expires_in: int
    max_bytes: int


class MediaOut(Schema):
    id: int
    kind: MediaKind
    status: MediaStatus
    content_type: str
    size_bytes: int
    created_at: datetime


class PhotoCategory(StrEnum):
    """Las categorías de la pantalla Fotos (Actividades, Comidas, Siesta, Incidentes)."""

    ACTIVITIES = "ACTIVITIES"
    MEALS = "MEALS"
    NAP = "NAP"
    INCIDENTS = "INCIDENTS"


class FeedItem(BaseModel):
    id: int
    # URL firmada de 10 minutos; sirve también para el botón "Descargar".
    url: str
    category: PhotoCategory
    taken_at: datetime
    teacher_name: str
    # la descripción del reporte de la foto.
    note: str | None
    child_ids: list[int]
