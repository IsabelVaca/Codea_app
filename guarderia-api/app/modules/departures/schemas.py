import uuid
from datetime import date, datetime
from enum import StrEnum

from pydantic import BaseModel


class PickupKind(StrEnum):
    """De dónde sale quien puede recoger al niño."""

    AUTHORIZED = "AUTHORIZED"
    GUARDIAN = "GUARDIAN"


class PickupChoice(BaseModel):
    """Una persona de la lista que la docente ve al registrar la salida de un niño."""

    kind: PickupKind
    # el id de la persona autorizada (número) o del tutor (uuid), como texto.
    id: str
    full_name: str
    relationship: str | None


class DepartureIn(BaseModel):
    """Quien recoge: exactamente uno de los dos."""

    pickup_id: int | None = None
    guardian_id: uuid.UUID | None = None


class DepartureOut(BaseModel):
    child_id: int
    date: date
    departed_at: datetime
    picked_up_by: str


class DepartureChild(BaseModel):
    """Un niño presente hoy, con quién puede recogerlo y su salida si ya se fue."""

    child_id: int
    name: str
    full_name: str
    choices: list[PickupChoice]
    departure: DepartureOut | None


class DepartureSheet(BaseModel):
    """El pase de salida de la sala: los niños que estuvieron hoy y cuántos faltan por salir."""

    date: date
    children: list[DepartureChild]
    pending: int
    # true cuando todos los presentes ya salieron: la docente puede finalizar el día.
    all_done: bool
