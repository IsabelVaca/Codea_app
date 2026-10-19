"""Mi hijo (salud que escribe la familia) y el pase de salida de la docente."""

from sqlalchemy import select

from app.modules.audit.models import AuditLog
from app.modules.documents.models import AuthorizedPickup
from tests.conftest import mark_present


# ---------------------------------------------------------------- Mi hijo: salud del niño


async def test_the_family_writes_the_health_of_their_child(client, world):
    path = f"/children/{world.sofia.id}/health"
    saved = await client.patch(
        path,
        headers=world.mom,
        json={
            "allergies": "  Nueces  ",
            "medical_conditions": "Asma leve",
            "intolerances": "Lactosa",
            "medications": "Inhalador en la mochila",
            "other_notes": "Le da miedo la oscuridad",
        },
    )
    assert saved.status_code == 200
    body = saved.json()
    assert body["allergies"] == "Nueces"
    assert (body["medical_conditions"], body["intolerances"]) == ("Asma leve", "Lactosa")
    assert (body["medications"], body["other_notes"]) == ("Inhalador en la mochila", "Le da miedo la oscuridad")

    # un campo ausente no se toca; uno vacío se borra.
    partial = await client.patch(path, headers=world.mom, json={"intolerances": ""})
    assert partial.json()["intolerances"] is None
    assert partial.json()["allergies"] == "Nueces"

    # la docente de la sala lo ve en la ficha, y la familia en su propio expediente.
    for headers in (world.teacher_a, world.mom):
        detail = (await client.get(f"/children/{world.sofia.id}", headers=headers)).json()
        assert detail["medical_conditions"] == "Asma leve" and detail["medications"] == "Inhalador en la mochila"


async def test_health_is_private_and_limited(client, world):
    path = f"/children/{world.sofia.id}/health"
    # otra familia no ve ni escribe; la docente solo lee.
    assert (await client.patch(path, headers=world.dad, json={"allergies": "x"})).status_code == 404
    assert (await client.patch(path, headers=world.teacher_a, json={"allergies": "x"})).status_code == 403
    assert (await client.patch(path, headers=world.admin, json={"allergies": "Polen"})).json()["allergies"] == "Polen"
    too_long = await client.patch(path, headers=world.mom, json={"other_notes": "a" * 1001})
    assert too_long.status_code == 422 and too_long.json()["code"] == "TEXT_TOO_LONG"


async def test_health_changes_are_audited_without_the_content(client, world, db):
    await client.patch(f"/children/{world.sofia.id}/health", headers=world.mom, json={"allergies": "Penicilina"})
    row = await db.scalar(select(AuditLog).where(AuditLog.action == "CHILD_HEALTH_UPDATED"))
    assert row is not None and row.detail == {"fields": ["allergies"]}
    assert "Penicilina" not in str(row.detail)


# ---------------------------------------------------------------- pase de salida


async def _pickup(db, world, child, name="Andrea Torres") -> int:
    pickup = AuthorizedPickup(child_id=child.id, full_name=name, relationship="Tía", added_by=world.admin_id)
    db.add(pickup)
    await db.commit()
    return pickup.id


async def test_the_sheet_lists_only_present_children_with_who_can_pick_them_up(client, world, db):
    await mark_present(client, world, world.sofia)
    await _pickup(db, world, world.sofia)
    sheet = (await client.get(f"/classrooms/{world.room_a.id}/departures", headers=world.teacher_a)).json()
    assert [c["full_name"] for c in sheet["children"]] == ["Sofía Márquez"]
    assert sheet["pending"] == 1 and sheet["all_done"] is False
    choices = sheet["children"][0]["choices"]
    # las autorizadas que registró el admin, y la mamá como tutora.
    assert [(c["kind"], c["full_name"]) for c in choices] == [
        ("AUTHORIZED", "Andrea Torres"),
        ("GUARDIAN", "Mariana Prueba"),
    ]
    assert choices[1]["relationship"] == "MOTHER"


async def test_register_every_departure_and_the_sheet_closes(client, world, db):
    await mark_present(client, world, world.sofia, world.diego)
    pickup_id = await _pickup(db, world, world.sofia)

    first = await client.post(
        f"/children/{world.sofia.id}/departure", headers=world.teacher_a, json={"pickup_id": pickup_id}
    )
    assert first.status_code == 201
    assert first.json()["picked_up_by"] == "Andrea Torres" and first.json()["departed_at"].startswith("2026-10-14T13:52")

    sheet = (await client.get(f"/classrooms/{world.room_a.id}/departures", headers=world.teacher_a)).json()
    assert sheet["pending"] == 1 and sheet["all_done"] is False

    # Diego no tiene personas autorizadas: sale con un tutor; aquí con la mamá de Sofía no (no es su tutora).
    mom_of_other = await client.post(
        f"/children/{world.diego.id}/departure", headers=world.teacher_a, json={"guardian_id": str(world.mom_id)}
    )
    assert mom_of_other.status_code == 404 and mom_of_other.json()["code"] == "PICKUP_NOT_FOUND"

    other_pickup = await _pickup(db, world, world.diego, "Luis Torres")
    assert (
        await client.post(
            f"/children/{world.diego.id}/departure", headers=world.teacher_a, json={"pickup_id": other_pickup}
        )
    ).status_code == 201
    sheet = (await client.get(f"/classrooms/{world.room_a.id}/departures", headers=world.teacher_a)).json()
    assert sheet["pending"] == 0 and sheet["all_done"] is True


async def test_departure_rules(client, world, db):
    path = f"/children/{world.sofia.id}/departure"
    pickup_id = await _pickup(db, world, world.sofia)

    # sin pase de lista no hay salida.
    absent = await client.post(path, headers=world.teacher_a, json={"pickup_id": pickup_id})
    assert absent.status_code == 409 and absent.json()["code"] == "CHILD_NOT_PRESENT"

    await mark_present(client, world, world.sofia)
    nobody = await client.post(path, headers=world.teacher_a, json={})
    assert nobody.status_code == 422 and nobody.json()["code"] == "PICKUP_REQUIRED"
    both = await client.post(
        path, headers=world.teacher_a, json={"pickup_id": pickup_id, "guardian_id": str(world.mom_id)}
    )
    assert both.json()["code"] == "PICKUP_REQUIRED"

    # una persona autorizada de otro niño no sirve.
    foreign = await _pickup(db, world, world.diego, "Luis Torres")
    wrong = await client.post(path, headers=world.teacher_a, json={"pickup_id": foreign})
    assert wrong.status_code == 404 and wrong.json()["code"] == "PICKUP_NOT_FOUND"

    guardian = await client.post(path, headers=world.teacher_a, json={"guardian_id": str(world.mom_id)})
    assert guardian.status_code == 201 and guardian.json()["picked_up_by"] == "Mariana Prueba"
    again = await client.post(path, headers=world.teacher_a, json={"pickup_id": pickup_id})
    assert again.status_code == 409 and again.json()["code"] == "ALREADY_DEPARTED"

    # se puede corregir: se quita la salida y se registra bien.
    assert (await client.delete(path, headers=world.teacher_a)).status_code == 204
    assert (await client.delete(path, headers=world.teacher_a)).json()["code"] == "DEPARTURE_NOT_FOUND"
    fixed = await client.post(path, headers=world.teacher_a, json={"pickup_id": pickup_id})
    assert fixed.json()["picked_up_by"] == "Andrea Torres"


async def test_the_departure_keeps_the_name_if_the_person_is_removed(client, world, db):
    await mark_present(client, world, world.sofia)
    pickup_id = await _pickup(db, world, world.sofia)
    await client.post(f"/children/{world.sofia.id}/departure", headers=world.teacher_a, json={"pickup_id": pickup_id})
    assert (
        await client.delete(f"/children/{world.sofia.id}/authorized-pickups/{pickup_id}", headers=world.admin)
    ).status_code == 204
    sheet = (await client.get(f"/classrooms/{world.room_a.id}/departures", headers=world.teacher_a)).json()
    assert sheet["children"][0]["departure"]["picked_up_by"] == "Andrea Torres"


async def test_departures_respect_roles_and_rooms(client, world, db):
    await mark_present(client, world, world.sofia)
    pickup_id = await _pickup(db, world, world.sofia)
    path = f"/children/{world.sofia.id}/departure"
    # la familia y otra docente no registran; la sala de otra docente no se consulta.
    assert (await client.post(path, headers=world.mom, json={"pickup_id": pickup_id})).status_code == 403
    assert (await client.post(path, headers=world.teacher_b, json={"pickup_id": pickup_id})).status_code == 404
    assert (await client.get(f"/classrooms/{world.room_a.id}/departures", headers=world.mom)).status_code == 403
    assert (await client.get(f"/classrooms/{world.room_a.id}/departures", headers=world.teacher_b)).status_code == 404
    assert (await client.get(f"/classrooms/{world.room_a.id}/departures", headers=world.admin)).status_code == 200


# ---------------------------------------------------------------- chats: de quién es cada conversación


async def test_the_inbox_says_whose_parent_each_chat_is(client, world):
    inbox = (await client.get("/conversations", headers=world.teacher_a)).json()
    by_child = {c["child"]["full_name"]: c["child"]["guardians"] for c in inbox}
    # Sofía es de la sala de Karla; su mamá es Mariana. Diego no tiene tutor registrado.
    assert by_child["Sofía Márquez"] == [{"full_name": "Mariana Prueba", "relationship": "MOTHER", "is_primary": True}]
    assert by_child["Diego Álvarez"] == []
    # la otra sala no se mezcla.
    assert "Renata Gómez" not in by_child


async def test_the_family_sees_when_the_child_left_and_with_whom(client, world, db):
    await mark_present(client, world, world.sofia)
    pickup_id = await _pickup(db, world, world.sofia)

    before = (await client.get(f"/children/{world.sofia.id}/day", headers=world.mom)).json()
    assert all(event["type"] != "DEPARTURE" for event in before["timeline"])

    assert (
        await client.post(f"/children/{world.sofia.id}/departure", headers=world.teacher_a, json={"pickup_id": pickup_id})
    ).status_code == 201
    day = (await client.get(f"/children/{world.sofia.id}/day", headers=world.mom)).json()
    last = day["timeline"][-1]
    assert (last["type"], last["title"], last["description"]) == ("DEPARTURE", "Salida", "Lo recogió Andrea Torres.")
    assert last["time"].startswith("2026-10-14T13:52")

    # si la docente se equivocó y lo corrige, desaparece.
    assert (await client.delete(f"/children/{world.sofia.id}/departure", headers=world.teacher_a)).status_code == 204
    after = (await client.get(f"/children/{world.sofia.id}/day", headers=world.mom)).json()
    assert all(event["type"] != "DEPARTURE" for event in after["timeline"])
