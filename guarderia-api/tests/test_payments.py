PAST = "2020-01-15"
FUTURE = "2099-01-15"


async def _create(client, headers, child_id, **fields):
    body = {"concept": "Colegiatura de octubre", "amount_cents": 350_000, **fields}
    return await client.post(f"/children/{child_id}/payments", headers=headers, json=body)


async def test_admin_registers_charges_and_payments(client, world):
    pending = await _create(client, world.admin, world.sofia.id, due_date=PAST)
    assert pending.status_code == 201
    assert pending.json()["status"] == "PENDING" and pending.json()["is_overdue"] is True

    upcoming = await _create(client, world.admin, world.sofia.id, concept="Inscripción", amount_cents=100_000, due_date=FUTURE)
    assert upcoming.json()["is_overdue"] is False

    paid = await _create(
        client, world.admin, world.sofia.id, concept="Material", amount_cents=25_000, status="PAID", method="CASH"
    )
    assert paid.status_code == 201
    # el reloj de las pruebas está fijo en el 14 de octubre de 2026
    assert paid.json()["status"] == "PAID" and paid.json()["paid_on"] == "2026-10-14"

    listing = (await client.get(f"/children/{world.sofia.id}/payments", headers=world.admin)).json()
    assert len(listing["items"]) == 3
    assert listing["pending_cents"] == 450_000
    assert listing["overdue_cents"] == 350_000
    assert listing["paid_cents"] == 25_000

    only_paid = (await client.get(f"/children/{world.sofia.id}/payments", params={"status": "PAID"}, headers=world.admin)).json()
    assert [p["concept"] for p in only_paid["items"]] == ["Material"]


async def test_payment_validation(client, world):
    sofia = world.sofia.id
    assert (await _create(client, world.admin, sofia, concept="  ")).json()["code"] == "CONCEPT_REQUIRED"
    assert (await _create(client, world.admin, sofia, amount_cents=0)).json()["code"] == "AMOUNT_INVALID"
    assert (await _create(client, world.admin, sofia, amount_cents=-5)).json()["code"] == "AMOUNT_INVALID"
    assert (await _create(client, world.admin, sofia, amount_cents=10**12)).json()["code"] == "AMOUNT_TOO_LARGE"
    assert (await _create(client, world.admin, sofia, status="PAID")).json()["code"] == "METHOD_REQUIRED"
    assert (await _create(client, world.admin, sofia, status="CANCELLED")).json()["code"] == "STATUS_INVALID"
    assert (await _create(client, world.admin, 99999)).status_code == 404


async def test_pay_edit_and_cancel(client, world):
    created = (await _create(client, world.admin, world.sofia.id, due_date=PAST)).json()
    pid = created["id"]

    edited = await client.patch(
        f"/payments/{pid}", headers=world.admin, json={"amount_cents": 400_000, "notes": "Con beca parcial"}
    )
    assert edited.json()["amount_cents"] == 400_000 and edited.json()["notes"] == "Con beca parcial"

    paid = await client.post(f"/payments/{pid}/pay", headers=world.admin, json={"method": "TRANSFER", "reference": "SPEI 123"})
    assert paid.status_code == 200
    assert paid.json()["status"] == "PAID" and paid.json()["method"] == "TRANSFER" and paid.json()["is_overdue"] is False

    # pagado: ya no se edita ni se vuelve a pagar
    assert (await client.patch(f"/payments/{pid}", headers=world.admin, json={"concept": "otro"})).json()["code"] == "PAYMENT_LOCKED"
    assert (await client.post(f"/payments/{pid}/pay", headers=world.admin, json={"method": "CASH"})).json()["code"] == "PAYMENT_NOT_PENDING"

    cancelled = await client.post(f"/payments/{pid}/cancel", headers=world.admin, json={"reason": "Se cobró doble"})
    assert cancelled.json()["status"] == "CANCELLED" and "Se cobró doble" in cancelled.json()["notes"]
    assert (await client.post(f"/payments/{pid}/cancel", headers=world.admin)).json()["code"] == "PAYMENT_CANCELLED"
    assert (await client.post("/payments/99999/cancel", headers=world.admin)).status_code == 404

    # lo cancelado queda en la bitácora del admin, pero no cuenta en los totales
    listing = (await client.get(f"/children/{world.sofia.id}/payments", headers=world.admin)).json()
    assert len(listing["items"]) == 1 and listing["paid_cents"] == 0 and listing["pending_cents"] == 0


async def test_payment_permissions(client, world):
    pid = (await _create(client, world.admin, world.sofia.id)).json()["id"]
    cancelled = (await _create(client, world.admin, world.sofia.id, concept="Error")).json()["id"]
    await client.post(f"/payments/{cancelled}/cancel", headers=world.admin)

    # la mamá ve lo vigente de su hija, sin lo cancelado
    mine = (await client.get(f"/children/{world.sofia.id}/payments", headers=world.mom)).json()
    assert [p["id"] for p in mine["items"]] == [pid]

    # no ve a otros niños; la docente y los papás no escriben
    assert (await client.get(f"/children/{world.renata.id}/payments", headers=world.mom)).status_code == 404
    assert (await client.get(f"/children/{world.sofia.id}/payments", headers=world.dad)).status_code == 404
    assert (await client.get(f"/children/{world.sofia.id}/payments", headers=world.teacher_a)).status_code == 403
    assert (await _create(client, world.mom, world.sofia.id)).status_code == 403
    assert (await _create(client, world.teacher_a, world.sofia.id)).status_code == 403
    assert (await client.post(f"/payments/{pid}/pay", headers=world.mom, json={"method": "CASH"})).status_code == 403
    assert (await client.post(f"/payments/{pid}/cancel", headers=world.mom)).status_code == 403
    assert (await client.patch(f"/payments/{pid}", headers=world.mom, json={"concept": "x"})).status_code == 403
    assert (await client.get("/payments", headers=world.mom)).status_code == 403


async def test_payments_overview(client, world):
    await _create(client, world.admin, world.sofia.id, concept="A", due_date=PAST)
    await _create(client, world.admin, world.diego.id, concept="B", due_date=FUTURE)
    await _create(client, world.admin, world.renata.id, concept="C", status="PAID", method="CARD")

    everything = (await client.get("/payments", headers=world.admin)).json()
    assert [(p["concept"], p["child_name"]) for p in everything["items"]] == [
        ("C", "Renata Gómez"),
        ("B", "Diego Álvarez"),
        ("A", "Sofía Márquez"),
    ]
    overdue = (await client.get("/payments", params={"overdue": "true"}, headers=world.admin)).json()
    assert [p["concept"] for p in overdue["items"]] == ["A"]
    paid = (await client.get("/payments", params={"status": "PAID"}, headers=world.admin)).json()
    assert [p["concept"] for p in paid["items"]] == ["C"]

    first = (await client.get("/payments", params={"limit": 2}, headers=world.admin)).json()
    assert len(first["items"]) == 2 and first["next_cursor"]
    rest = (await client.get("/payments", params={"limit": 2, "cursor": first["next_cursor"]}, headers=world.admin)).json()
    assert [p["concept"] for p in rest["items"]] == ["A"] and rest["next_cursor"] is None
