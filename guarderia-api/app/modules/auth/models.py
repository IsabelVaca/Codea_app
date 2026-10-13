import uuid
from datetime import datetime
from enum import StrEnum

from sqlalchemy import DateTime, ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, TimestampMixin, str_enum


class Role(StrEnum):
    """Los mismos roles que el enum Role de la app."""

    ADMIN = "ADMIN"
    TEACHER = "TEACHER"
    PARENT = "PARENT"


class UserStatus(StrEnum):
    ACTIVE = "ACTIVE"
    INACTIVE = "INACTIVE"


class AppUser(TimestampMixin, Base):
    __tablename__ = "app_user"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    name: Mapped[str] = mapped_column(String(100))
    surname: Mapped[str] = mapped_column(String(100))
    phone: Mapped[str | None] = mapped_column(Text)
    # siempre en minúsculas: con él se encuentra la invitación al entrar con Google.
    email: Mapped[str | None] = mapped_column(Text, unique=True)
    # el uid de Firebase; queda vacío hasta que la persona acepta su invitación.
    firebase_uid: Mapped[str | None] = mapped_column(Text, unique=True)
    status: Mapped[UserStatus] = mapped_column(
        str_enum(UserStatus), default=UserStatus.ACTIVE, server_default=UserStatus.ACTIVE.value
    )

    @property
    def full_name(self) -> str:
        return f"{self.name} {self.surname}".strip()


class Invitation(IdMixin, Base):
    """La invitación que crea el administrador al dar de alta a una docente o a un tutor."""

    __tablename__ = "invitation"

    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))
    email: Mapped[str] = mapped_column(Text)
    role: Mapped[Role] = mapped_column(str_enum(Role))
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    accepted_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
