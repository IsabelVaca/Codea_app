import uuid
from datetime import datetime
from enum import StrEnum

from sqlalchemy import DateTime, ForeignKey, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, TimestampMixin, str_enum


class MediaKind(StrEnum):
    PHOTO = "PHOTO"
    DOCUMENT = "DOCUMENT"


class MediaStatus(StrEnum):
    # la API ya dio el permiso, pero el archivo todavía no llega al bucket.
    PENDING = "PENDING"
    READY = "READY"
    # el admin aprobó una solicitud de borrado: el archivo ya no está en el bucket ni sale en ninguna pantalla.
    DELETED = "DELETED"


class Media(IdMixin, Base):
    """Un archivo del bucket. La llave no lleva datos personales."""

    __tablename__ = "media"

    kind: Mapped[MediaKind] = mapped_column(str_enum(MediaKind))
    storage_key: Mapped[str] = mapped_column(String(200), unique=True)
    content_type: Mapped[str] = mapped_column(String(80))
    size_bytes: Mapped[int]
    status: Mapped[MediaStatus] = mapped_column(str_enum(MediaStatus))
    uploaded_by: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))


class MediaChild(TimestampMixin, Base):
    """A quién se etiqueta en una foto: eso decide qué papás la pueden ver."""

    __tablename__ = "media_child"

    media_id: Mapped[int] = mapped_column(ForeignKey("media.id"), primary_key=True)
    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"), primary_key=True)


class DeletionStatus(StrEnum):
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"


class MediaDeletionRequest(IdMixin, Base):
    """El botón "Pedir que se borre" de la pantalla Fotos."""

    __tablename__ = "media_deletion_request"
    __table_args__ = (UniqueConstraint("media_id", "requested_by"),)

    media_id: Mapped[int] = mapped_column(ForeignKey("media.id"))
    requested_by: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))
    reason: Mapped[str | None] = mapped_column(Text)
    status: Mapped[DeletionStatus] = mapped_column(
        str_enum(DeletionStatus), default=DeletionStatus.PENDING, server_default=DeletionStatus.PENDING.value
    )
    # quién la resolvió (admin), cuándo y con qué explicación para la familia.
    decided_by: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("app_user.id"))
    decided_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    decision_note: Mapped[str | None] = mapped_column(Text)
