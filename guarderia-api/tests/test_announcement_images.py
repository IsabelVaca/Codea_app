"""Avisos con imagen (HN5): el admin publica texto, imagen o ambos y los demás ven la imagen."""

from tests.test_messaging import _upload


async def test_an_announcement_can_carry_an_image(client, world, storage):
    media = await _upload(client, storage, world.admin, "PHOTO")
    created = await client.post(
        "/announcements", headers=world.admin, json={"text": "Festival", "media_id": media, "classroom_id": world.room_a.id}
    )
    assert created.status_code == 201
    assert created.json()["media_url"].startswith("http://storage.test/")

    # la ven la docente de la sala, la mamá y el historial del admin; la otra sala no.
    for headers in (world.admin, world.teacher_a, world.mom):
        listed = (await client.get("/announcements", headers=headers)).json()
        assert [a["media_url"] is not None for a in listed] == [True]
    assert (await client.get("/announcements", headers=world.teacher_b)).json() == []


async def test_an_announcement_can_be_only_an_image(client, world, storage):
    media = await _upload(client, storage, world.admin, "PHOTO")
    created = await client.post("/announcements", headers=world.admin, json={"media_id": media})
    assert created.status_code == 201
    assert created.json()["text"] == "" and created.json()["media_url"]


async def test_an_announcement_without_text_or_image_is_rejected(client, world):
    response = await client.post("/announcements", headers=world.admin, json={"text": "  "})
    assert response.json()["code"] == "TEXT_REQUIRED"


async def test_the_image_must_be_a_ready_image_of_mine(client, world, storage):
    url = "/announcements"
    pending = (
        await client.post(
            "/media/uploads", headers=world.admin, json={"content_type": "image/jpeg", "size_bytes": 2048, "kind": "PHOTO"}
        )
    ).json()["media_id"]
    assert (await client.post(url, headers=world.admin, json={"media_id": pending})).json()["code"] == "MEDIA_NOT_VALID"

    pdf = await _upload(client, storage, world.admin, "DOCUMENT", "application/pdf")
    assert (await client.post(url, headers=world.admin, json={"media_id": pdf})).json()["code"] == "MEDIA_NOT_VALID"

    # la imagen de otra persona no es mía.
    others = await _upload(client, storage, world.teacher_a, "PHOTO")
    assert (await client.post(url, headers=world.admin, json={"media_id": others})).status_code == 404


async def test_only_the_admin_publishes(client, world, storage):
    media = await _upload(client, storage, world.teacher_a, "PHOTO")
    assert (await client.post("/announcements", headers=world.teacher_a, json={"media_id": media})).status_code == 403


async def test_a_deleted_image_no_longer_shows(client, world, storage, db):
    from sqlalchemy import update

    from app.modules.media.models import Media, MediaStatus

    media = await _upload(client, storage, world.admin, "PHOTO")
    await client.post("/announcements", headers=world.admin, json={"text": "Con foto", "media_id": media})
    await db.execute(update(Media).where(Media.id == media).values(status=MediaStatus.DELETED))
    await db.commit()
    listed = (await client.get("/announcements", headers=world.admin)).json()
    assert listed[0]["text"] == "Con foto" and listed[0]["media_url"] is None
