package org.bkkz.lumaapp.data.remote

import org.bkkz.lumaapp.data.entity.auth.EmailRegistrationRequest
import org.bkkz.lumaapp.data.entity.auth.EmailRegistrationResponse
import org.bkkz.lumaapp.data.entity.auth.EmailSignInRequest
import org.bkkz.lumaapp.data.entity.auth.EmailSignInResponse
import org.bkkz.lumaapp.data.entity.auth.GoogleSignInRequest
import org.bkkz.lumaapp.data.entity.auth.LogoutRequest
import org.bkkz.lumaapp.data.entity.auth.ResetPasswordRequest
import org.bkkz.lumaapp.data.entity.auth.TokenRequest
import org.bkkz.lumaapp.data.entity.auth.TokenResponse
import org.bkkz.lumaapp.data.entity.chat.LLMChatRequest
import org.bkkz.lumaapp.data.entity.chat.LLMProcess
import org.bkkz.lumaapp.data.entity.chat_history.ChatHistory
import org.bkkz.lumaapp.data.entity.google_calendar.CalendarEventRequest
import org.bkkz.lumaapp.data.entity.google_calendar.GoogleAuthRequest
import org.bkkz.lumaapp.data.entity.report_history.ReportHistory
import org.bkkz.lumaapp.data.entity.task.CreateTaskRequest
import org.bkkz.lumaapp.data.entity.task.EditTaskRequest
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.data.entity.user.UserInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LumaApi {

    /*=========== AUTHENTICATION API ===========*/
    @POST("auth/login-email")
    suspend fun loginWithEmail(
        @Body emailSignInRequest: EmailSignInRequest
    ) : EmailSignInResponse

    @POST("auth/login-google")
    suspend fun loginWithGoogle(
        @Body googleSignInRequest: GoogleSignInRequest
    ) : TokenResponse

    @POST("auth/token")
    suspend fun tokenRequest(
        @Body tokenRequest: TokenRequest
    ) : Response<TokenResponse>

    @POST("auth/register")
    suspend fun registerWithEmail(
        @Body emailRegistrationRequest: EmailRegistrationRequest
    ) : EmailRegistrationResponse

    @POST("auth/logout")
    suspend fun logout(
        @Body logoutRequest: LogoutRequest
    ) : Response<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body resetPasswordRequest: ResetPasswordRequest
    ) : Response<Unit>

    /*=========== TASK API ===========*/
    @GET("task/my-tasks")
    suspend fun getUserTasks(
        @Query("date") date : String
    ) : Response<ApiResponse<Task>>

    @POST("task/")
    suspend fun createTask(
        @Body createTaskRequest: CreateTaskRequest
    ) : Response<ApiResponse<Any>>

    @DELETE("task/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId : String
    ) : Response<ApiResponse<Any>>

    @PATCH("task/{taskId}")
    suspend fun editTask(
        @Path("taskId") taskId : String,
        @Body editTaskRequest: EditTaskRequest
    ) : Response<ApiResponse<Any>>

    /*=========== CHAT LOG API ===========*/
    @GET("llm/history")
    suspend fun getChatLogs(
        @Query("intent") intent : String?,
        @Query("date") date : String?,
        @Query("keyword", encoded = true) keyword : String?
    ) : Response<ApiResponse<ChatHistory>>

    @POST("llm/")
    suspend fun chatWithLuma(
        @Body llmChatRequest: LLMChatRequest
    ) : Response<ApiResponse<LLMProcess>>

    /*=========== FORM API ===========*/
    @POST("form/generate-monthly-task-report")
    suspend fun generateMISReport(
        @Query(value = "reportYearMonth") reportYearMonth : String
    ) : Response<Void>

    @GET("form/forms")
    suspend fun getReports(
        @Query(value = "formType") formType : String
    ) : Response<ApiResponse<ReportHistory>>

    @DELETE("form/delete-form")
    suspend fun deleteReport(
        @Query(value = "formType") formType : String,
        @Query(value = "fileName") fileName : String
    ) : Response<ApiResponse<Any>>

    /*=========== GOOGLE CALENDAR API ===========*/
    @POST("google-calendar/auth")
    suspend fun authenticateGoogleCalendar(
        @Body googleAuthRequest: GoogleAuthRequest
    ): Response<Void>

    @GET("google-calendar/sync")
    suspend fun syncGoogleCalendar(): Response<Void>

    @GET("google-calendar/connection")
    suspend fun getCalendarConnectionStatus(): Response<ApiResponse<Any>>

    @DELETE("google-calendar/connection")
    suspend fun revokeGoogleCalendarAccess(): Response<ApiResponse<Any>>

    @POST("google-calendar/event")
    suspend fun createGoogleCalendarEvent(
        @Body calendarEventRequest: CalendarEventRequest
    ): Response<ApiResponse<Any>>

    @PATCH("google-calendar/event/{eventId}")
    suspend fun updateGoogleCalendarEvent(
        @Path("eventId") eventId : String,
        @Body calendarEventRequest: CalendarEventRequest
    ): Response<ApiResponse<Any>>

    @DELETE("google-calendar/event/{eventId}")
    suspend fun deleteGoogleCalendarEvent(
        @Path("eventId") eventId : String
    ): Response<ApiResponse<Any>>

    /*=========== USER ===========*/
    @GET("user/")
    suspend fun getUserInfo(): Response<ApiResponse<UserInfo>>
}