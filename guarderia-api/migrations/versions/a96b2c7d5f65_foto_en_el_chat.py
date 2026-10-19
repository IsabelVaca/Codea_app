"""foto en el chat: el mensaje puede apuntar a un archivo de media

Revision ID: a96b2c7d5f65
Revises: f85a1b6c4e54
Create Date: 2026-10-19 10:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'a96b2c7d5f65'
down_revision: Union[str, None] = 'f85a1b6c4e54'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column('message', sa.Column('media_id', sa.Integer(), nullable=True))
    op.create_foreign_key('fk_message_media_id_media', 'message', 'media', ['media_id'], ['id'])


def downgrade() -> None:
    op.drop_constraint('fk_message_media_id_media', 'message', type_='foreignkey')
    op.drop_column('message', 'media_id')
