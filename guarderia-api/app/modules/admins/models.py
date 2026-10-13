import uuid                                    

from sqlalchemy import ForeignKey              
from sqlalchemy.orm import Mapped, mapped_column 

from app.core.db import Base, TimestampMixin  

class Admin(TimestampMixin, Base):
    __tablename__ = "admin"  
    user_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("app_user.id"), primary_key=True)
    daycare_center_id: Mapped[int] = mapped_column(ForeignKey("daycare_center.id"))
