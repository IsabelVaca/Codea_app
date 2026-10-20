"""menú del día

Revision ID: d74a0f6c4e15
Revises: c63f9e5b3d04
Create Date: 2026-10-19 14:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'd74a0f6c4e15'
down_revision: Union[str, None] = 'c63f9e5b3d04'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        'daily_menu',
        sa.Column('daycare_center_id', sa.Integer(), nullable=False),
        sa.Column('date', sa.Date(), nullable=False),
        sa.Column('breakfast', sa.Text(), nullable=True),
        sa.Column('lunch', sa.Text(), nullable=True),
        sa.Column('snack', sa.Text(), nullable=True),
        sa.Column('updated_by', sa.Uuid(), nullable=False),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
        sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
        sa.Column('id', sa.Integer(), nullable=False),
        sa.ForeignKeyConstraint(['daycare_center_id'], ['daycare_center.id'], name='daily_menu_daycare_center_id_fkey'),
        sa.ForeignKeyConstraint(['updated_by'], ['app_user.id'], name='daily_menu_updated_by_fkey'),
        sa.PrimaryKeyConstraint('id', name='daily_menu_pkey'),
        sa.UniqueConstraint('daycare_center_id', 'date', name='daily_menu_daycare_center_id_date_key'),
    )


def downgrade() -> None:
    op.drop_table('daily_menu')
