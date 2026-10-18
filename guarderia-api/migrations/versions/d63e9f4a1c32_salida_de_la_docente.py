"""salida de la docente en el checador

Revision ID: d63e9f4a1c32
Revises: c52d8e3f0b21
Create Date: 2026-10-18 16:05:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'd63e9f4a1c32'
down_revision: Union[str, None] = 'c52d8e3f0b21'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column('staff_check_in', sa.Column('checked_out_at', sa.DateTime(timezone=True), nullable=True))
    op.add_column('staff_check_in', sa.Column('out_latitude', sa.Float(), nullable=True))
    op.add_column('staff_check_in', sa.Column('out_longitude', sa.Float(), nullable=True))
    op.add_column('staff_check_in', sa.Column('out_distance_m', sa.Integer(), nullable=True))


def downgrade() -> None:
    op.drop_column('staff_check_in', 'out_distance_m')
    op.drop_column('staff_check_in', 'out_longitude')
    op.drop_column('staff_check_in', 'out_latitude')
    op.drop_column('staff_check_in', 'checked_out_at')
