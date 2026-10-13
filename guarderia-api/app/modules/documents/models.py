import uuid
from datetime import date, datetime

from sqlalchemy import DateTime, ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin


class ChildDocument(IdMixin, Base):
    """Un documento que la guardería le pide a la familia (RequestedDocument en la app).

    El estado no se guarda: con archivo es CURRENT, sin archivo es MISSING.
    """

    __tablename__ = "child_document"

    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"))
    title: Mapped[str] = mapped_column(String(120))
    due_date: Mapped[date | None]
    media_id: Mapped[int | None] = mapped_column(ForeignKey("media.id"))
    uploaded_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class AuthorizedPickup(IdMixin, Base):
    """Una persona autorizada para recoger al niño (AuthorizedPerson en la app)."""

    __tablename__ = "authorized_pickup"

    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"))
    full_name: Mapped[str] = mapped_column(String(200))
    relationship: Mapped[str | None] = mapped_column(String(60))
    phone: Mapped[str | None] = mapped_column(Text)
    added_by: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"))
