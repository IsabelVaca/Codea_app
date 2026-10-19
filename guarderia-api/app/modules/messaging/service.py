import uuid

from sqlalchemy import and_, func, or_, select
from sqlalchemy.dialects.postgresql import insert
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.errors import ApiError
from app.core.pagination import Page
from app.core.security import CurrentUser, get_child_in_scope
from app.modules.auth.models import AppUser
from app.modules.children.models import Child, ChildStatus
from app.modules.classrooms.centers import center_today, default_center, get_active_classroom
from app.modules.classrooms.models import Classroom
from app.modules.parents.models import ParentChildRel

from .models import Announcement, Conversation, ConversationRead, Message, ParentNote
from .schemas import (
    AnnouncementIn,
    AnnouncementOut,
    ConversationChild,
    ConversationGuardian,
    ConversationOut,
    MessageIn,
    MessageOut,
    NoteIn,
    NoteOut,
)


def _require_text(text: str) -> str:
    if not text or not text.strip():
        raise ApiError(422, "TEXT_REQUIRED", "El texto es obligatorio")
    return text.strip()


# --- conversaciones ---
# hay una conversación por niño; participan sus tutores y las docentes de su sala.


async def ensure_conversations(db: AsyncSession, child_ids: list[int]) -> dict[int, int]:
    """Crea las conversaciones que falten y regresa {child_id: conversation_id}."""
    if not child_ids:
        return {}
    await db.execute(
        insert(Conversation).values([{"child_id": child_id} for child_id in child_ids]).on_conflict_do_nothing()
    )
    await db.commit()
    rows = await db.execute(select(Conversation.child_id, Conversation.id).where(Conversation.child_id.in_(child_ids)))
    return dict(rows.all())


async def _unread_counts(db: AsyncSession, user_id: uuid.UUID, conversation_ids: list[int]) -> dict[int, int]:
    last_read = (
        select(ConversationRead.last_read_message_id)
        .where(ConversationRead.conversation_id == Message.conversation_id, ConversationRead.user_id == user_id)
        .scalar_subquery()
    )
    rows = await db.execute(
        select(Message.conversation_id, func.count())
        .where(
            Message.conversation_id.in_(conversation_ids),
            Message.author_id != user_id,
            Message.id > func.coalesce(last_read, 0),
        )
        .group_by(Message.conversation_id)
    )
    return dict(rows.all())


async def unread_by_child(db: AsyncSession, user_id: uuid.UUID, child_ids: list[int]) -> dict[int, tuple[int, int]]:
    """{child_id: (conversation_id, mensajes sin leer)} para Mi sala."""
    conversations = await ensure_conversations(db, child_ids)
    unread = await _unread_counts(db, user_id, list(conversations.values()))
    return {child_id: (conv_id, unread.get(conv_id, 0)) for child_id, conv_id in conversations.items()}


async def _children_in_chat_scope(db: AsyncSession, current: CurrentUser) -> list[Child]:
    children: dict[int, Child] = {}
    if current.teacher is not None and current.teacher.classroom_id is not None:
        for child in (
            await db.scalars(
                select(Child).where(
                    Child.classroom_id == current.teacher.classroom_id, Child.status == ChildStatus.ACTIVE
                )
            )
        ).all():
            children[child.id] = child
    if current.parent is not None:
        for child in (
            await db.scalars(
                select(Child)
                .join(ParentChildRel, ParentChildRel.child_id == Child.id)
                .where(ParentChildRel.parent_id == current.id)
            )
        ).all():
            children[child.id] = child
    return sorted(children.values(), key=lambda child: (child.name, child.surname))


async def _get_conversation(db: AsyncSession, current: CurrentUser, conversation_id: int) -> Conversation:
    conversation = await db.get(Conversation, conversation_id)
    if conversation is not None:
        child = await db.get(Child, conversation.child_id)
        is_teacher = (
            current.teacher is not None
            and child.classroom_id is not None
            and child.classroom_id == current.teacher.classroom_id
        )
        is_guardian = current.parent is not None and await db.scalar(
            select(ParentChildRel.id).where(
                ParentChildRel.parent_id == current.id, ParentChildRel.child_id == child.id
            )
        )
        if is_teacher or is_guardian:
            return conversation
    raise ApiError(404, "CONVERSATION_NOT_FOUND", "No existe esa conversación")


async def _read_marks(db: AsyncSession, conversation_id: int, user_id: uuid.UUID) -> tuple[int, int]:
    """(hasta dónde leí yo, hasta dónde leyó alguien más)."""
    rows = (
        await db.execute(
            select(ConversationRead.user_id, ConversationRead.last_read_message_id).where(
                ConversationRead.conversation_id == conversation_id
            )
        )
    ).all()
    mine = max((last or 0 for uid, last in rows if uid == user_id), default=0)
    others = max((last or 0 for uid, last in rows if uid != user_id), default=0)
    return mine, others


def _message_out(message: Message, author: AppUser, me: uuid.UUID, mine_read: int, others_read: int) -> MessageOut:
    is_mine = message.author_id == me
    return MessageOut(
        id=message.id,
        conversation_id=message.conversation_id,
        author_id=message.author_id,
        author_name=author.full_name,
        text=message.text,
        client_id=message.client_id,
        created_at=message.created_at,
        is_mine=is_mine,
        is_read=(others_read if is_mine else mine_read) >= message.id,
    )


async def list_conversations(db: AsyncSession, current: CurrentUser) -> list[ConversationOut]:
    children = await _children_in_chat_scope(db, current)
    conversations = await ensure_conversations(db, [child.id for child in children])
    conversation_ids = list(conversations.values())
    unread = await _unread_counts(db, current.id, conversation_ids)

    last_ids = select(func.max(Message.id)).where(Message.conversation_id.in_(conversation_ids)).group_by(
        Message.conversation_id
    )
    last_messages = {
        message.conversation_id: (message, author)
        for message, author in (
            await db.execute(
                select(Message, AppUser).join(AppUser, AppUser.id == Message.author_id).where(Message.id.in_(last_ids))
            )
        ).all()
    }

    guardians: dict[int, list[ConversationGuardian]] = {}
    if children:
        for link, user in (
            await db.execute(
                select(ParentChildRel, AppUser)
                .join(AppUser, AppUser.id == ParentChildRel.parent_id)
                .where(ParentChildRel.child_id.in_([child.id for child in children]))
                .order_by(ParentChildRel.is_primary.desc(), ParentChildRel.id)
            )
        ).all():
            guardians.setdefault(link.child_id, []).append(
                ConversationGuardian(full_name=user.full_name, relationship=link.relationship, is_primary=link.is_primary)
            )

    result = []
    for child in children:
        conversation_id = conversations[child.id]
        last = None
        if conversation_id in last_messages:
            message, author = last_messages[conversation_id]
            mine_read, others_read = await _read_marks(db, conversation_id, current.id)
            last = _message_out(message, author, current.id, mine_read, others_read)
        result.append(
            ConversationOut(
                id=conversation_id,
                child=ConversationChild(
                    id=child.id,
                    full_name=child.full_name,
                    classroom_id=child.classroom_id,
                    guardians=guardians.get(child.id, []),
                ),
                unread_count=unread.get(conversation_id, 0),
                last_message=last,
            )
        )
    return result


async def list_messages(
    db: AsyncSession,
    current: CurrentUser,
    conversation_id: int,
    cursor: int | None,
    after: int | None,
    limit: int,
) -> Page[MessageOut]:
    """Sin after: del más nuevo al más viejo, por páginas. Con after: solo los nuevos, en orden."""
    await _get_conversation(db, current, conversation_id)
    query = (
        select(Message, AppUser)
        .join(AppUser, AppUser.id == Message.author_id)
        .where(Message.conversation_id == conversation_id)
    )
    if after is not None:
        rows = (await db.execute(query.where(Message.id > after).order_by(Message.id).limit(limit))).all()
        next_cursor = None
    else:
        if cursor is not None:
            query = query.where(Message.id < cursor)
        rows = (await db.execute(query.order_by(Message.id.desc()).limit(limit + 1))).all()
        next_cursor = str(rows[limit - 1][0].id) if len(rows) > limit else None
        rows = rows[:limit]
    mine_read, others_read = await _read_marks(db, conversation_id, current.id)
    return Page(
        items=[_message_out(message, author, current.id, mine_read, others_read) for message, author in rows],
        next_cursor=next_cursor,
    )


async def _mark_read_up_to(db: AsyncSession, conversation_id: int, user_id: uuid.UUID, message_id: int) -> None:
    statement = insert(ConversationRead).values(
        conversation_id=conversation_id, user_id=user_id, last_read_message_id=message_id
    )
    await db.execute(
        statement.on_conflict_do_update(
            index_elements=["conversation_id", "user_id"],
            set_={
                "last_read_message_id": func.greatest(
                    func.coalesce(ConversationRead.last_read_message_id, 0), message_id
                ),
                "updated_at": func.now(),
            },
        )
    )


async def send_message(
    db: AsyncSession, current: CurrentUser, conversation_id: int, data: MessageIn
) -> tuple[MessageOut, bool]:
    """Regresa (mensaje, creado). Si el client_id ya existe, regresa el mensaje que ya estaba."""
    await _get_conversation(db, current, conversation_id)
    text = _require_text(data.text)
    message = None
    if data.client_id is not None:
        message = await db.scalar(
            select(Message).where(Message.author_id == current.id, Message.client_id == data.client_id)
        )
    created = message is None
    if created:
        message = Message(conversation_id=conversation_id, author_id=current.id, text=text, client_id=data.client_id)
        db.add(message)
        await db.flush()
        await _mark_read_up_to(db, conversation_id, current.id, message.id)
        await db.commit()
        await db.refresh(message)
    mine_read, others_read = await _read_marks(db, conversation_id, current.id)
    return _message_out(message, current.user, current.id, mine_read, others_read), created


async def mark_read(db: AsyncSession, current: CurrentUser, conversation_id: int) -> None:
    await _get_conversation(db, current, conversation_id)
    last_id = await db.scalar(select(func.max(Message.id)).where(Message.conversation_id == conversation_id))
    if last_id is not None:
        await _mark_read_up_to(db, conversation_id, current.id, last_id)
        await db.commit()


# --- avisos y notas ---


async def create_announcement(db: AsyncSession, current: CurrentUser, data: AnnouncementIn) -> AnnouncementOut:
    text = _require_text(data.text)
    if data.classroom_id is not None:
        await get_active_classroom(db, data.classroom_id)
    announcement = Announcement(
        daycare_center_id=current.admin.daycare_center_id,
        classroom_id=data.classroom_id,
        text=text,
        author_id=current.id,
    )
    db.add(announcement)
    await db.commit()
    await db.refresh(announcement)
    return (await _announcements_out(db, [announcement]))[0]


async def _announcements_out(db: AsyncSession, rows: list[Announcement]) -> list[AnnouncementOut]:
    """Los avisos con el nombre de su sala."""
    room_ids = {row.classroom_id for row in rows if row.classroom_id is not None}
    names = dict((await db.execute(select(Classroom.id, Classroom.name).where(Classroom.id.in_(room_ids)))).all()) if room_ids else {}
    return [
        AnnouncementOut(
            id=row.id,
            text=row.text,
            classroom_id=row.classroom_id,
            classroom_name=names.get(row.classroom_id),
            created_at=row.created_at,
        )
        for row in rows
    ]


async def list_announcements(db: AsyncSession, current: CurrentUser, limit: int) -> list[AnnouncementOut]:
    query = select(Announcement).order_by(Announcement.created_at.desc(), Announcement.id.desc()).limit(limit)
    if current.admin is not None:
        query = query.where(Announcement.daycare_center_id == current.admin.daycare_center_id)
    else:
        # docente y papá ven los avisos de todo el centro y los de su sala (o las de sus hijos).
        room_ids: set[int] = set()
        center_ids: set[int] = set()
        if current.teacher is not None and current.teacher.classroom_id is not None:
            room_ids.add(current.teacher.classroom_id)
        if current.parent is not None:
            child_rooms = await db.scalars(
                select(Child.classroom_id)
                .join(ParentChildRel, ParentChildRel.child_id == Child.id)
                .where(ParentChildRel.parent_id == current.id, Child.classroom_id.is_not(None))
            )
            room_ids.update(child_rooms.all())
        # el centro sale de la sala; sin sala (docente nueva) se usa el único centro que hay.
        if room_ids:
            center_ids.update((await db.scalars(select(Classroom.daycare_center_id).where(Classroom.id.in_(room_ids)))).all())
        if not center_ids:
            center_ids.add((await default_center(db)).id)
        query = query.where(
            or_(
                and_(Announcement.classroom_id.is_(None), Announcement.daycare_center_id.in_(center_ids)),
                Announcement.classroom_id.in_(room_ids),
            )
        )
    return await _announcements_out(db, list((await db.scalars(query)).all()))


async def create_note(db: AsyncSession, current: CurrentUser, child_id: int, data: NoteIn) -> NoteOut:
    child = await get_child_in_scope(db, current, child_id)
    note = ParentNote(child_id=child.id, date=await center_today(db), text=_require_text(data.text), author_id=current.id)
    db.add(note)
    await db.commit()
    await db.refresh(note)
    return NoteOut.model_validate(note)


