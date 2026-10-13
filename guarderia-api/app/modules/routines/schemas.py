from datetime import date, datetime

from pydantic import BaseModel

from .models import MomentCategory, MomentStatus


class MomentOut(BaseModel):
    """Un momento de la rutina (Subprocess en la app)."""

    id: int
    number: int
    name: str
    category: MomentCategory
    status: MomentStatus
    # cuándo empezó (ACTIVE) y cuándo se confirmó (DONE).
    started_at: datetime | None
    confirmed_at: datetime | None
    description: str | None
    # el contador de reportes que aparece en Mi día.
    report_count: int


class RoutineOut(BaseModel):
    """La rutina de una sala en un día (Routine en la app)."""

    classroom_id: int
    date: date
    name: str | None
    moments: list[MomentOut]
    # los momentos hechos más el activo, como el "4 de 9" del prototipo.
    current_step: int
    total_steps: int
    active_moment_id: int | None
