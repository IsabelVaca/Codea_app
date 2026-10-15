from pydantic import BaseModel


class CenterUpdate(BaseModel):
    """Solo cambia lo que se manda. Para quitar la ubicación, manda latitude y longitude en null."""

    name: str | None = None
    latitude: float | None = None
    longitude: float | None = None
    # qué tan lejos del centro puede checar la docente.
    check_in_radius_m: int | None = None
    timezone: str | None = None
