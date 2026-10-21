"""Seguridad de la entrada: límite de intentos, cabeceras, CORS y la bitácora de auditoría (RF-14)."""

from datetime import timedelta

import pytest

from app.core.config import settings
from tests.conftest import NOW, TEST_PASSWORD, bearer

WRONG = "esta-no-es-la-clave"


async def login(client, email="karla@test.dev", password=TEST_PASSWORD):
    return await client.post("/auth/login", json={"email": email, "password": password})


async def audit_entries(client, world, **params):
    response = await client.get("/audit-log", headers=world.admin, params=params)
    assert response.status_code == 200
    return response.json()["items"]


# ---------------------------------------------------------------- límite de intentos


async def test_five_failures_lock_the_email_even_for_the_right_password(client, world):
    for _ in range(settings.auth_max_failures):
        assert (await login(client, password=WRONG)).status_code == 401

    locked = await login(client)  # ya con la contraseña buena
    assert locked.status_code == 429
    body = locked.json()
    assert body["code"] == "TOO_MANY_ATTEMPTS"
    assert 0 < body["retry_after_s"] <= settings.auth_window_s
    assert locked.headers["retry-after"] == str(body["retry_after_s"])


async def test_unknown_emails_are_locked_the_same_way(client, world):
    """El bloqueo no debe delatar qué correos existen."""
    for _ in range(settings.auth_max_failures):
        await login(client, "nadie@test.dev", WRONG)
    assert (await login(client, "nadie@test.dev", WRONG)).status_code == 429


async def test_lock_is_lifted_when_the_window_passes(client, world, fixed_clock):
    for _ in range(settings.auth_max_failures):
        await login(client, password=WRONG)
    assert (await login(client)).status_code == 429

    fixed_clock(NOW + timedelta(seconds=settings.auth_window_s + 1))
    assert (await login(client)).status_code == 200


async def test_a_good_login_resets_the_failures(client, world):
    for _ in range(settings.auth_max_failures - 1):
        await login(client, password=WRONG)
    assert (await login(client)).status_code == 200
    # el contador volvió a cero: otros cuatro fallos todavía no bloquean.
    for _ in range(settings.auth_max_failures - 1):
        assert (await login(client, password=WRONG)).status_code == 401
    assert (await login(client)).status_code == 200


async def test_another_email_is_not_locked(client, world):
    for _ in range(settings.auth_max_failures):
        await login(client, password=WRONG)
    assert (await login(client, "fernando@test.dev")).status_code == 200


async def test_one_ip_sweeping_many_emails_is_locked(client, world, monkeypatch):
    monkeypatch.setattr(settings, "auth_max_failures_ip", 3)
    for index in range(3):
        assert (await login(client, f"persona{index}@test.dev", WRONG)).status_code == 401
    # con otro correo y hasta con una cuenta buena: la IP ya está bloqueada.
    assert (await login(client, "fernando@test.dev")).status_code == 429


async def test_guessing_activation_codes_is_limited(client, world):
    for _ in range(settings.auth_max_failures):
        response = await client.post(
            "/auth/activate", json={"email": "karla@test.dev", "code": "AAAA-BBBB", "password": "clave-larga-123"}
        )
        assert response.status_code == 403
        assert response.json()["code"] == "CODE_INVALID"
    response = await client.post(
        "/auth/activate", json={"email": "karla@test.dev", "code": "CCCC-DDDD", "password": "clave-larga-123"}
    )
    assert response.status_code == 429


async def test_login_and_activation_limits_are_independent(client, world):
    for _ in range(settings.auth_max_failures):
        await login(client, password=WRONG)
    response = await client.post(
        "/auth/activate", json={"email": "karla@test.dev", "code": "AAAA-BBBB", "password": "clave-larga-123"}
    )
    assert response.status_code == 403  # el código se revisa: no lo bloquea el límite del login


# ---------------------------------------------------------------- límite genérico por IP


async def test_generic_rate_limit_returns_429(client, monkeypatch):
    monkeypatch.setattr(settings, "rate_limit_max_requests", 3)
    for _ in range(3):
        assert (await client.get("/me")).status_code == 401
    blocked = await client.get("/me")
    assert blocked.status_code == 429
    body = blocked.json()
    assert body["code"] == "TOO_MANY_REQUESTS"
    assert 0 < body["retry_after_s"] <= settings.rate_limit_window_s
    assert blocked.headers["retry-after"] == str(body["retry_after_s"])


async def test_generic_rate_limit_covers_refresh(client, monkeypatch):
    """/auth/refresh no tiene su propio límite de intentos (throttle.py es solo login/activate);
    el genérico por IP lo cubre."""
    monkeypatch.setattr(settings, "rate_limit_max_requests", 3)
    for _ in range(3):
        response = await client.post("/auth/refresh", json={"refresh_token": "no-existe"})
        assert response.status_code == 401
    blocked = await client.post("/auth/refresh", json={"refresh_token": "no-existe"})
    assert blocked.status_code == 429


async def test_generic_rate_limit_response_has_security_headers(client, monkeypatch):
    monkeypatch.setattr(settings, "rate_limit_max_requests", 1)
    await client.get("/me")
    blocked = await client.get("/me")
    assert blocked.status_code == 429
    assert blocked.headers["x-content-type-options"] == "nosniff"
    assert blocked.headers["cache-control"] == "no-store"


async def test_health_checks_are_exempt_from_the_generic_rate_limit(client, monkeypatch):
    monkeypatch.setattr(settings, "rate_limit_max_requests", 1)
    for _ in range(5):
        assert (await client.get("/health/live")).status_code == 200
        assert (await client.get("/health/ready")).status_code == 200


# ---------------------------------------------------------------- cabeceras y CORS


async def test_security_headers_on_every_response(client):
    response = await client.get("/health/live")
    assert response.headers["x-content-type-options"] == "nosniff"
    assert response.headers["x-frame-options"] == "DENY"
    assert response.headers["referrer-policy"] == "no-referrer"
    assert response.headers["content-security-policy"] == "default-src 'none'; frame-ancestors 'none'"
    assert response.headers["cache-control"] == "no-store"
    assert "geolocation=()" in response.headers["permissions-policy"]


async def test_security_headers_also_on_errors(client):
    response = await client.get("/me")  # 401
    assert response.status_code == 401
    assert response.headers["x-content-type-options"] == "nosniff"
    assert response.headers["cache-control"] == "no-store"


async def test_hsts_only_outside_development(client, monkeypatch):
    assert "strict-transport-security" not in (await client.get("/health/live")).headers
    monkeypatch.setattr(settings, "env", "prod")
    response = await client.get("/health/live")
    assert response.headers["strict-transport-security"].startswith("max-age=31536000")


async def test_cors_is_closed_by_default(client):
    response = await client.options(
        "/auth/login",
        headers={"Origin": "https://evil.example", "Access-Control-Request-Method": "POST"},
    )
    assert "access-control-allow-origin" not in response.headers


# ---------------------------------------------------------------- bitácora de auditoría


async def test_login_ok_and_failed_are_recorded(client, world):
    await login(client, password=WRONG)
    await login(client)
    entries = await audit_entries(client, world)
    actions = [entry["action"] for entry in entries]
    assert actions[:2] == ["LOGIN_OK", "LOGIN_FAILED"]  # lo más nuevo primero
    failed = entries[1]
    assert failed["actor_email"] == "karla@test.dev"
    assert failed["actor_name"] == "Karla Prueba"
    assert failed["ip"]


async def test_lock_is_recorded_once(client, world):
    for _ in range(settings.auth_max_failures + 2):
        await login(client, password=WRONG)
    locked = await audit_entries(client, world, action="LOGIN_LOCKED")
    assert len(locked) == 1
    assert len(await audit_entries(client, world, action="LOGIN_FAILED")) == settings.auth_max_failures


async def test_unknown_email_failure_is_recorded_without_actor(client, world):
    await login(client, "nadie@test.dev", WRONG)
    [entry] = await audit_entries(client, world, action="LOGIN_FAILED")
    assert entry["actor_id"] is None
    assert entry["actor_email"] == "nadie@test.dev"


async def test_only_the_admin_can_read_the_log(client, world):
    for headers in (world.teacher_a, world.mom):
        response = await client.get("/audit-log", headers=headers)
        assert response.status_code == 403
    assert (await client.get("/audit-log")).status_code == 401


async def test_the_log_cannot_be_edited_or_deleted(client, world):
    await login(client)
    for method in ("put", "patch", "delete", "post"):
        response = await getattr(client, method)("/audit-log", headers=world.admin)
        assert response.status_code == 405
    response = await client.delete("/audit-log/1", headers=world.admin)
    assert response.status_code in (404, 405)


async def test_admin_actions_are_recorded_with_who_did_them(client, world):
    created = await client.post(
        "/teachers",
        headers=world.admin,
        json={"name": "Paola Sánchez", "phone": "8112345678", "email": "paola@test.dev", "classroom_id": world.room_a.id},
    )
    assert created.status_code == 201
    teacher_id = created.json()["id"]
    assert (await client.post(f"/teachers/{teacher_id}/deactivate", headers=world.admin)).status_code == 200
    assert (await client.post(f"/teachers/{teacher_id}/reactivate", headers=world.admin)).status_code == 200
    code = await client.post(f"/users/{teacher_id}/access-code", headers=world.admin)
    assert code.status_code == 201

    entries = await audit_entries(client, world)
    assert [entry["action"] for entry in entries[:4]] == [
        "ACCESS_CODE_ISSUED",
        "USER_REACTIVATED",
        "USER_DEACTIVATED",
        "USER_CREATED",
    ]
    assert all(entry["actor_name"] == "Directora Prueba" for entry in entries[:4])
    assert all(entry["entity_id"] == teacher_id for entry in entries[:4])
    assert entries[3]["detail"] == {"role": "TEACHER"}


async def test_child_enrollment_and_changes_are_recorded(client, world):
    created = await client.post(
        "/children",
        headers=world.admin,
        json={"name": "Luna", "surname": "Ortiz", "birth_date": "2022-05-01", "classroom_id": world.room_a.id},
    )
    assert created.status_code == 201
    child_id = created.json()["id"]
    await client.patch(f"/children/{child_id}", headers=world.admin, json={"classroom_id": world.room_b.id})
    await client.post(f"/children/{child_id}/deactivate", headers=world.admin)

    entries = await audit_entries(client, world)
    assert [entry["action"] for entry in entries[:3]] == ["CHILD_DEACTIVATED", "CHILD_UPDATED", "CHILD_ENROLLED"]
    assert entries[2]["entity_type"] == "CHILD" and entries[2]["entity_id"] == str(child_id)
    assert entries[1]["detail"] == {"classroom_id": world.room_b.id}


async def test_the_log_never_stores_passwords_codes_or_tokens(client, world):
    created = await client.post(
        "/teachers",
        headers=world.admin,
        json={"name": "Paola Sánchez", "phone": "8112345678", "email": "paola@test.dev", "classroom_id": world.room_a.id},
    )
    code = created.json()["access_code"]["code"]
    session = (await login(client)).json()
    await client.post("/auth/login", json={"email": "karla@test.dev", "password": "clave-que-no-debe-guardarse"})
    await client.post(
        "/me/password",
        headers=bearer(session["access_token"]),
        json={"current_password": TEST_PASSWORD, "new_password": "otra-clave-larga-9"},
    )

    dump = (await client.get("/audit-log?limit=200", headers=world.admin)).text
    for secret in (code, code.replace("-", ""), session["access_token"], session["refresh_token"],
                   TEST_PASSWORD, "clave-que-no-debe-guardarse", "otra-clave-larga-9"):
        assert secret not in dump


async def test_the_log_paginates(client, world):
    for _ in range(3):
        await login(client)
    first = (await client.get("/audit-log?limit=2", headers=world.admin)).json()
    assert len(first["items"]) == 2 and first["next_cursor"]
    second = (await client.get(f"/audit-log?limit=2&cursor={first['next_cursor']}", headers=world.admin)).json()
    assert len(second["items"]) == 1 and second["next_cursor"] is None
    assert first["items"][-1]["id"] > second["items"][0]["id"]


@pytest.mark.parametrize("limit", [0, 201])
async def test_the_log_limit_is_bounded(client, world, limit):
    assert (await client.get(f"/audit-log?limit={limit}", headers=world.admin)).status_code == 422
