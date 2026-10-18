"""checador sin ubicación y con cierre automático

Revision ID: e74f0a5b3d43
Revises: d63e9f4a1c32
Create Date: 2026-10-18 21:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'e74f0a5b3d43'
down_revision: Union[str, None] = 'd63e9f4a1c32'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # el checador ya no usa ubicación: se borran las coordenadas y distancias guardadas.
    op.drop_column('staff_check_in', 'latitude')
    op.drop_column('staff_check_in', 'longitude')
    op.drop_column('staff_check_in', 'distance_m')
    op.drop_column('staff_check_in', 'out_latitude')
    op.drop_column('staff_check_in', 'out_longitude')
    op.drop_column('staff_check_in', 'out_distance_m')
    op.add_column('staff_check_in', sa.Column('auto_closed', sa.Boolean(), server_default='false', nullable=False))


def downgrade() -> None:
    op.drop_column('staff_check_in', 'auto_closed')
    # las coordenadas borradas no se pueden recuperar: vuelven en cero para poder volver a marcar las columnas NOT NULL.
    op.add_column('staff_check_in', sa.Column('out_distance_m', sa.Integer(), nullable=True))
    op.add_column('staff_check_in', sa.Column('out_longitude', sa.Float(), nullable=True))
    op.add_column('staff_check_in', sa.Column('out_latitude', sa.Float(), nullable=True))
    op.add_column('staff_check_in', sa.Column('distance_m', sa.Integer(), server_default='0', nullable=False))
    op.add_column('staff_check_in', sa.Column('longitude', sa.Float(), server_default='0', nullable=False))
    op.add_column('staff_check_in', sa.Column('latitude', sa.Float(), server_default='0', nullable=False))
