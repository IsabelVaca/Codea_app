import uuid
from datetime import date

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.errors import ApiError
from app.core.pagination import Page
from app.core.security import CurrentUser, get_child_in_scope
from app.modules.children.models import Child
from app.modules.classrooms.centers import center_today

from . import rules
from .models import Payment, PaymentMethod, PaymentStatus
from .schemas import (
    CancelIn,
    PayIn,
    PaymentIn,
    PaymentList,
    PaymentOut,
    PaymentPatch,
    PaymentWithChild,
)


def _out(payment: Payment, today: date) -> PaymentOut:
    out = PaymentOut.model_validate(payment)
    out.is_overdue = rules.is_overdue(payment, today)
    return out


async def _get_payment(db: AsyncSession, payment_id: int) -> Payment:
    payment = await db.get(Payment, payment_id)
    if payment is None:
        raise ApiError(404, "PAYMENT_NOT_FOUND", "No existe ese pago")
    return payment


async def list_for_child(
    db: AsyncSession, current: CurrentUser, child_id: int, status: PaymentStatus | None
) -> PaymentList:
    """El admin ve todo, incluso lo cancelado; el papá solo lo vigente."""
    child = await get_child_in_scope(db, current, child_id)
    today = await center_today(db)
    query = select(Payment).where(Payment.child_id == child.id)
    if current.admin is None:
        query = query.where(Payment.status != PaymentStatus.CANCELLED)
    if status is not None:
        query = query.where(Payment.status == status)
    payments = (await db.scalars(query.order_by(Payment.id.desc()))).all()

    live = [p for p in payments if p.status != PaymentStatus.CANCELLED]
    pending = [p for p in live if p.status == PaymentStatus.PENDING]
    return PaymentList(
        items=[_out(p, today) for p in payments],
        pending_cents=sum(p.amount_cents for p in pending),
        overdue_cents=sum(p.amount_cents for p in pending if rules.is_overdue(p, today)),
        paid_cents=sum(p.amount_cents for p in live if p.status == PaymentStatus.PAID),
    )


async def create(db: AsyncSession, current: CurrentUser, child_id: int, data: PaymentIn) -> PaymentOut:
    child = await get_child_in_scope(db, current, child_id)
    today = await center_today(db)
    if data.status == PaymentStatus.CANCELLED:
        raise ApiError(422, "STATUS_INVALID", "Un pago nuevo es pendiente o pagado")
    payment = Payment(
        child_id=child.id,
        concept=rules.check_concept(data.concept),
        amount_cents=rules.check_amount(data.amount_cents),
        status=data.status,
        due_date=data.due_date,
        reference=rules.clean(data.reference),
        notes=rules.clean(data.notes),
        recorded_by=current.id,
    )
    if data.status == PaymentStatus.PAID:
        if data.method is None:
            raise ApiError(422, "METHOD_REQUIRED", "Indica cómo se pagó")
        payment.method = data.method
        payment.paid_on = data.paid_on or today
    db.add(payment)
    await db.commit()
    await db.refresh(payment)
    return _out(payment, today)


async def update(db: AsyncSession, payment_id: int, data: PaymentPatch) -> PaymentOut:
    payment = await _get_payment(db, payment_id)
    if payment.status != PaymentStatus.PENDING:
        raise ApiError(409, "PAYMENT_LOCKED", "Solo se corrige un cargo pendiente; cancela este y registra otro")
    fields = data.model_fields_set
    if "concept" in fields:
        payment.concept = rules.check_concept(data.concept)
    if "amount_cents" in fields:
        payment.amount_cents = rules.check_amount(data.amount_cents or 0)
    if "due_date" in fields:
        payment.due_date = data.due_date
    if "notes" in fields:
        payment.notes = rules.clean(data.notes)
    await db.commit()
    await db.refresh(payment)
    return _out(payment, await center_today(db))


async def pay(db: AsyncSession, current: CurrentUser, payment_id: int, data: PayIn) -> PaymentOut:
    payment = await _get_payment(db, payment_id)
    if payment.status != PaymentStatus.PENDING:
        raise ApiError(409, "PAYMENT_NOT_PENDING", "Ese cargo ya no está pendiente")
    today = await center_today(db)
    payment.status = PaymentStatus.PAID
    payment.method = data.method
    payment.paid_on = data.paid_on or today
    payment.reference = rules.clean(data.reference) or payment.reference
    payment.recorded_by = current.id
    await db.commit()
    await db.refresh(payment)
    return _out(payment, today)


async def cancel(db: AsyncSession, current: CurrentUser, payment_id: int, data: CancelIn) -> PaymentOut:
    payment = await _get_payment(db, payment_id)
    if payment.status == PaymentStatus.CANCELLED:
        raise ApiError(409, "PAYMENT_CANCELLED", "Ese pago ya estaba cancelado")
    reason = rules.clean(data.reason)
    if reason:
        payment.notes = f"{payment.notes}\nCancelado: {reason}" if payment.notes else f"Cancelado: {reason}"
    payment.status = PaymentStatus.CANCELLED
    payment.recorded_by = current.id
    await db.commit()
    await db.refresh(payment)
    return _out(payment, await center_today(db))


async def overview(
    db: AsyncSession, status: PaymentStatus | None, overdue: bool, cursor: int | None, limit: int
) -> Page[PaymentWithChild]:
    """Todos los pagos del centro, del más nuevo al más viejo (pantalla del admin)."""
    today = await center_today(db)
    query = select(Payment, Child).join(Child, Child.id == Payment.child_id)
    if status is not None:
        query = query.where(Payment.status == status)
    if overdue:
        query = query.where(Payment.status == PaymentStatus.PENDING, Payment.due_date < today)
    if cursor is not None:
        query = query.where(Payment.id < cursor)
    rows = (await db.execute(query.order_by(Payment.id.desc()).limit(limit + 1))).all()
    next_cursor = str(rows[limit - 1][0].id) if len(rows) > limit else None
    return Page(
        items=[
            PaymentWithChild(**_out(payment, today).model_dump(), child_name=child.full_name)
            for payment, child in rows[:limit]
        ],
        next_cursor=next_cursor,
    )
