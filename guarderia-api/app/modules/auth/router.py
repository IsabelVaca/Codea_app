import uuid

from fastapi import APIRouter, Request, Response, status

from app.core.db import Db
from app.core.schemas import AccessCodeOut
from app.core.security import AdminUser, AnyUser

from . import service
from .models import Role
from .schemas import (
    ActivateIn,
    ChangePasswordIn,
    InvitationCreate,
    InvitationOut,
    InvitationStatus,
    LoginIn,
    MeOut,
    RefreshIn,
    SessionsOut,
    TokenOut,
)

router = APIRouter(tags=["cuenta"])


def _agent(request: Request) -> str | None:
    return request.headers.get("user-agent")


def _ip(request: Request) -> str | None:
    # detrás de un proxy hay que configurarlo para que esta sea la IP real (uvicorn --proxy-headers).
    return request.client.host if request.client else None


@router.post("/auth/login", response_model=TokenOut)
async def login(db: Db, request: Request, data: LoginIn) -> TokenOut:
    """Correo y contraseña. 401 INVALID_CREDENTIALS (el mismo para correo desconocido y contraseña mal);
    403 ACCOUNT_NOT_ACTIVATED o USER_INACTIVE; 429 TOO_MANY_ATTEMPTS tras varios fallos seguidos (trae retry_after_s)."""
    return await service.login(db, data, _agent(request), _ip(request))


@router.post("/auth/activate", response_model=TokenOut)
async def activate(db: Db, request: Request, data: ActivateIn) -> TokenOut:
    """Primer acceso: correo + código del administrador + contraseña nueva.
    403 CODE_INVALID, CODE_EXPIRED o USER_INACTIVE; 422 PASSWORD_TOO_SHORT; 429 TOO_MANY_ATTEMPTS."""
    return await service.activate(db, data, _agent(request), _ip(request))


@router.post("/auth/refresh", response_model=TokenOut)
async def refresh(db: Db, request: Request, data: RefreshIn) -> TokenOut:
    """Cambia un refresh token por un par nuevo (el viejo deja de servir). 401 REFRESH_INVALID."""
    return await service.refresh(db, data.refresh_token, _agent(request))


@router.post("/auth/logout", status_code=status.HTTP_204_NO_CONTENT)
async def logout(db: Db, data: RefreshIn) -> Response:
    """Cierra la sesión: revoca el refresh token. No falla si ya no servía."""
    await service.logout(db, data.refresh_token)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.get("/me", response_model=MeOut)
async def me(db: Db, current: AnyUser) -> MeOut:
    """El perfil de quien inició sesión: su rol, y su sala o sus hijos. Si es docente, también su turno y el centro para el checador."""
    return await service.build_me(db, current)


@router.post("/me/password", response_model=TokenOut)
async def change_password(db: Db, request: Request, current: AnyUser, data: ChangePasswordIn) -> TokenOut:
    """Cambia la contraseña y cierra todas las sesiones; regresa tokens nuevos para esta. 403 PASSWORD_INCORRECT."""
    return await service.change_password(db, current, data, _agent(request))


@router.delete("/me/sessions", response_model=SessionsOut)
async def close_sessions(db: Db, current: AnyUser) -> SessionsOut:
    """Cierra todas mis sesiones, en todos los dispositivos."""
    return await service.close_sessions(db, current)


@router.post("/users/{user_id}/access-code", response_model=AccessCodeOut, status_code=status.HTTP_201_CREATED)
async def access_code(db: Db, request: Request, current: AdminUser, user_id: uuid.UUID) -> AccessCodeOut:
    """Genera un código: INVITATION si la persona nunca entró, PASSWORD_RESET si ya entró (olvidó su contraseña)."""
    return await service.issue_access_code(db, user_id, current.user.id, _ip(request))


@router.post("/invitations", response_model=InvitationOut, status_code=status.HTTP_201_CREATED)
async def create_invitation(db: Db, current: AdminUser, data: InvitationCreate) -> InvitationOut:
    """Invita a una persona nueva con el rol que se le dé; regresa su código de acceso una sola vez.
    409 EMAIL_TAKEN o ALREADY_HAS_ACCOUNT."""
    return await service.invite(db, current, data)


@router.get("/invitations", response_model=list[InvitationOut])
async def list_invitations(
    db: Db, _: AdminUser, status: InvitationStatus | None = None, role: Role | None = None
) -> list[InvitationOut]:
    """Los códigos con su estado: PENDING, ACCEPTED o EXPIRED. Filtra con ?status= y ?role=."""
    return await service.list_invitations(db, status, role)
