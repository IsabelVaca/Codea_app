from fastapi.concurrency import run_in_threadpool
from pwdlib import PasswordHash

# Argon2id con los parámetros recomendados de pwdlib. La sal va dentro del hash.
_hasher = PasswordHash.recommended()

# se compara contra este hash cuando el correo no existe, para que tarde lo mismo que un correo real
# y nadie pueda averiguar qué correos están registrados midiendo el tiempo de respuesta.
_DUMMY_HASH = _hasher.hash("contraseña-falsa-solo-para-igualar-tiempos")


def hash_password(password: str) -> str:
    """Síncrona: la usan el seed y las pruebas. En la API usa hash_password_async."""
    return _hasher.hash(password)


def verify_password(password: str, password_hash: str | None) -> bool:
    return _hasher.verify(password, password_hash or _DUMMY_HASH) and password_hash is not None


# Argon2 tarda a propósito (es lo que lo hace resistente), así que no se corre en el hilo del servidor.
async def hash_password_async(password: str) -> str:
    return await run_in_threadpool(hash_password, password)


async def verify_password_async(password: str, password_hash: str | None) -> bool:
    return await run_in_threadpool(verify_password, password, password_hash)
