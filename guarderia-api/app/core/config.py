from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Lo que cambia entre tu máquina y el servidor vive en variables de entorno (.env), no en el código."""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    env: str = "dev"
    database_url: str = "postgresql+asyncpg://guarderia:guarderia@localhost:5432/guarderia"


settings = Settings()