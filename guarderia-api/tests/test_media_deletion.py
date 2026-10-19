"""RF-10: la familia pide borrar una foto y el administrador decide."""

from sqlalchemy import select

from app.modules.media.models import Media, MediaStatus
from tests.conftest import mark_present


async def _photo_of_sofia(client, world, storage) -> tuple[int, str]:
    """Una foto de Sofía en un reporte; regresa su id y su llave en el bucket."""
    await mark_present(client, world, world.sofia)
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    permit = await client.post(
        "/media/uploads", headers=world.teacher_a, json={"content_type": "image/jpeg", "size_bytes": 2048}
    )
    media_id, key = permit.json()["media_id"], permit.json()["fields"]["key"]
    storage.put(key)
    assert (await client.post(f"/media/{media_id}/complete", headers=world.teacher_a)).status_code == 200
    report = await client.post(
        "/reports",
        headers=world.teacher_a,
        json={
            "routine_moment_id": routine["moments"][0]["id"],
            "type": "INCIDENT",
            "method": "PHOTO",
            "description": "Se cayó en el patio",
            "child_ids": [world.sofia.id],
            "media_id": media_id,
        },
    )
    assert report.status_code == 201
    return media_id, key


async def _request_deletion(client, world, media_id, reason="No quiero que salga mi hija"):
    response = await client.post(f"/media/{media_id}/deletion-request", headers=world.mom, json={"reason": reason})
    assert response.status_code == 201
    return response.json()["id"]


async def test_admin_sees_the_pending_requests_with_the_photo(client, world, storage):
    media_id, _ = await _photo_of_sofia(client, world, storage)
    request_id = await _request_deletion(client, world, media_id)

    listed = (await client.get("/media/deletion-requests", headers=world.admin)).json()
    assert [item["id"] for item in listed] == [request_id]
    item = listed[0]
    assert item["status"] == "PENDING"
    assert item["reason"] == "No quiero que salga mi hija"
    assert item["requested_by_name"] == "Mariana Prueba"
    assert item["child_names"] == ["Sofía Márquez"]
    assert item["photo_url"].startswith("http://storage.test/")
    assert item["decided_at"] is None


async def test_only_the_admin_reviews_requests(client, world, storage):
    media_id, _ = await _photo_of_sofia(client, world, storage)
    request_id = await _request_deletion(client, world, media_id)
    for headers in (world.teacher_a, world.mom, world.dad):
        assert (await client.get("/media/deletion-requests", headers=headers)).status_code == 403
        approve = await client.post(f"/media/deletion-requests/{request_id}/approve", headers=headers)
        assert approve.status_code == 403
        reject = await client.post(f"/media/deletion-requests/{request_id}/reject", headers=headers)
        assert reject.status_code == 403
    assert (await client.get("/media/deletion-requests")).status_code == 401


async def test_approving_deletes_the_photo_everywhere(client, world, storage, db):
    media_id, key = await _photo_of_sofia(client, world, storage)
    request_id = await _request_deletion(client, world, media_id)
    assert key in storage.objects

    approved = await client.post(
        f"/media/deletion-requests/{request_id}/approve", headers=world.admin, json={"note": "Se borró, gracias"}
    )
    assert approved.status_code == 200
    body = approved.json()
    assert body["status"] == "APPROVED" and body["decision_note"] == "Se borró, gracias"
    assert body["decided_at"] is not None and body["photo_url"] is None

    # ya no está en el bucket ni en la galería de la familia, ni en la de la docente.
    assert key not in storage.objects
    for headers in (world.mom, world.teacher_a, world.admin):
        feed = (await client.get("/media", params={"child_id": world.sofia.id}, headers=headers)).json()
        assert feed["items"] == []
    stored = await db.scalar(select(Media).where(Media.id == media_id))
    await db.refresh(stored)
    assert stored.status == MediaStatus.DELETED
    # el reporte conserva su texto, pero ya no dice que tiene foto.
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    reports = (
        await client.get("/reports", params={"moment_id": routine["moments"][0]["id"]}, headers=world.teacher_a)
    ).json()
    assert [(item["description"], item["has_photo"]) for item in reports] == [("Se cayó en el patio", False)]


async def test_rejecting_keeps_the_photo(client, world, storage):
    media_id, key = await _photo_of_sofia(client, world, storage)
    request_id = await _request_deletion(client, world, media_id)

    rejected = await client.post(
        f"/media/deletion-requests/{request_id}/reject", headers=world.admin, json={"note": "Es un reporte de salud"}
    )
    assert rejected.status_code == 200 and rejected.json()["status"] == "REJECTED"
    assert key in storage.objects
    feed = (await client.get("/media", params={"child_id": world.sofia.id}, headers=world.mom)).json()
    assert [item["id"] for item in feed["items"]] == [media_id]
    # ya no aparece como pendiente, pero sí en el historial de rechazadas.
    assert (await client.get("/media/deletion-requests", headers=world.admin)).json() == []
    history = (await client.get("/media/deletion-requests?status=REJECTED", headers=world.admin)).json()
    assert [item["id"] for item in history] == [request_id]


async def test_a_request_is_decided_only_once(client, world, storage):
    media_id, _ = await _photo_of_sofia(client, world, storage)
    request_id = await _request_deletion(client, world, media_id)
    assert (await client.post(f"/media/deletion-requests/{request_id}/reject", headers=world.admin)).status_code == 200
    again = await client.post(f"/media/deletion-requests/{request_id}/approve", headers=world.admin)
    assert again.status_code == 409 and again.json()["code"] == "REQUEST_ALREADY_DECIDED"


async def test_unknown_request_is_404(client, world):
    for action in ("approve", "reject"):
        response = await client.post(f"/media/deletion-requests/9999/{action}", headers=world.admin)
        assert response.status_code == 404 and response.json()["code"] == "DELETION_REQUEST_NOT_FOUND"


async def test_approving_closes_every_request_for_the_same_photo(client, world, storage, db):
    from app.modules.parents.models import ParentChildRel, Relationship

    media_id, _ = await _photo_of_sofia(client, world, storage)
    # Carlos también es tutor de Sofía y pide borrar la misma foto.
    db.add(ParentChildRel(parent_id=world.dad_id, child_id=world.sofia.id, relationship=Relationship.OTHER))
    await db.commit()
    first = await _request_deletion(client, world, media_id)
    second = await client.post(f"/media/{media_id}/deletion-request", headers=world.dad, json={})
    assert second.status_code == 201

    await client.post(f"/media/deletion-requests/{first}/approve", headers=world.admin)
    assert (await client.get("/media/deletion-requests", headers=world.admin)).json() == []
    approved = (await client.get("/media/deletion-requests?status=APPROVED", headers=world.admin)).json()
    assert {item["id"] for item in approved} == {first, second.json()["id"]}


async def test_if_the_bucket_fails_nothing_is_marked_as_deleted(client, world, storage, monkeypatch):
    media_id, key = await _photo_of_sofia(client, world, storage)
    request_id = await _request_deletion(client, world, media_id)

    async def broken(_key):
        raise RuntimeError("el almacén no responde")

    monkeypatch.setattr(storage, "delete", broken)
    response = await client.post(f"/media/deletion-requests/{request_id}/approve", headers=world.admin)
    assert response.status_code == 502 and response.json()["code"] == "STORAGE_UNAVAILABLE"
    pending = (await client.get("/media/deletion-requests", headers=world.admin)).json()
    assert [item["id"] for item in pending] == [request_id]
    feed = (await client.get("/media", params={"child_id": world.sofia.id}, headers=world.mom)).json()
    assert [item["id"] for item in feed["items"]] == [media_id]


async def test_decisions_are_audited(client, world, storage):
    media_id, _ = await _photo_of_sofia(client, world, storage)
    request_id = await _request_deletion(client, world, media_id)
    await client.post(f"/media/deletion-requests/{request_id}/approve", headers=world.admin)
    [entry] = (await client.get("/audit-log?action=MEDIA_DELETION_APPROVED", headers=world.admin)).json()["items"]
    assert entry["actor_name"] == "Directora Prueba"
    assert entry["entity_type"] == "MEDIA" and entry["entity_id"] == str(media_id)
    assert entry["detail"] == {"request_id": request_id}


# ---------------------------------------------------------------- la familia borra directo (cambio pedido el 18/oct)


async def test_the_family_deletes_its_own_photo_directly(client, world, storage, db):
    media_id, key = await _photo_of_sofia(client, world, storage)
    await _request_deletion(client, world, media_id)

    assert (await client.delete(f"/media/{media_id}", headers=world.mom)).status_code == 204

    assert key not in storage.objects
    for headers in (world.mom, world.teacher_a, world.admin):
        feed = (await client.get("/media", params={"child_id": world.sofia.id}, headers=headers)).json()
        assert feed["items"] == []
    stored = await db.scalar(select(Media).where(Media.id == media_id))
    await db.refresh(stored)
    assert stored.status == MediaStatus.DELETED
    # la solicitud que había quedó resuelta con ella y lo dice la bitácora.
    assert (await client.get("/media/deletion-requests", headers=world.admin)).json() == []
    audit = (await client.get("/audit-log?action=MEDIA_DELETED_BY_FAMILY", headers=world.admin)).json()
    assert [item["entity_id"] for item in audit["items"]] == [str(media_id)]
    # borrarla otra vez ya no existe.
    assert (await client.delete(f"/media/{media_id}", headers=world.mom)).status_code == 404


async def test_a_photo_with_other_families_children_is_not_deleted_by_one_family(client, world, storage):
    await mark_present(client, world, world.sofia, world.diego)
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    permit = await client.post(
        "/media/uploads", headers=world.teacher_a, json={"content_type": "image/jpeg", "size_bytes": 2048}
    )
    media_id, key = permit.json()["media_id"], permit.json()["fields"]["key"]
    storage.put(key)
    await client.post(f"/media/{media_id}/complete", headers=world.teacher_a)
    report = await client.post(
        "/reports",
        headers=world.teacher_a,
        json={
            "routine_moment_id": routine["moments"][0]["id"],
            "type": "ACHIEVEMENT",
            "method": "PHOTO",
            "description": "Pintaron juntos",
            "child_ids": [world.sofia.id, world.diego.id],
            "media_id": media_id,
        },
    )
    assert report.status_code == 201

    shared = await client.delete(f"/media/{media_id}", headers=world.mom)
    assert shared.status_code == 409 and shared.json()["code"] == "MEDIA_SHARED"
    assert key in storage.objects


async def test_only_a_parent_of_the_child_can_delete_the_photo(client, world, storage):
    media_id, key = await _photo_of_sofia(client, world, storage)
    for headers in (world.teacher_a, world.admin):
        assert (await client.delete(f"/media/{media_id}", headers=headers)).status_code == 403
    # otra familia ni siquiera sabe que existe.
    assert (await client.delete(f"/media/{media_id}", headers=world.dad)).status_code == 404
    assert (await client.delete(f"/media/{media_id}")).status_code == 401
    assert key in storage.objects
