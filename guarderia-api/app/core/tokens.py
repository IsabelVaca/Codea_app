import hashlib
import secrets
import uuid
from datetime import timedelta
from urllib.parse import urlencode

import jwt

from . import clock
from .config import settings
from .errors import ApiError

ALGORITHM = "HS256"

# alfabeto sin 0/O ni 1/I/L, para que el código se lea y se dicte sin confundirse.
_CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"


def create_access_token(user_id: uuid.UUID, token_version: int) -> str:
    """El JWT que la app manda en cada petición. No lleva rol ni datos personales:
    el rol se lee de Postgres en cada request, así la baja corta el acceso al instante."""
    now = clock.now()
    payload = {
        "sub": str(user_id),
        "type": "access",
        "tv": token_version,
        "iat": int(now.timestamp()),
        "exp": int((now + timedelta(seconds=settings.access_ttl_s)).timestamp()),
        "jti": uuid.uuid4().hex,
    }
    return jwt.encode(payload, settings.jwt_secret, algorithm=ALGORITHM)


def decode_access_token(token: str) -> dict:
    """Verifica firma y tipo. La vigencia se compara con clock.now() (no con el reloj real de PyJWT)
    para que las pruebas puedan mover la hora."""
    try:
        # el algoritmo se fija aquí: nunca se acepta el que diga el propio token.
        payload = jwt.decode(
            token,
            settings.jwt_secret,
            algorithms=[ALGORITHM],
            options={"verify_exp": False, "verify_iat": False, "require": ["sub", "type", "tv", "exp"]},
        )
    except jwt.PyJWTError:
        raise ApiError(401, "TOKEN_INVALID", "El token no es válido")
    if payload.get("type") != "access":
        raise ApiError(401, "TOKEN_INVALID", "El token no es válido")
    if payload["exp"] <= clock.now().timestamp():
        raise ApiError(401, "TOKEN_EXPIRED", "El token ya expiró")
    return payload


def new_refresh_token() -> str:
    """Texto aleatorio, no un JWT: en la base solo se guarda su hash y se puede revocar uno por uno."""
    return secrets.token_urlsafe(32)


def hash_token(raw: str) -> str:
    return hashlib.sha256(raw.encode()).hexdigest()


def new_access_code() -> str:
    """Código de 8 caracteres que el administrador comparte: XXXX-XXXX."""
    raw = "".join(secrets.choice(_CODE_ALPHABET) for _ in range(8))
    return f"{raw[:4]}-{raw[4:]}"


def invite_link(email: str, code: str) -> str | None:
    """El enlace que el admin manda por WhatsApp: abre la página /invitar del servidor, y de ahí la app.
    None si el servidor no tiene dirección pública configurada."""
    base = settings.public_base_url.strip().rstrip("/")
    if not base:
        return None
    return f"{base}/invitar?{urlencode({'email': email, 'code': code})}"


def normalize_code(code: str) -> str:
    """Se compara solo con letras y números, sin importar mayúsculas: el guion, los espacios y los caracteres
    invisibles que trae un texto copiado de WhatsApp no cuentan."""
    return "".join(ch for ch in code.upper() if ch.isascii() and ch.isalnum())


def hash_code(code: str) -> str:
    return hash_token(normalize_code(code))
