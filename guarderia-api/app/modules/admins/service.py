from zoneinfo import ZoneInfo, ZoneInfoNotFoundError

from sqlalchemy.ext.asyncio import AsyncSession

from app.core import rules
from app.core.errors import ApiError
from app.core.security import CurrentUser
from app.modules.audit import service as audit
from app.modules.classrooms.models import DaycareCenter
from app.modules.classrooms.schemas import CenterOut

from .schemas import CenterUpdate

MIN_RADIUS_M = 10
MAX_RADIUS_M = 1000
SCHEDULE_FIELDS = {"entry_time", "exit_time", "auto_close_time", "work_days"}


def _apply_schedule(center: DaycareCenter, data: CenterUpdate, sent: set[str]) -> None:
    """Valida el horario completo (lo que no se mandó se queda como estaba) y lo guarda.
    Reglas: la entrada va antes de la salida, y la salida no pasa del cierre automático."""
    entry = data.entry_time if "entry_time" in sent else center.entry_time
    exit_ = data.exit_time if "exit_time" in sent else center.exit_time
    close = data.auto_close_time if "auto_close_time" in sent else center.auto_close_time
    if entry is None or exit_ is None or close is None:
        raise ApiError(422, "SCHEDULE_INVALID", "El horario necesita entrada, salida y cierre automático")
    if not entry < exit_:
        raise ApiError(422, "SCHEDULE_INVALID", "La entrada debe ser antes de la salida")
    if not exit_ <= close:
        raise ApiError(422, "SCHEDULE_INVALID", "El cierre automático no puede ser antes de la salida")

    days = data.work_days if "work_days" in sent else center.work_days
    if not days or any(d < 1 or d > 7 for d in days):
        raise ApiError(422, "WORKDAYS_INVALID", "Elige al menos un día, de 1 (lunes) a 7 (domingo)")

    center.entry_time, center.exit_time, center.auto_close_time = entry, exit_, close
    center.work_days = sorted(set(days))


async def _center_of(db: AsyncSession, current: CurrentUser) -> DaycareCenter:
    center = await db.get(DaycareCenter, current.admin.daycare_center_id)
    if center is None:
        raise ApiError(409, "CENTER_MISSING", "Todavía no hay un centro registrado")
    return center


async def get_center(db: AsyncSession, current: CurrentUser) -> CenterOut:
    return CenterOut.model_validate(await _center_of(db, current))


async def update_center(db: AsyncSession, current: CurrentUser, data: CenterUpdate) -> CenterOut:
    """Cambia el nombre, la ubicación o el radio del checador. Valida antes de guardar."""
    center = await _center_of(db, current)
    sent = data.model_fields_set

    if "name" in sent:
        center.name = rules.require_name(data.name or "")

    if "latitude" in sent or "longitude" in sent:
        latitude = data.latitude if "latitude" in sent else center.latitude
        longitude = data.longitude if "longitude" in sent else center.longitude
        # la ubicación va completa o no va: con solo una de las dos el checador no sirve.
        if (latitude is None) != (longitude is None):
            raise ApiError(422, "LOCATION_INCOMPLETE", "Manda latitud y longitud juntas")
        if latitude is not None and not (-90 <= latitude <= 90 and -180 <= longitude <= 180):
            raise ApiError(422, "LOCATION_INVALID", "La ubicación no es válida")
        center.latitude, center.longitude = latitude, longitude

    if "check_in_radius_m" in sent:
        radius = data.check_in_radius_m
        if radius is None or not MIN_RADIUS_M <= radius <= MAX_RADIUS_M:
            raise ApiError(
                422, "RADIUS_INVALID", f"El radio debe estar entre {MIN_RADIUS_M} y {MAX_RADIUS_M} metros"
            )
        center.check_in_radius_m = radius

    if "timezone" in sent:
        try:
            ZoneInfo(data.timezone or "")
        except (ZoneInfoNotFoundError, ValueError):
            raise ApiError(422, "TIMEZONE_INVALID", "La zona horaria no existe", "Ejemplo: America/Monterrey")
        center.timezone = data.timezone

    schedule_changed = bool(sent & SCHEDULE_FIELDS)
    if schedule_changed:
        _apply_schedule(center, data, sent)

    # el checador de todas las docentes depende de estos datos: queda registrado quién los cambió y a qué.
    detail = {
        "fields": sorted(sent),
        "latitude": center.latitude,
        "longitude": center.longitude,
        "check_in_radius_m": center.check_in_radius_m,
    }
    if schedule_changed:
        detail["schedule"] = {
            "entry_time": center.entry_time.isoformat(timespec="minutes"),
            "exit_time": center.exit_time.isoformat(timespec="minutes"),
            "auto_close_time": center.auto_close_time.isoformat(timespec="minutes"),
            "work_days": center.work_days,
        }
    audit.record(
        db,
        audit.CENTER_UPDATED,
        actor_id=current.user.id,
        entity_type="CENTER",
        entity_id=center.id,
        detail=detail,
    )
    await db.commit()
    return CenterOut.model_validate(center)
