"""La página del enlace de invitación y la verificación de la app (App Links).

Dos direcciones públicas, fuera de /api/v1:
- GET /invitar?email=...&code=...: lo que se abre al tocar el enlace que el admin manda por WhatsApp. Si la app ya está
  verificada, Android la abre directo y esta página ni se ve; si no, aquí hay un botón para abrirla y el correo y el
  código escritos. **Solo lee: nunca gasta el código** (WhatsApp pide la página para armar la vista previa).
- GET /.well-known/assetlinks.json: le dice a Android que esta app puede abrir los enlaces de este servidor.
"""

import html
import re
from urllib.parse import urlencode

from fastapi import APIRouter, Query
from fastapi.responses import HTMLResponse

from app.core.config import settings

router = APIRouter(tags=["invitación"], include_in_schema=False)

APP_PACKAGE = "mx.tec.codea"

# lo que se refleja en la página tiene que cuadrar con estas formas; todo lo demás se rechaza sin mostrarse.
_CODE = re.compile(r"^[A-Z0-9]{4}-?[A-Z0-9]{4}$")
_EMAIL = re.compile(r"^[A-Za-z0-9._%+\-]{1,64}@[A-Za-z0-9.\-]{1,190}\.[A-Za-z]{2,24}$")

_STYLE = """
body{font-family:system-ui,sans-serif;background:#fffdfa;color:#2a2140;margin:0;padding:24px}
main{max-width:420px;margin:0 auto}
h1{font-size:24px;margin:24px 0 8px}
p{line-height:1.5}
.boton{display:block;text-align:center;background:#6c4bf6;color:#fff;text-decoration:none;font-weight:700;
padding:16px;border-radius:16px;margin:20px 0}
.dato{background:#efe9ff;border-radius:14px;padding:12px 16px;margin:8px 0;word-break:break-all}
.codigo{font-family:ui-monospace,monospace;font-size:28px;font-weight:800;letter-spacing:2px;text-align:center}
small{color:#8c7fa8}
"""


def _page(title: str, body: str, status_code: int = 200) -> HTMLResponse:
    document = (
        "<!doctype html><html lang='es'><head><meta charset='utf-8'>"
        "<meta name='viewport' content='width=device-width,initial-scale=1'>"
        "<meta name='robots' content='noindex'>"
        f"<title>{html.escape(title)}</title><style>{_STYLE}</style></head>"
        f"<body><main>{body}</main></body></html>"
    )
    return HTMLResponse(document, status_code=status_code, headers={"Cache-Control": "no-store"})


@router.get("/invitar")
async def invitation(email: str = Query("", max_length=254), code: str = Query("", max_length=16)) -> HTMLResponse:
    email = email.strip()
    code = code.strip().upper()
    if not _EMAIL.match(email) or not _CODE.match(code):
        return _page(
            "Enlace no válido",
            "<h1>Este enlace no es válido</h1><p>Pídele a la dirección de la estancia que te mande la invitación otra vez.</p>",
            status_code=400,
        )
    clean = code.replace("-", "")
    code = f"{clean[:4]}-{clean[4:]}"
    deep_link = "codea://invitar?" + urlencode({"email": email, "code": code})
    return _page(
        "Te invitaron a Codea",
        "<h1>Te invitaron a Codea</h1>"
        "<p>La estancia La Oruga te invita a crear tu cuenta.</p>"
        f"<a class='boton' href='{html.escape(deep_link, quote=True)}'>Abrir en la app</a>"
        "<p>Si no se abre, instala Codea, toca <b>«Es mi primera vez»</b> y escribe:</p>"
        f"<div class='dato'><small>Correo</small><br>{html.escape(email)}</div>"
        f"<div class='dato'><small>Código</small><div class='codigo'>{html.escape(code)}</div></div>"
        "<p><small>El código sirve una sola vez y vence en unos días.</small></p>",
    )


@router.get("/.well-known/assetlinks.json")
async def asset_links() -> list[dict]:
    fingerprints = [item.strip().upper() for item in settings.android_cert_sha256.split(",") if item.strip()]
    if not fingerprints:
        return []
    return [
        {
            "relation": ["delegate_permission/common.handle_all_urls"],
            "target": {
                "namespace": "android_app",
                "package_name": APP_PACKAGE,
                "sha256_cert_fingerprints": fingerprints,
            },
        }
    ]
