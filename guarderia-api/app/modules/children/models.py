import uuid                                    

from sqlalchemy import ForeignKey              
from sqlalchemy.orm import Mapped, mapped_column 
from sqlalchemy import String, Text

from app.core.db import Base, IdMixin

class Child(IdMixin, Base):
    __tablename__ = "child"  
    classroom_id: Mapped[int] = mapped_column(ForeignKey("classroom.id"))
    name: Mapped[str] = mapped_column(String(100))
    surname: Mapped[str] = mapped_column(String(100))