import uuid
from enum import StrEnum

from sqlalchemy import ForeignKey, UniqueConstraint, text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base, IdMixin, TimestampMixin, str_enum


class Relationship(StrEnum):
    """Parentesco con el niño. Los textos (Mamá, Papá, Tutor legal…) viven en la app."""

    MOTHER = "MOTHER"
    FATHER = "FATHER"
    LEGAL_GUARDIAN = "LEGAL_GUARDIAN"
    OTHER = "OTHER"


class Parent(TimestampMixin, Base):
    __tablename__ = "parent"

    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"), primary_key=True)


class ParentChildRel(IdMixin, Base):
    __tablename__ = "parent_child_rel"
    __table_args__ = (UniqueConstraint("parent_id", "child_id"),)

    parent_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("parent.user_id"))
    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"))
    relationship: Mapped[Relationship] = mapped_column(
        str_enum(Relationship), default=Relationship.OTHER, server_default=Relationship.OTHER.value
    )
    # el tutor que se registró al inscribir al niño.
    is_primary: Mapped[bool] = mapped_column(default=False, server_default=text("false"))
