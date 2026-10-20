from tests.conftest import mark_present


async def _start_day(client, world) -> list[int]:
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    return [moment["id"] for moment in routine["moments"]]


async def test_today_screen(client, world):
    await mark_present(client, world, world.sofia)
    arrival, breakfast, _ = await _start_day(client, world)
    await client.post(f"/routine-moments/{arrival}/confirm", headers=world.teacher_a)
    await client.post(
        "/reports",
        headers=world.teacher_a,
        json={
            "routine_moment_id": breakfast,
            "type": "ACHIEVEMENT",
            "method": "WRITTEN",
            "description": "Comió todo: fruta, avena y leche.",
            "child_ids": [world.sofia.id],
        },
    )
    entry = await client.post(
        f"/children/{world.sofia.id}/daily-entries",
        headers=world.teacher_a,
        json={"mood": "HAPPY", "meals": "ALL", "nap_minutes": 50},
    )
    assert entry.status_code == 200
    # actualizar solo un dato no borra los demás.
    entry = await client.post(
        f"/children/{world.sofia.id}/daily-entries", headers=world.teacher_a, json={"diaper_changes": 2}
    )
    assert entry.json()["mood"] == "HAPPY" and entry.json()["diaper_changes"] == 2

    note = await client.post(f"/children/{world.sofia.id}/notes", headers=world.mom, json={"text": "Hoy la recoge su abuela"})
    assert note.status_code == 201

    day = (await client.get(f"/children/{world.sofia.id}/day", headers=world.mom)).json()
    assert day["child"]["center_name"] == "La Oruga"
    assert day["attendance"] == "PRESENT"
    assert day["summary"]["nap_minutes"] == 50
    assert [(event["type"], event["title"]) for event in day["timeline"]] == [
        ("ARRIVAL", "Entrada"),
        ("CHECK_IN", "Recepción"),
        ("MEAL", "Desayuno"),
    ]
    assert day["timeline"][2]["description"] == "Comió todo: fruta, avena y leche."
    assert [n["text"] for n in day["notes"]] == ["Hoy la recoge su abuela"]

    assert (await client.get(f"/children/{world.renata.id}/day", headers=world.mom)).status_code == 404
    other = await client.post(f"/children/{world.renata.id}/notes", headers=world.mom, json={"text": "Hola"})
    assert other.status_code == 404


async def test_a_routine_day_without_roll_call_counts_as_absent(client, world):
    """Sin pase de lista el niño falta: el calendario del papá lo dice igual que Mi sala."""
    await _start_day(client, world)  # la rutina de hoy (miércoles 14) existe, pero nadie pasó lista
    day = lambda calendar: {d["day"]: d["status"] for d in calendar["days"]}[14]  # noqa: E731
    path = f"/children/{world.sofia.id}/attendance?month=2026-10"

    assert day((await client.get(path, headers=world.mom)).json()) == "ABSENT"
    await mark_present(client, world, world.sofia)
    assert day((await client.get(path, headers=world.mom)).json()) == "FULL_DAY"


async def test_calendar(client, world):
    await _start_day(client, world)
    await client.post(
        f"/children/{world.sofia.id}/daily-entries", headers=world.teacher_a, json={"general_note": "Día tranquilo"}
    )
    await client.put(
        f"/attendance/{world.sofia.id}", headers=world.teacher_a, json={"status": "ABSENT", "date": "2026-10-07"}
    )

    calendar = (await client.get(f"/children/{world.sofia.id}/attendance?month=2026-10", headers=world.mom)).json()
    days = {day["day"]: day["status"] for day in calendar["days"]}
    assert len(days) == 31
    assert days[7] == "ABSENT"
    assert days[14] == "WITH_NOTE"
    assert days[13] == "NO_RECORD" and days[20] == "NO_RECORD"
    assert calendar["summary"] == {"full_days": 1, "with_note": 1, "absences": 1}

    assert (await client.get(f"/children/{world.sofia.id}/attendance?month=2026-13", headers=world.mom)).json()[
        "code"
    ] == "MONTH_INVALID"
    assert (await client.get(f"/children/{world.sofia.id}/attendance?month=2026-10", headers=world.admin)).status_code == 200
