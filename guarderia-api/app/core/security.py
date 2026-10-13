import logging
import uuid
from dataclasses import dataclass
from typing import Annotated

from fastapi import Depends
from fastapi.concurrency import run_in_threadpool
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import select

from app.modules.admins.models import Admin
from app.modules.auth.models import AppUser, Role, UserStatus
from app.modules.children.models import Child
from app.modules.parents.models import Parent, ParentChildRel
from app.modules.teachers.models import Teacher

from .config import settings
from .db import Db
from .errors import ApiError

log = logging.getLogger(__name__)

# Firebase dice quién eres; la API dice qué puedes hacer.
# El token de Firebase solo prueba la identidad: el rol y lo que cada quien puede ver salen de Postgres.

bearer = HTTPBearer(auto_error=False, description="ID token de Firebase (o dev:<correo> en desarrollo)")


@dataclass(frozen=True)
class Identity:
    """Lo que dice el token: quién es, aunque todavía no tenga cuenta en la guardería."""

    uid: str
    email: str | None
    email_verified: bool


@dataclass(frozen=True)
class CurrentUser:
    """La persona que hace la petición, con sus roles ya cargados."""

    user: AppUser
    admin: Admin | None
    teacher: Teacher | None
    parent: Parent | None

    @property
    def id(self) -> uuid.UUID:
        return self.user.id

    @property
    def roles(self) -> list[Role]:
        roles = []
        if self.admin is not None:
            roles.append(Role.ADMIN)
        if self.teacher is not None:
            roles.append(Role.TEACHER)
        if self.parent is not None:
            roles.append(Role.PARENT)
        return roles


def _verify_firebase(token: str) -> dict:
    from firebase_admin import auth

    return auth.verify_id_token(token)


async def get_identity(credentials: HTTPAuthorizationCredentials | None = Depends(bearer)) -> Identity:
    if credentials is None:
        raise ApiError(401, "UNAUTHENTICATED", "Falta el token de acceso")
    token = credentials.credentials

    if settings.auth_mode == "dev":
        # solo en desarrollo: "dev:<correo>" se hace pasar por esa persona.
        if not token.startswith("dev:") or "@" not in token:
            raise ApiError(401, "TOKEN_INVALID", "El token no es válido", "En modo dev usa Bearer dev:<correo>")
        email = token.removeprefix("dev:").strip().lower()
        return Identity(uid=f"dev:{email}", email=email, email_verified=True)

    try:
        claims = await run_in_threadpool(_verify_firebase, token)
    except Exception:
        raise ApiError(401, "TOKEN_INVALID", "El token no es válido o ya expiró")
    email = claims.get("email")
    return Identity(
        uid=claims["uid"],
        email=email.lower() if email else None,
        email_verified=bool(claims.get("email_verified")),
    )


async def get_current_user(db: Db, identity: Identity = Depends(get_identity)) -> CurrentUser:
    user = await db.scalar(select(AppUser).where(AppUser.firebase_uid == identity.uid))
    if user is None:
        raise ApiError(403, "INVITATION_REQUIRED", "Tu cuenta no tiene acceso; pide tu invitación en la dirección")
    if user.status != UserStatus.ACTIVE:
        raise ApiError(403, "USER_INACTIVE", "Tu acceso a la app está desactivado")
    return await load_current_user(db, user)


async def load_current_user(db: Db, user: AppUser) -> CurrentUser:
    return CurrentUser(
        user=user,
        admin=await db.get(Admin, user.id),
        teacher=await db.get(Teacher, user.id),
        parent=await db.get(Parent, user.id),
    )


def require_role(*roles: Role):
    """Permiso por rol: qué endpoints puede llamar. El permiso por registro lo da el servicio."""

    async def dependency(current: CurrentUser = Depends(get_current_user)) -> CurrentUser:
        if not any(role in current.roles for role in roles):
            raise ApiError(403, "FORBIDDEN", "Tu rol no puede hacer esto")
        return current

    return dependency


AnyUser = Annotated[CurrentUser, Depends(get_current_user)]
AdminUser = Annotated[CurrentUser, Depends(require_role(Role.ADMIN))]
TeacherUser = Annotated[CurrentUser, Depends(require_role(Role.TEACHER))]
ParentUser = Annotated[CurrentUser, Depends(require_role(Role.PARENT))]
StaffUser = Annotated[CurrentUser, Depends(require_role(Role.ADMIN, Role.TEACHER))]
AdminOrParent = Annotated[CurrentUser, Depends(require_role(Role.ADMIN, Role.PARENT))]


# --- permiso por registro ---
# fuera del alcance responde 404, no 403: así no revela que el registro existe.


async def can_see_child(db: Db, current: CurrentUser, child: Child) -> bool:
    if current.admin is not None:
        return True
    if (
        current.teacher is not None
        and child.classroom_id is not None
        and child.classroom_id == current.teacher.classroom_id
    ):
        return True
    if current.parent is not None:
        linked = await db.scalar(
            select(ParentChildRel.id).where(
                ParentChildRel.parent_id == current.id, ParentChildRel.child_id == child.id
            )
        )
        return linked is not None
    return False


async def get_child_in_scope(db: Db, current: CurrentUser, child_id: int) -> Child:
    child = await db.get(Child, child_id)
    if child is None or not await can_see_child(db, current, child):
        raise ApiError(404, "CHILD_NOT_FOUND", "No existe ese niño")
    return child


def ensure_classroom_access(current: CurrentUser, classroom_id: int) -> None:
    """El administrador ve todas las salas; la docente solo la suya."""
    if current.admin is not None:
        return
    if current.teacher is not None and current.teacher.classroom_id == classroom_id:
        return
    raise ApiError(404, "CLASSROOM_NOT_FOUND", "No existe esa sala")


def revoke_sessions(firebase_uid: str | None) -> None:
    """Cierra las sesiones de Firebase de alguien dado de baja. Si no hay credenciales, solo lo anota."""
    if not firebase_uid or settings.auth_mode != "firebase" or not settings.firebase_credentials:
        return
    try:
        from firebase_admin import auth

        auth.revoke_refresh_tokens(firebase_uid)
    except Exception:
        log.exception("no se pudieron revocar las sesiones de %s", firebase_uid)
