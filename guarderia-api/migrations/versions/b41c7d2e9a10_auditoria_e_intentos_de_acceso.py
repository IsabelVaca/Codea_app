"""bitácora de auditoría e intentos fallidos de acceso

Revision ID: b41c7d2e9a10
Revises: 98306224d8b5
Create Date: 2026-10-18 14:10:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'b41c7d2e9a10'
down_revision: Union[str, None] = '98306224d8b5'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table('audit_log',
    sa.Column('actor_id', sa.Uuid(), nullable=True),
    sa.Column('actor_email', sa.Text(), nullable=True),
    sa.Column('action', sa.String(length=40), nullable=False),
    sa.Column('entity_type', sa.String(length=40), nullable=True),
    sa.Column('entity_id', sa.String(length=64), nullable=True),
    sa.Column('detail', sa.JSON(), nullable=True),
    sa.Column('ip', sa.String(length=64), nullable=True),
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
    sa.ForeignKeyConstraint(['actor_id'], ['app_user.id'], name=op.f('audit_log_actor_id_fkey')),
    sa.PrimaryKeyConstraint('id', name=op.f('audit_log_pkey'))
    )
    op.create_index(op.f('ix_audit_log_action'), 'audit_log', ['action'], unique=False)
    op.create_index(op.f('ix_audit_log_actor_id'), 'audit_log', ['actor_id'], unique=False)

    op.create_table('auth_attempt',
    sa.Column('kind', sa.String(length=20), nullable=False),
    sa.Column('email', sa.Text(), nullable=False),
    sa.Column('ip', sa.String(length=64), nullable=True),
    sa.Column('attempted_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
    sa.PrimaryKeyConstraint('id', name=op.f('auth_attempt_pkey'))
    )
    op.create_index(op.f('ix_auth_attempt_attempted_at'), 'auth_attempt', ['attempted_at'], unique=False)
    op.create_index(op.f('ix_auth_attempt_email'), 'auth_attempt', ['email'], unique=False)
    op.create_index(op.f('ix_auth_attempt_ip'), 'auth_attempt', ['ip'], unique=False)


def downgrade() -> None:
    op.drop_index(op.f('ix_auth_attempt_ip'), table_name='auth_attempt')
    op.drop_index(op.f('ix_auth_attempt_email'), table_name='auth_attempt')
    op.drop_index(op.f('ix_auth_attempt_attempted_at'), table_name='auth_attempt')
    op.drop_table('auth_attempt')
    op.drop_index(op.f('ix_audit_log_actor_id'), table_name='audit_log')
    op.drop_index(op.f('ix_audit_log_action'), table_name='audit_log')
    op.drop_table('audit_log')
