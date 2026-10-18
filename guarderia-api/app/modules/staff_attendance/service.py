import logging
from datetime import datetime
from zoneinfo import ZoneInfo

from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock
from app.core.errors import ApiError
from app.core.security import CurrentUser
from app.modules.classrooms.centers import center_of_classroom
from app.modules.classrooms.models import DaycareCenter
from app.modules.routines import service as routines

from .models import CheckInStatus, CoordinationNotice, StaffCheckIn
from .schemas import CheckInIn, CheckInOut, NotifyIn, NotifyOut

log = logging.getLogger(__name__)

def _to_out(record: StaffCheckIn, center: DaycareCenter) -> CheckInOut:
    return CheckInOut(
        id=record.id,
        date=record.date,
        checked_in_at=record.checked_in_at,
        status=record.status,
        minutes_late=record.minutes_late,
        late_reason=record.late_reason,
        place_name=center.name,
        checked_out_at=record.checked_out_at,
        auto_closed=record.auto_closed,
    )


async def close_stale(db: AsyncSession, center: DaycareCenter) -> None:
    """Cierra las entradas sin salida cuyo cierre automático ya pasó: un día anterior, o hoy después de la hora
    de cierre que el admin configuró en el centro (21:00 por omisión).

    No hay proceso aparte: corre al inicio de cada petición del checador, así que el registro queda cerrado
    la primera vez que alguien lo consulta. Hay un solo centro, por eso la zona horaria es la suya.
    La salida queda en la hora de cierre (o en la hora de la entrada, si la docente llegó después).
    """
    tz = ZoneInfo(center.timezone)
    now = clock.local_now(center.timezone)
    after_cutoff = now.time() >= center.auto_close_time
    stale = await db.scalars(
        select(StaffCheckIn).where(
            StaffCheckIn.checked_out_at.is_(None),
            or_(StaffCheckIn.date < now.date(), (StaffCheckIn.date == now.date()) & after_cutoff),
        )
    )
    changed = False
    for record in stale:
        cutoff = datetime.combine(record.date, center.auto_close_time, tzinfo=tz)
        record.checked_out_at = max(cutoff, record.checked_in_at)
        record.auto_closed = True
        changed = True
    if changed:
        await db.commit()


async def _find(db: AsyncSession, current: CurrentUser, center: DaycareCenter) -> StaffCheckIn | None:
    day = clock.local_today(center.timezone)
    return await db.scalar(
        select(StaffCheckIn).where(StaffCheckIn.teacher_id == current.id, StaffCheckIn.date == day)
    )


async def today(db: AsyncSession, current: CurrentUser) -> CheckInOut | None:
    center = await center_of_classroom(db, current.teacher.classroom_id)
    await close_stale(db, center)
    record = await _find(db, current, center)
    return _to_out(record, center) if record else None


async def check_in(db: AsyncSession, current: CurrentUser, data: CheckInIn) -> tuple[CheckInOut, bool]:
    """Registra la entrada con la hora del servidor, sin ubicación. Regresa (registro, creado)."""
    center = await center_of_classroom(db, current.teacher.classroom_id)
    await close_stale(db, center)

    existing = await _find(db, current, center)
    if existing is not None:
        return _to_out(existing, center), False

    now = clock.local_now(center.timezone)
    # el centro cierra algunos días: ese día no se registra entrada.
    if now.isoweekday() not in center.work_days:
        raise ApiError(409, "NOT_WORKING_DAY", "Hoy no es día de trabajo", "El administrador define los días en Ajustes")
    # el retardo se mide contra la hora de entrada del centro, igual para todas.
    arrival = now.hour * 60 + now.minute
    start = center.entry_time.hour * 60 + center.entry_time.minute
    minutes_late = max(arrival - start, 0)

    record = StaffCheckIn(
        teacher_id=current.id,
        date=now.date(),
        checked_in_at=clock.now(),
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


async def check_out(db: AsyncSession, current: CurrentUser) -> tuple[CheckInOut, bool]:
    """Registra la salida del día. Pide haber registrado la entrada. Repetirla regresa la misma salida
    (también si el cierre de las 9 pm ya la puso). Regresa (registro, creada)."""
    center = await center_of_classroom(db, current.teacher.classroom_id)
    await close_stale(db, center)

    record = await _find(db, current, center)
    if record is None:
        raise ApiError(409, "NOT_CHECKED_IN", "Primero registra tu entrada de hoy")
    if record.checked_out_at is not None:
        return _to_out(record, center), False

    record.checked_out_at = clock.now()
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
