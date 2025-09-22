package org.bkkz.lumaapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bkkz.lumaapp.data.entity.auth.EmailRegistrationRequest
import org.bkkz.lumaapp.data.entity.auth.EmailSignInRequest
import org.bkkz.lumaapp.data.entity.auth.EmailSignInResponse
import org.bkkz.lumaapp.data.entity.auth.GoogleSignInRequest
import org.bkkz.lumaapp.data.entity.auth.LogoutRequest
import org.bkkz.lumaapp.data.entity.auth.TokenRequest
import org.bkkz.lumaapp.data.local.TokenManager
import org.bkkz.lumaapp.data.local.UserChat
import org.bkkz.lumaapp.data.local.UserChatDao
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.data.remote.LumaApi
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64


class Repository(
    private val lumaApi: LumaApi,
    private val tokenManager: TokenManager,
    private val userChatDao: UserChatDao) {

    /*===========LOCAL DATA SOURCES===========*/
    fun getAllChats(): List<UserChat> {
        return userChatDao.getAllUserChat()
    }

    suspend fun insertChat(userChat: UserChat){
        withContext(Dispatchers.IO){
            userChatDao.insertUserChat(userChat)
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

    /*===========REMOTE DATA SOURCES===========*/
    suspend fun loginWithEmail(email: String, password: String): ApiResult<Unit> = withContext(
        Dispatchers.IO) {
        try {
            // 1. Generate PKCE
            val (codeVerifier, codeChallenge) = generatePkceChallenge()

            // 2. Request Authorization Code
            val authCode = requestAuthorizationCode(email, password, codeChallenge)

            // 3. Exchange Code for Token
            exchangeCodeForToken(authCode.code, codeVerifier)

            ApiResult.Success(Unit) // Return success
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
        try {
            val response = lumaApi.logout(LogoutRequest(refreshToken!!))
            tokenManager.clearTokens()
            if (!response.isSuccessful) throw Exception("Failed to logout")
        }catch (e: Exception){
            Log.e("AuthRepository", "Logout failed", e)
        }
    }

    suspend fun registerWithEmail(email: String, password: String, name: String ) = withContext(Dispatchers.IO){
        try {
            val (codeVerifier, codeChallenge) = generatePkceChallenge()
            val response = lumaApi.registerWithEmail(EmailRegistrationRequest(email,password,name,codeChallenge))
            exchangeCodeForToken(response.authorizationCode, codeVerifier)
            ApiResult.Success(Unit)
        }catch (e: Exception) {
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
}