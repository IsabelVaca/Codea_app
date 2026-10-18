# `payments`: bitácora de pagos por infante (CRF-01)

Tabla `payment`: una línea por cargo o pago de un niño. El dinero va en **centavos** (entero, siempre pesos mexicanos).
Estados: `PENDING` (cargo por cobrar), `PAID` (ya se recibió, con método y fecha) y `CANCELLED` (anulado; no se borra, queda en la bitácora).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /children/{id}/payments?status=` | admin o padre | Lista del niño con `pending_cents`, `overdue_cents` y `paid_cents`. El papá solo ve lo vigente (sin cancelados) y solo de sus hijos |
| `POST /children/{id}/payments` | admin | Registra un cargo (`PENDING`) o un pago ya recibido (`PAID`, con `method`) |
| `GET /payments?status=&overdue=&cursor=&limit=` | admin | Todos los pagos del centro, con el nombre del niño; `overdue=true` deja los pendientes con fecha límite pasada |
| `PATCH /payments/{id}` | admin | Corrige concepto, monto, fecha límite o notas de un cargo **pendiente** |
| `POST /payments/{id}/pay` | admin | Marca un cargo pendiente como pagado (`method` obligatorio, `paid_on` es hoy si falta) |
| `POST /payments/{id}/cancel` | admin | Cancela un cargo o un pago; el motivo se agrega a las notas |

Reglas (`rules.py`): concepto obligatorio (hasta 120), monto entre 1 centavo y 1 000 000 de pesos, un pago nuevo es `PENDING` o `PAID`, un pago ya hecho no se edita (se cancela y se registra otro), `is_overdue` se calcula con la fecha del centro. La docente no tiene acceso (403).

## Estado

| Hecho | Pendiente |
|---|---|
| Modelo, migración `c3a9e5f1d2b8`, 6 endpoints, 5 pruebas (`tests/test_payments.py`) | Pantalla del admin en la app y vista de solo lectura del padre |
| | Recibos o comprobantes (adjuntar imagen o PDF con `media`) |
| | Bitácora de auditoría de quién cambió qué (hoy solo `recorded_by` del último cambio) |
| | Pago en línea: fuera de alcance (la bitácora solo registra) |
