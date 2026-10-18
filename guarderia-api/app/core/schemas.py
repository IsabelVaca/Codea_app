from datetime import datetime

from pydantic import BaseModel, ConfigDict


class Schema(BaseModel):
    """Base de las respuestas: se pueden armar directo desde un modelo de SQLAlchemy."""

    model_config = ConfigDict(from_attributes=True)


class ClassroomRef(Schema):
    id: int
    name: str


class AccessCodeOut(BaseModel):
    """El código que el administrador comparte para activar la cuenta (INVITATION) o poner una
    contraseña nueva (PASSWORD_RESET). Solo se muestra una vez: en la base queda su hash."""

    code: str
    purpose: str
    expires_at: datetime
