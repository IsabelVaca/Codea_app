from datetime import date, datetime

from pydantic import BaseModel

from app.core.schemas import Schema

from .models import PaymentMethod, PaymentStatus


class PaymentIn(BaseModel):
    concept: str
    amount_cents: int
    # PENDING (un cargo por cobrar) o PAID (un pago que ya se recibió). CANCELLED no se crea.
    status: PaymentStatus = PaymentStatus.PENDING
    due_date: date | None = None
    # solo con PAID: cuándo y cómo se pagó (paid_on es hoy si no se manda).
    paid_on: date | None = None
    method: PaymentMethod | None = None
    reference: str | None = None
    notes: str | None = None


class PaymentPatch(BaseModel):
    """Solo se corrige un cargo pendiente; uno pagado ya no se edita (se cancela y se registra otro)."""

    concept: str | None = None
    amount_cents: int | None = None
    due_date: date | None = None
    notes: str | None = None


class PayIn(BaseModel):
    method: PaymentMethod
    paid_on: date | None = None
    reference: str | None = None


class CancelIn(BaseModel):
    reason: str | None = None


class PaymentOut(Schema):
    id: int
    child_id: int
    concept: str
    amount_cents: int
    status: PaymentStatus
    due_date: date | None
    paid_on: date | None
    method: PaymentMethod | None
    reference: str | None
    notes: str | None
    # pendiente y con fecha límite ya pasada.
    is_overdue: bool = False
    created_at: datetime


class PaymentList(BaseModel):
    """Los pagos de un niño y sus totales (lo cancelado no cuenta)."""

    items: list[PaymentOut]
    pending_cents: int
    overdue_cents: int
    paid_cents: int


class PaymentWithChild(PaymentOut):
    child_name: str
