from datetime import date, datetime

from pydantic import BaseModel, Field

from app.core.schemas import Schema


class MenuIn(BaseModel):
    """Lo que come el centro ese día. Al menos una comida debe traer texto."""

    breakfast: str | None = Field(default=None, max_length=500)
    lunch: str | None = Field(default=None, max_length=500)
    snack: str | None = Field(default=None, max_length=500)


class MenuOut(Schema):
    date: date
    breakfast: str | None
    lunch: str | None
    snack: str | None
    updated_at: datetime
