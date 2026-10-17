"""Pruebas contra un Postgres real (la base guarderia_test en el mismo contenedor).

Se corren desde guarderia-api/ con la base de docker compose prendida: .venv/bin/pytest
"""

import asyncio
import os
import uuid
from datetime import UTC, date, datetime, time
from pathlib import Path
from types import SimpleNamespace

import pytest


def _dotenv() -> dict[str, str]:
    path = Path(__file__).resolve().parents[1] / ".env"
    if not path.exists():
        return {}
    pairs = (line.split("=", 1) for line in path.read_text().splitlines() if "=" in line and not line.startswith("#"))
    return {key.strip(): value.strip() for key, value in pairs}


_env = {**_dotenv(), **os.environ}
DB_USER = _env.get("POSTGRES_USER", "guarderia")
DB_PASSWORD = _env.get("POSTGRES_PASSWORD", "guarderia")
DB_HOST = _env.get("TEST_DB_HOST", "localhost")
TEST_DB = "guarderia_test"
TEST_URL = f"postgresql+asyncpg://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:5432/{TEST_DB}"

# antes de importar la app: la base de pruebas y la llave del JWT. Los access tokens de las pruebas duran
# 30 días para que las pruebas que mueven el reloj no los venzan; la prueba de expiración fija su propio TTL.
os.environ.update(ENV="dev", DATABASE_URL=TEST_URL, ACCESS_TTL_S=str(30 * 24 * 3600))
os.environ.setdefault("JWT_SECRET", "llave-solo-para-pruebas-0123456789abcdefghij")

import asyncpg  # noqa: E402
from httpx import ASGITransport, AsyncClient  # noqa: E402
from sqlalchemy import text  # noqa: E402
from sqlalchemy.ext.asyncio import async_sessionmaker, create_async_engine  # noqa: E402
from sqlalchemy.pool import NullPool  # noqa: E402

import app.core.registry  # noqa: E402,F401
from app.core import clock  # noqa: E402
from app.core.db import Base, get_db  # noqa: E402
from app.core.passwords import hash_password  # noqa: E402
from app.core.tokens import create_access_token  # noqa: E402
from app.core.storage import get_storage  # noqa: E402
from app.main import app  # noqa: E402
from app.modules.admins.models import Admin  # noqa: E402
from app.modules.auth.models import AppUser  # noqa: E402
from app.modules.children.models import Child  # noqa: E402
from app.modules.classrooms.models import Classroom, DaycareCenter  # noqa: E402
from app.modules.parents.models import Parent, ParentChildRel, Relationship  # noqa: E402
from app.modules.routines.models import MomentCategory, RoutineTemplate, RoutineTemplateMoment  # noqa: E402
from app.modules.teachers.models import Shift, Teacher  # noqa: E402

# miércoles 14 de octubre de 2026, 7:52 en Monterrey (UTC-6): 8 minutos antes del turno.
NOW = datetime(2026, 10, 14, 13, 52, tzinfo=UTC)
TODAY = date(2026, 10, 14)
SCHOOL = (25.651600, -100.289600)


# Argon2 tarda a propósito: el hash de la contraseña de prueba se calcula una sola vez para todas las pruebas.
TEST_PASSWORD = "clave-de-prueba-123"
TEST_HASH = hash_password(TEST_PASSWORD)

# correo -> (id, token_version) de las personas que crea la fixture `world`, para armar su token.
_USERS: dict[str, tuple[uuid.UUID, int]] = {}


def bearer(token: str) -> dict[str, str]:
    return {"Authorization": f"Bearer {token}"}


def as_user(email: str) -> dict[str, str]:
    """El encabezado con un access token real de una persona de `world` (ya existe en la base)."""
    user_id, version = _USERS[email]
    return bearer(create_access_token(user_id, version))


@pytest.fixture(scope="session", autouse=True)
def _schema():
    async def create() -> None:
        conn = await asyncpg.connect(user=DB_USER, password=DB_PASSWORD, host=DB_HOST, database="postgres")
        if not await conn.fetchval("SELECT 1 FROM pg_database WHERE datname = $1", TEST_DB):
            await conn.execute(f'CREATE DATABASE "{TEST_DB}"')
        await conn.close()
        engine = create_async_engine(TEST_URL, poolclass=NullPool)
        async with engine.begin() as connection:
            await connection.run_sync(Base.metadata.drop_all)
            await connection.run_sync(Base.metadata.create_all)
        await engine.dispose()

    asyncio.run(create())


@pytest.fixture
async def engine():
    engine = create_async_engine(TEST_URL, poolclass=NullPool)
    tables = ", ".join(f'"{table.name}"' for table in Base.metadata.sorted_tables)
    async with engine.begin() as connection:
        await connection.execute(text(f"TRUNCATE {tables} RESTART IDENTITY CASCADE"))
    yield engine
    await engine.dispose()


@pytest.fixture
async def db(engine):
    async with async_sessionmaker(engine, expire_on_commit=False)() as session:
        yield session


@pytest.fixture(autouse=True)
def fixed_clock(monkeypatch):
    """El reloj de la API queda fijo en NOW; set_clock(otra_hora) lo mueve."""

    def set_clock(moment: datetime) -> None:
        monkeypatch.setattr(clock, "now", lambda: moment)

    set_clock(NOW)
    return set_clock


class FakeStorage:
    """El bucket en memoria: put() simula que la app ya subió el archivo."""

    def __init__(self) -> None:
        self.objects: dict[str, int] = {}

    def presign_upload(self, key: str, content_type: str, max_bytes: int) -> dict:
        return {"url": "http://storage.test/guarderia", "fields": {"key": key, "Content-Type": content_type}}

    def presign_get(self, key: str) -> str:
        return f"http://storage.test/guarderia/{key}?firma=1"

    async def size_of(self, key: str) -> int | None:
        return self.objects.get(key)

    def put(self, key: str, size: int = 1234) -> None:
        self.objects[key] = size


@pytest.fixture
def storage() -> FakeStorage:
    return FakeStorage()


@pytest.fixture
async def client(engine, storage):
    sessions = async_sessionmaker(engine, expire_on_commit=False)

    async def override_db():
        async with sessions() as session:
            yield session

    app.dependency_overrides[get_db] = override_db
    app.dependency_overrides[get_storage] = lambda: storage
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test/api/v1") as http:
        yield http
    app.dependency_overrides.clear()


@pytest.fixture
async def world(db) -> SimpleNamespace:
    """Un centro chico: 2 salas, 1 admin, 2 docentes, 3 niños y 2 papás, todos ya con cuenta."""

    async def person(name: str, email: str) -> AppUser:
        user = AppUser(name=name, surname="Prueba", email=email, phone="8112345678", password_hash=TEST_HASH)
        db.add(user)
        await db.flush()
        _USERS[email] = (user.id, user.token_version)
        return user

    center = DaycareCenter(name="La Oruga", latitude=SCHOOL[0], longitude=SCHOOL[1], check_in_radius_m=60)
    db.add(center)
    await db.flush()
    shift = Shift(daycare_center_id=center.id, name="Matutino", start_time=time(8), end_time=time(16))
    template = RoutineTemplate(name="Día completo · Preescolar", is_default=True)
    db.add_all([shift, template])
    await db.flush()
    db.add_all(
        [
            RoutineTemplateMoment(template_id=template.id, number=1, name="Recepción", category=MomentCategory.ARRIVAL),
            RoutineTemplateMoment(template_id=template.id, number=2, name="Desayuno", category=MomentCategory.MEAL),
            RoutineTemplateMoment(template_id=template.id, number=3, name="Siesta", category=MomentCategory.NAP),
        ]
    )
    room_a = Classroom(daycare_center_id=center.id, name="Preescolar 2", capacity=12)
    room_b = Classroom(daycare_center_id=center.id, name="Maternal", capacity=10)
    db.add_all([room_a, room_b])
    await db.flush()

    admin = await person("Directora", "admin@test.dev")
    db.add(Admin(user_id=admin.id, daycare_center_id=center.id))
    teacher_a = await person("Karla", "karla@test.dev")
    teacher_b = await person("Fernando", "fernando@test.dev")
    db.add_all(
        [
            Teacher(user_id=teacher_a.id, classroom_id=room_a.id, is_primary=True, shift_id=shift.id),
            Teacher(user_id=teacher_b.id, classroom_id=room_b.id, is_primary=True, shift_id=shift.id),
        ]
    )
    sofia = Child(name="Sofía", surname="Márquez", birth_date=date(2022, 1, 20), classroom_id=room_a.id)
    diego = Child(name="Diego", surname="Álvarez", birth_date=date(2021, 12, 9), classroom_id=room_a.id)
    renata = Child(name="Renata", surname="Gómez", birth_date=date(2023, 2, 20), classroom_id=room_b.id)
    db.add_all([sofia, diego, renata])
    await db.flush()
    mom = await person("Mariana", "mariana@test.dev")
    dad = await person("Carlos", "carlos@test.dev")
    db.add_all([Parent(user_id=mom.id), Parent(user_id=dad.id)])
    await db.flush()
    db.add_all(
        [
            ParentChildRel(parent_id=mom.id, child_id=sofia.id, relationship=Relationship.MOTHER, is_primary=True),
            ParentChildRel(parent_id=dad.id, child_id=renata.id, relationship=Relationship.FATHER, is_primary=True),
        ]
    )
    await db.commit()

    return SimpleNamespace(
        center=center,
        shift=shift,
        room_a=room_a,
        room_b=room_b,
        sofia=sofia,
        diego=diego,
        renata=renata,
        admin=as_user(admin.email),
        teacher_a=as_user(teacher_a.email),
        teacher_b=as_user(teacher_b.email),
        mom=as_user(mom.email),
        dad=as_user(dad.email),
        teacher_a_id=teacher_a.id,
        teacher_b_id=teacher_b.id,
        mom_id=mom.id,
    )
