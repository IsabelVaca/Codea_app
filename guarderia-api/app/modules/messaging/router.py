from fastapi import APIRouter, Query, Response, status

from app.core.db import Db
from app.core.pagination import Page, parse_cursor
from app.core.security import AdminUser, AnyUser, ParentUser

from . import service
from .schemas import AnnouncementIn, AnnouncementOut, ConversationOut, MessageIn, MessageOut, NoteIn, NoteOut

router = APIRouter(tags=["comunicación"])


@router.post("/announcements", response_model=AnnouncementOut, status_code=status.HTTP_201_CREATED)
async def create_announcement(db: Db, current: AdminUser, data: AnnouncementIn) -> AnnouncementOut:
    """Publica un aviso. Sin classroom_id es para todo el centro; con él, solo para esa sala."""
    return await service.create_announcement(db, current, data)


@router.get("/conversations", response_model=list[ConversationOut])
async def list_conversations(db: Db, current: AnyUser) -> list[ConversationOut]:
    """Las conversaciones (una por niño), cada una con su conteo de mensajes sin leer."""
    return await service.list_conversations(db, current)


@router.get("/conversations/{conversation_id}/messages", response_model=Page[MessageOut])
async def list_messages(
    db: Db,
    current: AnyUser,
    conversation_id: int,
    cursor: str | None = None,
    after: int | None = None,
    limit: int = Query(30, ge=1, le=100),
) -> Page[MessageOut]:
    """Los mensajes de una conversación, paginados. Con ?after=<id> trae solo los nuevos."""
    return await service.list_messages(db, current, conversation_id, parse_cursor(cursor), after, limit)


@router.post("/conversations/{conversation_id}/messages", response_model=MessageOut, status_code=status.HTTP_201_CREATED)
async def send_message(
    db: Db, current: AnyUser, conversation_id: int, data: MessageIn, response: Response
) -> MessageOut:
    """Envía un mensaje. Si el client_id ya se usó, regresa el mensaje existente con 200."""
    message, created = await service.send_message(db, current, conversation_id, data)
    if not created:
        response.status_code = status.HTTP_200_OK
    return message


@router.post("/conversations/{conversation_id}/read", status_code=status.HTTP_204_NO_CONTENT)
async def mark_read(db: Db, current: AnyUser, conversation_id: int) -> None:
    """Marca la conversación como leída."""
    await service.mark_read(db, current, conversation_id)


@router.post("/children/{child_id}/notes", response_model=NoteOut, status_code=status.HTTP_201_CREATED)
async def create_note(db: Db, current: ParentUser, child_id: int, data: NoteIn) -> NoteOut:
    """Manda una nota a la asistente para hoy."""
    return await service.create_note(db, current, child_id, data)
