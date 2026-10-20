"""RF-10: la familia borra las fotos de su hijo directamente, sin que nadie lo apruebe."""

from sqlalchemy import select

from app.modules.media.models import Media, MediaStatus
from tests.conftest import mark_present


async def _photo(client, world, storage, *children) -> tuple[int, str]:
    """Una foto en un reporte con esos niños (por omisión, Sofía); regresa su id y su llave en el bucket."""
    children = children or (world.sofia,)
    await mark_present(client, world, *children)
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
            "child_ids": [child.id for child in children],
            "media_id": media_id,
        },
    )
    assert report.status_code == 201
    return media_id, key


async def test_the_family_deletes_its_own_photo_directly(client, world, storage, db):
    media_id, key = await _photo(client, world, storage)

    assert (await client.delete(f"/media/{media_id}", headers=world.mom)).status_code == 204

    assert key not in storage.objects
    for headers in (world.mom, world.teacher_a, world.admin):
        feed = (await client.get("/media", params={"child_id": world.sofia.id}, headers=headers)).json()
        assert feed["items"] == []
    stored = await db.scalar(select(Media).where(Media.id == media_id))
    await db.refresh(stored)
    assert stored.status == MediaStatus.DELETED
    audit = (await client.get("/audit-log?action=MEDIA_DELETED_BY_FAMILY", headers=world.admin)).json()
    assert [item["entity_id"] for item in audit["items"]] == [str(media_id)]
    # borrarla otra vez ya no existe.
    assert (await client.delete(f"/media/{media_id}", headers=world.mom)).status_code == 404


async def test_a_photo_with_other_families_children_is_deleted_too(client, world, storage):
    """Cambio del 19/oct: basta con que salga un hijo suyo; ya no hay 409 ni revisión de la dirección."""
    media_id, key = await _photo(client, world, storage, world.sofia, world.diego)

    assert (await client.delete(f"/media/{media_id}", headers=world.mom)).status_code == 204

    assert key not in storage.objects
    # tampoco queda en la galería ni en el reporte de la otra familia.
    feed = (await client.get("/media", params={"child_id": world.diego.id}, headers=world.teacher_a)).json()
    assert feed["items"] == []
    # queda en la bitácora con los niños que salían.
    audit = (await client.get("/audit-log?action=MEDIA_DELETED_BY_FAMILY", headers=world.admin)).json()
    assert [item["entity_id"] for item in audit["items"]] == [str(media_id)]


async def test_only_a_parent_of_a_child_in_the_photo_can_delete_it(client, world, storage):
    media_id, key = await _photo(client, world, storage)
    for headers in (world.teacher_a, world.admin):
        assert (await client.delete(f"/media/{media_id}", headers=headers)).status_code == 403
    # otra familia ni siquiera sabe que existe.
    assert (await client.delete(f"/media/{media_id}", headers=world.dad)).status_code == 404
    assert (await client.delete(f"/media/{media_id}")).status_code == 401
    assert key in storage.objects


async def test_if_the_bucket_fails_nothing_is_marked_as_deleted(client, world, storage, monkeypatch, db):
    media_id, key = await _photo(client, world, storage)

    async def broken(_key):
        raise RuntimeError("bucket caído")

    monkeypatch.setattr(storage, "delete", broken)
    response = await client.delete(f"/media/{media_id}", headers=world.mom)
    assert response.status_code == 502 and response.json()["code"] == "STORAGE_UNAVAILABLE"
    stored = await db.scalar(select(Media).where(Media.id == media_id))
    await db.refresh(stored)
    assert stored.status == MediaStatus.READY and key in storage.objects


async def test_the_deletion_request_flow_no_longer_exists(client, world, storage):
    media_id, _ = await _photo(client, world, storage)
    assert (await client.post(f"/media/{media_id}/deletion-request", headers=world.mom, json={})).status_code in (404, 405)
    assert (await client.get("/media/deletion-requests", headers=world.admin)).status_code in (404, 405, 422)
