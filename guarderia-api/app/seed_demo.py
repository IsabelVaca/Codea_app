"""Datos de prueba completos: borra TODA la base y el bucket, y llena cada tabla con datos coherentes.

Pensado para el servidor compartido (codeaapp.site) y para desarrollo. Arma las últimas dos semanas de un centro
con tres salas: pase de lista, rutina, reportes con fotos, resúmenes del día, salidas, checador de las docentes,
menús, chats, avisos y notas. Hoy solo la sala Maternal va a medio día; Preescolar 1 y 2 quedan sin empezar para
probar el flujo de la docente desde cero.

Todas las cuentas entran con la contraseña de SEED_PASSWORD. Fuera de desarrollo se niega a usar la de por omisión
(`codea1234`), porque esa está escrita en el repositorio, que es público.

    python -m app.seed_demo --borrar-todo            # sin fotos
    python -m app.seed_demo --borrar-todo --con-fotos # con fotos de prueba generadas

Quién es quién (familias, salas y correos): docs/DATOS-DE-PRUEBA.md.
"""

import asyncio
import random
import secrets
import struct
import sys
import uuid
import zlib
from datetime import date, datetime, time, timedelta
from zoneinfo import ZoneInfo

from sqlalchemy import text

import app.core.registry  # noqa: F401  (registra todas las tablas)
from app.core import tokens
from app.core.config import settings
from app.core.db import Base, SessionLocal
from app.core.passwords import hash_password
from app.core.storage import get_storage
from app.modules.admins.models import Admin
from app.modules.attendance.models import Attendance, AttendanceStatus
from app.modules.audit.models import AuditLog
from app.modules.auth.models import AppUser, AuthAttempt, CodePurpose, Invitation, RefreshToken, Role, UserStatus
from app.modules.children.models import Child
from app.modules.classrooms.models import Classroom, DaycareCenter
from app.modules.departures.models import ChildDeparture
from app.modules.documents.models import AuthorizedPickup, ChildDocument
from app.modules.media.models import DeletionStatus, Media, MediaChild, MediaDeletionRequest, MediaKind, MediaStatus
from app.modules.menus.models import DailyMenu
from app.modules.messaging.models import Announcement, Conversation, ConversationRead, Message, ParentNote
from app.modules.parents.models import Parent, ParentChildRel, Relationship
from app.modules.reports.models import DailySummary, MealAmount, Mood, Report, ReportChild, ReportMethod, ReportType
from app.modules.routines.models import (
    MomentCategory,
    MomentStatus,
    RoutineMoment,
    RoutineTemplate,
    RoutineTemplateMoment,
)
from app.modules.staff_attendance.models import CheckInStatus, CoordinationNotice, CoordinationReason, StaffCheckIn
from app.modules.teachers.models import Shift, Teacher

TZ = ZoneInfo("America/Monterrey")
PAST_DAYS = 10  # días hábiles de historia
PHOTO_DAYS = 6  # de esos, cuántos llevan fotos (solo con --con-fotos)
# sin fotos por ahora (19/oct): las tablas de media quedan vacías y el bucket solo se limpia.
WITH_PHOTOS = "--con-fotos" in sys.argv
rnd = random.Random(2026)

# (número, nombre, categoría, hora de inicio, minutos)
PRESCHOOL = [
    (1, "Recepción y bienvenida", MomentCategory.ARRIVAL, time(8, 0), 30),
    (2, "Desayuno", MomentCategory.MEAL, time(8, 30), 40),
    (3, "Trazos y motricidad", MomentCategory.ACTIVITY, time(9, 15), 60),
    (4, "Patio y juego libre", MomentCategory.ACTIVITY, time(10, 30), 45),
    (5, "Comida", MomentCategory.MEAL, time(12, 30), 40),
    (6, "Siesta", MomentCategory.NAP, time(13, 15), 60),
    (7, "Higiene y cambio", MomentCategory.HYGIENE, time(14, 20), 20),
    (8, "Actividad de la tarde", MomentCategory.ACTIVITY, time(14, 45), 40),
    (9, "Entrega a familias", MomentCategory.PICKUP, time(15, 30), 45),
]
TODDLER = [
    (1, "Recepción y bienvenida", MomentCategory.ARRIVAL, time(8, 0), 30),
    (2, "Desayuno", MomentCategory.MEAL, time(8, 30), 40),
    (3, "Estimulación temprana", MomentCategory.ACTIVITY, time(9, 15), 45),
    (4, "Juego sensorial", MomentCategory.ACTIVITY, time(10, 15), 45),
    (5, "Comida", MomentCategory.MEAL, time(12, 0), 40),
    (6, "Siesta", MomentCategory.NAP, time(12, 45), 90),
    (7, "Cambio de pañal", MomentCategory.HYGIENE, time(14, 20), 20),
    (8, "Música y cuentos", MomentCategory.ACTIVITY, time(14, 45), 40),
    (9, "Entrega a familias", MomentCategory.PICKUP, time(15, 30), 45),
]
MOMENT_NOTES = {
    MomentCategory.ARRIVAL: "Llegaron todos tranquilos; se revisó temperatura y mochilas.",
    MomentCategory.MEAL: "Comieron en grupo; se reforzó lavarse las manos antes y después.",
    MomentCategory.ACTIVITY: "Actividad guiada en mesas de trabajo y cierre con canción.",
    MomentCategory.NAP: "Descanso con luz baja y música suave.",
    MomentCategory.HYGIENE: "Cambio de pañal / ida al baño y lavado de manos.",
    MomentCategory.PICKUP: "Entrega a las familias con pase de salida.",
}

# (nombre, apellido, sala, ¿principal?, turno, teléfono)
TEACHERS = [
    ("Fernando", "Ríos", "Maternal", True, "Matutino", "81 1100 2001"),
    ("Karla", "Vega", "Maternal", False, "Matutino", "81 1100 2002"),
    ("Gabriela", "Ortiz", "Preescolar 1", True, "Matutino", "81 1100 2003"),
    ("Andrés", "Cano", "Preescolar 1", False, "Vespertino", "81 1100 2004"),
    ("Paola", "Sánchez", "Preescolar 2", True, "Matutino", "81 1100 2005"),
    ("Karla", "Robles", "Preescolar 2", False, "Matutino", "81 1100 2006"),
    ("Luis", "Medina", "Preescolar 2", False, "Vespertino", "81 1100 2007"),
]

# tutores: clave -> (nombre, apellido, teléfono)
GUARDIANS = {
    "laura": ("Laura", "Iglesias", "81 2200 3001"),
    "hector": ("Héctor", "Iglesias", "81 2200 3002"),
    "carlos": ("Carlos", "Gómez", "81 2200 3003"),
    "daniela": ("Daniela", "Pérez", "81 2200 3004"),
    "marisol": ("Marisol", "Hernández", "81 2200 3005"),
    "rosa": ("Rosa", "Treviño", "81 2200 3006"),
    "andrea": ("Andrea", "Torres", "81 2200 3007"),
    "ricardo": ("Ricardo", "Torres", "81 2200 3008"),
    "jorge": ("Jorge", "Flores", "81 2200 3009"),
    "elena": ("Elena", "Márquez", "81 2200 3010"),
    "miguel": ("Miguel", "Cantú", "81 2200 3011"),
    "sofia": ("Sofía", "Ruiz", "81 2200 3012"),
    "patricia": ("Patricia", "Ramírez", "81 2200 3013"),
}

M, F, LG, O = Relationship.MOTHER, Relationship.FATHER, Relationship.LEGAL_GUARDIAN, Relationship.OTHER
# (nombre, apellido, nacimiento, sala, [(tutor, parentesco, ¿principal?)], salud, autorizados)
# salud: (alergias, condiciones médicas, intolerancias, medicamentos, otras indicaciones)
# autorizados: [(nombre completo, parentesco, teléfono)]
CHILDREN = [
    ("Ana", "Iglesias", date(2024, 9, 14), "Maternal", [("laura", M, True), ("hector", F, False)],
     ("Ninguna conocida", "Ninguna", "Lactosa (leche deslactosada)", "Ninguno", "Usa chupón solo para dormir"),
     [("Carmen Iglesias", "Abuela", "81 3300 4001")]),
    ("Renata", "Gómez", date(2024, 2, 20), "Maternal", [("carlos", F, True), ("daniela", M, False)],
     ("Penicilina", "Dermatitis atópica leve", "Ninguna", "Crema hidratante después del baño", "Le gusta dormir con su manta"),
     [("Gloria Pérez", "Abuela", "81 3300 4002")]),
    ("Diego", "Hernández", date(2025, 1, 15), "Maternal", [("marisol", M, True)],
     ("Ninguna conocida", "Ninguna", "Huevo (en observación)", "Ninguno", "Está dejando el biberón"),
     [("Tomás Hernández", "Tío", "81 3300 4003"), ("Lupita Garza", "Niñera", "81 3300 4004")]),
    ("Leo", "Martínez", date(2024, 6, 3), "Maternal", [("rosa", LG, True)],
     ("Picadura de abeja", "Ninguna", "Ninguna", "Ninguno", "Vive con su abuela, que es su tutora legal"),
     [("Alberto Treviño", "Tío abuelo", "81 3300 4005")]),
    ("Emiliano", "Torres", date(2023, 4, 2), "Preescolar 1", [("andrea", M, True), ("ricardo", F, False)],
     ("Cacahuate", "Asma leve", "Ninguna", "Salbutamol en inhalador si hay crisis", "El inhalador va en su mochila"),
     [("Luis Torres", "Abuelo", "81 3300 4006")]),
    ("Camila", "Flores", date(2023, 6, 12), "Preescolar 1", [("jorge", F, True)],
     ("Ninguna conocida", "Ninguna", "Gluten", "Ninguno", "Usa lentes; recordarle ponérselos"),
     [("Mariana Flores", "Tía", "81 3300 4007")]),
    ("Sofía", "Márquez", date(2023, 9, 25), "Preescolar 1", [("elena", M, True)],
     ("Fresa", "Ninguna", "Ninguna", "Ninguno", "Zurda"),
     [("Raúl Márquez", "Papá (no vive con ella)", "81 3300 4008")]),
    ("Santiago", "Cantú", date(2023, 3, 8), "Preescolar 1", [("miguel", F, True)],
     ("Ninguna conocida", "Ninguna", "Ninguna", "Ninguno", "En terapia de lenguaje los martes"),
     [("Ana Cantú", "Tía", "81 3300 4009")]),
    ("Mateo", "Iglesias", date(2022, 5, 10), "Preescolar 2", [("laura", M, True), ("hector", F, False)],
     ("Ninguna conocida", "Ninguna", "Ninguna", "Ninguno", "Hermano mayor de Ana (Maternal)"),
     [("Carmen Iglesias", "Abuela", "81 3300 4001")]),
    ("Valentina", "Ruiz", date(2022, 8, 30), "Preescolar 2", [("sofia", O, True)],
     ("Mariscos", "Ninguna", "Ninguna", "Ninguno", "Su tía Sofía es quien la cuida"),
     [("Pedro Ruiz", "Tío", "81 3300 4010")]),
    ("Regina", "Torres", date(2022, 1, 20), "Preescolar 2", [("andrea", M, True), ("ricardo", F, False)],
     ("Ninguna conocida", "Ninguna", "Ninguna", "Vitaminas por la mañana (en casa)", "Hermana mayor de Emiliano (Preescolar 1)"),
     [("Luis Torres", "Abuelo", "81 3300 4006")]),
    ("Lucía", "Ramírez", date(2022, 4, 18), "Preescolar 2", [("patricia", M, True)],
     ("Polen", "Rinitis alérgica", "Ninguna", "Loratadina en temporada de polen", "Avisar si estornuda mucho"),
     [("Jesús Ramírez", "Abuelo", "81 3300 4011")]),
]

MENU_ROTATION = [
    ("Avena con plátano y leche", "Sopa de fideo, pollo con verduras y arroz", "Gelatina de fresa"),
    ("Hot cakes de avena con fruta", "Crema de calabaza, albóndigas en caldillo y frijoles", "Pepino y jícama con limón"),
    ("Huevo revuelto con frijoles y tortilla", "Sopa de verduras, pescado empanizado y puré", "Yogur natural con granola"),
    ("Molletes con pico de gallo", "Arroz rojo, tinga de pollo y ensalada", "Manzana en gajos"),
    ("Pan tostado con aguacate y fruta", "Sopa de lentejas, carne deshebrada y calabacitas", "Galletas de avena caseras"),
]


def email_for(name: str, surname: str) -> str:
    plain = str.maketrans("áéíóúñÁÉÍÓÚÑ", "aeiounAEIOUN")
    return f"{name}.{surname}".translate(plain).lower() + "@oruga.test"


def at(day: date, hour: int, minute: int = 0) -> datetime:
    return datetime.combine(day, time(hour, minute), TZ)


def plus(moment: datetime, minutes: int) -> datetime:
    return moment + timedelta(minutes=minutes)


# ---------- imágenes de prueba (PNG sin librerías) ----------

PALETTES = {
    "MEAL": ((255, 214, 153), (240, 140, 60)),
    "ACTIVITY": ((190, 235, 190), (60, 160, 90)),
    "NAP": ((200, 210, 255), (70, 90, 180)),
    "INCIDENT": ((255, 205, 205), (200, 70, 70)),
    "CHAT": ((230, 215, 255), (130, 90, 200)),
    "NOTICE": ((255, 240, 180), (220, 160, 40)),
    "DOCUMENT": ((245, 245, 245), (180, 180, 180)),
}


def make_png(kind: str, seed: int, width: int = 1200, height: int = 900) -> bytes:
    """Un degradado con círculos: suficiente para ver que la foto llega completa y en buena resolución."""
    top, bottom = PALETTES[kind]
    r = random.Random(seed)
    circles = [
        (r.randint(100, width - 100), r.randint(100, height - 100), r.randint(40, 180),
         tuple(min(255, c + r.randint(-60, 60)) if c > 60 else c for c in bottom))
        for _ in range(r.randint(3, 6))
    ]
    raw = bytearray()
    for y in range(height):
        t = y / (height - 1)
        base = bytes(int(a + (b - a) * t) for a, b in zip(top, bottom))
        row = bytearray(base * width)
        for cx, cy, radius, color in circles:
            dy = y - cy
            if abs(dy) < radius:
                half = int((radius * radius - dy * dy) ** 0.5)
                x0, x1 = max(cx - half, 0), min(cx + half, width)
                row[x0 * 3:x1 * 3] = bytes(color) * (x1 - x0)
        raw += b"\x00" + row

    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data))

    header = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", header) + chunk(b"IDAT", zlib.compress(bytes(raw), 6)) + chunk(b"IEND", b"")


# ---------- borrar todo ----------


async def wipe(storage) -> None:
    async with SessionLocal() as db:
        names = ", ".join(f'"{t.name}"' for t in Base.metadata.sorted_tables)
        await db.execute(text(f"TRUNCATE {names} RESTART IDENTITY CASCADE"))
        await db.commit()
    storage.ensure_bucket()
    client, bucket = storage._internal, storage.bucket
    deleted = 0
    for page in client.get_paginator("list_objects_v2").paginate(Bucket=bucket):
        keys = [{"Key": item["Key"]} for item in page.get("Contents", [])]
        if keys:
            client.delete_objects(Bucket=bucket, Delete={"Objects": keys})
            deleted += len(keys)
    print(f"Base vacía ({len(Base.metadata.sorted_tables)} tablas) y {deleted} archivos borrados del bucket.")


# ---------- llenar ----------


async def seed(password: str) -> None:
    storage = get_storage()
    now = datetime.now(TZ)
    today = now.date()
    workdays: list[date] = []
    cursor = today - timedelta(days=1)
    while len(workdays) < PAST_DAYS:
        if cursor.isoweekday() <= 5:
            workdays.append(cursor)
        cursor -= timedelta(days=1)
    workdays.reverse()
    photo_days = set(workdays[-PHOTO_DAYS:])
    start = workdays[0] - timedelta(days=30)  # cuándo "se inscribieron" todos

    pw_hash = hash_password(password)
    image_seed = iter(range(10_000))

    async with SessionLocal() as db:

        def stamp(row, when: datetime):
            row.created_at = when
            row.updated_at = when
            return row

        def upload(kind: str, author: AppUser, when: datetime, media_kind=MediaKind.PHOTO) -> Media:
            data = make_png(kind, next(image_seed))
            key = f"media/{when:%Y/%m}/{uuid.uuid4()}/original.png"
            storage._internal.put_object(Bucket=storage.bucket, Key=key, Body=data, ContentType="image/png")
            media = stamp(
                Media(kind=media_kind, storage_key=key, content_type="image/png", size_bytes=len(data),
                      status=MediaStatus.READY, uploaded_by=author.id),
                when,
            )
            db.add(media)
            return media

        audit: list[AuditLog] = []

        def log(action, when, actor=None, entity_type=None, entity_id=None, detail=None, email=None, ip="187.190.10.20"):
            audit.append(stamp(AuditLog(actor_id=actor.id if actor else None, actor_email=email or (actor.email if actor else None),
                                        action=action, entity_type=entity_type,
                                        entity_id=str(entity_id) if entity_id is not None else None,
                                        detail=detail, ip=ip), when))

        # --- centro, turnos, rutinas, salas ---
        center = stamp(DaycareCenter(name="La Oruga", latitude=25.6516, longitude=-100.2896, check_in_radius_m=60,
                                     timezone="America/Monterrey", entry_time=time(8), exit_time=time(16),
                                     auto_close_time=time(21), work_days=[1, 2, 3, 4, 5]), at(start, 9))
        db.add(center)
        await db.flush()
        shifts = {
            "Matutino": Shift(daycare_center_id=center.id, name="Matutino", start_time=time(8), end_time=time(16)),
            "Vespertino": Shift(daycare_center_id=center.id, name="Vespertino", start_time=time(12), end_time=time(20)),
        }
        t_pre = RoutineTemplate(name="Día completo · Preescolar", is_default=True)
        t_mat = RoutineTemplate(name="Día completo · Maternal", is_default=False)
        db.add_all([*shifts.values(), t_pre, t_mat])
        await db.flush()
        templates = {t_pre.id: PRESCHOOL, t_mat.id: TODDLER}
        for template_id, moments in templates.items():
            db.add_all(RoutineTemplateMoment(template_id=template_id, number=n, name=name, category=cat)
                       for n, name, cat, _, _ in moments)

        rooms = {
            "Maternal": Classroom(daycare_center_id=center.id, name="Maternal", school_grade="Maternal (1 a 3 años)",
                                  capacity=10, routine_template_id=t_mat.id),
            "Preescolar 1": Classroom(daycare_center_id=center.id, name="Preescolar 1", school_grade="1° de preescolar",
                                      capacity=12, routine_template_id=t_pre.id),
            "Preescolar 2": Classroom(daycare_center_id=center.id, name="Preescolar 2", school_grade="2° de preescolar",
                                      capacity=12, routine_template_id=t_pre.id),
        }
        archived = Classroom(daycare_center_id=center.id, name="Lactantes", school_grade="Lactantes (0 a 1 año)",
                             capacity=8, routine_template_id=t_mat.id, deleted_at=at(start, 12))
        db.add_all([*rooms.values(), archived])
        await db.flush()
        template_of = {room.id: templates[room.routine_template_id] for room in rooms.values()}

        # --- cuentas ---
        users: list[tuple[AppUser, Role]] = []

        def person(name, surname, phone, role, email=None, status=UserStatus.ACTIVE) -> AppUser:
            user = AppUser(name=name, surname=surname, phone=phone, email=email or email_for(name, surname),
                           password_hash=pw_hash, password_changed_at=at(start, 10),
                           last_login_at=plus(at(workdays[-1], 7), rnd.randint(30, 80)), token_version=0, status=status)
            stamp(user, at(start, 9, 30))
            db.add(user)
            users.append((user, role))
            return user

        admin = person("Dirección", "La Oruga", "81 1000 2000", Role.ADMIN, email=settings.seed_admin_email.lower())
        await db.flush()
        db.add(Admin(user_id=admin.id, daycare_center_id=center.id))

        teachers: dict[str, list[AppUser]] = {name: [] for name in rooms}
        primary: dict[str, AppUser] = {}
        for name, surname, room, is_primary, shift, phone in TEACHERS:
            user = person(name, surname, phone, Role.TEACHER)
            await db.flush()
            db.add(Teacher(user_id=user.id, classroom_id=rooms[room].id, is_primary=is_primary,
                           shift_id=shifts[shift].id))
            teachers[room].append(user)
            if is_primary:
                primary[room] = user
        # una docente dada de baja: no puede entrar (USER_INACTIVE) y no tiene sala.
        former = person("Mónica", "Salinas", "81 1100 2099", Role.TEACHER, status=UserStatus.INACTIVE)
        former.token_version = 1
        await db.flush()
        db.add(Teacher(user_id=former.id, classroom_id=None, is_primary=False, shift_id=shifts["Matutino"].id))

        guardians: dict[str, AppUser] = {}
        for key, (name, surname, phone) in GUARDIANS.items():
            guardians[key] = person(name, surname, phone, Role.PARENT)
        await db.flush()
        db.add_all(Parent(user_id=user.id) for user in guardians.values())

        # --- niños ---
        children: list[Child] = []
        room_of: dict[int, str] = {}
        family: dict[int, list[tuple[AppUser, Relationship]]] = {}
        pickups: dict[int, list[AuthorizedPickup]] = {}
        for name, surname, birth, room, rels, health, authorized in CHILDREN:
            allergies, medical, intolerances, medications, notes = health
            child = stamp(Child(classroom_id=rooms[room].id, name=name, surname=surname, birth_date=birth,
                                allergies=allergies, medical_conditions=medical, intolerances=intolerances,
                                medications=medications, other_notes=notes), at(start, 11))
            db.add(child)
            await db.flush()
            children.append(child)
            room_of[child.id] = room
            family[child.id] = [(guardians[key], rel) for key, rel, _ in rels]
            for key, rel, is_primary in rels:
                db.add(ParentChildRel(parent_id=guardians[key].id, child_id=child.id, relationship=rel,
                                      is_primary=is_primary))
            pickups[child.id] = [
                AuthorizedPickup(child_id=child.id, full_name=full, relationship=rel, phone=phone, added_by=admin.id)
                for full, rel, phone in authorized
            ]
            db.add_all(pickups[child.id])
            log("CHILD_ENROLLED", at(start, 11), admin, "CHILD", child.id, {"classroom": room})
        await db.flush()
        by_room = {room: [c for c in children if room_of[c.id] == room] for room in rooms}

        # --- documentos: el acta ya está, la cartilla falta ---
        for child in children:
            parent = family[child.id][0][0]
            when = plus(at(start + timedelta(days=3), 19), rnd.randint(0, 120))
            doc_media = upload("DOCUMENT", parent, when, MediaKind.DOCUMENT) if WITH_PHOTOS else None
            await db.flush()
            db.add_all([
                ChildDocument(child_id=child.id, title="Acta de nacimiento", due_date=start + timedelta(days=7),
                              media_id=doc_media.id if doc_media else None, uploaded_at=when if doc_media else None),
                ChildDocument(child_id=child.id, title="Cartilla de vacunación actualizada",
                              due_date=today + timedelta(days=14)),
            ])

        # --- invitaciones, sesiones e intentos ---
        for user, role in users:
            accepted = None if user.status == UserStatus.INACTIVE else at(start, 10)
            db.add(stamp(Invitation(user_id=user.id, email=user.email, role=role,
                                    code_hash=tokens.hash_code(secrets.token_hex(4)), purpose=CodePurpose.INVITATION,
                                    expires_at=at(start + timedelta(days=7), 23, 59), accepted_at=accepted),
                         at(start, 9, 30)))
            log("USER_CREATED", at(start, 9, 30), admin, "USER", user.id, {"role": role.value})
            if accepted:
                log("ACCOUNT_ACTIVATED", accepted, user, "USER", user.id)
        reset_user = guardians["marisol"]
        reset_at = at(workdays[2], 20, 15)
        db.add(stamp(Invitation(user_id=reset_user.id, email=reset_user.email, role=Role.PARENT,
                                code_hash=tokens.hash_code(secrets.token_hex(4)), purpose=CodePurpose.PASSWORD_RESET,
                                expires_at=reset_at + timedelta(days=7), accepted_at=plus(reset_at, 25)), reset_at))
        log("ACCESS_CODE_ISSUED", reset_at, admin, "USER", reset_user.id, {"purpose": "PASSWORD_RESET"})
        log("PASSWORD_CHANGED", plus(reset_at, 25), reset_user, "USER", reset_user.id)

        for user in (admin, primary["Maternal"], guardians["laura"]):
            family_id = uuid.uuid4()
            first_login = at(workdays[-3], 7, 50)
            old = RefreshToken(user_id=user.id, token_hash=tokens.hash_token(tokens.new_refresh_token()),
                               family_id=family_id, expires_at=first_login + timedelta(days=30),
                               revoked_at=first_login + timedelta(hours=2), last_used_at=first_login + timedelta(hours=2),
                               user_agent="okhttp/4.12.0 (Codea Android)")
            stamp(old, first_login)
            db.add(old)
            await db.flush()
            new = RefreshToken(user_id=user.id, token_hash=tokens.hash_token(tokens.new_refresh_token()),
                               family_id=family_id, expires_at=first_login + timedelta(days=30, hours=2),
                               revoked_at=at(workdays[-1], 18), last_used_at=at(workdays[-1], 17, 30),
                               user_agent="okhttp/4.12.0 (Codea Android)")
            stamp(new, first_login + timedelta(hours=2))
            db.add(new)
            await db.flush()
            old.replaced_by_id = new.id
            log("LOGIN_OK", first_login, user, "SESSION", family_id)
            log("SESSIONS_CLOSED", at(workdays[-1], 18), user, "SESSION", family_id)
        for minutes in (0, 1, 3):
            when = plus(at(workdays[1], 21), minutes)
            db.add(AuthAttempt(kind="LOGIN", email="carlos.gomez@oruga.test", ip="201.141.33.7", attempted_at=when))
            log("LOGIN_FAILED", when, guardians["carlos"], ip="201.141.33.7")
        db.add(AuthAttempt(kind="ACTIVATE", email="nadie@oruga.test", ip="201.141.33.8",
                           attempted_at=at(workdays[1], 21, 10)))
        log("ACTIVATION_FAILED", at(workdays[1], 21, 10), None, email="nadie@oruga.test", ip="201.141.33.8")
        log("USER_DEACTIVATED", at(workdays[0], 17), admin, "USER", former.id, {"reason": "Dejó de trabajar en el centro"})
        log("CENTER_UPDATED", at(start, 9, 15), admin, "CENTER", center.id, {"entry_time": "08:00", "exit_time": "16:00"})

        # --- menús: la historia, hoy y las dos semanas que siguen ---
        menu_day = workdays[0]
        while menu_day <= today + timedelta(days=14):
            if menu_day.isoweekday() <= 5:
                breakfast, lunch, snack = MENU_ROTATION[menu_day.toordinal() % len(MENU_ROTATION)]
                db.add(stamp(DailyMenu(daycare_center_id=center.id, date=menu_day, breakfast=breakfast, lunch=lunch,
                                       snack=snack, updated_by=admin.id), at(workdays[0] - timedelta(days=3), 13)))
            menu_day += timedelta(days=1)
        log("MENU_SAVED", at(workdays[0] - timedelta(days=3), 13), admin, "MENU", None,
            {"from": workdays[0].isoformat(), "to": (today + timedelta(days=14)).isoformat()})

        # --- los días ---
        photo_media: list[tuple[Media, list[Child]]] = []

        async def run_day(day: date, room: str, upto: int | None = None) -> None:
            """Un día de una sala. upto = número del momento en curso (hoy); None = día completo."""
            classroom = rooms[room]
            lead = primary[room]
            staff = teachers[room]
            kids = by_room[room]

            # checador de las docentes
            for teacher in staff:
                arrival = plus(at(day, 7, 40), rnd.randint(0, 28))
                late = max(0, (arrival.hour * 60 + arrival.minute) - 8 * 60)
                checkout = None if upto else plus(at(day, 16), rnd.randint(0, 35))
                auto = False
                if not upto and teacher is staff[-1] and day == workdays[3]:
                    checkout, auto = at(day, 21), True
                db.add(StaffCheckIn(teacher_id=teacher.id, date=day, checked_in_at=arrival,
                                    status=CheckInStatus.LATE if late else CheckInStatus.ON_TIME, minutes_late=late,
                                    late_reason="Tráfico en Constitución" if late else None,
                                    checked_out_at=checkout, auto_closed=auto))
                if late:
                    db.add(stamp(CoordinationNotice(teacher_id=teacher.id, date=day, reason=CoordinationReason.LATE,
                                                    message=f"Voy en camino, llego como a las {arrival:%H:%M}."),
                                 plus(at(day, 7), 45)))

            # pase de lista
            present: list[Child] = []
            for child in kids:
                is_absent = rnd.random() < 0.1
                arrived = None if is_absent else plus(at(day, 7, 45), rnd.randint(0, 40))
                db.add(Attendance(child_id=child.id, date=day,
                                  status=AttendanceStatus.ABSENT if is_absent else AttendanceStatus.PRESENT,
                                  marked_by=lead.id, arrived_at=arrived))
                if not is_absent:
                    present.append(child)

            # rutina
            moments: dict[int, RoutineMoment] = {}
            for number, name, category, begin, minutes in template_of[classroom.id]:
                started = datetime.combine(day, begin, TZ)
                if upto is None or number < upto:
                    status, confirmed, by = MomentStatus.DONE, plus(started, minutes), rnd.choice(staff)
                elif number == upto:
                    status, confirmed, by = MomentStatus.ACTIVE, None, None
                else:
                    status, confirmed, by, started = MomentStatus.PENDING, None, None, None
                moment = RoutineMoment(classroom_id=classroom.id, template_id=classroom.routine_template_id, date=day,
                                       number=number, name=name, category=category, status=status, started_at=started,
                                       confirmed_at=confirmed, confirmed_by=by.id if by else None,
                                       description=MOMENT_NOTES[category] if status == MomentStatus.DONE else None)
                db.add(moment)
                moments[number] = moment
            await db.flush()

            # reportes (con foto los últimos días)
            with_photos = day in photo_days or upto is not None
            plans = [
                (2, ReportType.ACHIEVEMENT, ReportMethod.PHOTO, "MEAL", 1, "Se comió todo el desayuno sin ayuda."),
                (3, ReportType.ACHIEVEMENT, ReportMethod.WRITTEN, "ACTIVITY", 3,
                 "Trabajaron en equipo y terminaron su actividad; muy concentrados."),
                (6, ReportType.HEALTH, ReportMethod.DICTATION, None, 1, "Durmió tranquilo toda la siesta."),
                (4, ReportType.BEHAVIOR, ReportMethod.DICTATION, None, 2, "Compartieron los juguetes sin pelear."),
            ]
            if day in (workdays[-2], workdays[-6]):
                plans.append((4, ReportType.INCIDENT, ReportMethod.WRITTEN, "INCIDENT", 1,
                              "Se raspó la rodilla en el patio; se limpió, se puso curita y se avisó a la familia."))
            for number, rtype, method, photo_kind, how_many, description in plans:
                moment = moments[number]
                if moment.status != MomentStatus.DONE or not present:
                    continue
                tagged = rnd.sample(present, min(how_many, len(present)))
                when = plus(moment.started_at, rnd.randint(5, 25))
                media = None
                if photo_kind and with_photos and WITH_PHOTOS:
                    media = upload(photo_kind, moment_author := rnd.choice(staff), when)
                    await db.flush()
                    db.add_all(MediaChild(media_id=media.id, child_id=c.id) for c in tagged)
                    photo_media.append((media, tagged))
                else:
                    moment_author = rnd.choice(staff)
                report = stamp(Report(routine_moment_id=moment.id, type=rtype,
                                      method=ReportMethod.PHOTO if media and method == ReportMethod.PHOTO else (
                                          ReportMethod.WRITTEN if method == ReportMethod.PHOTO else method),
                                      description=description, media_id=media.id if media else None,
                                      created_by=moment_author.id), when)
                db.add(report)
                await db.flush()
                db.add_all(ReportChild(report_id=report.id, child_id=c.id) for c in tagged)

            if upto is not None:
                return  # hoy: el día sigue, todavía no hay resumen ni salidas

            # resumen del día y pase de salida
            toddler = room == "Maternal"
            for child in present:
                db.add(DailySummary(child_id=child.id, date=day, mood=rnd.choice(list(Mood)),
                                    meals=rnd.choice([MealAmount.ALL, MealAmount.ALL, MealAmount.MOST, MealAmount.HALF,
                                                      MealAmount.LITTLE]),
                                    diaper_changes=rnd.randint(3, 5) if toddler else rnd.randint(0, 1),
                                    nap_minutes=rnd.randint(70, 100) if toddler else rnd.randint(40, 65),
                                    general_note=rnd.choice(["Un gran día, muy participativo.",
                                                             "Estuvo un poco cansado después de comer.",
                                                             "Hizo un dibujo para la familia.",
                                                             "Jugó mucho con sus compañeros."]),
                                    recorded_by=lead.id))
                departed = plus(at(day, 15, 30), rnd.randint(0, 45))
                if rnd.random() < 0.75:
                    guardian = rnd.choice(family[child.id])[0]
                    db.add(ChildDeparture(child_id=child.id, date=day, departed_at=departed, guardian_id=guardian.id,
                                          picked_up_by=guardian.full_name, recorded_by=lead.id))
                else:
                    pickup = rnd.choice(pickups[child.id])
                    db.add(ChildDeparture(child_id=child.id, date=day, departed_at=departed, pickup_id=pickup.id,
                                          picked_up_by=pickup.full_name, recorded_by=lead.id))
                log("CHILD_DEPARTED", departed, lead, "CHILD", child.id, {"date": day.isoformat()})

        for day in workdays:
            for room in rooms:
                await run_day(day, room)
        await run_day(today, "Maternal", upto=4)

        # --- borrados de fotos (historial) ---
        if WITH_PHOTOS:
            await photo_history(db, storage, photo_media, family, admin, primary, workdays, today, log, stamp)

        # --- avisos ---
        notices = [
            (None, "Este viernes es el Festival de Otoño: los esperamos a las 10:00 con disfraz.", "NOTICE", admin, workdays[-4]),
            (None, "Junta de padres el próximo martes a las 18:00 en el salón de usos múltiples.", None, admin, workdays[-2]),
            ("Preescolar 2", "Mañana vamos al museo: traer gorra, agua y lonche en una bolsa con nombre.", "NOTICE",
             primary["Preescolar 2"], workdays[-3]),
            ("Maternal", "Recuerden mandar pañales y toallitas para toda la semana.", None, primary["Maternal"], workdays[-1]),
            ("Preescolar 1", "Esta semana practicamos los colores; manden algo amarillo el jueves.", None,
             primary["Preescolar 1"], workdays[-5]),
        ]
        for room, body, image, author, day in notices:
            when = at(day, 9, 30)
            media = upload(image, author, when) if image and WITH_PHOTOS else None
            await db.flush()
            db.add(stamp(Announcement(daycare_center_id=center.id, classroom_id=rooms[room].id if room else None,
                                      text=body, author_id=author.id, media_id=media.id if media else None), when))

        # --- chats: uno por niño ---
        lines_parent = ["Buenos días, hoy llega un poco más tarde por cita con el pediatra.",
                        "¿Cómo comió hoy?", "¡Qué bien! Gracias por contarnos.",
                        "Mañana lo recoge su abuela."]
        lines_teacher = ["¡Buenos días! Sin problema, aquí lo esperamos.",
                         "Comió muy bien, se terminó la sopa.", "Hoy estuvo muy participativo en la actividad.",
                         "Anotado, gracias por avisar."]
        for child in children:
            conversation = stamp(Conversation(child_id=child.id), at(start, 11))
            db.add(conversation)
            await db.flush()
            parent = family[child.id][0][0]
            teacher = primary[room_of[child.id]]
            messages: list[Message] = []
            for i, day in enumerate(workdays[-3:]):
                ask = stamp(Message(conversation_id=conversation.id, author_id=parent.id, text=lines_parent[i],
                                    client_id=uuid.uuid4()), at(day, 7, 30 + i))
                answer_when = at(day, 9, 5 + i)
                media = None
                if i == 2 and WITH_PHOTOS:
                    media = upload("CHAT", teacher, answer_when)
                    await db.flush()
                answer = stamp(Message(conversation_id=conversation.id, author_id=teacher.id,
                                       text="" if media else lines_teacher[i], client_id=uuid.uuid4(),
                                       media_id=media.id if media else None), answer_when)
                db.add_all([ask, answer])
                await db.flush()
                messages += [ask, answer]
            db.add_all([
                ConversationRead(conversation_id=conversation.id, user_id=teacher.id,
                                 last_read_message_id=messages[-2].id),
                ConversationRead(conversation_id=conversation.id, user_id=parent.id,
                                 last_read_message_id=messages[-3].id),
            ])

        # --- notas de la familia para la docente ---
        for i, child in enumerate(children):
            parent = family[child.id][0][0]
            for day, body, read in (
                (workdays[-1], "Hoy durmió poco en la noche; puede estar cansado.", True),
                (today, "Hoy lo recoge su abuela a las 3:30.", False),
            ):
                if day == today and i % 2:
                    continue
                read_at = at(day, 8, 20) if read else None
                db.add(stamp(ParentNote(child_id=child.id, date=day, text=body, author_id=parent.id, read_at=read_at),
                             at(day, 7, 15)))

        db.add_all(audit)
        await db.commit()

    print(f"Listo: {len(users)} cuentas, {len(children)} niños en 3 salas, {len(workdays)} días de historia "
          f"({workdays[0]} a {workdays[-1]}) y hoy ({today}) Maternal a medio día.")
    print(f"Contraseña de todas las cuentas: la de SEED_PASSWORD. Cuentas:")
    for user, role in users:
        mark = "  (dada de baja)" if user.status == UserStatus.INACTIVE else ""
        print(f"  {role.value:<8} {user.email}{mark}")


async def photo_history(db, storage, photo_media, family, admin, primary, workdays, today, log, stamp) -> None:
    """Fotos borradas (solicitud aprobada, rechazada y borrado directo) y una subida que no se completó."""
    media_a, kids_a = photo_media[0]
    media_b, kids_b = photo_media[1]
    media_c, kids_c = photo_media[2]
    parent_a = family[kids_a[0].id][0][0]
    parent_b = family[kids_b[0].id][0][0]
    parent_c = family[kids_c[0].id][0][0]
    storage._internal.delete_object(Bucket=storage.bucket, Key=media_a.storage_key)
    media_a.status = MediaStatus.DELETED
    storage._internal.delete_object(Bucket=storage.bucket, Key=media_c.storage_key)
    media_c.status = MediaStatus.DELETED
    decided = at(workdays[-PHOTO_DAYS + 1], 12)
    db.add_all([
        stamp(MediaDeletionRequest(media_id=media_a.id, requested_by=parent_a.id,
                                   reason="Prefiero que no haya fotos de mi hijo comiendo.",
                                   status=DeletionStatus.APPROVED, decided_by=admin.id, decided_at=decided,
                                   decision_note="Listo, la foto se borró de todas las galerías."),
              decided - timedelta(hours=3)),
        stamp(MediaDeletionRequest(media_id=media_b.id, requested_by=parent_b.id, reason="Salió movida.",
                                   status=DeletionStatus.REJECTED, decided_by=admin.id, decided_at=decided,
                                   decision_note="Se conserva: en la foto salen otros niños de la actividad."),
              decided - timedelta(hours=2)),
    ])
    log("MEDIA_DELETION_APPROVED", decided, admin, "MEDIA", media_a.id)
    log("MEDIA_DELETION_REJECTED", decided, admin, "MEDIA", media_b.id)
    log("MEDIA_DELETED_BY_FAMILY", plus(decided, 90), parent_c, "MEDIA", media_c.id)
    # una subida que nunca se completó (se cortó la red)
    db.add(stamp(Media(kind=MediaKind.PHOTO, storage_key=f"media/{today:%Y/%m}/{uuid.uuid4()}/original.jpg",
                       content_type="image/jpeg", size_bytes=2_345_678, status=MediaStatus.PENDING,
                       uploaded_by=primary["Preescolar 1"].id), at(workdays[-1], 10, 5)))


async def main() -> None:
    if "--borrar-todo" not in sys.argv:
        sys.exit("Esto borra TODA la base y el bucket. Corre con --borrar-todo si de verdad quieres hacerlo.")
    password = settings.seed_password
    if settings.env != "dev" and password == "codea1234":
        sys.exit("Fuera de desarrollo pon SEED_PASSWORD con otra contraseña: codea1234 está publicada en el repo.")
    await wipe(get_storage())
    await seed(password)


if __name__ == "__main__":
    asyncio.run(main())
