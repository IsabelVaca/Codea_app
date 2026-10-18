import uuid
from datetime import datetime

from pydantic import BaseModel


class AuditOut(BaseModel):
    id: int
    created_at: datetime
    action: str
    actor_id: uuid.UUID | None
    actor_name: str | None
    actor_email: str | None
    entity_type: str | None
    entity_id: str | None
    detail: dict | None
    ip: str | None
