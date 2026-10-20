import uuid
from datetime import date

from sqlalchemy import ForeignKey, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin


class DailyMenu(IdMixin, Base):
    """El menú de un día del centro: desayuno, comida y colación en texto libre. Hay uno por centro y por día."""

    __tablename__ = "daily_menu"
    __table_args__ = (UniqueConstraint("daycare_center_id", "date"),)

    daycare_center_id: Mapped[int] = mapped_column(ForeignKey("daycare_center.id"))
    date: Mapped[date]
    # cada comida es opcional, pero el menú lleva al menos una.
    breakfast: Mapped[str | None] = mapped_column(Text)
    lunch: Mapped[str | None] = mapped_column(Text)
    snack: Mapped[str | None] = mapped_column(Text)
    updated_by: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))
