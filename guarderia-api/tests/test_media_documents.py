from sqlalchemy import select

from app.modules.documents.models import ChildDocument
from app.modules.media.models import Media
from tests.conftest import mark_present


async def _upload(client, storage, headers, kind="PHOTO", content_type="image/jpeg") -> int:
    permit = await client.post(
        "/media/uploads", headers=headers, json={"content_type": content_type, "size_bytes": 2048, "kind": kind}
    )
    assert permit.status_code == 201
    media_id = permit.json()["media_id"]
    storage.put(permit.json()["fields"]["key"])
    assert (await client.post(f"/media/{media_id}/complete", headers=headers)).json()["status"] == "READY"
    return media_id


async def test_upload_rules(client, world, storage):
    bad_type = await client.post(
        "/media/uploads", headers=world.teacher_a, json={"content_type": "application/zip", "size_bytes": 10}
    )
    assert bad_type.json()["code"] == "CONTENT_TYPE_NOT_ALLOWED"
    too_big = await client.post(
        "/media/uploads", headers=world.teacher_a, json={"content_type": "image/jpeg", "size_bytes": 50_000_000}
    )
    assert too_big.json()["code"] == "FILE_TOO_LARGE"
    parent_photo = await client.post(
        "/media/uploads", headers=world.mom, json={"content_type": "image/jpeg", "size_bytes": 10}
    )
    assert parent_photo.status_code == 403

    permit = (
        await client.post("/media/uploads", headers=world.teacher_a, json={"content_type": "image/jpeg", "size_bytes": 10})
    ).json()
    assert permit["expires_in"] == 300
    missing = await client.post(f"/media/{permit['media_id']}/complete", headers=world.teacher_a)
    assert missing.status_code == 409 and missing.json()["code"] == "UPLOAD_MISSING"
    other = await client.post(f"/media/{permit['media_id']}/complete", headers=world.teacher_b)
    assert other.status_code == 404


async def test_photo_report_reaches_the_family(client, world, storage, db):
    await mark_present(client, world, world.sofia)
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    media_id = await _upload(client, storage, world.teacher_a)
    report = await client.post(
        "/reports",
        headers=world.teacher_a,
        json={
            "routine_moment_id": routine["moments"][0]["id"],
            "type": "INCIDENT",
            "method": "PHOTO",
            "description": "Se cayó en el patio, ya está bien",
            "child_ids": [world.sofia.id],
            "media_id": media_id,
        },
    )
    assert report.status_code == 201 and report.json()["has_photo"] is True

    feed = (await client.get("/media", params={"child_id": world.sofia.id}, headers=world.mom)).json()
    assert [item["id"] for item in feed["items"]] == [media_id]
    photo = feed["items"][0]
    assert photo["category"] == "INCIDENTS"
    assert photo["note"] == "Se cayó en el patio, ya está bien"
    assert photo["url"].startswith("http://storage.test/")
    meals = (await client.get("/media", params={"child_id": world.sofia.id, "category": "MEALS"}, headers=world.mom)).json()
    assert meals["items"] == []

    # Diego no sale en la foto, y Carlos no es papá de Sofía.
    assert (await client.get("/media", params={"child_id": world.diego.id}, headers=world.teacher_a)).json()["items"] == []
    assert (await client.get("/media", params={"child_id": world.sofia.id}, headers=world.dad)).status_code == 404
    assert (await client.post(f"/media/{media_id}/deletion-request", headers=world.dad)).status_code == 404

    first = await client.post(f"/media/{media_id}/deletion-request", headers=world.mom, json={"reason": "No me gusta"})
    assert first.status_code == 201 and first.json()["status"] == "PENDING"
    again = await client.post(f"/media/{media_id}/deletion-request", headers=world.mom)
    assert again.status_code == 200 and again.json()["id"] == first.json()["id"]

    stored = await db.scalar(select(Media).where(Media.id == media_id))
    assert stored.size_bytes == 1234


async def test_feed_pages(client, world, storage):
    await mark_present(client, world, world.sofia)
    routine = (await client.get(f"/classrooms/{world.room_a.id}/routine", headers=world.teacher_a)).json()
    for number in range(3):
        media_id = await _upload(client, storage, world.teacher_a)
        await client.post(
            "/reports",
            headers=world.teacher_a,
            json={
                "routine_moment_id": routine["moments"][0]["id"],
                "type": "ACHIEVEMENT",
                "method": "PHOTO",
                "description": f"Foto número {number} del día",
                "child_ids": [world.sofia.id],
                "media_id": media_id,
            },
        )
    page = (await client.get("/media", params={"child_id": world.sofia.id, "limit": 2}, headers=world.mom)).json()
    assert len(page["items"]) == 2 and page["next_cursor"]
    rest = (
        await client.get(
            "/media", params={"child_id": world.sofia.id, "limit": 2, "cursor": page["next_cursor"]}, headers=world.mom
        )
    ).json()
    assert len(rest["items"]) == 1 and rest["next_cursor"] is None


async def test_documents_and_pickups(client, world, storage, db):
    db.add(ChildDocument(child_id=world.sofia.id, title="Certificado de alergias"))
    await db.commit()
    path = f"/children/{world.sofia.id}/documents"
    docs = (await client.get(path, headers=world.mom)).json()
    assert [(d["title"], d["status"]) for d in docs] == [("Certificado de alergias", "MISSING")]

    media_id = await _upload(client, storage, world.mom, kind="DOCUMENT", content_type="application/pdf")
    uploaded = await client.put(f"{path}/{docs[0]['id']}", headers=world.mom, json={"media_id": media_id})
    assert uploaded.json()["status"] == "CURRENT" and uploaded.json()["url"]
    assert (await client.get(path, headers=world.dad)).status_code == 404
    assert (await client.get(path, headers=world.teacher_a)).status_code == 403

    pickups = f"/children/{world.sofia.id}/authorized-pickups"
    nameless = await client.post(pickups, headers=world.mom, json={"full_name": ""})
    assert nameless.json()["code"] == "NAME_REQUIRED"
    added = await client.post(pickups, headers=world.mom, json={"full_name": "Andrea Torres", "relationship": "Tía"})
    assert added.status_code == 201
    assert [p["full_name"] for p in (await client.get(pickups, headers=world.teacher_a)).json()] == ["Andrea Torres"]
    assert (await client.delete(f"{pickups}/{added.json()['id']}", headers=world.mom)).status_code == 204
    assert (await client.get(pickups, headers=world.mom)).json() == []
