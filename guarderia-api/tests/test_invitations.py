"""El enlace de invitación que el admin manda por WhatsApp: el código trae su enlace, la página /invitar solo lee
(no gasta el código) y no refleja nada que no tenga la forma de un correo y un código."""

from urllib.parse import parse_qs, urlsplit

from app.core.config import settings

NEW_PASSWORD = "otra-clave-larga-9"
BASE = "https://api.codea.test"
# el cliente de pruebas cuelga de /api/v1; estas dos páginas viven en la raíz del servidor.
ROOT = "http://test"


async def _invite(client, world, email="paola@test.dev"):
    created = await client.post(
        "/teachers",
        headers=world.admin,
        json={"name": "Paola Sánchez", "phone": "8112345678", "email": email, "classroom_id": world.room_a.id},
    )
    assert created.status_code == 201
    return created.json()["access_code"]


async def test_the_code_comes_with_its_link(client, world, monkeypatch):
    monkeypatch.setattr(settings, "public_base_url", BASE + "/")  # la barra final se ignora
    access = await _invite(client, world)
    parts = urlsplit(access["link"])
    assert f"{parts.scheme}://{parts.netloc}{parts.path}" == f"{BASE}/invitar"
    assert parse_qs(parts.query) == {"email": ["paola@test.dev"], "code": [access["code"]]}


async def test_without_a_public_address_there_is_no_link(client, world, monkeypatch):
    monkeypatch.setattr(settings, "public_base_url", "")
    assert (await _invite(client, world))["link"] is None


async def test_the_page_reads_the_link_without_spending_the_code(client, world, monkeypatch):
    monkeypatch.setattr(settings, "public_base_url", BASE)
    access = await _invite(client, world)
    parts = urlsplit(access["link"])

    page = await client.get(f"{ROOT}{parts.path}?{parts.query}")
    assert page.status_code == 200
    assert page.headers["content-type"].startswith("text/html")
    assert "paola@test.dev" in page.text and access["code"] in page.text
    assert "codea://invitar?" in page.text
    assert page.headers["cache-control"] == "no-store"
    assert "script-src" not in page.headers["content-security-policy"]

    # abrirla (también la vista previa de WhatsApp) no gastó el código: sigue sirviendo para crear la cuenta.
    await client.get(f"{ROOT}{parts.path}?{parts.query}")
    activated = await client.post(
        "/auth/activate", json={"email": "paola@test.dev", "code": access["code"], "password": NEW_PASSWORD}
    )
    assert activated.status_code == 200


async def test_the_page_accepts_the_code_without_a_dash_in_lowercase(client):
    page = await client.get(f"{ROOT}/invitar", params={"email": "a@b.co", "code": "k7qxm4pz"})
    assert page.status_code == 200 and "K7QX-M4PZ" in page.text


async def test_the_page_never_reflects_anything_odd(client):
    for params in (
        {"email": '"><script>alert(1)</script>@x.co', "code": "K7QX-M4PZ"},
        {"email": "a@b.co", "code": "<script>"},
        {"email": "a@b.co"},
        {"code": "K7QX-M4PZ"},
        {},
    ):
        page = await client.get(f"{ROOT}/invitar", params=params)
        assert page.status_code == 400, params
        assert "<script" not in page.text and "alert" not in page.text


async def test_asset_links_without_a_fingerprint_is_empty(client, monkeypatch):
    monkeypatch.setattr(settings, "android_cert_sha256", "")
    response = await client.get(f"{ROOT}/.well-known/assetlinks.json")
    assert response.status_code == 200 and response.json() == []


async def test_asset_links_lists_the_app_and_its_certificates(client, monkeypatch):
    monkeypatch.setattr(settings, "android_cert_sha256", "aa:bb:cc, DD:EE:FF")
    (entry,) = (await client.get(f"{ROOT}/.well-known/assetlinks.json")).json()
    assert entry["relation"] == ["delegate_permission/common.handle_all_urls"]
    assert entry["target"] == {
        "namespace": "android_app",
        "package_name": "mx.tec.codea",
        "sha256_cert_fingerprints": ["AA:BB:CC", "DD:EE:FF"],
    }
