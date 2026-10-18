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

    # el checador de todas las docentes depende de estos datos: queda registrado quién los cambió y a qué.
    audit.record(
        db,
        audit.CENTER_UPDATED,
        actor_id=current.user.id,
        entity_type="CENTER",
        entity_id=center.id,
        detail={
            "fields": sorted(sent),
            "latitude": center.latitude,
            "longitude": center.longitude,
            "check_in_radius_m": center.check_in_radius_m,
        },
    )
    await db.commit()
    return CenterOut.model_validate(center)
