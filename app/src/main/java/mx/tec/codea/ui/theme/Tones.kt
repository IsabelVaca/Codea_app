package mx.tec.codea.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

// el gris de los textos secundarios: la tinta con algo de transparencia, así se lee igual en claro y en oscuro
// (contraste de al menos 5:1 en claro y 8:1 en oscuro, más que el de "onSurfaceVariant").
val ColorScheme.muted: Color
    get() = onSurface.copy(alpha = 0.68f)
