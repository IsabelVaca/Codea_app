package mx.tec.codea.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Path
import retrofit2.http.Query

// una función por endpoint de la API.
interface CodeaApi {

    // ----- salas -----
    @GET("classrooms")
    suspend fun classrooms(): List<ClassroomDto>

    @POST("classrooms")
    suspend fun createClassroom(@Body body: ClassroomCreateBody): ClassroomDto

    @PATCH("classrooms/{id}")
    suspend fun updateClassroom(@Path("id") id: Int, @Body body: ClassroomCapacityBody): ClassroomDto

    @PUT("classrooms/{id}/staff")
    suspend fun setStaff(@Path("id") id: Int, @Body body: StaffBody): ClassroomDto

    @DELETE("classrooms/{id}")
    suspend fun deleteClassroom(@Path("id") id: Int)

    // ----- centro (solo admin) -----
    @GET("center")
    suspend fun center(): CenterDto

    @PATCH("center")
    suspend fun updateCenterSchedule(@Body body: CenterScheduleBody): CenterDto

    // ----- solicitudes de borrado de fotos (solo admin) -----
    @GET("media/deletion-requests")
    suspend fun deletionRequests(@Query("status") status: String = "PENDING"): List<DeletionReviewDto>

    @POST("media/deletion-requests/{id}/approve")
    suspend fun approveDeletion(@Path("id") id: Int, @Body body: DecisionBody = DecisionBody()): DeletionReviewDto

    @POST("media/deletion-requests/{id}/reject")
    suspend fun rejectDeletion(@Path("id") id: Int, @Body body: DecisionBody = DecisionBody()): DeletionReviewDto

    // ----- plantillas de rutina (solo admin) -----
    @GET("routine-templates")
    suspend fun routineTemplates(): List<TemplateDto>

    @POST("routine-templates")
    suspend fun createRoutineTemplate(@Body body: TemplateBody): TemplateDto

    @PUT("routine-templates/{id}")
    suspend fun updateRoutineTemplate(@Path("id") id: Int, @Body body: TemplateBody): TemplateDto

    @PUT("classrooms/{id}/routine-template")
    suspend fun assignRoutineTemplate(@Path("id") classroomId: Int, @Body body: TemplateAssignBody)

    //  docentes
    @GET("teachers")
    suspend fun teachers(): List<TeacherDto>

    @POST("teachers")
    suspend fun createTeacher(@Body body: TeacherCreateBody): TeacherDto

    @PATCH("teachers/{id}")
    suspend fun updateTeacher(@Path("id") id: String, @Body body: TeacherClassroomBody): TeacherDto

    @POST("teachers/{id}/deactivate")
    suspend fun deactivateTeacher(@Path("id") id: String): TeacherDto

    @POST("teachers/{id}/reactivate")
    suspend fun reactivateTeacher(@Path("id") id: String): TeacherDto

    // ----- infantes -----
    @GET("children")
    suspend fun children(): List<ChildDto>

    @POST("children")
    suspend fun createChild(@Body body: ChildCreateBody): ChildDto

    @PATCH("children/{id}")
    suspend fun updateChild(@Path("id") id: Int, @Body body: ChildClassroomBody): ChildDto

    @POST("children/{id}/deactivate")
    suspend fun deactivateChild(@Path("id") id: Int): ChildDto

    @POST("children/{id}/reactivate")
    suspend fun reactivateChild(@Path("id") id: Int): ChildDto

    // ----- padres -----
    @GET("parents")
    suspend fun parents(): List<ParentDto>

    @POST("parents")
    suspend fun createParent(@Body body: ParentCreateBody): ParentDto

    @POST("parents/{id}/deactivate")
    suspend fun deactivateParent(@Path("id") id: String): ParentDto

    @POST("parents/{id}/reactivate")
    suspend fun reactivateParent(@Path("id") id: String): ParentDto

    @POST("parents/{id}/resend-invitation")
    suspend fun resendInvitation(@Path("id") id: String): ParentDto

    // ----- perfil -----
    @GET("me")
    suspend fun me(): MeDto

    // cambia la contraseña y regresa tokens nuevos: la API cierra todas las sesiones, también esta.
    @POST("me/password")
    suspend fun changePassword(@Body body: ChangePasswordBody): TokenDto

    // ----- mi sala -----
    @GET("classrooms/{id}/children")
    suspend fun roomChildren(@Path("id") id: Int): List<RoomChildDto>

    // la ficha del niño: alergias, fecha de nacimiento y tutores con su teléfono.
    @GET("children/{id}")
    suspend fun childDetail(@Path("id") id: Int): ChildDetailDto

    // la familia (o el admin) escribe la salud del niño: alergias, condiciones médicas, intolerancias…
    @PATCH("children/{id}/health")
    suspend fun updateHealth(@Path("id") id: Int, @Body body: HealthBody): ChildDetailDto

    @PUT("attendance/{id}")
    suspend fun setAttendance(@Path("id") id: Int, @Body body: AttendanceBody)

    // ----- rutina y reportes -----
    @GET("classrooms/{id}/routine")
    suspend fun routine(@Path("id") id: Int): RoutineDto

    @POST("routine-moments/{id}/confirm")
    suspend fun confirmMoment(@Path("id") id: Int): RoutineDto

    @POST("reports")
    suspend fun createReport(@Body body: ReportBody)

    // ----- pase de salida de la docente -----
    @GET("classrooms/{id}/departures")
    suspend fun departures(@Path("id") id: Int): DepartureSheetDto

    @POST("children/{id}/departure")
    suspend fun registerDeparture(@Path("id") id: Int, @Body body: DepartureBody): DepartureDto

    @DELETE("children/{id}/departure")
    suspend fun undoDeparture(@Path("id") id: Int)

    // ----- checador -----
    // la API manda null si todavía no checa; por eso llega como JsonElement.
    @GET("staff-attendance/today")
    suspend fun checkInToday(): JsonElement

    @POST("staff-attendance/check-in")
    suspend fun checkIn(@Body body: CheckInBody): CheckInDto

    @POST("staff-attendance/check-out")
    suspend fun checkOut(): CheckInDto

    // ----- chat -----
    @GET("conversations")
    suspend fun conversations(): List<ConversationDto>

    // after = solo los mensajes más nuevos que ese id (null = los últimos).
    @GET("conversations/{id}/messages")
    suspend fun messages(
        @Path("id") id: Int,
        @Query("limit") limit: Int,
        @Query("after") after: Int?,
    ): MessagePage

    @POST("conversations/{id}/messages")
    suspend fun sendMessage(@Path("id") id: Int, @Body body: MessageBody): MessageDto

    @POST("conversations/{id}/read")
    suspend fun markRead(@Path("id") id: Int)

    // ----- padre: hoy, calendario y notas -----
    @GET("children/{id}/day")
    suspend fun childDay(@Path("id") id: Int, @Query("date") date: String?): DayDto

    @GET("children/{id}/attendance")
    suspend fun childAttendance(@Path("id") id: Int, @Query("month") month: String): CalendarDto

    @POST("children/{id}/daily-entries")
    suspend fun saveDailyEntry(@Path("id") id: Int, @Body body: DailyEntryBody): DailyEntryDto

    @GET("children/{id}/weekly-summary")
    suspend fun weeklySummary(@Path("id") id: Int): WeeklySummaryDto

    @POST("children/{id}/notes")
    suspend fun sendNote(@Path("id") id: Int, @Body body: NoteBody)

    // ----- documentos y personas autorizadas (las autorizadas las administra el admin) -----
    @GET("children/{id}/documents")
    suspend fun documents(@Path("id") id: Int): List<DocumentDto>

    @PUT("children/{id}/documents/{documentId}")
    suspend fun attachDocument(
        @Path("id") id: Int,
        @Path("documentId") documentId: Int,
        @Body body: DocumentUploadBody,
    )

    @GET("children/{id}/authorized-pickups")
    suspend fun pickups(@Path("id") id: Int): List<PickupDto>

    @POST("children/{id}/authorized-pickups")
    suspend fun addPickup(@Path("id") id: Int, @Body body: PickupBody)

    @DELETE("children/{id}/authorized-pickups/{personId}")
    suspend fun removePickup(@Path("id") id: Int, @Path("personId") personId: Int)

    // ----- fotos -----
    @GET("media")
    suspend fun photoFeed(@Query("child_id") childId: Int, @Query("limit") limit: Int): FeedPage

    // la familia borra la foto de su hijo de inmediato; 409 MEDIA_SHARED si salen niños de otras familias.
    @DELETE("media/{id}")
    suspend fun deletePhoto(@Path("id") id: Int)

    @POST("media/uploads")
    suspend fun requestUpload(@Body body: UploadBody): UploadDto

    @POST("media/{id}/complete")
    suspend fun completeMedia(@Path("id") id: Int)

    // ----- avisos -----
    @POST("announcements")
    suspend fun publishAnnouncement(@Body body: AnnouncementBody)

    // el historial, el más reciente primero (solo admin).
    @GET("announcements")
    suspend fun announcements(@Query("limit") limit: Int = 50): List<AnnouncementDto>
}
