import uuid
from datetime import date, datetime

from sqlalchemy import DateTime, ForeignKey, Index, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, TimestampMixin


class Conversation(IdMixin, Base):
    """Una conversación por niño, entre sus tutores y las docentes de su sala."""

    __tablename__ = "conversation"

    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"), unique=True)


class Message(IdMixin, Base):
    __tablename__ = "message"
    __table_args__ = (
        # un reintento con el mismo client_id no duplica el mensaje.
        UniqueConstraint("author_id", "client_id"),
        Index("ix_message_conversation_id_id", "conversation_id", "id"),
    )

    conversation_id: Mapped[int] = mapped_column(ForeignKey("conversation.id"))
    author_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))
    # vacío si el mensaje es solo una foto.
    text: Mapped[str] = mapped_column(Text)
    client_id: Mapped[uuid.UUID | None]
    # la foto del mensaje (opcional); vive en el bucket igual que las demás.
    media_id: Mapped[int | None] = mapped_column(ForeignKey("media.id"))


class ConversationRead(TimestampMixin, Base):
    """Hasta qué mensaje leyó cada persona: con esto se cuentan los no leídos."""

    __tablename__ = "conversation_read"

    conversation_id: Mapped[int] = mapped_column(ForeignKey("conversation.id"), primary_key=True)
    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"), primary_key=True)
    last_read_message_id: Mapped[int | None]


class Announcement(IdMixin, Base):
    """Un aviso del centro. Sin classroom_id es para todo el centro."""

    __tablename__ = "announcement"

    daycare_center_id: Mapped[int] = mapped_column(ForeignKey("daycare_center.id"))
    classroom_id: Mapped[int | None] = mapped_column(ForeignKey("classroom.id"))
    text: Mapped[str] = mapped_column(Text)
    author_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))


class ParentNote(IdMixin, Base):
    """La nota que el papá le manda a la asistente para hoy."""

    __tablename__ = "parent_note"

    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"))
    date: Mapped[date]
    text: Mapped[str] = mapped_column(Text)
    author_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))
    read_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
