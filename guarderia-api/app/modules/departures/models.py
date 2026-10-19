import uuid
from datetime import date, datetime

from sqlalchemy import DateTime, ForeignKey, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin


class ChildDeparture(IdMixin, Base):
    """La salida de un niño al terminar el día: a qué hora se fue y con quién (pase de salida).

    Hay una por niño y por día. El nombre de quien lo recogió se guarda tal cual: si después quitan a la
    persona autorizada de la lista, el registro del día no cambia.
    """

    __tablename__ = "child_departure"
    __table_args__ = (UniqueConstraint("child_id", "date"),)

    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"))
    date: Mapped[date]
    departed_at: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    # quien lo recogió: una persona autorizada o uno de sus tutores (solo una de las dos).
    pickup_id: Mapped[int | None] = mapped_column(ForeignKey("authorized_pickup.id", ondelete="SET NULL"))
    guardian_id: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("app_user.id", ondelete="SET NULL"))
    picked_up_by: Mapped[str] = mapped_column(String(200))
    recorded_by: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))
