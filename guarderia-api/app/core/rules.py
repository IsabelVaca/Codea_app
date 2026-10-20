import unicodedata
from datetime import date

from .errors import ApiError

# Las mismas reglas que los validadores de domain/ en la app (SalaValidator,
# DocenteValidator, InfanteValidator, PadreValidator). La app responde al instante;
# la API repite la regla y tiene la última palabra. Cada error lleva el code que la app traduce.


def require_name(name: str) -> str:
    if not name or not name.strip():
        raise ApiError(422, "NAME_REQUIRED", "El nombre es obligatorio")
    return name.strip()


def check_phone(phone: str) -> str:
    if phone is None or len(phone.strip()) < 10:
        raise ApiError(422, "PHONE_INVALID", "El teléfono debe tener al menos 10 caracteres")
    return phone.strip()


def clean_email(email: str | None) -> str:
    """El correo sin espacios ni caracteres invisibles (de formato, como U+200B o U+2068) y en minúsculas.
    Un correo copiado de WhatsApp puede traerlos, y con ellos ya no coincide con el de la invitación."""
    return "".join(ch for ch in (email or "") if not ch.isspace() and unicodedata.category(ch) != "Cf").lower()


def check_email(email: str) -> str:
    email = clean_email(email)
    if "@" not in email or "." not in email:
        raise ApiError(422, "EMAIL_INVALID", "El correo no es válido")
    return email


def check_capacity(capacity: int) -> int:
    if capacity is None or capacity <= 0:
        raise ApiError(422, "CAPACITY_INVALID", "El cupo debe ser mayor que 0")
    return capacity


def age_on(birth_date: date, today: date) -> int:
    """Años cumplidos a una fecha."""
    years = today.year - birth_date.year
    if (today.month, today.day) < (birth_date.month, birth_date.day):
        years -= 1
    return years


def check_age(birth_date: date, today: date) -> int:
    if birth_date > today or not 0 <= age_on(birth_date, today) <= 12:
        raise ApiError(422, "AGE_INVALID", "La edad debe estar entre 0 y 12 años")
    return age_on(birth_date, today)


PASSWORD_MIN = 9
PASSWORD_MAX = 128


def check_password(password: str, email: str | None = None) -> str:
    """Más de 8 caracteres (RNF01; la app además pide una mayúscula y un número, solo del
    lado de la app), y un tope para que nadie mande textos enormes y gaste CPU en Argon2."""
    if password is None or len(password) < PASSWORD_MIN:
        raise ApiError(422, "PASSWORD_TOO_SHORT", f"La contraseña debe tener al menos {PASSWORD_MIN} caracteres")
    if len(password) > PASSWORD_MAX:
        raise ApiError(422, "PASSWORD_TOO_LONG", f"La contraseña no puede pasar de {PASSWORD_MAX} caracteres")
    if email and password.strip().lower() == email.strip().lower():
        raise ApiError(422, "PASSWORD_IS_EMAIL", "La contraseña no puede ser tu correo")
    return password
