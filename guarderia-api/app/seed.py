"""Datos de ejemplo: los mismos que hoy tienen los repositorios en memoria de la app.

Uso: python -m app.seed (dentro del contenedor: docker compose exec api python -m app.seed).
Solo siembra si la base está vacía.
"""

import asyncio
from datetime import date, time

from sqlalchemy import select

import app.core.registry  # noqa: F401  (registra todas las tablas)
from app.core.config import settings
from app.core.db import SessionLocal
from app.modules.admins.models import Admin
from app.modules.auth import service as auth
from app.modules.auth.models import AppUser, Role
from app.modules.children.models import Child
from app.modules.classrooms.models import Classroom, DaycareCenter
from app.modules.documents.models import AuthorizedPickup, ChildDocument
from app.modules.parents.models import Parent, ParentChildRel, Relationship
from app.modules.routines.models import MomentCategory, RoutineTemplate, RoutineTemplateMoment
from app.modules.teachers.models import Shift, Teacher

ROUTINE = [
    (1, "Recepción y bienvenida", MomentCategory.ARRIVAL),
    (2, "Desayuno", MomentCategory.MEAL),
    (3, "Trazos y motricidad", MomentCategory.ACTIVITY),
    (4, "Patio y juego libre", MomentCategory.ACTIVITY),
    (5, "Comida", MomentCategory.MEAL),
    (6, "Siesta", MomentCategory.NAP),
    (7, "Higiene y cambio", MomentCategory.HYGIENE),
    (8, "Actividad de la tarde", MomentCategory.ACTIVITY),
    (9, "Entrega a familias", MomentCategory.PICKUP),
]

# (nombre, apellido, sala, ¿principal?)
TEACHERS = [
    ("Karla", "Robles", "Preescolar 2", False),
    ("Paola", "Sánchez", "Preescolar 2", True),
    ("Luis", "Medina", "Preescolar 2", False),
    ("Fernando", "Ríos", "Maternal", True),
    ("Karla", "Vega", "Maternal", False),
    ("Gabriela", "Ortiz", "Preescolar 1", True),
    ("Andrés", "Cano", "Preescolar 1", False),
]

# (nombre, apellido, nacimiento, sala, tutor: (nombre, apellido, parentesco, teléfono) o None)
CHILDREN = [
    ("Mateo", "Iglesias", date(2022, 5, 10), "Preescolar 2", ("Laura", "Iglesias", Relationship.MOTHER, "55 1234 5678")),
    ("Renata", "Gómez", date(2023, 2, 20), "Maternal", ("Carlos", "Gómez", Relationship.FATHER, "55 2345 6789")),
    ("Emiliano", "Torres", date(2021, 4, 2), "Preescolar 1", ("Andrea", "Torres", Relationship.MOTHER, "55 3456 7890")),
    ("Valentina", "Ruiz", date(2022, 8, 30), "Preescolar 2", ("Sofía", "Ruiz", Relationship.OTHER, "55 4567 8901")),
    ("Diego", "Hernández", date(2024, 1, 15), "Maternal", ("Marisol", "Hernández", Relationship.MOTHER, "55 5678 9012")),
    ("Camila", "Flores", date(2021, 6, 12), "Preescolar 1", ("Jorge", "Flores", Relationship.OTHER, "55 6789 0123")),
    # los niños de Mi sala de miss Karla.
    ("Sofía", "Márquez", date(2022, 1, 20), "Preescolar 2", None),
    ("Mateo", "Torres", date(2021, 11, 3), "Preescolar 2", None),
    ("Lucía", "Ramírez", date(2022, 4, 18), "Preescolar 2", None),
    ("Diego", "Álvarez", date(2021, 12, 9), "Preescolar 2", None),
    ("Renata", "Núñez", date(2022, 2, 27), "Preescolar 2", None),
]


def email_for(name: str, surname: str) -> str:
    plain = str.maketrans("áéíóúñÁÉÍÓÚÑ", "aeiounAEIOUN")
    return f"{name}.{surname}".translate(plain).lower() + "@oruga.test"


async def seed() -> None:
    async with SessionLocal() as db:
        if await db.scalar(select(DaycareCenter.id).limit(1)) is not None:
            print("La base ya tiene datos; no se siembra nada.")
            return

        # en modo dev cada persona ya "aceptó" su invitación y entra con Bearer dev:<correo>;
        # con Firebase, cada quien la acepta al entrar con Google.
        logins: list[tuple[str, str]] = []

        async def account(user: AppUser, role: Role) -> None:
            await db.flush()
            if settings.auth_mode == "dev":
                user.firebase_uid = f"dev:{user.email}"
            else:
                await auth.create_invitation(db, user, role)
            logins.append((role.value, user.email))

        center = DaycareCenter(name="La Oruga", latitude=25.6516, longitude=-100.2896, check_in_radius_m=60)
        db.add(center)
        await db.flush()

        morning = Shift(daycare_center_id=center.id, name="Matutino", start_time=time(8), end_time=time(16))
        afternoon = Shift(daycare_center_id=center.id, name="Vespertino", start_time=time(12), end_time=time(20))
        template = RoutineTemplate(name="Día completo · Preescolar", is_default=True)
        db.add_all([morning, afternoon, template])
        await db.flush()
        db.add_all(
            RoutineTemplateMoment(template_id=template.id, number=number, name=name, category=category)
            for number, name, category in ROUTINE
        )

        classrooms = {
            name: Classroom(daycare_center_id=center.id, name=name, capacity=capacity, school_grade=grade)
            for name, capacity, grade in [
                ("Maternal", 10, "Maternal"),
                ("Preescolar 1", 12, "Preescolar"),
                ("Preescolar 2", 12, "Preescolar"),
            ]
        }
        db.add_all(classrooms.values())
        await db.flush()

        admin = AppUser(name="Dirección", surname="La Oruga", email=settings.seed_admin_email.lower())
        db.add(admin)
        await account(admin, Role.ADMIN)
        db.add(Admin(user_id=admin.id, daycare_center_id=center.id))

        for name, surname, classroom, primary in TEACHERS:
            user = AppUser(name=name, surname=surname, phone="81 1000 2000", email=email_for(name, surname))
            db.add(user)
            await account(user, Role.TEACHER)
            db.add(
                Teacher(
                    user_id=user.id,
                    classroom_id=classrooms[classroom].id,
                    is_primary=primary,
                    shift_id=morning.id,
                )
            )

        first_child = None
        for name, surname, birth_date, classroom, guardian in CHILDREN:
            child = Child(name=name, surname=surname, birth_date=birth_date, classroom_id=classrooms[classroom].id)
            db.add(child)
            await db.flush()
            first_child = first_child or child
            if guardian is None:
                continue
            g_name, g_surname, relationship, phone = guardian
            user = AppUser(name=g_name, surname=g_surname, phone=phone, email=email_for(g_name, g_surname))
            db.add(user)
            await account(user, Role.PARENT)
            db.add(Parent(user_id=user.id))
            await db.flush()
            db.add(ParentChildRel(parent_id=user.id, child_id=child.id, relationship=relationship, is_primary=True))

        # la pantalla Documentos de Mateo Iglesias.
        db.add_all(
            [
                ChildDocument(child_id=first_child.id, title="Certificado de alergias"),
                ChildDocument(child_id=first_child.id, title="Informe de discapacidad", due_date=date(2026, 10, 9)),
                AuthorizedPickup(child_id=first_child.id, full_name="Andrea Torres", added_by=admin.id),
                AuthorizedPickup(child_id=first_child.id, full_name="Luis Torres", added_by=admin.id),
            ]
        )
        await db.commit()

        print(f"Listo: centro «{center.name}», 3 salas, {len(TEACHERS)} docentes y {len(CHILDREN)} niños.")
        if settings.auth_mode == "dev":
            print("Entra con Authorization: Bearer dev:<correo>")
        for role, email in logins:
            print(f"  {role:<8} {email}")


if __name__ == "__main__":
    asyncio.run(seed())
