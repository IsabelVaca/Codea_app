import uuid
from datetime import date
from enum import StrEnum

from sqlalchemy import ForeignKey, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, TimestampMixin, str_enum


class ReportType(StrEnum):
    INCIDENT = "INCIDENT"
    ACHIEVEMENT = "ACHIEVEMENT"
    HEALTH = "HEALTH"
    BEHAVIOR = "BEHAVIOR"


class ReportMethod(StrEnum):
    WRITTEN = "WRITTEN"
    DICTATION = "DICTATION"
    PHOTO = "PHOTO"


class Report(IdMixin, Base):
    """Un reporte de un momento de la rutina (Report en la app)."""

    __tablename__ = "report"

    routine_moment_id: Mapped[int] = mapped_column(ForeignKey("routine_moment.id"))
    type: Mapped[ReportType] = mapped_column(str_enum(ReportType))
    method: Mapped[ReportMethod] = mapped_column(str_enum(ReportMethod))
    description: Mapped[str] = mapped_column(Text)
    media_id: Mapped[int | None] = mapped_column(ForeignKey("media.id"))
    created_by: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))


class ReportChild(TimestampMixin, Base):
    __tablename__ = "report_child"

    report_id: Mapped[int] = mapped_column(ForeignKey("report.id"), primary_key=True)
    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"), primary_key=True)


class Mood(StrEnum):
    HAPPY = "HAPPY"
    CALM = "CALM"
    TIRED = "TIRED"
    SAD = "SAD"
    UPSET = "UPSET"


class MealAmount(StrEnum):
    ALL = "ALL"
    MOST = "MOST"
    HALF = "HALF"
    LITTLE = "LITTLE"
    NONE = "NONE"


class DailySummary(IdMixin, Base):
    """El resumen del día de un niño que ve el papá en Hoy: ánimo, comida, cambios y siesta."""

    __tablename__ = "daily_summary"
    __table_args__ = (UniqueConstraint("child_id", "date"),)

    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"))
    date: Mapped[date]
    mood: Mapped[Mood | None] = mapped_column(str_enum(Mood))
    meals: Mapped[MealAmount | None] = mapped_column(str_enum(MealAmount))
    diaper_changes: Mapped[int | None]
    nap_minutes: Mapped[int | None]
    general_note: Mapped[str | None] = mapped_column(Text)
    recorded_by: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))
