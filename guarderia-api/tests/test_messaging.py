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
