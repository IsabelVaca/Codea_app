"""hora de entrada del niño

Revision ID: b18e4d7a2c93
Revises: a07c3d8e6f76
Create Date: 2026-10-19 12:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'b18e4d7a2c93'
down_revision: Union[str, None] = 'a07c3d8e6f76'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # opcional: las marcas de antes quedan sin hora de entrada.
    op.add_column('attendance', sa.Column('arrived_at', sa.DateTime(timezone=True), nullable=True))


def downgrade() -> None:
    op.drop_column('attendance', 'arrived_at')
