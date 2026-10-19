import uuid


async def _sofia_chat(client, world) -> int:
    conversations = (await client.get("/conversations", headers=world.mom)).json()
    assert [c["child"]["full_name"] for c in conversations] == ["Sofía Márquez"]
    return conversations[0]["id"]


async def test_chat_between_teacher_and_family(client, world):
    chat = await _sofia_chat(client, world)
    teacher_chats = {c["child"]["id"]: c for c in (await client.get("/conversations", headers=world.teacher_a)).json()}
    assert set(teacher_chats) == {world.sofia.id, world.diego.id}
    assert teacher_chats[world.sofia.id]["id"] == chat

    client_id = str(uuid.uuid4())
    sent = await client.post(
        f"/conversations/{chat}/messages",
        headers=world.mom,
        json={"text": "Buenos días miss, ¿Sofía desayunó bien?", "client_id": client_id},
    )
    assert sent.status_code == 201
    assert sent.json()["is_mine"] is True and sent.json()["is_read"] is False
    retry = await client.post(
        f"/conversations/{chat}/messages", headers=world.mom, json={"text": "otra vez", "client_id": client_id}
    )
    assert retry.status_code == 200 and retry.json()["id"] == sent.json()["id"]

    room = {kid["id"]: kid for kid in (await client.get(f"/classrooms/{world.room_a.id}/children", headers=world.teacher_a)).json()}
    assert room[world.sofia.id]["has_unread_chat"] is True and room[world.sofia.id]["unread_count"] == 1

    assert (await client.post(f"/conversations/{chat}/read", headers=world.teacher_a)).status_code == 204
    room = {kid["id"]: kid for kid in (await client.get(f"/classrooms/{world.room_a.id}/children", headers=world.teacher_a)).json()}
    assert room[world.sofia.id]["has_unread_chat"] is False

    reply = await client.post(
        f"/conversations/{chat}/messages", headers=world.teacher_a, json={"text": "Desayunó todo y se quedó contenta."}
    )
    page = (await client.get(f"/conversations/{chat}/messages", headers=world.mom)).json()
    assert [m["text"] for m in page["items"]] == ["Desayunó todo y se quedó contenta.", "Buenos días miss, ¿Sofía desayunó bien?"]
    # la miss ya leyó el mensaje de la mamá.
    assert page["items"][1]["is_read"] is True

    newer = (
        await client.get(f"/conversations/{chat}/messages", params={"after": sent.json()["id"]}, headers=world.mom)
    ).json()
    assert [m["id"] for m in newer["items"]] == [reply.json()["id"]]

    mom_chats = (await client.get("/conversations", headers=world.mom)).json()
    assert mom_chats[0]["unread_count"] == 1
    assert mom_chats[0]["last_message"]["text"] == "Desayunó todo y se quedó contenta."


async def test_chat_scope(client, world):
    chat = await _sofia_chat(client, world)
    assert (await client.get(f"/conversations/{chat}/messages", headers=world.dad)).status_code == 404
    assert (await client.get(f"/conversations/{chat}/messages", headers=world.teacher_b)).status_code == 404
    empty = await client.post(f"/conversations/{chat}/messages", headers=world.mom, json={"text": "  "})
    assert empty.json()["code"] == "TEXT_REQUIRED"


async def _upload(client, storage, headers, kind, content_type="image/jpeg", complete=True) -> int:
    permit = (
        await client.post("/media/uploads", headers=headers, json={"content_type": content_type, "size_bytes": 2048, "kind": kind})
    ).json()
    if complete:
        storage.put(permit["fields"]["key"])
        assert (await client.post(f"/media/{permit['media_id']}/complete", headers=headers)).status_code == 200
    return permit["media_id"]


async def test_chat_photo(client, world, storage):
    chat = await _sofia_chat(client, world)
    # la mamá sube su foto como documento (los papás no pueden subir PHOTO); la miss, como foto.
    mom_media = await _upload(client, storage, world.mom, "DOCUMENT")
    sent = await client.post(
        f"/conversations/{chat}/messages",
        headers=world.mom,
        json={"text": "", "media_id": mom_media, "client_id": str(uuid.uuid4())},
    )
    assert sent.status_code == 201
    assert sent.json()["text"] == "" and sent.json()["media_url"].startswith("http://storage.test/")

    teacher_media = await _upload(client, storage, world.teacher_a, "PHOTO")
    reply = await client.post(
        f"/conversations/{chat}/messages", headers=world.teacher_a, json={"text": "Así quedó", "media_id": teacher_media}
    )
    assert reply.status_code == 201 and reply.json()["text"] == "Así quedó"

    page = (await client.get(f"/conversations/{chat}/messages", headers=world.mom)).json()
    assert [bool(m["media_url"]) for m in page["items"]] == [True, True]
    assert (await client.get("/conversations", headers=world.mom)).json()[0]["last_message"]["media_url"]


async def test_chat_photo_rules(client, world, storage):
    chat = await _sofia_chat(client, world)
    url = f"/conversations/{chat}/messages"
    pending = await _upload(client, storage, world.teacher_a, "PHOTO", complete=False)
    not_ready = await client.post(url, headers=world.teacher_a, json={"media_id": pending})
    assert not_ready.status_code == 422 and not_ready.json()["code"] == "MEDIA_NOT_VALID"

    pdf = await _upload(client, storage, world.mom, "DOCUMENT", content_type="application/pdf")
    assert (await client.post(url, headers=world.mom, json={"media_id": pdf})).json()["code"] == "MEDIA_NOT_VALID"

    # la foto de otra persona no se puede usar.
    ready = await _upload(client, storage, world.teacher_a, "PHOTO")
    assert (await client.post(url, headers=world.mom, json={"media_id": ready})).status_code == 404
