import logging
import uuid
from datetime import timedelta

from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock, rules, tokens
from app.core.config import settings
from app.core.errors import ApiError
from app.core.passwords import hash_password_async, verify_password_async
from app.core.schemas import AccessCodeOut, ClassroomRef
from app.core.security import CurrentUser, load_current_user, revoke_all_sessions
from app.modules.admins.models import Admin
from app.modules.children.models import Child
from app.modules.classrooms.centers import center_of_classroom
from app.modules.classrooms.models import Classroom, DaycareCenter
from app.modules.classrooms.schemas import CenterOut
from app.modules.parents.models import Parent, ParentChildRel
from app.modules.teachers.models import Shift, Teacher
from app.modules.teachers.schemas import ShiftOut

from app.modules.audit import service as audit

from . import throttle
from .models import AppUser, CodePurpose, Invitation, RefreshToken, Role, UserStatus
from .schemas import (
    ActivateIn,
    ChangePasswordIn,
    InvitationCreate,
    InvitationOut,
    InvitationStatus,
    LoginIn,
    MeAdmin,
    MeChild,
    MeOut,
    MeParent,
    MeTeacher,
    SessionsOut,
    TokenOut,
)

log = logging.getLogger(__name__)


# ---------------------------------------------------------------- códigos de acceso


async def _expire_pending(db: AsyncSession, user_id: uuid.UUID) -> None:
    """Un código nuevo deja sin vigencia a los anteriores que no se hayan usado."""
    await db.execute(
        update(Invitation)
        .where(Invitation.user_id == user_id, Invitation.accepted_at.is_(None), Invitation.expires_at > clock.now())
        .values(expires_at=clock.now())
    )


async def create_invitation(
    db: AsyncSession, user: AppUser, role: Role, purpose: CodePurpose = CodePurpose.INVITATION
) -> tuple[Invitation, AccessCodeOut] | None:
    """Crea un código de acceso. Para una invitación, quien ya activó su cuenta no lo necesita (regresa None).
    El código en claro solo existe en este regreso: en la base queda su hash."""
    if not user.email:
        return None
    if purpose == CodePurpose.INVITATION and user.password_hash is not None:
        return None
    await _expire_pending(db, user.id)
    code = tokens.new_access_code()
    invitation = Invitation(
        user_id=user.id,
        email=user.email,
        role=role,
        code_hash=tokens.hash_code(code),
        purpose=purpose,
        expires_at=clock.now() + timedelta(days=settings.invitation_days),
    )
    db.add(invitation)
    await db.flush()
    return invitation, AccessCodeOut(code=code, purpose=purpose.value, expires_at=invitation.expires_at)


async def renew_invitation(db: AsyncSession, user: AppUser, role: Role) -> AccessCodeOut:
    """Reenviar = generar un código nuevo para quien todavía no activa su cuenta."""
    if user.password_hash is not None:
        raise ApiError(409, "INVITATION_ALREADY_ACCEPTED", "Esta persona ya entró a la app")
    created = await create_invitation(db, user, role)
    if created is None:
        raise ApiError(422, "EMAIL_INVALID", "Esta persona no tiene correo")
    return created[1]


async def issue_access_code(
    db: AsyncSession, user_id: uuid.UUID, admin_id: uuid.UUID | None = None, ip: str | None = None
) -> AccessCodeOut:
    """El administrador genera un código: de invitación si la persona nunca entró, y si ya entró
    sirve para poner una contraseña nueva (olvidó la suya)."""
    user = await db.get(AppUser, user_id)
    if user is None:
        raise ApiError(404, "USER_NOT_FOUND", "No existe esa persona")
    if user.status != UserStatus.ACTIVE:
        raise ApiError(409, "USER_INACTIVE", "Esa persona está dada de baja")
    roles = (await load_current_user(db, user)).roles
    if not roles:
        raise ApiError(404, "USER_NOT_FOUND", "No existe esa persona")
    purpose = CodePurpose.INVITATION if user.password_hash is None else CodePurpose.PASSWORD_RESET
    created = await create_invitation(db, user, roles[0], purpose)
    if created is None:
        raise ApiError(422, "EMAIL_INVALID", "Esta persona no tiene correo")
    audit.record(
        db, audit.ACCESS_CODE_ISSUED, actor_id=admin_id, entity_type="USER", entity_id=user.id,
        detail={"purpose": purpose.value}, ip=ip,
    )
    await db.commit()
    return created[1]


# ---------------------------------------------------------------- sesión


async def _issue_tokens(
    db: AsyncSession,
    user: AppUser,
    *,
    family_id: uuid.UUID | None = None,
    user_agent: str | None = None,
    with_me: bool = True,
) -> tuple[TokenOut, RefreshToken]:
    """Crea el par de tokens. El refresh token se guarda solo como hash."""
    raw = tokens.new_refresh_token()
    row = RefreshToken(
        user_id=user.id,
        token_hash=tokens.hash_token(raw),
        family_id=family_id or uuid.uuid4(),
        expires_at=clock.now() + timedelta(seconds=settings.refresh_ttl_s),
        user_agent=(user_agent or "")[:300] or None,
    )
    db.add(row)
    await db.flush()
    me = await build_me(db, await load_current_user(db, user)) if with_me else None
    out = TokenOut(
        access_token=tokens.create_access_token(user.id, user.token_version),
        expires_in=settings.access_ttl_s,
        refresh_token=raw,
        refresh_expires_in=settings.refresh_ttl_s,
        me=me,
    )
    return out, row


async def login(db: AsyncSession, data: LoginIn, user_agent: str | None, ip: str | None = None) -> TokenOut:
    email = data.email.strip().lower()
    # primero el límite de intentos: con el correo bloqueado ni siquiera se revisa la contraseña.
    await throttle.check(db, throttle.LOGIN, email, ip)
    user = await db.scalar(select(AppUser).where(AppUser.email == email))
    # siempre se verifica contra algún hash (el falso si no hay cuenta): tarda igual y no delata qué correos existen.
    valid = await verify_password_async(data.password, user.password_hash if user else None)
    if user is None or (user.password_hash is not None and not valid):
        locked = await throttle.fail(db, throttle.LOGIN, email, ip)
        actor_id = user.id if user else None
        audit.record(db, audit.LOGIN_FAILED, actor_id=actor_id, actor_email=email, ip=ip)
        if locked:
            audit.record(db, audit.LOGIN_LOCKED, actor_id=actor_id, actor_email=email, ip=ip)
        await db.commit()
        raise ApiError(401, "INVALID_CREDENTIALS", "Correo o contraseña incorrectos")
    if user.password_hash is None:
        raise ApiError(403, "ACCOUNT_NOT_ACTIVATED", "Aún no activas tu cuenta; usa el código que te dio la dirección")
    if user.status != UserStatus.ACTIVE:
        raise ApiError(403, "USER_INACTIVE", "Tu acceso a la app está desactivado")
    user.last_login_at = clock.now()
    await throttle.clear(db, throttle.LOGIN, email)
    audit.record(db, audit.LOGIN_OK, actor_id=user.id, actor_email=email, ip=ip)
    out, _ = await _issue_tokens(db, user, user_agent=user_agent)
    await db.commit()
    return out


async def activate(db: AsyncSession, data: ActivateIn, user_agent: str | None, ip: str | None = None) -> TokenOut:
    """Primer acceso (o contraseña nueva): el código del administrador abre la cuenta y fija la contraseña."""
    email = data.email.strip().lower()
    rules.check_password(data.password, email)
    # el código es corto, así que también se limitan los intentos para que no se pueda adivinar.
    await throttle.check(db, throttle.ACTIVATE, email, ip)
    code_hash = tokens.hash_code(data.code)
    pending = (
        await db.scalars(
            select(Invitation)
            .where(Invitation.email == email, Invitation.accepted_at.is_(None))
            .order_by(Invitation.id.desc())
        )
    ).all()
    match = next((invitation for invitation in pending if invitation.code_hash == code_hash), None)
    if match is None:
        locked = await throttle.fail(db, throttle.ACTIVATE, email, ip)
        audit.record(db, audit.ACTIVATION_FAILED, actor_email=email, ip=ip, detail={"locked": locked})
        await db.commit()
        raise ApiError(403, "CODE_INVALID", "El código no es válido")
    if match.expires_at <= clock.now():
        raise ApiError(403, "CODE_EXPIRED", "El código ya venció; pide uno nuevo en la dirección")
    user = await db.get(AppUser, match.user_id)
    if user.status != UserStatus.ACTIVE:
        raise ApiError(403, "USER_INACTIVE", "Tu acceso a la app está desactivado")

    now = clock.now()
    for invitation in pending:
        if invitation.user_id == user.id:
            invitation.accepted_at = now
    user.password_hash = await hash_password_async(data.password)
    user.password_changed_at = now
    user.last_login_at = now
    if match.purpose == CodePurpose.PASSWORD_RESET:
        # con una contraseña nueva, las sesiones anteriores ya no deben servir.
        await revoke_all_sessions(db, user)
    await throttle.clear(db, throttle.ACTIVATE, email)
    audit.record(
        db, audit.ACCOUNT_ACTIVATED, actor_id=user.id, actor_email=email, ip=ip,
        detail={"purpose": match.purpose.value},
    )
    out, _ = await _issue_tokens(db, user, user_agent=user_agent)
    await db.commit()
    return out


async def refresh(db: AsyncSession, raw: str, user_agent: str | None) -> TokenOut:
    """Cambia un refresh token por un par nuevo. El viejo queda revocado y apunta al nuevo (rotación)."""
    row = await db.scalar(
        select(RefreshToken).where(RefreshToken.token_hash == tokens.hash_token(raw)).with_for_update()
    )
    if row is None:
        raise ApiError(401, "REFRESH_INVALID", "La sesión ya no es válida; vuelve a entrar")
    now = clock.now()
    if row.revoked_at is not None:
        if row.replaced_by_id is not None:
            # ya se había rotado: alguien lo copió. Se cierra toda la familia.
            await db.execute(
                update(RefreshToken)
                .where(RefreshToken.family_id == row.family_id, RefreshToken.revoked_at.is_(None))
                .values(revoked_at=now)
            )
            audit.record(db, audit.TOKEN_REUSE_DETECTED, actor_id=row.user_id, entity_type="SESSION")
            await db.commit()
        raise ApiError(401, "REFRESH_INVALID", "La sesión ya no es válida; vuelve a entrar")
    user = await db.get(AppUser, row.user_id)
    if row.expires_at <= now or user is None or user.status != UserStatus.ACTIVE:
        raise ApiError(401, "REFRESH_INVALID", "La sesión ya no es válida; vuelve a entrar")

    out, new_row = await _issue_tokens(
        db, user, family_id=row.family_id, user_agent=user_agent or row.user_agent, with_me=False
    )
    row.revoked_at = now
    row.last_used_at = now
    row.replaced_by_id = new_row.id
    await db.commit()
    return out


async def logout(db: AsyncSession, raw: str) -> None:
    """Revoca el refresh token. No falla si ya no servía."""
    row = await db.scalar(select(RefreshToken).where(RefreshToken.token_hash == tokens.hash_token(raw)))
    if row is not None and row.revoked_at is None:
        row.revoked_at = clock.now()
        await db.commit()


async def change_password(
    db: AsyncSession, current: CurrentUser, data: ChangePasswordIn, user_agent: str | None
) -> TokenOut:
    """Cambia la contraseña y cierra todas las sesiones (también esta); regresa tokens nuevos para que
    quien la cambió siga dentro."""
    user = current.user
    if not await verify_password_async(data.current_password, user.password_hash):
        raise ApiError(403, "PASSWORD_INCORRECT", "La contraseña actual no es correcta")
    rules.check_password(data.new_password, user.email)
    user.password_hash = await hash_password_async(data.new_password)
    user.password_changed_at = clock.now()
    await revoke_all_sessions(db, user)
    audit.record(db, audit.PASSWORD_CHANGED, actor_id=user.id, entity_type="USER", entity_id=user.id)
    out, _ = await _issue_tokens(db, user, user_agent=user_agent)
    await db.commit()
    return out


async def close_sessions(db: AsyncSession, current: CurrentUser) -> SessionsOut:
    revoked = await revoke_all_sessions(db, current.user)
    audit.record(
        db, audit.SESSIONS_CLOSED, actor_id=current.user.id, entity_type="USER", entity_id=current.user.id,
        detail={"revoked": revoked},
    )
    await db.commit()
    return SessionsOut(revoked=revoked)


# ---------------------------------------------------------------- perfil e invitaciones


async def build_me(db: AsyncSession, current: CurrentUser) -> MeOut:
    user = current.user
    admin = teacher = parent = None

    if current.admin is not None:
        center = await db.get(DaycareCenter, current.admin.daycare_center_id)
        admin = MeAdmin(center=CenterOut.model_validate(center))

    if current.teacher is not None:
        classroom = (
            await db.get(Classroom, current.teacher.classroom_id) if current.teacher.classroom_id else None
        )
        shift = await db.get(Shift, current.teacher.shift_id) if current.teacher.shift_id else None
        center = await center_of_classroom(db, current.teacher.classroom_id)
        teacher = MeTeacher(
            classroom=ClassroomRef.model_validate(classroom) if classroom else None,
            is_primary=current.teacher.is_primary,
            shift=ShiftOut.model_validate(shift) if shift else None,
            center=CenterOut.model_validate(center),
        )

    if current.parent is not None:
        rows = (
            await db.execute(
                select(Child, ParentChildRel.relationship, Classroom)
                .join(ParentChildRel, ParentChildRel.child_id == Child.id)
                .outerjoin(Classroom, Classroom.id == Child.classroom_id)
                .where(ParentChildRel.parent_id == user.id)
                .order_by(Child.name)
            )
        ).all()
        parent = MeParent(
            children=[
                MeChild(
                    id=child.id,
                    name=child.name,
                    surname=child.surname,
                    full_name=child.full_name,
                    classroom=ClassroomRef.model_validate(classroom) if classroom else None,
                    relationship=relationship,
                )
                for child, relationship, classroom in rows
            ]
        )

    return MeOut(
        id=user.id,
        name=user.name,
        surname=user.surname,
        full_name=user.full_name,
        email=user.email,
        phone=user.phone,
        roles=current.roles,
        admin=admin,
        teacher=teacher,
        parent=parent,
    )


def _invitation_status(invitation: Invitation) -> InvitationStatus:
    if invitation.accepted_at is not None:
        return InvitationStatus.ACCEPTED
    if invitation.expires_at <= clock.now():
        return InvitationStatus.EXPIRED
    return InvitationStatus.PENDING


def _invitation_out(invitation: Invitation, user: AppUser) -> InvitationOut:
    return InvitationOut(
        id=invitation.id,
        user_id=user.id,
        name=user.name,
        surname=user.surname,
        full_name=user.full_name,
        email=invitation.email,
        role=invitation.role,
        status=_invitation_status(invitation),
        expires_at=invitation.expires_at,
        accepted_at=invitation.accepted_at,
        created_at=invitation.created_at,
    )


async def invite(db: AsyncSession, current: CurrentUser, data: InvitationCreate) -> InvitationOut:
    """Invita a una persona con cualquier rol. Crea su usuario, su registro de rol y su invitación."""
    name = rules.require_name(data.name)
    phone = rules.check_phone(data.phone)
    email = rules.check_email(data.email)

    user = await db.scalar(select(AppUser).where(AppUser.email == email))
    role_model = {Role.ADMIN: Admin, Role.TEACHER: Teacher, Role.PARENT: Parent}[data.role]
    if user is not None:
        if await db.get(role_model, user.id) is not None:
            raise ApiError(409, "EMAIL_TAKEN", "Ya hay una persona con ese rol y ese correo")
        if user.password_hash is not None:
            # ya tiene cuenta, así que no hay a quién invitar; el rol extra se da desde su ficha.
            raise ApiError(409, "ALREADY_HAS_ACCOUNT", "Esa persona ya entró a la app")
    else:
        user = AppUser(name=name, surname=data.surname.strip(), phone=phone, email=email)
        db.add(user)
        await db.flush()

    if data.role == Role.ADMIN:
        db.add(Admin(user_id=user.id, daycare_center_id=current.admin.daycare_center_id))
    else:
        db.add(role_model(user_id=user.id))
    invitation, access_code = await create_invitation(db, user, data.role)
    audit.record(
        db, audit.USER_INVITED, actor_id=current.user.id, entity_type="USER", entity_id=user.id,
        detail={"role": data.role.value},
    )
    await db.commit()
    await db.refresh(invitation)
    out = _invitation_out(invitation, user)
    out.access_code = access_code
    return out


async def list_invitations(
    db: AsyncSession, status: InvitationStatus | None = None, role: Role | None = None
) -> list[InvitationOut]:
    """Las invitaciones, de la más nueva a la más vieja. Se puede filtrar por estado y por rol."""
    query = select(Invitation, AppUser).join(AppUser, AppUser.id == Invitation.user_id)
    if role is not None:
        query = query.where(Invitation.role == role)
    rows = (await db.execute(query.order_by(Invitation.id.desc()))).all()
    items = [_invitation_out(invitation, user) for invitation, user in rows]
    if status is not None:
        items = [item for item in items if item.status == status]
    return items
