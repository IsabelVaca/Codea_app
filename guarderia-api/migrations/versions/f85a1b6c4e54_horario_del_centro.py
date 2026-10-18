"""horario del centro: entrada, salida, cierre automático y días de trabajo

Revision ID: f85a1b6c4e54
Revises: e74f0a5b3d43
Create Date: 2026-10-18 22:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'f85a1b6c4e54'
down_revision: Union[str, None] = 'e74f0a5b3d43'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # con valores por omisión (8:00 a 16:00, cierre 21:00, lunes a viernes) nada cambia hasta que el admin lo toque.
    op.add_column('daycare_center', sa.Column('entry_time', sa.Time(), server_default='08:00', nullable=False))
    op.add_column('daycare_center', sa.Column('exit_time', sa.Time(), server_default='16:00', nullable=False))
    op.add_column('daycare_center', sa.Column('auto_close_time', sa.Time(), server_default='21:00', nullable=False))
    op.add_column(
        'daycare_center',
        sa.Column('work_days', sa.ARRAY(sa.Integer()), server_default=sa.text("'{1,2,3,4,5}'"), nullable=False),
    )


def downgrade() -> None:
    op.drop_column('daycare_center', 'work_days')
    op.drop_column('daycare_center', 'auto_close_time')
    op.drop_column('daycare_center', 'exit_time')
    op.drop_column('daycare_center', 'entry_time')
