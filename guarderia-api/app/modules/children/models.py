from datetime import date
from enum import StrEnum

from sqlalchemy import ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, str_enum


class ChildStatus(StrEnum):
    ACTIVE = "ACTIVE"
    # dado de baja; se puede reactivar.
    INACTIVE = "INACTIVE"


class Child(IdMixin, Base):
    __tablename__ = "child"

    # vacía cuando el niño queda sin sala ("Sacar de la sala").
    classroom_id: Mapped[int | None] = mapped_column(ForeignKey("classroom.id"))
    name: Mapped[str] = mapped_column(String(100))
    surname: Mapped[str] = mapped_column(String(100))
    # se guarda la fecha y la API calcula la edad (Infante.edad en la app).
    birth_date: Mapped[date | None]
    allergies: Mapped[str | None] = mapped_column(Text)
    status: Mapped[ChildStatus] = mapped_column(
        str_enum(ChildStatus), default=ChildStatus.ACTIVE, server_default=ChildStatus.ACTIVE.value
    )

    @property
    def full_name(self) -> str:
        return f"{self.name} {self.surname}".strip()
