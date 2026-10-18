import uuid
from dataclasses import dataclass
from typing import Annotated

from fastapi import Depends
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import select, update

from app.modules.admins.models import Admin
from app.modules.auth.models import AppUser, RefreshToken, Role, UserStatus
from app.modules.children.models import Child
from app.modules.parents.models import Parent, ParentChildRel
from app.modules.teachers.models import Teacher

from . import clock
from .db import Db
from .errors import ApiError
from .tokens import decode_access_token

# El token dice quién eres; la API dice qué puedes hacer.
# El JWT solo lleva el id de la persona: el rol y lo que cada quien puede ver salen de Postgres en cada petición.

bearer = HTTPBearer(auto_error=False, description="access_token que devuelve POST /auth/login")


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


async def get_current_user(
    db: Db, credentials: HTTPAuthorizationCredentials | None = Depends(bearer)
) -> CurrentUser:
    if credentials is None:
        raise ApiError(401, "UNAUTHENTICATED", "Falta el token de acceso")
    payload = decode_access_token(credentials.credentials)
    try:
        user_id = uuid.UUID(payload["sub"])
    except ValueError:
        raise ApiError(401, "TOKEN_INVALID", "El token no es válido")
    user = await db.get(AppUser, user_id)
    if user is None:
        raise ApiError(401, "TOKEN_INVALID", "El token no es válido")
    # primero el estado: quien está de baja ve un mensaje claro aunque su token ya no coincida.
    if user.status != UserStatus.ACTIVE:
        raise ApiError(403, "USER_INACTIVE", "Tu acceso a la app está desactivado")
    # versión distinta = la persona cambió su contraseña (o la dieron de baja y la reactivaron) después de emitir el token.
    if user.token_version != payload["tv"]:
        raise ApiError(401, "TOKEN_INVALID", "El token no es válido")
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


async def revoke_all_sessions(db: Db, user: AppUser) -> int:
    """Cierra todas las sesiones de una persona: los access tokens vivos dejan de servir (token_version)
    y los refresh tokens quedan revocados. No hace commit: lo hace quien la llama."""
    user.token_version += 1
    result = await db.execute(
        update(RefreshToken)
        .where(RefreshToken.user_id == user.id, RefreshToken.revoked_at.is_(None))
        .values(revoked_at=clock.now())
    )
    return result.rowcount or 0
