async def test_classroom_rules(client, world):
    no_name = await client.post("/classrooms", headers=world.admin, json={"name": "  ", "capacity": 10})
    assert no_name.json()["code"] == "NAME_REQUIRED"
    no_capacity = await client.post("/classrooms", headers=world.admin, json={"name": "Lactantes", "capacity": 0})
    assert no_capacity.json()["code"] == "CAPACITY_INVALID"


async def test_create_classroom_with_teacher_and_occupancy(client, world):
    created = await client.post(
        "/classrooms",
        headers=world.admin,
        json={"name": "Lactantes", "capacity": 8, "teacher_id": str(world.teacher_b_id)},
    )
    assert created.status_code == 201
    body = created.json()
    assert body["primary_teacher"]["id"] == str(world.teacher_b_id)

    rooms = {room["name"]: room for room in (await client.get("/classrooms", headers=world.admin)).json()}
    assert rooms["Preescolar 2"]["enrolled"] == 2
    assert rooms["Preescolar 2"]["available"] == 10
    # Fernando se fue a Lactantes, así que Maternal se quedó sin asistente.
    assert rooms["Maternal"]["primary_teacher"] is None


async def test_staff_and_capacity(client, world):
    room = world.room_a.id
    staff = await client.put(
        f"/classrooms/{room}/staff",
        headers=world.admin,
        json={"teacher_ids": [str(world.teacher_a_id), str(world.teacher_b_id)], "primary_teacher_id": str(world.teacher_b_id)},
    )
    assert staff.status_code == 200
    assert staff.json()["primary_teacher"]["id"] == str(world.teacher_b_id)
    assert len(staff.json()["teachers"]) == 2

    wrong = await client.put(
        f"/classrooms/{room}/staff",
        headers=world.admin,
        json={"teacher_ids": [str(world.teacher_a_id)], "primary_teacher_id": str(world.teacher_b_id)},
    )
    assert wrong.json()["code"] == "PRIMARY_NOT_IN_STAFF"

    patched = await client.patch(f"/classrooms/{room}", headers=world.admin, json={"capacity": 15})
    assert patched.json()["capacity"] == 15


async def test_delete_classroom(client, world):
    full = await client.delete(f"/classrooms/{world.room_b.id}", headers=world.admin)
    assert full.status_code == 409
    assert full.json()["code"] == "CLASSROOM_NOT_EMPTY"

    moved = await client.patch(f"/children/{world.renata.id}", headers=world.admin, json={"classroom_id": None})
    assert moved.json()["classroom"] is None
    assert (await client.delete(f"/classrooms/{world.room_b.id}", headers=world.admin)).status_code == 204
    assert (await client.get(f"/classrooms/{world.room_b.id}", headers=world.admin)).status_code == 404
    teacher = (await client.get(f"/teachers/{world.teacher_b_id}", headers=world.admin)).json()
    assert teacher["classroom"] is None


async def test_teacher_rules_and_changes(client, world):
    base = {"name": "Gabriela", "phone": "8112345678", "email": "gaby@test.dev"}
    assert (await client.post("/teachers", headers=world.admin, json={**base, "name": ""})).json()["code"] == "NAME_REQUIRED"
    assert (await client.post("/teachers", headers=world.admin, json={**base, "phone": "123"})).json()["code"] == "PHONE_INVALID"
    assert (await client.post("/teachers", headers=world.admin, json={**base, "email": "gaby"})).json()["code"] == "EMAIL_INVALID"
    taken = await client.post("/teachers", headers=world.admin, json={**base, "email": "karla@test.dev"})
    assert taken.status_code == 409 and taken.json()["code"] == "EMAIL_TAKEN"

    shifts = (await client.get("/shifts", headers=world.admin)).json()
    created = (
        await client.post("/teachers", headers=world.admin, json={**base, "shift_id": shifts[0]["id"]})
    ).json()
    assert created["shift"]["name"] == "Matutino"
    moved = await client.patch(f"/teachers/{created['id']}", headers=world.admin, json={"classroom_id": world.room_b.id})
    assert moved.json()["classroom"]["name"] == "Maternal"

    karla = (await client.get(f"/teachers/{world.teacher_a_id}", headers=world.admin)).json()
    assert karla["children_count"] == 2
    off = await client.post(f"/teachers/{world.teacher_a_id}/deactivate", headers=world.admin)
    assert off.json()["active"] is False
    assert (await client.get("/me", headers=world.teacher_a)).json()["code"] == "USER_INACTIVE"
    await client.post(f"/teachers/{world.teacher_a_id}/reactivate", headers=world.admin)
    assert (await client.get("/me", headers=world.teacher_a)).status_code == 200


async def test_enroll_child_without_guardian(client, world):
    created = await client.post(
        "/children",
        headers=world.admin,
        json={"name": "Lucas", "birth_date": "2022-05-10", "classroom_id": world.room_a.id},
    )
    assert created.status_code == 201
    assert created.json()["guardian"] is None
    assert created.json()["classroom"]["name"] == "Preescolar 2"


async def test_enroll_child_with_guardian(client, world):
    base = {
        "name": "Mateo",
        "surname": "Iglesias",
        "birth_date": "2022-05-10",
        "allergies": "Cacahuate",
        "classroom_id": world.room_a.id,
        "guardian": {"name": "Laura Iglesias", "relationship": "MOTHER", "phone": "5512345678", "email": "laura@test.dev"},
    }
    old = await client.post("/children", headers=world.admin, json={**base, "birth_date": "2010-01-01"})
    assert old.json()["code"] == "AGE_INVALID"
    nameless = await client.post(
        "/children", headers=world.admin, json={**base, "guardian": {**base["guardian"], "name": " "}}
    )
    assert nameless.json()["code"] == "GUARDIAN_REQUIRED"
    bad_phone = await client.post(
        "/children", headers=world.admin, json={**base, "guardian": {**base["guardian"], "phone": "55"}}
    )
    assert bad_phone.json()["code"] == "PHONE_INVALID"

    created = await client.post("/children", headers=world.admin, json=base)
    assert created.status_code == 201
    child = created.json()
    assert child["age"] == 4
    assert child["guardian"]["full_name"] == "Laura Iglesias"
    assert child["guardian"]["relationship"] == "MOTHER"

    # la mamá entra con su invitación y ve a su hijo.
    laura = {"Authorization": "Bearer dev:laura@test.dev"}
    me = (await client.post("/auth/accept-invitation", headers=laura)).json()
    assert [kid["id"] for kid in me["parent"]["children"]] == [child["id"]]

    off = await client.post(f"/children/{child['id']}/deactivate", headers=world.admin)
    assert off.json()["status"] == "INACTIVE"
    listed = {kid["id"]: kid for kid in (await client.get("/children", headers=world.admin)).json()}
    assert listed[child["id"]]["active"] is False


async def test_child_scope(client, world):
    assert (await client.get(f"/children/{world.sofia.id}", headers=world.mom)).status_code == 200
    assert (await client.get(f"/children/{world.renata.id}", headers=world.mom)).status_code == 404
    assert (await client.get(f"/children/{world.sofia.id}", headers=world.teacher_a)).status_code == 200
    assert (await client.get(f"/children/{world.sofia.id}", headers=world.teacher_b)).status_code == 404


async def test_parents(client, world):
    no_kids = await client.post(
        "/parents",
        headers=world.admin,
        json={"name": "Jorge", "phone": "5512345678", "email": "jorge@test.dev", "relationship": "FATHER", "child_ids": []},
    )
    assert no_kids.json()["code"] == "CHILDREN_REQUIRED"

    created = await client.post(
        "/parents",
        headers=world.admin,
        json={
            "name": "Jorge",
            "surname": "Márquez",
            "phone": "5512345678",
            "email": "jorge@test.dev",
            "relationship": "FATHER",
            "child_ids": [world.sofia.id],
        },
    )
    assert created.status_code == 201
    parent = created.json()
    assert parent["child_ids"] == [world.sofia.id]
    assert parent["has_access"] is True and parent["has_account"] is False

    resent = await client.post(f"/parents/{parent['id']}/resend-invitation", headers=world.admin)
    assert resent.status_code == 200
    already = await client.post(f"/parents/{world.mom_id}/resend-invitation", headers=world.admin)
    assert already.json()["code"] == "INVITATION_ALREADY_ACCEPTED"

    off = await client.post(f"/parents/{world.mom_id}/deactivate", headers=world.admin)
    assert off.json()["has_access"] is False
    assert len((await client.get("/parents", headers=world.admin)).json()) == 3


async def test_announcement(client, world):
    empty = await client.post("/announcements", headers=world.admin, json={"text": " "})
    assert empty.json()["code"] == "TEXT_REQUIRED"
    created = await client.post(
        "/announcements", headers=world.admin, json={"text": "Mañana no hay servicio", "classroom_id": world.room_a.id}
    )
    assert created.status_code == 201
    assert created.json()["classroom_id"] == world.room_a.id
