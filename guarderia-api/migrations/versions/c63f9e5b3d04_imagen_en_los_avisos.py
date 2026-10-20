"""imagen en los avisos

Revision ID: c63f9e5b3d04
Revises: b18e4d7a2c93
Create Date: 2026-10-19 13:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'c63f9e5b3d04'
down_revision: Union[str, None] = 'b18e4d7a2c93'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # opcional: los avisos de antes quedan sin imagen.
    op.add_column('announcement', sa.Column('media_id', sa.Integer(), nullable=True))
    op.create_foreign_key('fk_announcement_media_id_media', 'announcement', 'media', ['media_id'], ['id'])


def downgrade() -> None:
    op.drop_constraint('fk_announcement_media_id_media', 'announcement', type_='foreignkey')
    op.drop_column('announcement', 'media_id')
