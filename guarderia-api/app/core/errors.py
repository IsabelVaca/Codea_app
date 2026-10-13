from fastapi import FastAPI, Request
from fastapi.encoders import jsonable_encoder
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

PROBLEM_JSON = "application/problem+json"


class ApiError(Exception):
    """Un error con un code estable que la app puede traducir.

    Ejemplo: raise ApiError(422, "CAPACITY_INVALID", "El cupo debe ser mayor que 0")
    """

    def __init__(self, status: int, code: str, title: str, detail: str | None = None):
        self.status = status
        self.code = code
        self.title = title
        self.detail = detail


def _problem(status: int, code: str, title: str, detail: str | None = None, **extra) -> JSONResponse:
    body = {"type": "about:blank", "title": title, "status": status, "code": code}
    if detail:
        body["detail"] = detail
    body.update(extra)
    return JSONResponse(body, status_code=status, media_type=PROBLEM_JSON)


def install(app: FastAPI) -> None:
    """Hace que todos los errores salgan con el mismo formato """

    @app.exception_handler(ApiError)
    async def api_error(_: Request, e: ApiError) -> JSONResponse:
        return _problem(e.status, e.code, e.title, e.detail)

    @app.exception_handler(RequestValidationError)
    async def validation_error(_: Request, e: RequestValidationError) -> JSONResponse:
        return _problem(422, "VALIDATION_ERROR", "Los datos enviados no son válidos", errors=jsonable_encoder(e.errors()))

    @app.exception_handler(StarletteHTTPException)
    async def http_error(_: Request, e: StarletteHTTPException) -> JSONResponse:
        return _problem(e.status_code, f"HTTP_{e.status_code}", str(e.detail))