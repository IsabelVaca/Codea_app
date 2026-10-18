"""Imprime docs/api/matriz-de-roles.md leyendo las dependencias reales de las rutas.

Uso (desde guarderia-api/): JWT_SECRET=<32+ caracteres> .venv/bin/python scripts/roles_matrix.py > ../docs/api/matriz-de-roles.md
"""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from tests.test_access_matrix import PUBLIC, ROUTES, _roles_required  # noqa: E402

HEADER = """# Matriz de roles

Quién puede llamar a cada endpoint. **Se genera leyendo las dependencias reales de cada ruta** (`require_role`) y se comprueba en [`tests/test_access_matrix.py`](../../guarderia-api/tests/test_access_matrix.py): sin token toda ruta privada responde `401`, y cada rol que no está permitido recibe `403 FORBIDDEN`. Si se agrega un endpoint sin protegerlo, esas pruebas fallan.

- ✅ puede llamarlo · — recibe `403`
- **Sesión** = basta con tener cuenta; el servicio decide por registro (un padre solo ve a sus hijos, una docente solo a los de su sala, y lo que está fuera de alcance responde `404`, no `403`, para no revelar que existe).
- **Pública** = no pide sesión (las de entrada están limitadas por intentos, ver [`auth`](../../guarderia-api/app/modules/auth/README.md)).

| Método | Ruta | Admin | Docente | Padre | Tipo |
|---|---|:-:|:-:|:-:|---|"""


def main() -> None:
    rows = [(p.replace("/api/v1", ""), m, _roles_required(r), (m, p) in PUBLIC) for m, p, r in ROUTES]
    print(HEADER)
    for path, method, roles, public in sorted(rows):
        kind = "Pública" if public else ("Por rol" if roles else "Sesión")

        def cell(role: str) -> str:
            return "✅" if public or not roles or role in roles else "—"

        print(f"| {method} | `{path}` | {cell('ADMIN')} | {cell('TEACHER')} | {cell('PARENT')} | {kind} |")
    private = [r for r in rows if not r[3]]
    print(
        f"\nTotal: {len(rows)} endpoints ({len(rows) - len(private)} públicos, "
        f"{sum(1 for r in private if r[2])} por rol, {sum(1 for r in private if not r[2])} con alcance por registro)."
    )
    print("\n## Cómo se regenera\n")
    print("```bash\ncd guarderia-api")
    print("JWT_SECRET=una-llave-de-al-menos-32-caracteres-aaaa .venv/bin/python scripts/roles_matrix.py > ../docs/api/matriz-de-roles.md\n```")


if __name__ == "__main__":
    main()
