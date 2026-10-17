from datetime import date, datetime

from pydantic import BaseModel

from app.core.schemas import ClassroomRef

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


class TemplateMomentIn(BaseModel):
    name: str
    category: MomentCategory


class TemplateIn(BaseModel):
    """Una plantilla completa: al editarla se manda entera, con sus momentos en orden."""

    name: str
    moments: list[TemplateMomentIn]
    # la que usan las salas que no tienen una propia.
    is_default: bool = False


class TemplateMomentOut(BaseModel):
    number: int
    name: str
    category: MomentCategory


class TemplateOut(BaseModel):
    id: int
    name: str
    is_default: bool
    moments: list[TemplateMomentOut]
    # las salas que la usan; las que no tienen plantilla propia usan la default.
    classrooms: list[ClassroomRef]


class TemplateAssign(BaseModel):
    """template_id = null deja a la sala con la plantilla por defecto."""

    template_id: int | None


class TemplateAssignOut(BaseModel):
    classroom: ClassroomRef
    template_id: int | None
