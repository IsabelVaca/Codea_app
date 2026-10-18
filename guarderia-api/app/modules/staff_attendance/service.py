import logging
import math

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock
from app.core.errors import ApiError
from app.core.security import CurrentUser
from app.modules.classrooms.centers import center_of_classroom
from app.modules.classrooms.models import DaycareCenter
from app.modules.routines import service as routines
from app.modules.teachers.models import Shift

from .models import CheckInStatus, CoordinationNotice, StaffCheckIn
from .schemas import CheckInIn, CheckInOut, NotifyIn, NotifyOut

log = logging.getLogger(__name__)

EARTH_RADIUS_M = 6_371_000


def distance_m(lat1: float, lon1: float, lat2: float, lon2: float) -> int:
    """Distancia en metros entre dos puntos (haversine)."""
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    d_phi = math.radians(lat2 - lat1)
    d_lambda = math.radians(lon2 - lon1)
    a = math.sin(d_phi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(d_lambda / 2) ** 2
    return round(2 * EARTH_RADIUS_M * math.asin(math.sqrt(a)))


def _to_out(record: StaffCheckIn, center: DaycareCenter) -> CheckInOut:
    return CheckInOut(
        id=record.id,
        date=record.date,
        checked_in_at=record.checked_in_at,
        status=record.status,
        minutes_late=record.minutes_late,
        late_reason=record.late_reason,
        distance_m=record.distance_m,
        allowed_radius_m=center.check_in_radius_m,
        place_name=center.name,
        checked_out_at=record.checked_out_at,
    )


async def _find(db: AsyncSession, current: CurrentUser, center: DaycareCenter) -> StaffCheckIn | None:
    day = clock.local_today(center.timezone)
    return await db.scalar(
        select(StaffCheckIn).where(StaffCheckIn.teacher_id == current.id, StaffCheckIn.date == day)
    )


async def today(db: AsyncSession, current: CurrentUser) -> CheckInOut | None:
    center = await center_of_classroom(db, current.teacher.classroom_id)
    record = await _find(db, current, center)
    return _to_out(record, center) if record else None


async def check_in(db: AsyncSession, current: CurrentUser, data: CheckInIn) -> tuple[CheckInOut, bool]:
    """Las reglas de CheckInRules, con el reloj del servidor. Regresa (registro, creado)."""
    center = await center_of_classroom(db, current.teacher.classroom_id)
    if center.latitude is None or center.longitude is None:
        raise ApiError(409, "CENTER_LOCATION_MISSING", "El centro todavía no tiene ubicación registrada")

    existing = await _find(db, current, center)
    if existing is not None:
        return _to_out(existing, center), False

    distance = distance_m(data.latitude, data.longitude, center.latitude, center.longitude)
    # fuera del radio no se registra nada.
    if distance > center.check_in_radius_m:
        raise ApiError(
            422,
            "OUT_OF_RANGE",
            "Estás fuera del perímetro del centro",
            f"Estás a {distance} m; el radio permitido es de {center.check_in_radius_m} m",
            distance_m=distance,
            allowed_radius_m=center.check_in_radius_m,
        )

    now = clock.local_now(center.timezone)
    minutes_late = 0
    shift = await db.get(Shift, current.teacher.shift_id) if current.teacher.shift_id else None
    if shift is not None:
        arrival = now.hour * 60 + now.minute
        start = shift.start_time.hour * 60 + shift.start_time.minute
        minutes_late = max(arrival - start, 0)

    record = StaffCheckIn(
        teacher_id=current.id,
        date=now.date(),
        checked_in_at=clock.now(),
        latitude=data.latitude,
        longitude=data.longitude,
        distance_m=distance,
        status=CheckInStatus.LATE if minutes_late > 0 else CheckInStatus.ON_TIME,
        minutes_late=minutes_late,
        late_reason=(data.late_reason or "").strip() or None,
    )
    db.add(record)
    await db.commit()
    # con la entrada de la docente arranca el día de su sala.
    if current.teacher.classroom_id is not None:
        await routines.ensure_day(db, current.teacher.classroom_id, record.date)
    return _to_out(record, center), True


async def check_out(db: AsyncSession, current: CurrentUser, data: CheckInIn) -> tuple[CheckInOut, bool]:
    """Registra la salida del día. Pide haber registrado la entrada y estar dentro del radio, igual que la entrada.
    Repetirla regresa la misma salida. Regresa (registro, creada)."""
    center = await center_of_classroom(db, current.teacher.classroom_id)
    if center.latitude is None or center.longitude is None:
        raise ApiError(409, "CENTER_LOCATION_MISSING", "El centro todavía no tiene ubicación registrada")

    record = await _find(db, current, center)
    if record is None:
        raise ApiError(409, "NOT_CHECKED_IN", "Primero registra tu entrada de hoy")
    if record.checked_out_at is not None:
        return _to_out(record, center), False

    distance = distance_m(data.latitude, data.longitude, center.latitude, center.longitude)
    if distance > center.check_in_radius_m:
        raise ApiError(
            422,
            "OUT_OF_RANGE",
            "Estás fuera del perímetro del centro",
            f"Estás a {distance} m; el radio permitido es de {center.check_in_radius_m} m",
            distance_m=distance,
            allowed_radius_m=center.check_in_radius_m,
        )
    record.checked_out_at = clock.now()
    record.out_latitude = data.latitude
    record.out_longitude = data.longitude
    record.out_distance_m = distance
    await db.commit()
    return _to_out(record, center), True


async def notify_coordination(db: AsyncSession, current: CurrentUser, data: NotifyIn) -> NotifyOut:
    center = await center_of_classroom(db, current.teacher.classroom_id)
    notice = CoordinationNotice(
        teacher_id=current.id,
        date=clock.local_today(center.timezone),
        reason=data.reason,
        message=(data.message or "").strip() or None,
    )
    db.add(notice)
    await db.commit()
    await db.refresh(notice)
    # todavía no hay push: por ahora el aviso queda guardado y en el log.
    log.warning("aviso a coordinación: %s (%s)", current.user.full_name, data.reason.value)
    return NotifyOut.model_validate(notice)
