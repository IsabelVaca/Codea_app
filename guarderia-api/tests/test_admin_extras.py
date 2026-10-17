from tests.conftest import as_user  # noqa: F401


# --- centro ---


async def test_center_get_and_update(client, world):
    got = await client.get("/center", headers=world.admin)
    assert got.status_code == 200
    assert got.json()["name"] == "La Oruga"

    changed = await client.patch(
        "/center", headers=world.admin, json={"latitude": 25.7, "longitude": -100.3, "check_in_radius_m": 120}
    )
    assert changed.status_code == 200
    assert changed.json()["check_in_radius_m"] == 120
    assert changed.json()["latitude"] == 25.7

    # lo que no se mandó no cambia.
    assert changed.json()["name"] == "La Oruga"


async def test_center_validations(client, world):
    cases = [
        ({"check_in_radius_m": 5}, "RADIUS_INVALID"),
        ({"check_in_radius_m": 5000}, "RADIUS_INVALID"),
        ({"latitude": 95, "longitude": -100}, "LOCATION_INVALID"),
        ({"latitude": None}, "LOCATION_INCOMPLETE"),
        ({"timezone": "Marte/Olimpo"}, "TIMEZONE_INVALID"),
        ({"name": "  "}, "NAME_REQUIRED"),
    ]
    for body, code in cases:
        response = await client.patch("/center", headers=world.admin, json=body)
        assert response.status_code == 422, body
        assert response.json()["code"] == code, body


async def test_center_is_admin_only(client, world):
    assert (await client.get("/center", headers=world.teacher_a)).status_code == 403
    assert (await client.patch("/center", headers=world.mom, json={"name": "X"})).status_code == 403


# --- invitaciones ---


async def test_invite_new_admin_and_list(client, world):
    created = await client.post(
        "/invitations",
        headers=world.admin,
        json={"name": "Lucía", "surname": "Ríos", "phone": "8112345678", "email": "Lucia@Test.dev", "role": "ADMIN"},
    )
    assert created.status_code == 201
    body = created.json()
    assert body["email"] == "lucia@test.dev"
    assert body["role"] == "ADMIN"
    assert body["status"] == "PENDING"

    pending = (await client.get("/invitations?status=PENDING", headers=world.admin)).json()
    assert [item["email"] for item in pending] == ["lucia@test.dev"]
    assert (await client.get("/invitations?status=ACCEPTED", headers=world.admin)).json() == []

    # al activar su cuenta con el código, la nueva administradora ya tiene su rol.
    assert body["access_code"]["purpose"] == "INVITATION"
    accepted = await client.post(
        "/auth/activate",
        json={"email": "lucia@test.dev", "code": body["access_code"]["code"], "password": "una-clave-larga-1"},
    )
    assert accepted.status_code == 200
    assert accepted.json()["me"]["roles"] == ["ADMIN"]
    done = (await client.get("/invitations?status=ACCEPTED", headers=world.admin)).json()
    assert [item["email"] for item in done] == ["lucia@test.dev"]


async def test_invite_teacher_and_parent_roles(client, world):
    for role, email in (("TEACHER", "nueva.docente@test.dev"), ("PARENT", "nuevo.padre@test.dev")):
        response = await client.post(
            "/invitations",
            headers=world.admin,
            json={"name": "Persona", "phone": "8112345678", "email": email, "role": role},
        )
        assert response.status_code == 201, response.text
    by_role = (await client.get("/invitations?role=PARENT", headers=world.admin)).json()
    assert [item["email"] for item in by_role] == ["nuevo.padre@test.dev"]


async def test_invite_validations(client, world):
    base = {"name": "Persona", "phone": "8112345678", "email": "x@test.dev", "role": "ADMIN"}
    bad_email = await client.post("/invitations", headers=world.admin, json={**base, "email": "sin-arroba"})
    assert bad_email.json()["code"] == "EMAIL_INVALID"
    bad_phone = await client.post("/invitations", headers=world.admin, json={**base, "phone": "123"})
    assert bad_phone.json()["code"] == "PHONE_INVALID"
    no_name = await client.post("/invitations", headers=world.admin, json={**base, "name": " "})
    assert no_name.json()["code"] == "NAME_REQUIRED"


async def test_invite_existing_account_is_409(client, world):
    # karla ya es docente y ya tiene cuenta.
    again = await client.post(
        "/invitations",
        headers=world.admin,
        json={"name": "Karla", "phone": "8112345678", "email": "karla@test.dev", "role": "TEACHER"},
    )
    assert again.status_code == 409
    assert again.json()["code"] == "EMAIL_TAKEN"
    other_role = await client.post(
        "/invitations",
        headers=world.admin,
        json={"name": "Karla", "phone": "8112345678", "email": "karla@test.dev", "role": "ADMIN"},
    )
    assert other_role.status_code == 409
    assert other_role.json()["code"] == "ALREADY_HAS_ACCOUNT"


async def test_invitations_are_admin_only(client, world):
    assert (await client.get("/invitations", headers=world.teacher_a)).status_code == 403
    denied = await client.post(
        "/invitations",
        headers=world.mom,
        json={"name": "X", "phone": "8112345678", "email": "x@test.dev", "role": "ADMIN"},
    )
    assert denied.status_code == 403


# --- plantillas de rutina ---


async def test_templates_list_create_update_assign(client, world):
    listed = (await client.get("/routine-templates", headers=world.admin)).json()
    assert len(listed) == 1
    assert listed[0]["is_default"] is True
    assert [m["name"] for m in listed[0]["moments"]] == ["Recepción", "Desayuno", "Siesta"]

    created = await client.post(
        "/routine-templates",
        headers=world.admin,
        json={
            "name": "Medio día",
            "moments": [{"name": "Recepción", "category": "ARRIVAL"}, {"name": "Salida", "category": "PICKUP"}],
        },
    )
    assert created.status_code == 201
    template = created.json()
    assert [m["number"] for m in template["moments"]] == [1, 2]
    assert template["is_default"] is False

    assigned = await client.put(
        f"/classrooms/{world.room_b.id}/routine-template", headers=world.admin, json={"template_id": template["id"]}
    )
    assert assigned.status_code == 200
    assert assigned.json()["template_id"] == template["id"]

    # la sala ya usa la plantilla nueva y la docente ve esos momentos.
    routine = (await client.get(f"/classrooms/{world.room_b.id}/routine", headers=world.teacher_b)).json()
    assert [m["name"] for m in routine["moments"]] == ["Recepción", "Salida"]

    updated = await client.put(
        f"/routine-templates/{template['id']}",
        headers=world.admin,
        json={"name": "Medio día v2", "moments": [{"name": "Juego libre", "category": "ACTIVITY"}]},
    )
    assert updated.status_code == 200
    assert updated.json()["name"] == "Medio día v2"
    assert [m["name"] for m in updated.json()["moments"]] == ["Juego libre"]
    assert [c["name"] for c in updated.json()["classrooms"]] == ["Maternal"]

    # null deja a la sala con la default.
    back = await client.put(
        f"/classrooms/{world.room_b.id}/routine-template", headers=world.admin, json={"template_id": None}
    )
    assert back.json()["template_id"] is None


async def test_template_default_swap(client, world):
    created = await client.post(
        "/routine-templates",
        headers=world.admin,
        json={"name": "Nueva default", "is_default": True, "moments": [{"name": "Inicio", "category": "ARRIVAL"}]},
    )
    assert created.status_code == 201
    listed = (await client.get("/routine-templates", headers=world.admin)).json()
    assert [t["is_default"] for t in listed].count(True) == 1
    assert listed[0]["name"] == "Nueva default"

    # la vieja default ya no lo es; la nueva no se puede dejar sin default.
    unset = await client.put(
        f"/routine-templates/{created.json()['id']}",
        headers=world.admin,
        json={"name": "Nueva default", "moments": [{"name": "Inicio", "category": "ARRIVAL"}]},
    )
    assert unset.status_code == 409
    assert unset.json()["code"] == "DEFAULT_REQUIRED"


async def test_template_validations(client, world):
    no_moments = await client.post("/routine-templates", headers=world.admin, json={"name": "Vacía", "moments": []})
    assert no_moments.json()["code"] == "MOMENTS_REQUIRED"
    no_name = await client.post(
        "/routine-templates", headers=world.admin, json={"name": " ", "moments": [{"name": "A", "category": "MEAL"}]}
    )
    assert no_name.json()["code"] == "NAME_REQUIRED"
    missing = await client.put(
        "/routine-templates/999", headers=world.admin, json={"name": "X", "moments": [{"name": "A", "category": "MEAL"}]}
    )
    assert missing.status_code == 404
    assert missing.json()["code"] == "TEMPLATE_NOT_FOUND"
    bad_assign = await client.put(
        f"/classrooms/{world.room_a.id}/routine-template", headers=world.admin, json={"template_id": 999}
    )
    assert bad_assign.json()["code"] == "TEMPLATE_NOT_FOUND"


async def test_templates_are_admin_only(client, world):
    assert (await client.get("/routine-templates", headers=world.teacher_a)).status_code == 403
    denied = await client.put(
        f"/classrooms/{world.room_a.id}/routine-template", headers=world.teacher_a, json={"template_id": None}
    )
    assert denied.status_code == 403
