from fastapi import APIRouter, Depends

from app.core.db import Db
from app.core.security import AnyUser, Identity, get_identity, load_current_user

from . import service
from .schemas import MeOut

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
