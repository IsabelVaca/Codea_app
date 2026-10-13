import uuid                                    

from sqlalchemy import ForeignKey              
from sqlalchemy.orm import Mapped, mapped_column 

from app.core.db import Base, IdMixin, TimestampMixin


class Parent(TimestampMixin, Base):
    __tablename__ = "parent"  
    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"), primary_key=True)
    
class ParentChildRel(IdMixin, Base):

    __tablename__ = "parent_child_rel"

    parent_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("parent.user_id"))
    child_id: Mapped[int] = mapped_column(ForeignKey("child.id"))