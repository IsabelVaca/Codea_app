import uuid
from datetime import datetime  # noqa: F401  (lo usa el tipo de created_at del mixin)

from sqlalchemy import JSON, ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin


class AuditLog(IdMixin, Base):
    """Quién hizo qué y cuándo (RF-14). Solo se agregan filas: la API no tiene forma de editarlas ni borrarlas.

    No guarda contraseñas, códigos ni tokens: en `detail` solo van datos para entender el hecho
    (por ejemplo el rol del usuario que se dio de alta)."""

    __tablename__ = "audit_log"

    # quien hizo la acción; vacío cuando todavía no se sabe quién es (un login con un correo que no existe).
    actor_id: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("app_user.id"), index=True)
    # el correo con el que se intentó entrar, para los intentos fallidos.
    actor_email: Mapped[str | None] = mapped_column(Text)
    action: Mapped[str] = mapped_column(String(40), index=True)
    # sobre qué: "USER", "CHILD", "SESSION"…, y su id como texto (los ids son int o uuid).
    entity_type: Mapped[str | None] = mapped_column(String(40))
    entity_id: Mapped[str | None] = mapped_column(String(64))
    detail: Mapped[dict | None] = mapped_column(JSON)
    ip: Mapped[str | None] = mapped_column(String(64))
