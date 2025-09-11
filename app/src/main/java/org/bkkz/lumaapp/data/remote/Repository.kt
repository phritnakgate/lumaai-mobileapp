package org.bkkz.lumaapp.data.remote

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bkkz.lumaapp.data.local.TokenManager
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

class Repository(private val tokenManager: TokenManager) {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val apiBaseUrl = "http://10.0.2.2:8080/api/auth"

    suspend fun loginWithEmail(email: String, password: String): Result<Unit> = withContext(
        Dispatchers.IO) {
        try {
            // 1. Generate PKCE
            val (codeVerifier, codeChallenge) = generatePkceChallenge()

            // 2. Request Authorization Code
            val authCode = requestAuthorizationCode(email, password, codeChallenge)

            // 3. Exchange Code for Token
            exchangeCodeForToken(authCode, codeVerifier)

            Result.Success(Unit) // Return success
        } catch (e: Exception) {
            Log.e("AuthRepository", "Email login failed", e)
            Result.Error(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val requestBody = gson.toJson(mapOf("idToken" to idToken))
            val request = Request.Builder()
                .url("$apiBaseUrl/login-google")
                .post(requestBody.toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("Google login failed on server: ${response.message}")
            }

            val responseBody = response.body?.string()
            val accessToken = JSONObject(responseBody).getString("access_token")
            val refreshToken = JSONObject(responseBody).getString("refresh_token")
            tokenManager.saveTokens(accessToken, refreshToken)

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google login failed", e)
            Result.Error(e)
        }
    }


    suspend fun refreshToken(): Result<Boolean> = withContext(Dispatchers.IO) {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken == null) {
            return@withContext Result.Success(false)
        }

        try {
            val requestBodyMap = mapOf(
                "grantType" to "refresh_token",
                "refreshToken" to refreshToken
            )
            val requestBody = gson.toJson(requestBodyMap).toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$apiBaseUrl/token")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("Failed to refresh token")

            val responseBody = response.body?.string()
            val newAccessToken = JSONObject(responseBody).getString("access_token")
            val newRefreshToken = JSONObject(responseBody).getString("refresh_token")
            tokenManager.saveTokens(newAccessToken, newRefreshToken)

            Result.Success(true)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Session refresh failed", e)
            tokenManager.clearTokens()
            Result.Success(false)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val refreshToken = tokenManager.getRefreshToken()
        val requestBodyMap = mapOf(
            "refreshToken" to refreshToken
        )
        val requestBody = gson.toJson(requestBodyMap).toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$apiBaseUrl/logout")
            .post(requestBody)
            .build()
        tokenManager.clearTokens()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("Failed to logout")
        }catch (e: Exception){
            Log.e("AuthRepository", "Logout failed", e)
        }



    }

    private fun requestAuthorizationCode(email: String, password: String, codeChallenge: String): String {
        val requestBodyMap = mapOf(
            "email" to email,
            "password" to password,
            "codeChallenge" to codeChallenge,
            "codeChallengeMethod" to "S256"
        )
        val requestBody = gson.toJson(requestBodyMap).toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$apiBaseUrl/login-email")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful || responseBody == null) {
            throw Exception("Step 1 failed: ${response.message}")
        }
        return JSONObject(responseBody).getString("code")
    }

    private fun exchangeCodeForToken(authCode: String, codeVerifier: String) {
        val requestBodyMap = mapOf(
            "grantType" to "authorization_code",
            "code" to authCode,
            "codeVerifier" to codeVerifier
        )
        val requestBody = gson.toJson(requestBodyMap).toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$apiBaseUrl/token")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful || responseBody == null) {
            val errorDesc = JSONObject(responseBody ?: "{}").optString("error_description", response.message)
            throw Exception("Step 2 failed: $errorDesc")
        }

        val accessToken = JSONObject(responseBody).getString("access_token")
        val refreshToken = JSONObject(responseBody).getString("refresh_token")
        tokenManager.saveTokens(accessToken, refreshToken)
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