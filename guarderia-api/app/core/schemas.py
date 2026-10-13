from pydantic import BaseModel, ConfigDict


class Schema(BaseModel):
    """Base de las respuestas: se pueden armar directo desde un modelo de SQLAlchemy."""

    model_config = ConfigDict(from_attributes=True)


class ClassroomRef(Schema):
    id: int
    name: str
