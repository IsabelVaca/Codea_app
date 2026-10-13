# Alembic importa este archivo para conocer las tablas de los módulos
# Si agregas un modulo nuevo, agrega aquí su línea
from app.modules.auth import models as _auth  # noqa: F401
from app.modules.admins import models as _admins  # noqa: F401
from app.modules.parents import models as _parents  # noqa: F401
from app.modules.teachers import models as _teachers  # noqa: F401
from app.modules.classrooms import models as _classrooms  # noqa: F401
from app.modules.children import models as _children  # noqa: F401
from app.modules.staff_attendance import models as _staff_attendance  # noqa: F401
from app.modules.attendance import models as _attendance  # noqa: F401
from app.modules.routines import models as _routines  # noqa: F401
from app.modules.reports import models as _reports  # noqa: F401
from app.modules.media import models as _media  # noqa: F401
from app.modules.documents import models as _documents  # noqa: F401
from app.modules.messaging import models as _messaging  # noqa: F401