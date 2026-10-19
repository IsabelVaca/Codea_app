from datetime import UTC, datetime

from tests.conftest import TODAY, mark_present

# los mismos casos que CheckInRulesTest, RoutineRulesTest y ReportValidatorTest de la app.

# el checador ya no manda ubicación: solo el motivo del retardo, si lo hay.


async def test_check_in_on_time(client, world):
    assert (await client.get("/staff-attendance/today", headers=world.teacher_a)).json() is None
    response = await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})
    assert response.status_code == 201
    record = response.json()
    assert record["status"] == "ON_TIME"
    assert record["minutes_late"] == 0
    assert "distance_m" not in record and "latitude" not in record  # no se guarda ubicación
    assert record["auto_closed"] is False
    assert record["place_name"] == "La Oruga"
    assert (await client.get("/staff-attendance/today", headers=world.teacher_a)).json()["id"] == record["id"]

    # repetirla no duplica.
    again = await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})
    assert again.status_code == 200 and again.json()["id"] == record["id"]

    teachers = {t["id"]: t for t in (await client.get("/teachers", headers=world.admin)).json()}
    assert teachers[str(world.teacher_a_id)]["checked_in_at"] is not None


async def test_check_in_late(client, world, fixed_clock):
    fixed_clock(datetime(2026, 10, 14, 14, 19, tzinfo=UTC))  # 8:19 en Monterrey
    response = await client.post(
        "/staff-attendance/check-in", headers=world.teacher_a, json={"late_reason": "Tráfico"}
    )
    assert response.status_code == 201
    assert response.json()["status"] == "LATE"
    assert response.json()["minutes_late"] == 19
    assert response.json()["late_reason"] == "Tráfico"


async def test_check_in_ignores_any_location(client, world):
    """Sin ubicación: aunque el teléfono mande coordenadas lejanas, la entrada se registra y no se guardan."""
    response = await client.post(
        "/staff-attendance/check-in", headers=world.teacher_a, json={"latitude": 25.6642, "longitude": -100.2896}
    )
    assert response.status_code == 201
    assert "latitude" not in response.json() and "distance_m" not in response.json()


async def test_notify_coordination_only_for_late(client, world):
    ok = await client.post("/staff-attendance/notify-coordination", headers=world.teacher_a, json={"reason": "LATE"})
    assert ok.status_code == 201
    gone = await client.post(
        "/staff-attendance/notify-coordination", headers=world.teacher_a, json={"reason": "OUT_OF_RANGE"}
    )
    assert gone.status_code == 422


async def test_check_in_starts_the_routine(client, world):
    await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})
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
    # sin pase de lista, todos faltan: la docente los va marcando presentes en Mi día.
    assert all(kid["attendance"] == "ABSENT" for kid in kids)

    marked = await client.put(f"/attendance/{world.diego.id}", headers=world.teacher_a, json={"status": "PRESENT"})
    assert marked.json() == {"child_id": world.diego.id, "date": TODAY.isoformat(), "status": "PRESENT"}
    kids = {kid["id"]: kid for kid in (await client.get(room, headers=world.teacher_a)).json()}
    assert kids[world.diego.id]["attendance"] == "PRESENT"
    assert kids[world.sofia.id]["attendance"] == "ABSENT"

    # corregir una marca: de presente vuelve a ausente.
    await client.put(f"/attendance/{world.diego.id}", headers=world.teacher_a, json={"status": "ABSENT"})
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

    # Diego no tiene pase de lista: sin registro cuenta como ausente y no puede estar en un reporte.
    absent = await client.post("/reports", headers=world.teacher_a, json={**report, "child_ids": [world.diego.id]})
    assert absent.status_code == 409 and absent.json()["code"] == "CHILD_ABSENT"
    assert absent.json()["child_ids"] == [world.diego.id]

    # y tampoco si se le marcó ausente a propósito.
    await client.put(f"/attendance/{world.diego.id}", headers=world.teacher_a, json={"status": "ABSENT"})
    marked_absent = await client.post("/reports", headers=world.teacher_a, json={**report, "child_ids": [world.diego.id]})
    assert marked_absent.status_code == 409 and marked_absent.json()["code"] == "CHILD_ABSENT"

    await mark_present(client, world, world.sofia)

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
    # Mi sala trae el texto de cada aviso, en orden, para leerlo sin abrir otra pantalla.
    assert [note["text"] for note in after["Sofía"]["notes"]] == ["Hoy no durmió bien.", "Se queda con su abuela a la salida."]
    assert after["Sofía"]["notes"][0]["created_at"] and after["Diego"]["notes"] == []

    # al día siguiente la nota ya no cuenta como "de hoy".
    fixed_clock(datetime(2026, 10, 15, 14, 0, tzinfo=UTC))
    tomorrow = by_name((await client.get(f"/classrooms/{world.room_a.id}/children", headers=world.teacher_a)).json())
    assert tomorrow["Sofía"]["has_note_today"] is False


async def test_my_room_says_who_already_has_the_daily_summary(client, world):
    room = f"/classrooms/{world.room_a.id}/children"
    before = {kid["name"]: kid for kid in (await client.get(room, headers=world.teacher_a)).json()}
    assert before["Sofía"]["has_summary_today"] is False

    await client.post(f"/children/{world.sofia.id}/daily-entries", headers=world.teacher_a, json={"mood": "HAPPY"})
    after = {kid["name"]: kid for kid in (await client.get(room, headers=world.teacher_a)).json()}
    assert after["Sofía"]["has_summary_today"] is True and after["Diego"]["has_summary_today"] is False


async def test_a_note_for_one_room_does_not_mark_the_other(client, world):
    await client.post(f"/children/{world.renata.id}/notes", headers=world.dad, json={"text": "Llega tarde."})
    room_a = (await client.get(f"/classrooms/{world.room_a.id}/children", headers=world.teacher_a)).json()
    room_b = (await client.get(f"/classrooms/{world.room_b.id}/children", headers=world.teacher_b)).json()
    assert not any(child["has_note_today"] for child in room_a)
    assert [child["has_note_today"] for child in room_b] == [True]


# ---------------------------------------------------------------- salida de la docente


async def test_check_out_needs_a_check_in_first(client, world):
    response = await client.post("/staff-attendance/check-out", headers=world.teacher_a)
    assert response.status_code == 409 and response.json()["code"] == "NOT_CHECKED_IN"


async def test_check_out_after_check_in(client, world, fixed_clock):
    assert (await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})).status_code == 201
    today = (await client.get("/staff-attendance/today", headers=world.teacher_a)).json()
    assert today["checked_out_at"] is None

    fixed_clock(datetime(2026, 10, 14, 22, 5, tzinfo=UTC))  # 16:05 en Monterrey
    out = await client.post("/staff-attendance/check-out", headers=world.teacher_a)
    assert out.status_code == 201
    assert out.json()["checked_out_at"].startswith("2026-10-14T22:05")
    assert out.json()["status"] in ("ON_TIME", "LATE")  # la entrada no cambia

    today = (await client.get("/staff-attendance/today", headers=world.teacher_a)).json()
    assert today["checked_out_at"] == out.json()["checked_out_at"]


async def test_check_out_twice_returns_the_same_exit(client, world, fixed_clock):
    await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})
    fixed_clock(datetime(2026, 10, 14, 22, 5, tzinfo=UTC))
    first = await client.post("/staff-attendance/check-out", headers=world.teacher_a)
    fixed_clock(datetime(2026, 10, 14, 22, 45, tzinfo=UTC))
    again = await client.post("/staff-attendance/check-out", headers=world.teacher_a)
    assert first.status_code == 201 and again.status_code == 200
    assert again.json()["checked_out_at"] == first.json()["checked_out_at"]  # no se pisa la hora


# ---------------------------------------------------------------- cierre automático a las 9 pm


async def test_open_check_in_closes_at_9pm(client, world, fixed_clock):
    await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})

    fixed_clock(datetime(2026, 10, 15, 2, 59, tzinfo=UTC))  # 20:59 en Monterrey: todavía abierta
    today = (await client.get("/staff-attendance/today", headers=world.teacher_a)).json()
    assert today["checked_out_at"] is None and today["auto_closed"] is False

    fixed_clock(datetime(2026, 10, 15, 3, 0, tzinfo=UTC))  # 21:00 en Monterrey
    today = (await client.get("/staff-attendance/today", headers=world.teacher_a)).json()
    assert today["auto_closed"] is True
    assert today["checked_out_at"].startswith("2026-10-15T03:00")  # la salida queda a las 21:00 locales


async def test_check_out_after_the_cutoff_returns_the_automatic_exit(client, world, fixed_clock):
    await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})
    fixed_clock(datetime(2026, 10, 15, 4, 30, tzinfo=UTC))  # 22:30 en Monterrey
    out = await client.post("/staff-attendance/check-out", headers=world.teacher_a)
    assert out.status_code == 200  # ya estaba cerrada: no se crea otra salida
    assert out.json()["auto_closed"] is True
    assert out.json()["checked_out_at"].startswith("2026-10-15T03:00")


async def test_a_day_left_open_is_closed_the_next_morning(client, world, fixed_clock):
    created = (await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})).json()
    fixed_clock(datetime(2026, 10, 15, 13, 0, tzinfo=UTC))  # 7:00 del día siguiente
    # "hoy" ya es otro día, pero el registro de ayer quedó cerrado a las 21:00 de ayer.
    assert (await client.get("/staff-attendance/today", headers=world.teacher_a)).json() is None
    teachers = await client.get("/teachers", headers=world.admin)
    assert teachers.status_code == 200
    again = await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})
    assert again.status_code == 201 and again.json()["id"] != created["id"]


async def test_a_manual_exit_is_not_overwritten_by_the_cutoff(client, world, fixed_clock):
    await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})
    fixed_clock(datetime(2026, 10, 14, 22, 5, tzinfo=UTC))  # 16:05
    manual = (await client.post("/staff-attendance/check-out", headers=world.teacher_a)).json()
    fixed_clock(datetime(2026, 10, 15, 5, 0, tzinfo=UTC))  # pasadas las 9 pm
    today = (await client.get("/staff-attendance/today", headers=world.teacher_a)).json()
    assert today["checked_out_at"] == manual["checked_out_at"] and today["auto_closed"] is False


async def test_check_in_after_the_cutoff_is_not_closed_before_it_starts(client, world, fixed_clock):
    fixed_clock(datetime(2026, 10, 15, 3, 30, tzinfo=UTC))  # 21:30 en Monterrey
    record = (await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})).json()
    today = (await client.get("/staff-attendance/today", headers=world.teacher_a)).json()
    assert today["id"] == record["id"]
    assert today["checked_out_at"] >= today["checked_in_at"]  # la salida nunca queda antes de la entrada


async def test_check_out_is_per_teacher(client, world):
    await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})
    # fernando no registró entrada, así que no puede registrar la salida de karla ni la suya.
    response = await client.post("/staff-attendance/check-out", headers=world.teacher_b)
    assert response.status_code == 409
    assert (await client.get("/staff-attendance/today", headers=world.teacher_b)).json() is None



async def test_one_report_can_be_for_several_children(client, world):
    """RF-05: un solo reporte para varios niños; cada papá lo ve en su Hoy y no en el de otro."""
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    moment = routine["moments"][0]["id"]
    await mark_present(client, world, world.sofia, world.diego)
    created = await client.post(
        "/reports",
        headers=world.teacher_a,
        json={
            "routine_moment_id": moment,
            "type": "ACHIEVEMENT",
            "method": "WRITTEN",
            "description": "Armaron juntos una torre de bloques.",
            "child_ids": [world.sofia.id, world.diego.id],
        },
    )
    assert created.status_code == 201
    assert sorted(created.json()["child_ids"]) == sorted([world.sofia.id, world.diego.id])

    # el contador del momento es uno solo, aunque el reporte sea de dos niños.
    listed = (await client.get("/reports", params={"moment_id": moment}, headers=world.teacher_a)).json()
    assert len(listed) == 1

    # la mamá de Sofía lo ve en su Hoy.
    day = (await client.get(f"/children/{world.sofia.id}/day", headers=world.mom)).json()
    assert any("torre de bloques" in event["description"] for event in day["timeline"])
    # Renata (otra sala) no aparece en ese reporte.
    other = (await client.get(f"/children/{world.renata.id}/day", headers=world.dad)).json()
    assert not any("torre de bloques" in event["description"] for event in other["timeline"])


async def test_a_report_cannot_include_a_child_from_another_room(client, world):
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    response = await client.post(
        "/reports",
        headers=world.teacher_a,
        json={
            "routine_moment_id": routine["moments"][0]["id"],
            "type": "ACHIEVEMENT",
            "method": "WRITTEN",
            "description": "Intento con un niño que no es de mi sala.",
            "child_ids": [world.sofia.id, world.renata.id],
        },
    )
    assert response.status_code in (404, 422)


# ---------------------------------------------------------------- horario del centro


async def test_lateness_uses_the_center_entry_time(client, world):
    # el reloj de las pruebas está en las 7:52: con la entrada a las 7:30 ya es retardo.
    await client.patch("/center", headers=world.admin, json={"entry_time": "07:30"})
    record = (await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})).json()
    assert record["status"] == "LATE" and record["minutes_late"] == 22


async def test_check_in_is_rejected_on_a_non_working_day(client, world):
    # el 14 de octubre de 2026 es miércoles (3): si solo se trabaja sábado y domingo, no se puede checar.
    await client.patch("/center", headers=world.admin, json={"work_days": [6, 7]})
    response = await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})
    assert response.status_code == 409 and response.json()["code"] == "NOT_WORKING_DAY"
    assert (await client.get("/staff-attendance/today", headers=world.teacher_a)).json() is None


async def test_check_in_works_again_when_the_day_is_added(client, world):
    await client.patch("/center", headers=world.admin, json={"work_days": [6, 7]})
    assert (await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})).status_code == 409
    await client.patch("/center", headers=world.admin, json={"work_days": [3, 6, 7]})
    assert (await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})).status_code == 201


async def test_auto_close_uses_the_configured_time(client, world, fixed_clock):
    await client.patch("/center", headers=world.admin, json={"auto_close_time": "18:00"})
    await client.post("/staff-attendance/check-in", headers=world.teacher_a, json={})

    fixed_clock(datetime(2026, 10, 14, 23, 59, tzinfo=UTC))  # 17:59 en Monterrey: todavía abierta
    assert (await client.get("/staff-attendance/today", headers=world.teacher_a)).json()["auto_closed"] is False

    fixed_clock(datetime(2026, 10, 15, 0, 0, tzinfo=UTC))  # 18:00 en Monterrey
    today = (await client.get("/staff-attendance/today", headers=world.teacher_a)).json()
    assert today["auto_closed"] is True
    assert today["checked_out_at"].startswith("2026-10-15T00:00")  # la salida queda en la hora configurada

