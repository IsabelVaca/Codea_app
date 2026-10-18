import uuid

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import rules
from app.core.errors import ApiError
from app.core.schemas import AccessCodeOut
from app.core.security import revoke_all_sessions
from app.modules.auth import service as auth
from app.modules.auth.models import AppUser, Invitation, Role, UserStatus
from app.modules.children.models import Child

from .models import Parent, ParentChildRel, Relationship
from .schemas import ParentChild, ParentCreate, ParentOut


async def link_guardian(
    db: AsyncSession,
    child: Child,
    *,
    name: str,
    surname: str,
    phone: str,
    email: str,
    relationship: Relationship,
    is_primary: bool,
) -> tuple[AppUser, AccessCodeOut | None]:
    """Liga un tutor a un niño. Si ya existe alguien con ese correo, se reutiliza.
    Regresa también su código de acceso si se le creó uno (quien ya activó su cuenta no lo necesita)."""
    user = await db.scalar(select(AppUser).where(AppUser.email == email))
    if user is None:
        user = AppUser(name=name, surname=surname, phone=phone, email=email)
        db.add(user)
        await db.flush()
    if await db.get(Parent, user.id) is None:
        db.add(Parent(user_id=user.id))
        await db.flush()
    linked = await db.scalar(
        select(ParentChildRel).where(ParentChildRel.parent_id == user.id, ParentChildRel.child_id == child.id)
    )
    if linked is None:
        db.add(ParentChildRel(parent_id=user.id, child_id=child.id, relationship=relationship, is_primary=is_primary))
    access_code = None
    if user.password_hash is None and not await _has_pending_invitation(db, user.id):
        created = await auth.create_invitation(db, user, Role.PARENT)
        access_code = created[1] if created else None
    return user, access_code


async def _has_pending_invitation(db: AsyncSession, user_id: uuid.UUID) -> bool:
    return (
        await db.scalar(
            select(Invitation.id).where(Invitation.user_id == user_id, Invitation.accepted_at.is_(None)).limit(1)
        )
    ) is not None


async def _rows(db: AsyncSession, parent_id: uuid.UUID | None = None) -> list[ParentOut]:
    query = select(Parent, AppUser).join(AppUser, AppUser.id == Parent.user_id).order_by(AppUser.name, AppUser.surname)
    if parent_id is not None:
        query = query.where(Parent.user_id == parent_id)
    parents = (await db.execute(query)).all()

    links = (
        await db.execute(
            select(ParentChildRel, Child)
            .join(Child, Child.id == ParentChildRel.child_id)
            .where(ParentChildRel.parent_id.in_([parent.user_id for parent, _ in parents]))
            .order_by(ParentChildRel.is_primary.desc(), Child.name)
        )
    ).all()
    children: dict[uuid.UUID, list[tuple[ParentChildRel, Child]]] = {}
    for link, child in links:
        children.setdefault(link.parent_id, []).append((link, child))

    result = []
    for parent, user in parents:
        mine = children.get(parent.user_id, [])
        result.append(
            ParentOut(
                id=user.id,
                name=user.name,
                surname=user.surname,
                full_name=user.full_name,
                phone=user.phone,
                email=user.email,
                relationship=mine[0][0].relationship if mine else None,
                child_ids=[child.id for _, child in mine],
                children=[
                    ParentChild(id=child.id, full_name=child.full_name, relationship=link.relationship)
                    for link, child in mine
                ],
                has_access=user.status == UserStatus.ACTIVE,
                has_account=user.password_hash is not None,
            )
        )
    return result


async def list_parents(db: AsyncSession) -> list[ParentOut]:
    return await _rows(db)


async def get_parent(db: AsyncSession, parent_id: uuid.UUID) -> ParentOut:
    rows = await _rows(db, parent_id)
    if not rows:
        raise ApiError(404, "PARENT_NOT_FOUND", "No existe ese padre o tutor")
    return rows[0]


async def create_parent(db: AsyncSession, data: ParentCreate) -> ParentOut:
    """Da de alta a un padre, lo liga a sus hijos y le crea su invitación."""
    name = rules.require_name(data.name)
    phone = rules.check_phone(data.phone)
    email = rules.check_email(data.email)
    if not data.child_ids:
        raise ApiError(422, "CHILDREN_REQUIRED", "Elige al menos un hijo")
    children = (await db.scalars(select(Child).where(Child.id.in_(set(data.child_ids))))).all()
    if len(children) != len(set(data.child_ids)):
        raise ApiError(404, "CHILD_NOT_FOUND", "No existe ese niño")
    existing = await db.scalar(select(AppUser).where(AppUser.email == email))
    if existing is not None and await db.get(Parent, existing.id) is not None:
        raise ApiError(409, "EMAIL_TAKEN", "Ya hay un padre o tutor con ese correo")

    user = access_code = None
    for child in children:
        user, code = await link_guardian(
            db,
            child,
            name=name,
            surname=data.surname.strip(),
            phone=phone,
            email=email,
            relationship=data.relationship,
            is_primary=False,
        )
        access_code = access_code or code
    await db.commit()
    out = await get_parent(db, user.id)
    out.access_code = access_code
    return out


async def _get_user(db: AsyncSession, parent_id: uuid.UUID) -> AppUser:
    if await db.get(Parent, parent_id) is None:
        raise ApiError(404, "PARENT_NOT_FOUND", "No existe ese padre o tutor")
    return await db.get(AppUser, parent_id)


async def set_active(db: AsyncSession, parent_id: uuid.UUID, active: bool) -> ParentOut:
    user = await _get_user(db, parent_id)
    user.status = UserStatus.ACTIVE if active else UserStatus.INACTIVE
    if not active:
        await revoke_all_sessions(db, user)
    await db.commit()
    return await get_parent(db, parent_id)


async def resend_invitation(db: AsyncSession, parent_id: uuid.UUID) -> ParentOut:
    user = await _get_user(db, parent_id)
    access_code = await auth.renew_invitation(db, user, Role.PARENT)
    await db.commit()
    out = await get_parent(db, parent_id)
    out.access_code = access_code
    return out
