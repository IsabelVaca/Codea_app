from datetime import date

from fastapi import APIRouter, status

from app.core.db import Db
from app.core.security import StaffUser, TeacherUser

from . import service
from .schemas import DepartureIn, DepartureOut, DepartureSheet

router = APIRouter(tags=["pase de salida"])


@router.get("/classrooms/{classroom_id}/departures", response_model=DepartureSheet)
async def departure_sheet(db: Db, current: StaffUser, classroom_id: int, day: date | None = None) -> DepartureSheet:
    """El pase de salida: los niños presentes hoy, con las personas que pueden recogerlos (las autorizadas que
    registró el admin y los tutores) y la salida de los que ya se fueron. all_done = true cuando ya salieron todos."""
    return await service.sheet(db, current, classroom_id, day)


@router.post("/children/{child_id}/departure", response_model=DepartureOut, status_code=status.HTTP_201_CREATED)
async def register_departure(db: Db, current: TeacherUser, child_id: int, data: DepartureIn) -> DepartureOut:
    """Registra la hora de salida del niño y con quién se fue (pickup_id o guardian_id, solo uno).
    409 CHILD_NOT_PRESENT o ALREADY_DEPARTED; 404 PICKUP_NOT_FOUND; 422 PICKUP_REQUIRED."""
    return await service.register(db, current, child_id, data)


@router.delete("/children/{child_id}/departure", status_code=status.HTTP_204_NO_CONTENT)
async def undo_departure(db: Db, current: TeacherUser, child_id: int) -> None:
    """Quita la salida de hoy de un niño, por si se eligió a la persona equivocada."""
    await service.undo(db, current, child_id)
