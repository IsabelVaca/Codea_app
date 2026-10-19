"""salud del niño (Mi hijo) y pase de salida

Revision ID: a07c3d8e6f76
Revises: f85a1b6c4e54
Create Date: 2026-10-19 10:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'a07c3d8e6f76'
down_revision: Union[str, None] = 'f85a1b6c4e54'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # lo que la familia escribe en "Mi hijo": todo opcional, así que los niños de ahora quedan igual.
    for column in ('medical_conditions', 'intolerances', 'medications', 'other_notes'):
        op.add_column('child', sa.Column(column, sa.Text(), nullable=True))

    # el pase de salida: una fila por niño y por día.
    op.create_table(
        'child_departure',
        sa.Column('child_id', sa.Integer(), nullable=False),
        sa.Column('date', sa.Date(), nullable=False),
        sa.Column('departed_at', sa.DateTime(timezone=True), nullable=False),
        sa.Column('pickup_id', sa.Integer(), nullable=True),
        sa.Column('guardian_id', sa.Uuid(), nullable=True),
        sa.Column('picked_up_by', sa.String(length=200), nullable=False),
        sa.Column('recorded_by', sa.Uuid(), nullable=False),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
        sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
        sa.Column('id', sa.Integer(), nullable=False),
        sa.ForeignKeyConstraint(['child_id'], ['child.id'], name='child_departure_child_id_fkey'),
        sa.ForeignKeyConstraint(
            ['pickup_id'], ['authorized_pickup.id'], name='child_departure_pickup_id_fkey', ondelete='SET NULL'
        ),
        sa.ForeignKeyConstraint(
            ['guardian_id'], ['app_user.id'], name='child_departure_guardian_id_fkey', ondelete='SET NULL'
        ),
        sa.ForeignKeyConstraint(['recorded_by'], ['app_user.id'], name='child_departure_recorded_by_fkey'),
        sa.PrimaryKeyConstraint('id', name='child_departure_pkey'),
        sa.UniqueConstraint('child_id', 'date', name='child_departure_child_id_date_key'),
    )


def downgrade() -> None:
    op.drop_table('child_departure')
    for column in ('other_notes', 'medications', 'intolerances', 'medical_conditions'):
        op.drop_column('child', column)
