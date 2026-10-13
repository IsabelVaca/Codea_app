from typing import Generic, TypeVar

from pydantic import BaseModel

T = TypeVar("T")


class Page(BaseModel, Generic[T]):
    """Una página de resultados. Para pedir la siguiente, manda next_cursor como ?cursor=."""

    items: list[T]
    next_cursor: str | None = None


def parse_cursor(cursor: str | None) -> int | None:
    """El cursor es el último id de la página anterior; si viene mal, se ignora."""
    if cursor is None or not cursor.isdigit():
        return None
    return int(cursor)
