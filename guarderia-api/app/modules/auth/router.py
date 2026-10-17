from fastapi import APIRouter, Depends, status

from app.core.db import Db
from app.core.security import AdminUser, AnyUser, Identity, get_identity, load_current_user

from . import service
from .models import Role
from .schemas import InvitationCreate, InvitationOut, InvitationStatus, MeOut

router = APIRouter(tags=["cuenta"])


@router.get("/me", response_model=MeOut)
async def me(db: Db, current: AnyUser) -> MeOut:
    """El perfil de quien inició sesión: su rol, y su sala o sus hijos. Si es docente, también su turno y el centro para el checador."""
    return await service.build_me(db, current)


@router.post("/auth/accept-invitation", response_model=MeOut)
async def accept_invitation(db: Db, identity: Identity = Depends(get_identity)) -> MeOut:
    """La primera vez que alguien entra con Google, liga su cuenta con la invitación del administrador."""
    user = await service.accept_invitation(db, identity)
    return await service.build_me(db, await load_current_user(db, user))


@router.post("/invitations", response_model=InvitationOut, status_code=status.HTTP_201_CREATED)
async def create_invitation(db: Db, current: AdminUser, data: InvitationCreate) -> InvitationOut:
    """Invita a una persona nueva con el rol que se le dé. 409 EMAIL_TAKEN o ALREADY_HAS_ACCOUNT."""
    return await service.invite(db, current, data)


@router.get("/invitations", response_model=list[InvitationOut])
async def list_invitations(
    db: Db, _: AdminUser, status: InvitationStatus | None = None, role: Role | None = None
) -> list[InvitationOut]:
    """Las invitaciones con su estado: PENDING, ACCEPTED o EXPIRED. Filtra con ?status= y ?role=."""
    return await service.list_invitations(db, status, role)
