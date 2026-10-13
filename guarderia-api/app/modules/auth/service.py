import logging
from datetime import timedelta

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core import clock
from app.core.config import settings
from app.core.errors import ApiError
from app.core.schemas import ClassroomRef
from app.core.security import CurrentUser, Identity
from app.modules.children.models import Child
from app.modules.classrooms.centers import center_of_classroom
from app.modules.classrooms.models import Classroom, DaycareCenter
from app.modules.classrooms.schemas import CenterOut
from app.modules.parents.models import ParentChildRel
from app.modules.teachers.models import Shift
from app.modules.teachers.schemas import ShiftOut

from .models import AppUser, Invitation, Role, UserStatus
from .schemas import MeAdmin, MeChild, MeOut, MeParent, MeTeacher

log = logging.getLogger(__name__)


async def create_invitation(db: AsyncSession, user: AppUser, role: Role) -> Invitation | None:
    """Crea la invitación para entrar a la app. Quien ya tiene cuenta no la necesita."""
    if user.firebase_uid is not None or not user.email:
        return None
    invitation = Invitation(
        user_id=user.id,
        email=user.email,
        role=role,
        expires_at=clock.now() + timedelta(days=settings.invitation_days),
    )
    db.add(invitation)
    # todavía no hay worker de correos: por ahora la invitación solo queda en el log.
    log.info("invitación %s para %s", role.value, user.email)
    return invitation


async def renew_invitation(db: AsyncSession, user: AppUser, role: Role) -> None:
    """Reenviar = renovar la vigencia de la invitación pendiente (o crear una nueva)."""
    if user.firebase_uid is not None:
        raise ApiError(409, "INVITATION_ALREADY_ACCEPTED", "Esta persona ya entró a la app")
    pending = (
        await db.scalars(
            select(Invitation).where(Invitation.user_id == user.id, Invitation.accepted_at.is_(None))
        )
    ).all()
    if not pending:
        await create_invitation(db, user, role)
        return
    for invitation in pending:
        invitation.expires_at = clock.now() + timedelta(days=settings.invitation_days)
    log.info("invitación reenviada a %s", user.email)


async def accept_invitation(db: AsyncSession, identity: Identity) -> AppUser:
    """Liga la cuenta de Google (Firebase) con la invitación que mandó el administrador."""
    user = await db.scalar(select(AppUser).where(AppUser.firebase_uid == identity.uid))
    if user is not None:
        return user
    if not identity.email:
        raise ApiError(403, "INVITATION_REQUIRED", "Tu cuenta no tiene acceso; pide tu invitación en la dirección")
    if not identity.email_verified:
        raise ApiError(403, "EMAIL_NOT_VERIFIED", "Tu correo todavía no está verificado")

    pending = (
        await db.scalars(
            select(Invitation)
            .where(Invitation.email == identity.email, Invitation.accepted_at.is_(None))
            .order_by(Invitation.id)
        )
    ).all()
    if not pending:
        raise ApiError(403, "INVITATION_REQUIRED", "Tu cuenta no tiene acceso; pide tu invitación en la dirección")
    now = clock.now()
    valid = [invitation for invitation in pending if invitation.expires_at > now]
    if not valid:
        raise ApiError(403, "INVITATION_EXPIRED", "Tu invitación ya venció; pide que te la reenvíen")

    user = await db.get(AppUser, valid[0].user_id)
    if user.status != UserStatus.ACTIVE:
        raise ApiError(403, "USER_INACTIVE", "Tu acceso a la app está desactivado")
    if user.firebase_uid is not None:
        raise ApiError(409, "ACCOUNT_ALREADY_LINKED", "Esta invitación ya está ligada a otra cuenta")
    user.firebase_uid = identity.uid
    for invitation in pending:
        if invitation.user_id == user.id:
            invitation.accepted_at = now
    await db.commit()
    return user


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
