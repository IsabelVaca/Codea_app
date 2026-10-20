package mx.tec.codea.data

import kotlinx.serialization.builtins.ListSerializer
import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.offline.cachedResponse
import mx.tec.codea.data.offline.fetchAndCache
import mx.tec.codea.data.api.CalendarDto
import mx.tec.codea.data.api.DayDto
import mx.tec.codea.data.api.FeedItemDto
import mx.tec.codea.data.api.WeeklySummaryDto
import mx.tec.codea.data.api.MeDto
import mx.tec.codea.data.api.NoteBody
import mx.tec.codea.data.api.ChildDetailDto
import mx.tec.codea.data.api.HealthBody
import mx.tec.codea.domain.ChildHealth
import mx.tec.codea.domain.ChildHealthRules

// todo lo que el padre pide o manda a la API. Lo que se lee se guarda en la caché (ApiCache) con una llave por hijo,
// y cada lectura tiene su "…Guardado()" con lo último que se vio, para mostrarlo al instante.
class ParentApiRepository {

    private val api get() = ApiClient.api

    suspend fun me(): MeDto = fetchAndCache("me", MeDto.serializer()) { api.me() }
    suspend fun meGuardado(): MeDto? = cachedResponse("me", MeDto.serializer())

    // la llave del día lleva la fecha (hoy, si no se eligió otra): el día de ayer nunca se muestra como si fuera hoy.
    private fun llaveDia(childId: Int, date: String?) = "day:$childId:${date ?: hoyIso()}"

    suspend fun day(childId: Int, date: String?): DayDto =
        fetchAndCache(llaveDia(childId, date), DayDto.serializer()) { api.childDay(childId, date) }
    suspend fun dayGuardado(childId: Int, date: String?): DayDto? = cachedResponse(llaveDia(childId, date), DayDto.serializer())

    suspend fun calendar(childId: Int, month: String): CalendarDto =
        fetchAndCache("calendar:$childId:$month", CalendarDto.serializer()) { api.childAttendance(childId, month) }
    suspend fun calendarGuardado(childId: Int, month: String): CalendarDto? =
        cachedResponse("calendar:$childId:$month", CalendarDto.serializer())

    suspend fun weekly(childId: Int): WeeklySummaryDto =
        fetchAndCache("weekly:$childId", WeeklySummaryDto.serializer()) { api.weeklySummary(childId) }
    suspend fun weeklyGuardado(childId: Int): WeeklySummaryDto? = cachedResponse("weekly:$childId", WeeklySummaryDto.serializer())

    suspend fun sendNote(childId: Int, text: String) {
        api.sendNote(childId, NoteBody(text.trim()))
    }

    // los datos del niño y lo que la familia ha escrito de su salud.
    suspend fun child(childId: Int): ChildDetailDto =
        fetchAndCache("child:$childId", ChildDetailDto.serializer()) { api.childDetail(childId) }
    suspend fun childGuardado(childId: Int): ChildDetailDto? = cachedResponse("child:$childId", ChildDetailDto.serializer())

    // lo que regresa la API después de guardar también se guarda: así la caché no se queda con la salud vieja.
    suspend fun saveHealth(childId: Int, health: ChildHealth): ChildDetailDto {
        val h = ChildHealthRules.clean(health)
        return fetchAndCache("child:$childId", ChildDetailDto.serializer()) {
            api.updateHealth(childId, HealthBody(h.allergies, h.medicalConditions, h.intolerances, h.medications, h.otherNotes))
        }
    }

    // las fotos traen URLs firmadas que vencen en 10 minutos: las guardadas sirven para pintar la cuadrícula al instante
    // (Coil las saca de su propia caché por la llave sin firma, ver imagenEstable) y la lista nueva las reemplaza.
    suspend fun photos(childId: Int): List<FeedItemDto> =
        fetchAndCache("photos:$childId", FOTOS) { api.photoFeed(childId, 50).items }
    suspend fun photosGuardadas(childId: Int): List<FeedItemDto>? = cachedResponse("photos:$childId", FOTOS)

    suspend fun deletePhoto(mediaId: Int) {
        api.deletePhoto(mediaId)
    }

    private companion object {
        val FOTOS = ListSerializer(FeedItemDto.serializer())
    }
}
