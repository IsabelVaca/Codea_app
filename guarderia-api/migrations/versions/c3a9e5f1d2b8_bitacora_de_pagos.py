"""bitácora de pagos por infante (CRF-01)

Revision ID: c3a9e5f1d2b8
Revises: 98306224d8b5
Create Date: 2026-10-18 11:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'c3a9e5f1d2b8'
down_revision: Union[str, None] = '98306224d8b5'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table('payment',
    sa.Column('child_id', sa.Integer(), nullable=False),
    sa.Column('concept', sa.String(length=120), nullable=False),
    sa.Column('amount_cents', sa.Integer(), nullable=False),
    sa.Column('status', sa.Enum('PENDING', 'PAID', 'CANCELLED', name='paymentstatus', native_enum=False, length=20), server_default='PENDING', nullable=False),
    sa.Column('due_date', sa.Date(), nullable=True),
    sa.Column('paid_on', sa.Date(), nullable=True),
    sa.Column('method', sa.Enum('CASH', 'TRANSFER', 'CARD', 'OTHER', name='paymentmethod', native_enum=False, length=20), nullable=True),
    sa.Column('reference', sa.String(length=120), nullable=True),
    sa.Column('notes', sa.Text(), nullable=True),
    sa.Column('recorded_by', sa.Uuid(), nullable=False),
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
    sa.CheckConstraint('amount_cents > 0', name=op.f('payment_amount_positive_check')),
    sa.ForeignKeyConstraint(['child_id'], ['child.id'], name=op.f('payment_child_id_fkey')),
    sa.ForeignKeyConstraint(['recorded_by'], ['app_user.id'], name=op.f('payment_recorded_by_fkey')),
    sa.PrimaryKeyConstraint('id', name=op.f('payment_pkey'))
    )
    op.create_index(op.f('ix_payment_child_id'), 'payment', ['child_id'], unique=False)


def downgrade() -> None:
    op.drop_index(op.f('ix_payment_child_id'), table_name='payment')
    op.drop_table('payment')
