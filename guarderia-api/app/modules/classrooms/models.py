from sqlalchemy import ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin


class DaycareCenter(IdMixin, Base):
    __tablename__ = "daycare_center"

    name: Mapped[str] = mapped_column(String(50))


class Classroom(IdMixin, Base):
    __tablename__ = "classroom"

    daycare_center_id: Mapped[int] = mapped_column(ForeignKey("daycare_center.id"))
    name: Mapped[str] = mapped_column(String(40))
    school_grade: Mapped[str | None] = mapped_column(String(40))