package mx.tec.codea.domain

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// cuentas con coordenadas. es Kotlin puro para poder probarlo sin el teléfono.
object GeoMath {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    // distancia en metros entre dos puntos sobre la esfera (fórmula de haversine).
    // a distancias de una guardería el error contra el elipsoide es de centímetros.
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return (2 * EARTH_RADIUS_METERS * asin(sqrt(a))).roundToInt()
    }
}
