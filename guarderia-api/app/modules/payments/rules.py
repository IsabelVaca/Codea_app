from datetime import date

from app.core.errors import ApiError

from .models import Payment, PaymentStatus

MAX_AMOUNT_CENTS = 100_000_000  # un millón de pesos: un tope para atrapar errores de dedo.


def check_concept(concept: str | None) -> str:
    if not concept or not concept.strip():
        raise ApiError(422, "CONCEPT_REQUIRED", "El concepto es obligatorio")
    concept = concept.strip()
    if len(concept) > 120:
        raise ApiError(422, "CONCEPT_TOO_LONG", "El concepto puede tener hasta 120 caracteres")
    return concept


def check_amount(amount_cents: int) -> int:
    if amount_cents <= 0:
        raise ApiError(422, "AMOUNT_INVALID", "El monto debe ser mayor que 0")
    if amount_cents > MAX_AMOUNT_CENTS:
        raise ApiError(422, "AMOUNT_TOO_LARGE", "El monto es demasiado grande")
    return amount_cents


def is_overdue(payment: Payment, today: date) -> bool:
    return payment.status == PaymentStatus.PENDING and payment.due_date is not None and payment.due_date < today


def clean(text: str | None) -> str | None:
    text = (text or "").strip()
    return text or None
