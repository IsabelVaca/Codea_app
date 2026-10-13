from datetime import timedelta

from tests.conftest import NOW, as_user


async def test_without_token_is_401(client, world):
    response = await client.get("/me")
    assert response.status_code == 401
    assert response.json()["code"] == "UNAUTHENTICATED"


async def test_unknown_account_needs_invitation(client, world):
    response = await client.get("/me", headers=as_user("nadie@test.dev"))
    assert response.status_code == 403
    assert response.json()["code"] == "INVITATION_REQUIRED"


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


async def test_new_teacher_accepts_invitation(client, world):
    created = await client.post(
        "/teachers",
        headers=world.admin,
        json={"name": "Paola Sánchez", "phone": "8112345678", "email": "Paola@Test.dev", "classroom_id": world.room_a.id},
    )
    assert created.status_code == 201
    assert created.json()["has_account"] is False

    paola = as_user("paola@test.dev")
    assert (await client.get("/me", headers=paola)).json()["code"] == "INVITATION_REQUIRED"

    accepted = await client.post("/auth/accept-invitation", headers=paola)
    assert accepted.status_code == 200
    assert accepted.json()["roles"] == ["TEACHER"]
    assert (await client.get("/me", headers=paola)).status_code == 200
    # aceptarla otra vez no falla.
    assert (await client.post("/auth/accept-invitation", headers=paola)).status_code == 200


async def test_expired_invitation(client, world, fixed_clock):
    await client.post(
        "/teachers", headers=world.admin, json={"name": "Luis", "phone": "8112345678", "email": "luis@test.dev"}
    )
    fixed_clock(NOW + timedelta(days=8))
    response = await client.post("/auth/accept-invitation", headers=as_user("luis@test.dev"))
    assert response.status_code == 403
    assert response.json()["code"] == "INVITATION_EXPIRED"


async def test_role_is_checked(client, world):
    response = await client.get("/teachers", headers=world.teacher_a)
    assert response.status_code == 403
    assert response.json()["code"] == "FORBIDDEN"
