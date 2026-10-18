import uuid
from datetime import datetime
from enum import StrEnum

from sqlalchemy import DateTime, ForeignKey, Integer, String, Text, text
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
    # siempre en minúsculas: con él se entra y se encuentra la invitación.
    email: Mapped[str | None] = mapped_column(Text, unique=True)
    # hash Argon2id; queda vacío hasta que la persona activa su cuenta con el código de invitación.
    password_hash: Mapped[str | None] = mapped_column(Text)
    password_changed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    last_login_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    # sube cuando alguien se da de baja o cambia su contraseña: los access tokens viejos dejan de coincidir.
    token_version: Mapped[int] = mapped_column(Integer, default=0, server_default=text("0"))
    status: Mapped[UserStatus] = mapped_column(
        str_enum(UserStatus), default=UserStatus.ACTIVE, server_default=UserStatus.ACTIVE.value
    )

    @property
    def full_name(self) -> str:
        return f"{self.name} {self.surname}".strip()


class CodePurpose(StrEnum):
    """Para qué sirve el código: activar la cuenta por primera vez o poner una contraseña nueva."""

    INVITATION = "INVITATION"
    PASSWORD_RESET = "PASSWORD_RESET"


class Invitation(IdMixin, Base):
    """El código de acceso que crea el administrador al dar de alta a una persona (o al generar uno nuevo).
    En la base solo queda el hash del código; accepted_at significa "ya se usó"."""

    __tablename__ = "invitation"

    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))
    email: Mapped[str] = mapped_column(Text)
    role: Mapped[Role] = mapped_column(str_enum(Role))
    code_hash: Mapped[str] = mapped_column(Text)
    purpose: Mapped[CodePurpose] = mapped_column(
        str_enum(CodePurpose), default=CodePurpose.INVITATION, server_default=CodePurpose.INVITATION.value
    )
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    accepted_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class RefreshToken(IdMixin, Base):
    """Un refresh token. Se guarda solo su hash y se rota en cada uso: el viejo queda revocado y apunta al nuevo.
    Los de un mismo inicio de sesión comparten family_id; si llega uno ya rotado, se revoca toda la familia."""

    __tablename__ = "refresh_token"

    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"), index=True)
    token_hash: Mapped[str] = mapped_column(Text, unique=True)
    family_id: Mapped[uuid.UUID] = mapped_column(index=True)
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    replaced_by_id: Mapped[int | None] = mapped_column(ForeignKey("refresh_token.id"))
    last_used_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    user_agent: Mapped[str | None] = mapped_column(Text)


class AuthAttempt(IdMixin, Base):
    """Un intento fallido de entrar (kind LOGIN) o de activar con un código (kind ACTIVATE).
    Con ellos se limita cuántas veces se puede fallar en poco tiempo (ver throttle.py).
    Se guarda por correo aunque no exista la cuenta, para que el bloqueo no delate qué correos están registrados."""

    __tablename__ = "auth_attempt"

    kind: Mapped[str] = mapped_column(String(20))
    email: Mapped[str] = mapped_column(Text, index=True)
    ip: Mapped[str | None] = mapped_column(String(64), index=True)
    attempted_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), index=True)
