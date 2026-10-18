import uuid
from datetime import date
from enum import StrEnum

from sqlalchemy import CheckConstraint, ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, str_enum


class PaymentStatus(StrEnum):
    # cargo registrado que todavía no se paga.
    PENDING = "PENDING"
    PAID = "PAID"
    # anulado por error o por acuerdo; no cuenta en los totales y no se borra (queda en la bitácora).
    CANCELLED = "CANCELLED"


class PaymentMethod(StrEnum):
    CASH = "CASH"
    TRANSFER = "TRANSFER"
    CARD = "CARD"
    OTHER = "OTHER"


class Payment(IdMixin, Base):
    """Una línea de la bitácora de pagos de un niño: un cargo pendiente o un pago ya hecho.

    El dinero va en centavos (entero) y siempre en pesos mexicanos: así no hay errores de redondeo.
    """

    __tablename__ = "payment"
    __table_args__ = (CheckConstraint("amount_cents > 0", name="amount_positive"),)

    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"), index=True)
    concept: Mapped[str] = mapped_column(String(120))
    amount_cents: Mapped[int]
    status: Mapped[PaymentStatus] = mapped_column(
        str_enum(PaymentStatus), default=PaymentStatus.PENDING, server_default=PaymentStatus.PENDING.value
    )
    due_date: Mapped[date | None]
    paid_on: Mapped[date | None]
    method: Mapped[PaymentMethod | None] = mapped_column(str_enum(PaymentMethod))
    reference: Mapped[str | None] = mapped_column(String(120))
    notes: Mapped[str | None] = mapped_column(Text)
    recorded_by: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))
