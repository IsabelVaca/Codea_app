"""La hora de entrada del niño (RF-03): se guarda al pasar lista y la ven la docente y la familia."""

from datetime import timedelta

from tests.conftest import NOW


async def _mark(client, world, child, status="PRESENT", **extra):
    return await client.put(f"/attendance/{child.id}", headers=world.teacher_a, json={"status": status, **extra})


async def _room(client, world):
    kids = (await client.get(f"/classrooms/{world.room_a.id}/children", headers=world.teacher_a)).json()
    return {kid["id"]: kid for kid in kids}


async def test_marking_present_saves_the_arrival_time(client, world):
    response = await _mark(client, world, world.sofia)
    assert response.status_code == 200
    assert response.json()["arrived_at"].startswith("2026-10-14T13:52")


async def test_marking_present_again_keeps_the_first_arrival(client, world, fixed_clock):
    await _mark(client, world, world.sofia)
    fixed_clock(NOW + timedelta(hours=2))
    again = await _mark(client, world, world.sofia)
    assert again.json()["arrived_at"].startswith("2026-10-14T13:52")


async def test_marking_absent_clears_it_and_coming_back_sets_a_new_one(client, world, fixed_clock):
    await _mark(client, world, world.sofia)
    absent = await _mark(client, world, world.sofia, "ABSENT")
    assert absent.json()["arrived_at"] is None
    assert (await _room(client, world))[world.sofia.id]["arrived_at"] is None

    fixed_clock(NOW + timedelta(hours=1))
    back = await _mark(client, world, world.sofia)
    assert back.json()["arrived_at"].startswith("2026-10-14T14:52")


async def test_a_mark_made_offline_keeps_the_time_the_app_sends(client, world):
    early = (NOW - timedelta(minutes=20)).isoformat()
    response = await _mark(client, world, world.sofia, arrived_at=early)
    assert response.json()["arrived_at"].startswith("2026-10-14T13:32")


async def test_an_arrival_in_the_future_uses_the_server_time(client, world):
    future = (NOW + timedelta(hours=5)).isoformat()
    response = await _mark(client, world, world.sofia, arrived_at=future)
    assert response.json()["arrived_at"].startswith("2026-10-14T13:52")


async def test_an_arrival_without_a_time_zone_is_rejected(client, world):
    response = await _mark(client, world, world.sofia, arrived_at="2026-10-14T07:30:00")
    assert response.status_code == 422


async def test_my_room_shows_when_each_child_arrived(client, world):
    await _mark(client, world, world.sofia)
    kids = await _room(client, world)
    assert kids[world.sofia.id]["arrived_at"].startswith("2026-10-14T13:52")
    # Diego no tiene pase de lista: no hay hora.
    assert kids[world.diego.id]["arrived_at"] is None


async def test_the_family_sees_the_arrival_first_in_the_day(client, world):
    before = (await client.get(f"/children/{world.sofia.id}/day", headers=world.mom)).json()
    assert all(event["type"] != "ARRIVAL" for event in before["timeline"])

    await _mark(client, world, world.sofia)
    day = (await client.get(f"/children/{world.sofia.id}/day", headers=world.mom)).json()
    first = day["timeline"][0]
    assert (first["type"], first["title"]) == ("ARRIVAL", "Entrada")
    assert first["time"].startswith("2026-10-14T13:52")

    # si la docente se equivocó y lo marca ausente, la entrada desaparece.
    await _mark(client, world, world.sofia, "ABSENT")
    after = (await client.get(f"/children/{world.sofia.id}/day", headers=world.mom)).json()
    assert all(event["type"] != "ARRIVAL" for event in after["timeline"])
