"""El menú del día (HN3): el admin lo captura y los demás lo ven."""



async def _put(client, headers, day="2026-10-14", **body):
    return await client.put(f"/menus/{day}", headers=headers, json=body)


async def test_the_admin_saves_and_replaces_a_menu(client, world):
    saved = await _put(client, world.admin, breakfast="Avena con fruta", lunch="Sopa de verduras y pollo", snack="Gelatina")
    assert saved.status_code == 200
    assert saved.json()["date"] == "2026-10-14"
    assert saved.json()["lunch"] == "Sopa de verduras y pollo"

    # volver a guardar el mismo día lo reemplaza: no se duplica.
    again = await _put(client, world.admin, breakfast="Hot cakes", snack="  ")
    assert again.json()["breakfast"] == "Hot cakes" and again.json()["lunch"] is None and again.json()["snack"] is None
    listed = (await client.get("/menus", params={"start": "2026-10-14", "end": "2026-10-14"}, headers=world.admin)).json()
    assert len(listed) == 1


async def test_a_menu_needs_at_least_one_meal(client, world):
    for body in ({}, {"breakfast": "  ", "lunch": ""}):
        response = await _put(client, world.admin, **body)
        assert response.status_code == 422 and response.json()["code"] == "MENU_EMPTY"


async def test_everyone_sees_the_menu_but_only_the_admin_writes(client, world):
    await _put(client, world.admin, lunch="Arroz con huevo")
    for headers in (world.admin, world.teacher_a, world.teacher_b, world.mom, world.dad):
        # sin fechas es el de hoy (miércoles 14 en las pruebas).
        today = (await client.get("/menus", headers=headers)).json()
        assert [m["lunch"] for m in today] == ["Arroz con huevo"]
    for headers in (world.teacher_a, world.mom):
        assert (await _put(client, headers, lunch="Pizza")).status_code == 403
        assert (await client.delete("/menus/2026-10-14", headers=headers)).status_code == 403


async def test_the_list_covers_a_range_and_skips_days_without_menu(client, world):
    for day in ("2026-10-12", "2026-10-14", "2026-10-16"):
        await _put(client, world.admin, day=day, lunch=f"Comida del {day}")
    week = (await client.get("/menus", params={"start": "2026-10-12", "end": "2026-10-16"}, headers=world.mom)).json()
    assert [m["date"] for m in week] == ["2026-10-12", "2026-10-14", "2026-10-16"]
    middle = (await client.get("/menus", params={"start": "2026-10-13", "end": "2026-10-15"}, headers=world.mom)).json()
    assert [m["date"] for m in middle] == ["2026-10-14"]


async def test_the_range_is_validated(client, world):
    backwards = await client.get("/menus", params={"start": "2026-10-16", "end": "2026-10-12"}, headers=world.admin)
    assert backwards.status_code == 422 and backwards.json()["code"] == "RANGE_INVALID"
    too_long = await client.get("/menus", params={"start": "2026-01-01", "end": "2026-12-31"}, headers=world.admin)
    assert too_long.json()["code"] == "RANGE_INVALID"
    assert (await client.get("/menus", params={"start": "no-es-fecha"}, headers=world.admin)).status_code == 422


async def test_the_admin_can_remove_a_menu(client, world):
    await _put(client, world.admin, lunch="Arroz")
    assert (await client.delete("/menus/2026-10-14", headers=world.admin)).status_code == 204
    assert (await client.get("/menus", headers=world.mom)).json() == []
    missing = await client.delete("/menus/2026-10-14", headers=world.admin)
    assert missing.status_code == 404 and missing.json()["code"] == "MENU_NOT_FOUND"


async def test_the_family_sees_the_menu_in_the_day(client, world):
    before = (await client.get(f"/children/{world.sofia.id}/day", headers=world.mom)).json()
    assert before["menu"] is None

    await _put(client, world.admin, breakfast="Avena", snack="Manzana")
    day = (await client.get(f"/children/{world.sofia.id}/day", headers=world.mom)).json()
    assert (day["menu"]["breakfast"], day["menu"]["lunch"], day["menu"]["snack"]) == ("Avena", None, "Manzana")

    # otro día no tiene menú.
    other = (await client.get(f"/children/{world.sofia.id}/day", params={"date": "2026-10-13"}, headers=world.mom)).json()
    assert other["menu"] is None


async def test_saving_and_removing_are_audited(client, world):
    await _put(client, world.admin, lunch="Arroz")
    await client.delete("/menus/2026-10-14", headers=world.admin)
    for action in ("MENU_SAVED", "MENU_DELETED"):
        audit = (await client.get(f"/audit-log?action={action}", headers=world.admin)).json()
        assert len(audit["items"]) == 1
