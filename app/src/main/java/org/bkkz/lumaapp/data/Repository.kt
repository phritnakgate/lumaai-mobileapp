package org.bkkz.lumaapp.data

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bkkz.lumaapp.data.entity.auth.EmailRegistrationRequest
import org.bkkz.lumaapp.data.entity.auth.EmailRegistrationResponse
import org.bkkz.lumaapp.data.entity.auth.EmailSignInRequest
import org.bkkz.lumaapp.data.entity.auth.EmailSignInResponse
import org.bkkz.lumaapp.data.entity.auth.GoogleSignInRequest
import org.bkkz.lumaapp.data.entity.auth.LogoutRequest
import org.bkkz.lumaapp.data.entity.auth.TokenRequest
import org.bkkz.lumaapp.data.entity.chat.LLMChatRequest
import org.bkkz.lumaapp.data.entity.chat.LLMProcess
import org.bkkz.lumaapp.data.entity.chat_history.ChatHistory
import org.bkkz.lumaapp.data.entity.report_history.ReportHistory
import org.bkkz.lumaapp.data.entity.task.CreateTaskRequest
import org.bkkz.lumaapp.data.entity.task.EditTaskRequest
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.data.local.TokenManager
import org.bkkz.lumaapp.data.local.UserChatEntity
import org.bkkz.lumaapp.data.local.UserChatDao
import org.bkkz.lumaapp.data.local.UserReportDao
import org.bkkz.lumaapp.data.local.UserReportEntity
import org.bkkz.lumaapp.data.remote.ApiResponse
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.data.remote.LumaApi
import retrofit2.HttpException
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64


class Repository(
    private val lumaApi: LumaApi,
    private val tokenManager: TokenManager,
    private val userChatDao: UserChatDao,
    private val userReportDao : UserReportDao
) {

    /*===========LOCAL DATA SOURCES===========*/
    fun getAllChats(): List<UserChatEntity> {
        return userChatDao.getAllUserChat()
    }

    suspend fun insertChat(userChatEntity: UserChatEntity){
        withContext(Dispatchers.IO){
            userChatDao.insertUserChat(userChatEntity)
        }
    }

    suspend fun deleleAllChat(){
        withContext(Dispatchers.IO){
            userChatDao.deleteAllUserChat()
        }
    }

    suspend fun confirmAction(dbId : Int){
        withContext(Dispatchers.IO){
            userChatDao.confirmAction(dbId)
        }
    }

    suspend fun confirmAllAction(){
        withContext(Dispatchers.IO){
            userChatDao.confirmActionAll()
        }
    }

    suspend fun insertUserReport(userReportEntity: UserReportEntity){
        withContext(Dispatchers.IO){
            userReportDao.insertUserReport(userReportEntity)
        }
    }

    suspend fun deleteAllCachedUserReport(){
        withContext(Dispatchers.IO){
            userReportDao.deleteAllUserReport()
        }
    }

    suspend fun isReportCached(fileName: String) : Boolean{
        return withContext(Dispatchers.IO){
            val report = userReportDao.getUserReportByFileName(fileName)
            report != null
        }
    }

    /*===========REMOTE DATA SOURCES===========*/
    suspend fun loginWithEmail(email: String, password: String): ApiResult<Unit> = withContext(
        Dispatchers.IO) {
        try {
            val (codeVerifier, codeChallenge) = generatePkceChallenge()
            val authCode = requestAuthorizationCode(email, password, codeChallenge)
            exchangeCodeForToken(authCode.code!!, codeVerifier)
            ApiResult.Success(Unit) // Return success

        } catch (e : HttpException){
            Log.e("AuthRepository", "Email login failed ${e.response()?.errorBody()?.string()}")
            ApiResult.Error(Exception("Incorrect Email or Password"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Email login failed", e)
            ApiResult.Error(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = lumaApi.loginWithGoogle(GoogleSignInRequest(idToken))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google login failed", e)
            ApiResult.Error(e)
        }
    }


    suspend fun refreshToken(): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken == null) {
            return@withContext ApiResult.Success(false)
        }

        try {
            val request = TokenRequest(
                grantType = "refresh_token",
                refreshToken = refreshToken
                )
            val response = lumaApi.tokenRequest(request)
            tokenManager.saveTokens(response.accessToken, response.refreshToken)

            ApiResult.Success(true)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Session refresh failed", e)
            tokenManager.clearTokens()
            ApiResult.Success(false)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val refreshToken = tokenManager.getRefreshToken()
        Log.d("AuthRepository", "Logout with $refreshToken")
        tokenManager.clearTokens()
        deleteAllCachedUserReport()
        try {
            val response = lumaApi.logout(LogoutRequest(refreshToken!!))
            if (!response.isSuccessful) throw Exception("Failed to logout")
        }catch (e: Exception){
            Log.e("AuthRepository", "Logout failed", e)
        }
    }

    suspend fun registerWithEmail(email: String, password: String, name: String ) = withContext(Dispatchers.IO){
        try {
            val (codeVerifier, codeChallenge) = generatePkceChallenge()
            val response = lumaApi.registerWithEmail(EmailRegistrationRequest(email,password,name,codeChallenge))
            if(response.error != null){
                ApiResult.Error(Exception(response.error))
            }else{
                exchangeCodeForToken(response.authorizationCode!!, codeVerifier)
                ApiResult.Success(Unit)
            }
        }catch (e : HttpException){
            val body = e.response()?.errorBody()?.string()
            val response = Gson().fromJson(body, EmailRegistrationResponse::class.java)
            Log.e("AuthRepository", "Email registration failed $body")
            ApiResult.Error(Exception(response.error))
        }
        catch (e: Exception) {
            Log.e("AuthRepository", "Email registration failed", e)
            ApiResult.Error(e)
        }
    }

    private suspend fun requestAuthorizationCode(email: String, password: String, codeChallenge: String): EmailSignInResponse {

        val request = EmailSignInRequest(email, password, codeChallenge)

        val response = lumaApi.loginWithEmail(request)
        return response
    }

    private suspend fun exchangeCodeForToken(authCode: String, codeVerifier: String) {
        val requestBody = TokenRequest(
            grantType = "authorization_code",
            code = authCode,
            codeVerifier = codeVerifier
        )
        val response = lumaApi.tokenRequest(requestBody)
        tokenManager.saveTokens(response.accessToken, response.refreshToken)
    }

    private fun generatePkceChallenge(): Pair<String, String> {
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)
        val codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(codeVerifier.toByteArray(StandardCharsets.US_ASCII))
        val codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hashedBytes)
        return Pair(codeVerifier, codeChallenge)
    }

    suspend fun getAllUserTasks(date : String) : ApiResult<List<Task>?> = withContext(Dispatchers.IO){
        try{
            val response = lumaApi.getUserTasks(date).body()
            if(response == null){
                val taskList = emptyList<Task>()
                ApiResult.Success(taskList)
            }else{
                val taskList = response.results
                ApiResult.Success(taskList)
            }

        } catch (e: Exception){
            Log.e("Repository","Failed to get task bc ${e.message}")
            ApiResult.Error(Exception(e.message))
        }
    }

    suspend fun createTask(createTaskRequest: CreateTaskRequest) : ApiResult<Any> = withContext(
        Dispatchers.IO){
        try{
            val response = lumaApi.createTask(createTaskRequest)
            if(response.isSuccessful){
                ApiResult.Success(response.body()?.result!!)
            }else{
                ApiResult.Error(Exception())
            }

        }catch (e: Exception){
            Log.e("Repository","Failed to create task bc ${e.message}")
            ApiResult.Error(Exception(e.message))
        }
    }
    suspend fun editTask(taskId : String, editTaskRequest: EditTaskRequest) : ApiResult<Any> = withContext(
        Dispatchers.IO){
        try{
            val response = lumaApi.editTask(taskId, editTaskRequest)
            if(response.isSuccessful){
                ApiResult.Success(response.body()?.result!!)
            }else{
                ApiResult.Error(Exception())
            }
        }catch (e: Exception){
            Log.e("Repository","Failed to edit task bc ${e.message}")
            ApiResult.Error(Exception(e.message))
        }
    }

    suspend fun deleteTask(taskId : String) : ApiResult<Any> = withContext(Dispatchers.IO){
        try{
            val response = lumaApi.deleteTask(taskId)
            if(response.isSuccessful){
                ApiResult.Success(response)
            }else{
                ApiResult.Error(Exception())
            }
        }catch (e: Exception){
            Log.e("Repository","Failed to delete task bc ${e.message}")
            ApiResult.Error(Exception(e.message))
        }
    }

    suspend fun getChatLogs(intent: String? = null, date: String? = null, keyword: String? = null) : ApiResult<List<ChatHistory>?> = withContext(
        Dispatchers.IO){
            try {
                val response = lumaApi.getChatLogs(intent, date, keyword)
                if(response.isSuccessful){
                    if(response.body() == null){
                        val chatHistoryList = emptyList<ChatHistory>()
                        ApiResult.Success(chatHistoryList)
                    }else{
                        val chatHistoryList = response.body()?.results
                        ApiResult.Success(chatHistoryList)
                    }
                }else{
                    ApiResult.Error(Exception(response.body()?.error))
                }
            }catch (e : Exception){
                Log.e("Repository","Failed to get log bc ${e.message}")
                ApiResult.Error(Exception(e.message))
            }
    }

    suspend fun chatWithLuma(message: String) : ApiResult<ApiResponse<LLMProcess>?> = withContext(
        Dispatchers.IO){
            try {
                val response = lumaApi.chatWithLuma(LLMChatRequest(message))
                if(response.isSuccessful){
                    ApiResult.Success(response.body())
                }else{
                    ApiResult.Error(Exception(response.errorBody()?.string()))
                }
            }catch (e : Exception){
                Log.e("Repository","Failed to chat with luma bc ${e.message}")
                ApiResult.Error(Exception(e.message))
            }
    }

    suspend fun generateMisTaskReport(reportYrM: String) : ApiResult<String> = withContext(Dispatchers.IO){
        try{
            val response = lumaApi.generateMISReport(reportYrM)
            if(response.code() == 302){
                val downloadUrl = response.headers()["Location"]
                if(downloadUrl == null){
                    return@withContext ApiResult.Error(Exception("Download URL is null"))
                }
                ApiResult.Success(downloadUrl)
            }else{
                ApiResult.Error(Exception("Cannot generate report"))
            }

        }catch (e: Exception){
            Log.e("Repository","Failed to generate MIS task report bc ${e.message}")
            ApiResult.Error(Exception(e.message))
        }
    }

    suspend fun getReportHistory(formType: String) : ApiResult<ApiResponse<ReportHistory>> = withContext(
        Dispatchers.IO){
            try {
                val response = lumaApi.getReports(formType)
                if(response.isSuccessful){
                    ApiResult.Success(response.body()!!)
                }else{
                    ApiResult.Error(Exception(response.body()?.error))
                }
            }catch (e : Exception){
                Log.e("Repository","Failed to get report history bc ${e.message}")
                ApiResult.Error(Exception(e.message))
            }
    }
}