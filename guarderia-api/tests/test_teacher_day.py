from datetime import UTC, datetime

from tests.conftest import SCHOOL, TODAY

# los mismos casos que CheckInRulesTest, RoutineRulesTest y ReportValidatorTest de la app.

AT_SCHOOL = {"latitude": 25.651708, "longitude": SCHOOL[1]}  # unos 12 m al norte


async def test_check_in_on_time(client, world):
    assert (await client.get("/staff-attendance/today", headers=world.teacher_a)).json() is None
    response = await client.post("/staff-attendance/check-in", headers=world.teacher_a, json=AT_SCHOOL)
    assert response.status_code == 201
    record = response.json()
    assert record["status"] == "ON_TIME"
    assert record["minutes_late"] == 0
    assert record["distance_m"] == 12
    assert record["place_name"] == "La Oruga"
    assert (await client.get("/staff-attendance/today", headers=world.teacher_a)).json()["id"] == record["id"]

    # repetirla no duplica.
    again = await client.post("/staff-attendance/check-in", headers=world.teacher_a, json=AT_SCHOOL)
    assert again.status_code == 200 and again.json()["id"] == record["id"]

    teachers = {t["id"]: t for t in (await client.get("/teachers", headers=world.admin)).json()}
    assert teachers[str(world.teacher_a_id)]["checked_in_at"] is not None


async def test_check_in_late(client, world, fixed_clock):
    fixed_clock(datetime(2026, 10, 14, 14, 19, tzinfo=UTC))  # 8:19 en Monterrey
    response = await client.post(
        "/staff-attendance/check-in", headers=world.teacher_a, json={**AT_SCHOOL, "late_reason": "Tráfico"}
    )
    assert response.status_code == 201
    assert response.json()["status"] == "LATE"
    assert response.json()["minutes_late"] == 19
    assert response.json()["late_reason"] == "Tráfico"


async def test_check_in_out_of_range(client, world):
    response = await client.post(
        "/staff-attendance/check-in", headers=world.teacher_a, json={"latitude": 25.6642, "longitude": SCHOOL[1]}
    )
    assert response.status_code == 422
    body = response.json()
    assert body["code"] == "OUT_OF_RANGE"
    assert body["distance_m"] > 1300 and body["allowed_radius_m"] == 60
    # fuera del radio no se guarda nada.
    assert (await client.get("/staff-attendance/today", headers=world.teacher_a)).json() is None

    notice = await client.post(
        "/staff-attendance/notify-coordination", headers=world.teacher_a, json={"reason": "OUT_OF_RANGE"}
    )
    assert notice.status_code == 201


async def test_check_in_starts_the_routine(client, world):
    await client.post("/staff-attendance/check-in", headers=world.teacher_a, json=AT_SCHOOL)
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    assert routine["name"] == "Día completo · Preescolar"
    assert [m["status"] for m in routine["moments"]] == ["ACTIVE", "PENDING", "PENDING"]
    assert routine["current_step"] == 1 and routine["total_steps"] == 3


async def test_confirm_moves_the_day(client, world):
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    first, second, third = (m["id"] for m in routine["moments"])

    not_active = await client.post(f"/routine-moments/{second}/confirm", headers=world.teacher_a)
    assert not_active.status_code == 409 and not_active.json()["code"] == "MOMENT_NOT_ACTIVE"

    after = (await client.post(f"/routine-moments/{first}/confirm", headers=world.teacher_a)).json()
    assert [m["status"] for m in after["moments"]] == ["DONE", "ACTIVE", "PENDING"]
    assert after["active_moment_id"] == second and after["current_step"] == 2

    await client.post(f"/routine-moments/{second}/confirm", headers=world.teacher_a)
    end = (await client.post(f"/routine-moments/{third}/confirm", headers=world.teacher_a)).json()
    assert end["active_moment_id"] is None and end["current_step"] == 3

    other_room = await client.post(f"/routine-moments/{first}/confirm", headers=world.teacher_b)
    assert other_room.status_code == 404


async def test_my_room_and_attendance(client, world):
    room = f"/classrooms/{world.room_a.id}/children"
    kids = (await client.get(room, headers=world.teacher_a)).json()
    assert [kid["full_name"] for kid in kids] == ["Diego Álvarez", "Sofía Márquez"]
    assert all(kid["attendance"] == "PRESENT" for kid in kids)

    marked = await client.put(f"/attendance/{world.diego.id}", headers=world.teacher_a, json={"status": "ABSENT"})
    assert marked.json() == {"child_id": world.diego.id, "date": TODAY.isoformat(), "status": "ABSENT"}
    kids = {kid["id"]: kid for kid in (await client.get(room, headers=world.teacher_a)).json()}
    assert kids[world.diego.id]["attendance"] == "ABSENT"

    assert (await client.get(room, headers=world.teacher_b)).status_code == 404
    other = await client.put(f"/attendance/{world.renata.id}", headers=world.teacher_a, json={"status": "ABSENT"})
    assert other.status_code == 404


async def test_report_rules(client, world):
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    active, pending = routine["moments"][0]["id"], routine["moments"][1]["id"]
    report = {
        "routine_moment_id": active,
        "type": "INCIDENT",
        "method": "WRITTEN",
        "description": "Raspón en la rodilla",
        "child_ids": [world.sofia.id],
    }

    short = await client.post("/reports", headers=world.teacher_a, json={**report, "description": "   corto   "})
    assert short.json()["code"] == "DESCRIPTION_TOO_SHORT"
    nobody = await client.post("/reports", headers=world.teacher_a, json={**report, "child_ids": []})
    assert nobody.json()["code"] == "CHILDREN_REQUIRED"
    not_started = await client.post("/reports", headers=world.teacher_a, json={**report, "routine_moment_id": pending})
    assert not_started.status_code == 409 and not_started.json()["code"] == "MOMENT_NOT_STARTED"

    await client.put(f"/attendance/{world.diego.id}", headers=world.teacher_a, json={"status": "ABSENT"})
    absent = await client.post("/reports", headers=world.teacher_a, json={**report, "child_ids": [world.diego.id]})
    assert absent.status_code == 409 and absent.json()["code"] == "CHILD_ABSENT"
    assert absent.json()["child_ids"] == [world.diego.id]

    created = await client.post("/reports", headers=world.teacher_a, json=report)
    assert created.status_code == 201
    assert created.json()["child_ids"] == [world.sofia.id] and created.json()["has_photo"] is False

    listed = (await client.get("/reports", params={"moment_id": active}, headers=world.teacher_a)).json()
    assert len(listed) == 1
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    assert routine["moments"][0]["report_count"] == 1


async def test_my_room_marks_children_with_a_note_from_the_family_today(client, world, fixed_clock):
    def by_name(children):
        return {child["name"]: child for child in children}

    before = by_name((await client.get(f"/classrooms/{world.room_a.id}/children", headers=world.teacher_a)).json())
    assert before["Sofía"]["has_note_today"] is False and before["Sofía"]["notes_today"] == 0

    for text in ("Hoy no durmió bien.", "Se queda con su abuela a la salida."):
        sent = await client.post(f"/children/{world.sofia.id}/notes", headers=world.mom, json={"text": text})
        assert sent.status_code == 201

    after = by_name((await client.get(f"/classrooms/{world.room_a.id}/children", headers=world.teacher_a)).json())
    assert after["Sofía"]["has_note_today"] is True and after["Sofía"]["notes_today"] == 2
    assert after["Diego"]["has_note_today"] is False  # solo marca a quien recibió la nota

    # al día siguiente la nota ya no cuenta como "de hoy".
    fixed_clock(datetime(2026, 10, 15, 14, 0, tzinfo=UTC))
    tomorrow = by_name((await client.get(f"/classrooms/{world.room_a.id}/children", headers=world.teacher_a)).json())
    assert tomorrow["Sofía"]["has_note_today"] is False


async def test_a_note_for_one_room_does_not_mark_the_other(client, world):
    await client.post(f"/children/{world.renata.id}/notes", headers=world.dad, json={"text": "Llega tarde."})
    room_a = (await client.get(f"/classrooms/{world.room_a.id}/children", headers=world.teacher_a)).json()
    room_b = (await client.get(f"/classrooms/{world.room_b.id}/children", headers=world.teacher_b)).json()
    assert not any(child["has_note_today"] for child in room_a)
    assert [child["has_note_today"] for child in room_b] == [True]
