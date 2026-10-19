package mx.tec.codea.data

import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.CalendarDto
import mx.tec.codea.data.api.DayDto
import mx.tec.codea.data.api.DeletionBody
import mx.tec.codea.data.api.FeedItemDto
import mx.tec.codea.data.api.WeeklySummaryDto
import mx.tec.codea.data.api.MeDto
import mx.tec.codea.data.api.NoteBody
import mx.tec.codea.data.api.ChildDetailDto
import mx.tec.codea.data.api.HealthBody
import mx.tec.codea.domain.ChildHealth
import mx.tec.codea.domain.ChildHealthRules

// todo lo que el padre pide o manda a la API.
class ParentApiRepository {

    private val api get() = ApiClient.api

    suspend fun me(): MeDto = api.me()

    suspend fun day(childId: Int, date: String?): DayDto = api.childDay(childId, date)

    suspend fun calendar(childId: Int, month: String): CalendarDto = api.childAttendance(childId, month)

    suspend fun weekly(childId: Int): WeeklySummaryDto = api.weeklySummary(childId)

    suspend fun sendNote(childId: Int, text: String) {
        api.sendNote(childId, NoteBody(text.trim()))
    }

    // los datos del niño y lo que la familia ha escrito de su salud.
    suspend fun child(childId: Int): ChildDetailDto = api.childDetail(childId)

    suspend fun saveHealth(childId: Int, health: ChildHealth): ChildDetailDto {
        val h = ChildHealthRules.clean(health)
        return api.updateHealth(childId, HealthBody(h.allergies, h.medicalConditions, h.intolerances, h.medications, h.otherNotes))
    }

    suspend fun photos(childId: Int): List<FeedItemDto> = api.photoFeed(childId, 50).items

    suspend fun requestPhotoDeletion(mediaId: Int) {
        api.requestPhotoDeletion(mediaId, DeletionBody())
    }
}
