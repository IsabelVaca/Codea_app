"""revisión de solicitudes de borrado de fotos (RF-10)

Revision ID: c52d8e3f0b21
Revises: b41c7d2e9a10
Create Date: 2026-10-18 15:20:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'c52d8e3f0b21'
down_revision: Union[str, None] = 'b41c7d2e9a10'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # media.status guarda el enum como texto sin CHECK, así que el valor nuevo DELETED no pide cambios.
    op.add_column('media_deletion_request', sa.Column('decided_by', sa.Uuid(), nullable=True))
    op.add_column('media_deletion_request', sa.Column('decided_at', sa.DateTime(timezone=True), nullable=True))
    op.add_column('media_deletion_request', sa.Column('decision_note', sa.Text(), nullable=True))
    op.create_foreign_key(
        op.f('media_deletion_request_decided_by_fkey'), 'media_deletion_request', 'app_user', ['decided_by'], ['id']
    )


def downgrade() -> None:
    op.drop_constraint(op.f('media_deletion_request_decided_by_fkey'), 'media_deletion_request', type_='foreignkey')
    op.drop_column('media_deletion_request', 'decision_note')
    op.drop_column('media_deletion_request', 'decided_at')
    op.drop_column('media_deletion_request', 'decided_by')
