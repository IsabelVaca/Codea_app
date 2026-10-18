from datetime import time

from pydantic import BaseModel


class CenterUpdate(BaseModel):
    """Solo cambia lo que se manda. Para quitar la ubicación, manda latitude y longitude en null."""

    name: str | None = None
    latitude: float | None = None
    longitude: float | None = None
    # qué tan lejos del centro puede checar la docente.
    check_in_radius_m: int | None = None
    timezone: str | None = None
    # el horario del centro (hora local). work_days: 1 = lunes … 7 = domingo.
    entry_time: time | None = None
    exit_time: time | None = None
    auto_close_time: time | None = None
    work_days: list[int] | None = None
