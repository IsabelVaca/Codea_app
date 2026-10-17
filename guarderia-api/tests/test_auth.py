import uuid
from datetime import timedelta

from sqlalchemy import select

from app.core.config import settings
from app.core.tokens import create_access_token
from app.modules.auth.models import AppUser, RefreshToken
from tests.conftest import NOW, TEST_PASSWORD, as_user, bearer

NEW_PASSWORD = "otra-clave-larga-9"


async def login(client, email="karla@test.dev", password=TEST_PASSWORD):
    return await client.post("/auth/login", json={"email": email, "password": password})


async def create_teacher(client, world, email="paola@test.dev"):
    created = await client.post(
        "/teachers",
        headers=world.admin,
        json={"name": "Paola Sánchez", "phone": "8112345678", "email": email, "classroom_id": world.room_a.id},
    )
    assert created.status_code == 201
    return created.json()


# ---------------------------------------------------------------- token en cada petición


async def test_without_token_is_401(client, world):
    response = await client.get("/me")
    assert response.status_code == 401
    assert response.json()["code"] == "UNAUTHENTICATED"


async def test_garbage_token_is_401(client, world):
    response = await client.get("/me", headers=bearer("esto-no-es-un-jwt"))
    assert response.status_code == 401
    assert response.json()["code"] == "TOKEN_INVALID"


async def test_token_of_nobody_is_401(client, world):
    response = await client.get("/me", headers=bearer(create_access_token(uuid.uuid4(), 0)))
    assert response.status_code == 401
    assert response.json()["code"] == "TOKEN_INVALID"


async def test_expired_access_token(client, world, fixed_clock, monkeypatch):
    monkeypatch.setattr(settings, "access_ttl_s", 300)
    token = bearer(create_access_token(world.teacher_a_id, 0))
    assert (await client.get("/me", headers=token)).status_code == 200
    fixed_clock(NOW + timedelta(seconds=301))
    response = await client.get("/me", headers=token)
    assert response.status_code == 401
    assert response.json()["code"] == "TOKEN_EXPIRED"


async def test_refresh_token_is_not_an_access_token(client, world):
    tokens = (await login(client)).json()
    response = await client.get("/me", headers=bearer(tokens["refresh_token"]))
    assert response.status_code == 401
    assert response.json()["code"] == "TOKEN_INVALID"


async def test_me_for_each_role(client, world):
    teacher = (await client.get("/me", headers=world.teacher_a)).json()
    assert teacher["roles"] == ["TEACHER"]
    assert teacher["teacher"]["classroom"]["name"] == "Preescolar 2"
    assert teacher["teacher"]["shift"]["start_time"] == "08:00:00"
    assert teacher["teacher"]["center"]["check_in_radius_m"] == 60

    parent = (await client.get("/me", headers=world.mom)).json()
    assert parent["roles"] == ["PARENT"]
    assert [child["full_name"] for child in parent["parent"]["children"]] == ["Sofía Márquez"]
    assert parent["parent"]["children"][0]["relationship"] == "MOTHER"

    admin = (await client.get("/me", headers=world.admin)).json()
    assert admin["roles"] == ["ADMIN"]
    assert admin["admin"]["center"]["name"] == "La Oruga"


async def test_role_is_checked(client, world):
    response = await client.get("/teachers", headers=world.teacher_a)
    assert response.status_code == 403
    assert response.json()["code"] == "FORBIDDEN"


# ---------------------------------------------------------------- login


async def test_login_ok(client, world):
    response = await login(client, "Karla@Test.dev")
    assert response.status_code == 200
    body = response.json()
    assert body["token_type"] == "bearer"
    assert body["expires_in"] == settings.access_ttl_s
    assert body["refresh_expires_in"] == settings.refresh_ttl_s
    assert body["me"]["roles"] == ["TEACHER"]
    # el access token sirve de inmediato
    assert (await client.get("/me", headers=bearer(body["access_token"]))).status_code == 200


async def test_wrong_password_and_unknown_email_look_the_same(client, world):
    wrong = await login(client, password="no-es-la-clave")
    unknown = await login(client, "nadie@test.dev")
    assert wrong.status_code == unknown.status_code == 401
    assert wrong.json()["code"] == unknown.json()["code"] == "INVALID_CREDENTIALS"
    assert wrong.json()["title"] == unknown.json()["title"]


async def test_login_before_activating(client, world):
    await create_teacher(client, world)
    response = await login(client, "paola@test.dev", "cualquiera-123")
    assert response.status_code == 403
    assert response.json()["code"] == "ACCOUNT_NOT_ACTIVATED"


async def test_login_of_deactivated_user(client, world):
    await client.post(f"/teachers/{world.teacher_a_id}/deactivate", headers=world.admin)
    response = await login(client)
    assert response.status_code == 403
    assert response.json()["code"] == "USER_INACTIVE"


# ---------------------------------------------------------------- activar con el código


async def test_new_teacher_activates_with_code(client, world):
    created = await create_teacher(client, world)
    assert created["has_account"] is False
    code = created["access_code"]
    assert code["purpose"] == "INVITATION"
    assert len(code["code"]) == 9 and code["code"][4] == "-"

    # el código se acepta en minúsculas y sin guion
    typed = code["code"].replace("-", "").lower()
    response = await client.post(
        "/auth/activate", json={"email": "PAOLA@test.dev", "code": typed, "password": NEW_PASSWORD}
    )
    assert response.status_code == 200
    body = response.json()
    assert body["me"]["roles"] == ["TEACHER"]
    assert (await client.get("/me", headers=bearer(body["access_token"]))).status_code == 200

    # ahora entra con su contraseña, y la cuenta aparece como activada
    assert (await login(client, "paola@test.dev", NEW_PASSWORD)).status_code == 200
    listed = {t["email"]: t for t in (await client.get("/teachers", headers=world.admin)).json()}
    assert listed["paola@test.dev"]["has_account"] is True
    assert listed["paola@test.dev"]["access_code"] is None


async def test_code_is_single_use(client, world):
    code = (await create_teacher(client, world))["access_code"]["code"]
    body = {"email": "paola@test.dev", "code": code, "password": NEW_PASSWORD}
    assert (await client.post("/auth/activate", json=body)).status_code == 200
    again = await client.post("/auth/activate", json=body)
    assert again.status_code == 403
    assert again.json()["code"] == "CODE_INVALID"


async def test_wrong_code(client, world):
    await create_teacher(client, world)
    response = await client.post(
        "/auth/activate", json={"email": "paola@test.dev", "code": "AAAA-AAAA", "password": NEW_PASSWORD}
    )
    assert response.status_code == 403
    assert response.json()["code"] == "CODE_INVALID"


async def test_expired_code(client, world, fixed_clock):
    code = (await create_teacher(client, world))["access_code"]["code"]
    fixed_clock(NOW + timedelta(days=8))
    response = await client.post(
        "/auth/activate", json={"email": "paola@test.dev", "code": code, "password": NEW_PASSWORD}
    )
    assert response.status_code == 403
    assert response.json()["code"] == "CODE_EXPIRED"


async def test_password_rules_on_activation(client, world):
    code = (await create_teacher(client, world))["access_code"]["code"]

    def body(password):
        return {"email": "paola@test.dev", "code": code, "password": password}

    short = await client.post("/auth/activate", json=body("12345678"))  # 8 caracteres: no basta
    assert short.status_code == 422 and short.json()["code"] == "PASSWORD_TOO_SHORT"
    huge = await client.post("/auth/activate", json=body("x" * 129))
    assert huge.json()["code"] == "PASSWORD_TOO_LONG"
    is_email = await client.post("/auth/activate", json=body("paola@test.dev"))
    assert is_email.json()["code"] == "PASSWORD_IS_EMAIL"
    # 9 caracteres sí, y el código sigue sirviendo porque los intentos fallidos no lo gastan
    assert (await client.post("/auth/activate", json=body("123456789"))).status_code == 200


async def test_new_code_replaces_the_old_one(client, world):
    created = await create_teacher(client, world)
    user_id = created["id"]
    new = await client.post(f"/users/{user_id}/access-code", headers=world.admin)
    assert new.status_code == 201
    assert new.json()["purpose"] == "INVITATION"
    old = await client.post(
        "/auth/activate",
        json={"email": "paola@test.dev", "code": created["access_code"]["code"], "password": NEW_PASSWORD},
    )
    assert old.json()["code"] in {"CODE_INVALID", "CODE_EXPIRED"}
    ok = await client.post(
        "/auth/activate",
        json={"email": "paola@test.dev", "code": new.json()["code"], "password": NEW_PASSWORD},
    )
    assert ok.status_code == 200


async def test_password_reset_code(client, world):
    # karla ya entró: su código es de PASSWORD_RESET y cierra sus sesiones anteriores
    old = (await login(client)).json()
    reset = await client.post(f"/users/{world.teacher_a_id}/access-code", headers=world.admin)
    assert reset.status_code == 201
    assert reset.json()["purpose"] == "PASSWORD_RESET"

    done = await client.post(
        "/auth/activate",
        json={"email": "karla@test.dev", "code": reset.json()["code"], "password": NEW_PASSWORD},
    )
    assert done.status_code == 200
    assert (await login(client, password=TEST_PASSWORD)).status_code == 401
    assert (await login(client, password=NEW_PASSWORD)).status_code == 200
    # la sesión de antes ya no sirve
    assert (await client.get("/me", headers=bearer(old["access_token"]))).json()["code"] == "TOKEN_INVALID"
    assert (await client.post("/auth/refresh", json={"refresh_token": old["refresh_token"]})).status_code == 401


async def test_access_code_is_admin_only_and_needs_an_active_user(client, world):
    forbidden = await client.post(f"/users/{world.mom_id}/access-code", headers=world.teacher_a)
    assert forbidden.status_code == 403
    missing = await client.post(f"/users/{uuid.uuid4()}/access-code", headers=world.admin)
    assert missing.status_code == 404 and missing.json()["code"] == "USER_NOT_FOUND"
    await client.post(f"/parents/{world.mom_id}/deactivate", headers=world.admin)
    off = await client.post(f"/users/{world.mom_id}/access-code", headers=world.admin)
    assert off.status_code == 409 and off.json()["code"] == "USER_INACTIVE"


async def test_codes_are_stored_hashed(client, world, db):
    code = (await create_teacher(client, world))["access_code"]["code"]
    stored = (await db.execute(select(AppUser.email))).all()  # solo para tener la sesión abierta
    assert stored
    from app.modules.auth.models import Invitation

    rows = (await db.scalars(select(Invitation))).all()
    assert rows and all(code.replace("-", "") not in row.code_hash for row in rows)
    assert all(len(row.code_hash) == 64 for row in rows)


# ---------------------------------------------------------------- refresh y logout


async def test_refresh_rotates_the_token(client, world):
    first = (await login(client)).json()
    second = await client.post("/auth/refresh", json={"refresh_token": first["refresh_token"]})
    assert second.status_code == 200
    body = second.json()
    assert body["me"] is None
    assert body["refresh_token"] != first["refresh_token"]
    assert (await client.get("/me", headers=bearer(body["access_token"]))).status_code == 200
    # el refresh token viejo ya no sirve
    old = await client.post("/auth/refresh", json={"refresh_token": first["refresh_token"]})
    assert old.status_code == 401 and old.json()["code"] == "REFRESH_INVALID"


async def test_reusing_a_rotated_token_closes_the_family(client, world):
    first = (await login(client)).json()
    second = (await client.post("/auth/refresh", json={"refresh_token": first["refresh_token"]})).json()
    # alguien presenta el viejo (lo copió): se revoca toda la familia, incluido el nuevo
    assert (await client.post("/auth/refresh", json={"refresh_token": first["refresh_token"]})).status_code == 401
    after = await client.post("/auth/refresh", json={"refresh_token": second["refresh_token"]})
    assert after.status_code == 401 and after.json()["code"] == "REFRESH_INVALID"


async def test_unknown_refresh_token(client, world):
    response = await client.post("/auth/refresh", json={"refresh_token": "no-existe"})
    assert response.status_code == 401 and response.json()["code"] == "REFRESH_INVALID"


async def test_expired_refresh_token(client, world, fixed_clock):
    tokens = (await login(client)).json()
    fixed_clock(NOW + timedelta(seconds=settings.refresh_ttl_s + 1))
    response = await client.post("/auth/refresh", json={"refresh_token": tokens["refresh_token"]})
    assert response.status_code == 401 and response.json()["code"] == "REFRESH_INVALID"


async def test_refresh_tokens_are_stored_hashed(client, world, db):
    tokens = (await login(client)).json()
    rows = (await db.scalars(select(RefreshToken))).all()
    assert len(rows) == 1
    assert rows[0].token_hash != tokens["refresh_token"] and len(rows[0].token_hash) == 64


async def test_logout_revokes_the_refresh_token(client, world):
    tokens = (await login(client)).json()
    out = await client.post("/auth/logout", json={"refresh_token": tokens["refresh_token"]})
    assert out.status_code == 204
    assert (await client.post("/auth/refresh", json={"refresh_token": tokens["refresh_token"]})).status_code == 401
    # cerrar dos veces, o con un token que no existe, no falla
    assert (await client.post("/auth/logout", json={"refresh_token": tokens["refresh_token"]})).status_code == 204
    assert (await client.post("/auth/logout", json={"refresh_token": "no-existe"})).status_code == 204


# ---------------------------------------------------------------- bajas y contraseña


async def test_deactivating_cuts_access_at_once(client, world):
    tokens = (await login(client)).json()
    mine = bearer(tokens["access_token"])
    assert (await client.get("/me", headers=mine)).status_code == 200

    await client.post(f"/teachers/{world.teacher_a_id}/deactivate", headers=world.admin)
    response = await client.get("/me", headers=mine)
    assert response.status_code == 403 and response.json()["code"] == "USER_INACTIVE"
    assert (await client.post("/auth/refresh", json={"refresh_token": tokens["refresh_token"]})).status_code == 401

    await client.post(f"/teachers/{world.teacher_a_id}/reactivate", headers=world.admin)
    # los tokens viejos siguen muertos (token_version cambió); hay que entrar de nuevo
    assert (await client.get("/me", headers=mine)).json()["code"] == "TOKEN_INVALID"
    assert (await login(client)).status_code == 200


async def test_change_password(client, world):
    tokens = (await login(client)).json()
    mine = bearer(tokens["access_token"])
    wrong = await client.post(
        "/me/password", headers=mine, json={"current_password": "no-es", "new_password": NEW_PASSWORD}
    )
    assert wrong.status_code == 403 and wrong.json()["code"] == "PASSWORD_INCORRECT"
    short = await client.post(
        "/me/password", headers=mine, json={"current_password": TEST_PASSWORD, "new_password": "corta"}
    )
    assert short.status_code == 422 and short.json()["code"] == "PASSWORD_TOO_SHORT"

    other_device = (await login(client)).json()
    changed = await client.post(
        "/me/password", headers=mine, json={"current_password": TEST_PASSWORD, "new_password": NEW_PASSWORD}
    )
    assert changed.status_code == 200
    fresh = changed.json()
    # quien la cambió sigue dentro con los tokens nuevos; las demás sesiones se cerraron
    assert (await client.get("/me", headers=bearer(fresh["access_token"]))).status_code == 200
    assert (await client.get("/me", headers=mine)).status_code == 401
    assert (await client.get("/me", headers=bearer(other_device["access_token"]))).status_code == 401
    assert (await client.post("/auth/refresh", json={"refresh_token": other_device["refresh_token"]})).status_code == 401
    assert (await login(client, password=TEST_PASSWORD)).status_code == 401
    assert (await login(client, password=NEW_PASSWORD)).status_code == 200


async def test_close_all_my_sessions(client, world):
    one = (await login(client)).json()
    two = (await login(client)).json()
    response = await client.delete("/me/sessions", headers=bearer(one["access_token"]))
    assert response.status_code == 200 and response.json()["revoked"] == 2
    assert (await client.get("/me", headers=bearer(two["access_token"]))).status_code == 401
    assert (await client.post("/auth/refresh", json={"refresh_token": two["refresh_token"]})).status_code == 401


async def test_resend_gives_a_new_code_and_refuses_active_accounts(client, world):
    parent = (
        await client.post(
            "/parents",
            headers=world.admin,
            json={
                "name": "Jorge",
                "phone": "5512345678",
                "email": "jorge@test.dev",
                "relationship": "FATHER",
                "child_ids": [world.sofia.id],
            },
        )
    ).json()
    first = parent["access_code"]["code"]
    resent = await client.post(f"/parents/{parent['id']}/resend-invitation", headers=world.admin)
    assert resent.status_code == 200
    second = resent.json()["access_code"]["code"]
    assert second != first
    activate = await client.post(
        "/auth/activate", json={"email": "jorge@test.dev", "code": second, "password": NEW_PASSWORD}
    )
    assert activate.status_code == 200
    again = await client.post(f"/parents/{parent['id']}/resend-invitation", headers=world.admin)
    assert again.status_code == 409 and again.json()["code"] == "INVITATION_ALREADY_ACCEPTED"


async def test_every_role_still_scoped_after_login(client, world):
    mom = bearer((await login(client, "mariana@test.dev")).json()["access_token"])
    assert (await client.get(f"/children/{world.sofia.id}", headers=mom)).status_code == 200
    assert (await client.get(f"/children/{world.renata.id}", headers=mom)).status_code == 404
    assert (await client.get("/teachers", headers=mom)).status_code == 403
    assert as_user("mariana@test.dev")  # el encabezado de las pruebas sigue funcionando
