package mx.tec.codea.data

import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.CalendarDto
import mx.tec.codea.data.api.DayDto
import mx.tec.codea.data.api.DeletionBody
import mx.tec.codea.data.api.DocumentDto
import mx.tec.codea.data.api.DocumentUploadBody
import mx.tec.codea.data.api.FeedItemDto
import mx.tec.codea.data.api.WeeklySummaryDto
import mx.tec.codea.data.api.MeDto
import mx.tec.codea.data.api.NoteBody
import mx.tec.codea.data.api.PickupBody
import mx.tec.codea.data.api.PickupDto

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

    suspend fun documents(childId: Int): List<DocumentDto> = api.documents(childId)

    suspend fun uploadDocument(childId: Int, documentId: Int, bytes: ByteArray, contentType: String) {
        val mediaId = MediaUploader.upload(bytes, contentType, "DOCUMENT")
        api.attachDocument(childId, documentId, DocumentUploadBody(mediaId))
    }

    suspend fun pickups(childId: Int): List<PickupDto> = api.pickups(childId)

    suspend fun addPickup(childId: Int, name: String) {
        api.addPickup(childId, PickupBody(name.trim()))
    }

    suspend fun removePickup(childId: Int, personId: Int) {
        api.removePickup(childId, personId)
    }

    suspend fun photos(childId: Int): List<FeedItemDto> = api.photoFeed(childId, 50).items

    suspend fun requestPhotoDeletion(mediaId: Int) {
        api.requestPhotoDeletion(mediaId, DeletionBody())
    }
}
