from fastapi import APIRouter, Query, status

from app.core.db import Db
from app.core.pagination import Page, parse_cursor
from app.core.security import AdminOrParent, AdminUser

from . import service
from .models import PaymentStatus
from .schemas import CancelIn, PayIn, PaymentIn, PaymentList, PaymentOut, PaymentPatch, PaymentWithChild

router = APIRouter(tags=["pagos"])


@router.get("/children/{child_id}/payments", response_model=PaymentList)
async def list_child_payments(
    db: Db, current: AdminOrParent, child_id: int, status: PaymentStatus | None = None
) -> PaymentList:
    """La bitácora de pagos de un niño, con lo pendiente, lo vencido y lo pagado. El papá solo ve lo vigente."""
    return await service.list_for_child(db, current, child_id, status)


@router.post("/children/{child_id}/payments", response_model=PaymentOut, status_code=status.HTTP_201_CREATED)
async def create_payment(db: Db, current: AdminUser, child_id: int, data: PaymentIn) -> PaymentOut:
    """Registra un cargo pendiente o un pago que ya se recibió."""
    return await service.create(db, current, child_id, data)


@router.get("/payments", response_model=Page[PaymentWithChild])
async def payments_overview(
    db: Db,
    current: AdminUser,
    status: PaymentStatus | None = None,
    overdue: bool = False,
    cursor: str | None = None,
    limit: int = Query(30, ge=1, le=100),
) -> Page[PaymentWithChild]:
    """Todos los pagos del centro. Con overdue=true, solo los pendientes cuya fecha límite ya pasó."""
    return await service.overview(db, status, overdue, parse_cursor(cursor), limit)


@router.patch("/payments/{payment_id}", response_model=PaymentOut)
async def update_payment(db: Db, current: AdminUser, payment_id: int, data: PaymentPatch) -> PaymentOut:
    """Corrige un cargo pendiente (concepto, monto, fecha límite, notas)."""
    return await service.update(db, payment_id, data)


@router.post("/payments/{payment_id}/pay", response_model=PaymentOut)
async def pay_payment(db: Db, current: AdminUser, payment_id: int, data: PayIn) -> PaymentOut:
    """Marca un cargo pendiente como pagado."""
    return await service.pay(db, current, payment_id, data)


@router.post("/payments/{payment_id}/cancel", response_model=PaymentOut)
async def cancel_payment(db: Db, current: AdminUser, payment_id: int, data: CancelIn | None = None) -> PaymentOut:
    """Cancela un cargo o un pago. No se borra: queda en la bitácora."""
    return await service.cancel(db, current, payment_id, data or CancelIn())
