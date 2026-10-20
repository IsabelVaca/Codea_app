package mx.tec.codea.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest

// las fotos y las imágenes de los avisos llegan con una URL firmada que cambia en cada carga (vence en 10 minutos).
// Coil guarda las imágenes por su URL, así que cada lista nueva las volvía a bajar. Con la URL sin la firma como llave,
// la imagen que ya se vio sale de la memoria o del disco del teléfono y no se baja otra vez.
@Composable
fun imagenEstable(model: Any): Any {
    val url = model as? String ?: return model
    if (!url.startsWith("http")) return model
    val context = LocalPlatformContext.current
    return remember(url) {
        val llave = url.substringBefore('?')
        ImageRequest.Builder(context)
            .data(url)
            .memoryCacheKey(llave)
            .diskCacheKey(llave)
            .build()
    }
}
