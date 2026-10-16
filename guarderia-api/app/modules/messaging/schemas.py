import uuid
from datetime import date, datetime

from pydantic import BaseModel

from app.core.schemas import Schema


class AnnouncementIn(BaseModel):
    text: str
    # sin sala, el aviso es para todo el centro.
    classroom_id: int | None = None


class AnnouncementOut(Schema):
    id: int
    text: str
    classroom_id: int | None
    # el nombre de la sala (null = aviso para todo el centro).
    classroom_name: str | None = None
    created_at: datetime


class MessageIn(BaseModel):
    text: str
    # un UUID que genera la app; si reintenta con el mismo, no se duplica el mensaje.
    client_id: uuid.UUID | None = None


class MessageOut(BaseModel):
    """Un mensaje del chat (ChatMessage en la app)."""

    id: int
    conversation_id: int
    author_id: uuid.UUID
    author_name: str
    text: str
    client_id: uuid.UUID | None
    created_at: datetime
    is_mine: bool
    # para los míos: si ya lo leyó alguien más. Para los de otros: si ya lo leí yo.
    is_read: bool


class ConversationChild(BaseModel):
    id: int
    full_name: str
    classroom_id: int | None


class ConversationOut(BaseModel):
    id: int
    child: ConversationChild
    unread_count: int
    last_message: MessageOut | None


class NoteIn(BaseModel):
    text: str


class NoteOut(Schema):
    id: int
    child_id: int
    date: date
    text: str
    created_at: datetime
