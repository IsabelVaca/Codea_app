import uuid                                    

from sqlalchemy import ForeignKey              
from sqlalchemy.orm import Mapped, mapped_column 

from app.core.db import Base, TimestampMixin  

class Teacher(TimestampMixin, Base):
    __tablename__ = "teacher"  
    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"), primary_key=True)
    classroom_id: Mapped[int] = mapped_column(ForeignKey("classroom.id"))
    
