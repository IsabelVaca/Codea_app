from typing import Literal

from pydantic import model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Lo que cambia entre tu máquina y el servidor vive en variables de entorno (.env), no en el código."""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    env: str = "dev"
    database_url: str = "postgresql+asyncpg://guarderia:guarderia@localhost:5432/guarderia"

    # firebase = tokens reales de Firebase Auth. dev = "Bearer dev:<correo>", solo para desarrollo y pruebas.
    auth_mode: Literal["firebase", "dev"] = "firebase"
    firebase_project_id: str | None = None
    # ruta al JSON de la cuenta de servicio; solo hace falta para revocar sesiones al dar de baja.
    firebase_credentials: str | None = None

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

    @model_validator(mode="after")
    def _dev_auth_only_in_dev(self) -> "Settings":
        if self.auth_mode == "dev" and self.env != "dev":
            raise ValueError("AUTH_MODE=dev solo se permite con ENV=dev")
        return self


settings = Settings()
