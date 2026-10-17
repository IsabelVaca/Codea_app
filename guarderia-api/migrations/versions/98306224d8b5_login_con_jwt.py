"""login con JWT: contraseñas, refresh tokens y códigos de acceso

Revision ID: 98306224d8b5
Revises: caf6344f99d5
Create Date: 2026-10-17 22:39:32.488883

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = '98306224d8b5'
down_revision: Union[str, None] = 'caf6344f99d5'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # generado con `alembic revision --autogenerate` y ajustado a mano donde se indica.
    op.create_table('refresh_token',
    sa.Column('user_id', sa.Uuid(), nullable=False),
    sa.Column('token_hash', sa.Text(), nullable=False),
    sa.Column('family_id', sa.Uuid(), nullable=False),
    sa.Column('expires_at', sa.DateTime(timezone=True), nullable=False),
    sa.Column('revoked_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('replaced_by_id', sa.Integer(), nullable=True),
    sa.Column('last_used_at', sa.DateTime(timezone=True), nullable=True),
    sa.Column('user_agent', sa.Text(), nullable=True),
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
    sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
    sa.ForeignKeyConstraint(['replaced_by_id'], ['refresh_token.id'], name=op.f('refresh_token_replaced_by_id_fkey')),
    sa.ForeignKeyConstraint(['user_id'], ['app_user.id'], name=op.f('refresh_token_user_id_fkey')),
    sa.PrimaryKeyConstraint('id', name=op.f('refresh_token_pkey')),
    sa.UniqueConstraint('token_hash', name=op.f('refresh_token_token_hash_key'))
    )
    op.create_index(op.f('ix_refresh_token_family_id'), 'refresh_token', ['family_id'], unique=False)
    op.create_index(op.f('ix_refresh_token_user_id'), 'refresh_token', ['user_id'], unique=False)
    op.add_column('app_user', sa.Column('password_hash', sa.Text(), nullable=True))
    op.add_column('app_user', sa.Column('password_changed_at', sa.DateTime(timezone=True), nullable=True))
    op.add_column('app_user', sa.Column('last_login_at', sa.DateTime(timezone=True), nullable=True))
    op.add_column('app_user', sa.Column('token_version', sa.Integer(), server_default=sa.text('0'), nullable=False))
    op.drop_constraint('app_user_firebase_uid_key', 'app_user', type_='unique')
    op.drop_column('app_user', 'firebase_uid')
    # las invitaciones que ya existen no tienen código que se pueda usar (antes se aceptaban con Google):
    # se rellenan con un hash que nadie conoce y se dan por vencidas; el administrador genera códigos nuevos.
    op.add_column('invitation', sa.Column('code_hash', sa.Text(), nullable=True))
    op.execute("UPDATE invitation SET code_hash = md5(random()::text || id::text) || md5(id::text || random()::text)")
    op.execute("UPDATE invitation SET expires_at = now() WHERE accepted_at IS NULL AND expires_at > now()")
    op.alter_column('invitation', 'code_hash', nullable=False)
    op.add_column('invitation', sa.Column('purpose', sa.Enum('INVITATION', 'PASSWORD_RESET', name='codepurpose', native_enum=False, length=20), server_default='INVITATION', nullable=False))


def downgrade() -> None:
    # volver atrás deja a las personas sin contraseña ni firebase_uid: es solo para desarrollo.
    # generado con `alembic revision --autogenerate` y ajustado a mano donde se indica.
    op.drop_column('invitation', 'purpose')
    op.drop_column('invitation', 'code_hash')
    op.add_column('app_user', sa.Column('firebase_uid', sa.TEXT(), autoincrement=False, nullable=True))
    op.create_unique_constraint('app_user_firebase_uid_key', 'app_user', ['firebase_uid'])
    op.drop_column('app_user', 'token_version')
    op.drop_column('app_user', 'last_login_at')
    op.drop_column('app_user', 'password_changed_at')
    op.drop_column('app_user', 'password_hash')
    op.drop_index(op.f('ix_refresh_token_user_id'), table_name='refresh_token')
    op.drop_index(op.f('ix_refresh_token_family_id'), table_name='refresh_token')
    op.drop_table('refresh_token')
    # ### end Alembic commands ###