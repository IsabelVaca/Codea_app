package mx.tec.codea.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.util.Consumer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class GeoPoint(val latitude: Double, val longitude: Double)

// la ubicación del teléfono con las APIs de Android (LocationManager), sin Play Services:
// el marco del curso no incluye esa librería. el checador solo la pide al registrar la entrada.
class DeviceLocation(private val context: Context) {

    fun hasPermission(): Boolean =
        isGranted(Manifest.permission.ACCESS_FINE_LOCATION) || isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

    // la posición de ahora, o null si no hay permiso, el GPS está apagado o no hubo señal a tiempo.
    @SuppressLint("MissingPermission") // hasPermission() ya lo revisó
    suspend fun current(): GeoPoint? {
        if (!hasPermission()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        // primero el GPS (más exacto) y luego la red. cada uno tiene su tiempo para contestar.
        for (provider in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
            if (!isEnabled(manager, provider)) continue
            val location = withTimeoutOrNull(PROVIDER_TIMEOUT_MS) { fresh(manager, provider) }
            if (location != null) return location.toPoint()
        }
        // sin posición nueva, sirve la última que el sistema guardó si es de hace poco.
        val last = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { isEnabled(manager, it) }
            .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .filter { System.currentTimeMillis() - it.time <= MAX_LAST_AGE_MS }
            .maxByOrNull { it.time }
        return last?.toPoint()
    }

    @SuppressLint("MissingPermission")
    private suspend fun fresh(manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            LocationManagerCompat.getCurrentLocation(
                manager,
                provider,
                signal,
                ContextCompat.getMainExecutor(context),
                Consumer { location -> if (continuation.isActive) continuation.resume(location) },
            )
        }

    private fun isEnabled(manager: LocationManager, provider: String): Boolean =
        runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun Location.toPoint() = GeoPoint(latitude, longitude)

    private companion object {
        const val PROVIDER_TIMEOUT_MS = 8_000L
        const val MAX_LAST_AGE_MS = 2 * 60 * 1000L
    }
}
