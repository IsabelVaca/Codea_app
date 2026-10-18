from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Lo que cambia entre tu máquina y el servidor vive en variables de entorno (.env), no en el código."""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    env: str = "dev"
    database_url: str = "postgresql+asyncpg://guarderia:guarderia@localhost:5432/guarderia"

    # llave con la que se firman los access tokens (HS256). Sin valor por omisión: la API no arranca sin ella.
    # se genera con: python -c "import secrets; print(secrets.token_urlsafe(48))"
    jwt_secret: str = Field(min_length=32)
    # el access token dura poco; el refresh token se rota en cada uso.
    access_ttl_s: int = 5 * 60
    refresh_ttl_s: int = 7 * 24 * 3600
    # intentos fallidos de entrada: tantos por correo (o por IP) en la ventana y se bloquea hasta que pase.
    auth_max_failures: int = 5
    auth_max_failures_ip: int = 30
    auth_window_s: int = 15 * 60
    # orígenes web que pueden llamar a la API desde un navegador, separados por coma. Vacío: ninguno.
    # la app Android no lo necesita; solo sirve si algún día hay una página web.
    cors_origins: str = ""
    # contraseña que el seed les pone a las personas de ejemplo (solo para desarrollo).
    seed_password: str = "codea1234"

    # s3_endpoint_url es la dirección interna (la API habla con el bucket);
    # s3_public_endpoint_url es la que ve el teléfono, y con ella se firman las URLs.
    s3_endpoint_url: str | None = "http://localhost:9000"
    s3_public_endpoint_url: str | None = None
    s3_bucket: str = "guarderia"
    s3_access_key: str = "guarderia"
    s3_secret_key: str = "guarderia-secret"
    s3_region: str = "us-east-1"

    # días que dura una invitación antes de que haya que reenviarla.
    invitation_days: int = 7
    # correo del primer administrador que crea el seed.
    seed_admin_email: str = "admin@oruga.test"


settings = Settings()
