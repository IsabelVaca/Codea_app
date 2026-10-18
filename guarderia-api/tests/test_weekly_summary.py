"""RF-12: el resumen semanal que ve el papá."""

# el reloj de las pruebas está en el miércoles 14 de octubre de 2026: la semana va del lunes 12 al viernes 16.
MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY = (f"2026-10-{day}" for day in (12, 13, 14, 15, 16))


async def _entry(client, world, day, **fields):
    response = await client.post(
        f"/children/{world.sofia.id}/daily-entries", headers=world.teacher_a, json={"date": day, **fields}
    )
    assert response.status_code == 200


async def _week(client, world, **params):
    response = await client.get(f"/children/{world.sofia.id}/weekly-summary", headers=world.mom, params=params)
    assert response.status_code == 200
    return response.json()


async def test_a_full_week(client, world):
    # lunes y martes con resumen; miércoles (hoy) con rutina y un logro; jueves ausente.
    await _entry(client, world, MONDAY, mood="HAPPY", meals="ALL", diaper_changes=3, nap_minutes=60)
    await _entry(client, world, TUESDAY, mood="HAPPY", meals="MOST", diaper_changes=2, nap_minutes=90)
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    moment = routine["moments"][0]["id"]
    await client.post(f"/routine-moments/{moment}/confirm", headers=world.teacher_a)
    report = await client.post(
        "/reports",
        headers=world.teacher_a,
        json={
            "routine_moment_id": moment,
            "type": "ACHIEVEMENT",
            "method": "WRITTEN",
            "description": "Compartió sus juguetes con todo el grupo.",
            "child_ids": [world.sofia.id],
        },
    )
    assert report.status_code == 201
    await client.put(f"/attendance/{world.sofia.id}", headers=world.teacher_a, json={"status": "ABSENT", "date": THURSDAY})
    await client.post(f"/children/{world.sofia.id}/notes", headers=world.mom, json={"text": "Hoy la recoge su abuela"})

    week = await _week(client, world)
    assert (week["week_start"], week["week_end"]) == (MONDAY, FRIDAY)
    assert week["child_name"] == "Sofía Márquez"
    assert [(day["date"], day["status"]) for day in week["days"]] == [
        (MONDAY, "ATTENDED"),
        (TUESDAY, "ATTENDED"),
        (WEDNESDAY, "ATTENDED"),
        (THURSDAY, "NO_RECORD"),  # todavía no llega ese día, aunque ya tenga la marca
        (FRIDAY, "NO_RECORD"),
    ]
    assert week["days_attended"] == 3 and week["days_absent"] == 0
    assert week["moods"] == {"HAPPY": 2}
    assert week["meals"] == {"ALL": 1, "MOST": 1}
    assert week["nap_minutes_average"] == 75
    assert week["diaper_changes_total"] == 5
    assert week["reports_by_type"] == {"ACHIEVEMENT": 1}
    assert week["achievements"] == ["Compartió sus juguetes con todo el grupo."]
    assert week["family_notes"] == 1
    assert week["days"][2]["reports"] == 1 and week["days"][2]["family_notes"] == 1


async def test_any_date_of_the_week_gives_the_same_monday(client, world):
    # sábado y domingo cuentan para la semana que acaba de terminar, no para la siguiente.
    for day in (MONDAY, WEDNESDAY, FRIDAY, "2026-10-17", "2026-10-18"):
        assert (await _week(client, world, week_start=day))["week_start"] == MONDAY
    assert (await _week(client, world, week_start="2026-10-19"))["week_start"] == "2026-10-19"


async def test_absence_in_a_past_week(client, world):
    await client.put(f"/attendance/{world.sofia.id}", headers=world.teacher_a, json={"status": "ABSENT", "date": "2026-10-07"})
    await _entry(client, world, "2026-10-06", mood="CALM")
    week = await _week(client, world, week_start="2026-10-05")
    assert week["week_start"] == "2026-10-05"
    by_date = {day["date"]: day["status"] for day in week["days"]}
    assert by_date["2026-10-06"] == "ATTENDED" and by_date["2026-10-07"] == "ABSENT"
    assert week["days_attended"] == 1 and week["days_absent"] == 1
    assert week["nap_minutes_average"] is None  # nadie registró siesta


async def test_an_empty_or_future_week_has_no_records(client, world):
    for day in ("2026-11-02", "2026-09-07"):
        week = await _week(client, world, week_start=day)
        assert week["days_attended"] == 0 and week["days_absent"] == 0
        assert week["moods"] == {} and week["meals"] == {} and week["achievements"] == []
        assert {d["status"] for d in week["days"]} == {"NO_RECORD"}


async def test_the_summary_is_scoped_like_the_rest_of_the_child(client, world):
    path = f"/children/{world.sofia.id}/weekly-summary"
    assert (await client.get(path, headers=world.dad)).status_code == 404  # papá de otra niña
    assert (await client.get(path, headers=world.teacher_b)).status_code == 404  # docente de otra sala
    assert (await client.get(path, headers=world.teacher_a)).status_code == 200
    assert (await client.get(path, headers=world.admin)).status_code == 200
    assert (await client.get(path)).status_code == 401


async def test_the_week_start_must_be_a_date(client, world):
    response = await client.get(
        f"/children/{world.sofia.id}/weekly-summary", headers=world.mom, params={"week_start": "pasado-mañana"}
    )
    assert response.status_code == 422
