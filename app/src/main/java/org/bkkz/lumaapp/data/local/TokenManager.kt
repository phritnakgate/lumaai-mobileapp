package org.bkkz.lumaapp.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit

class TokenManager(context: Context) {
    companion object {
        private const val TOKEN_FILE = "auth_tokens"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }

    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        TOKEN_FILE,
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            extractAndSaveRefreshTokenExpiry(refreshToken)
        }
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }

    fun clearTokens() {
        sharedPreferences.edit {
            remove(ACCESS_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            remove("refresh_token_expiry")
        }
    }

    fun getRefreshTokenExpiry() : Long {
        return sharedPreferences.getLong("refresh_token_expiry", 0L)
    }

    fun isRefreshTokenExpired(): Boolean {
        val expiry = getRefreshTokenExpiry()
        val currentTime = System.currentTimeMillis() / 1000
        return currentTime >= expiry
    }

    fun extractAndSaveRefreshTokenExpiry(refreshToken: String) {
        val parts = refreshToken.split(".")
        if (parts.size == 3) {
            val payload = parts[1]
            val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val decodedString = String(decodedBytes)
            val regex = """"exp"\s*:\s*(\d+)""".toRegex()
            val matchResult = regex.find(decodedString)
            val expiry = matchResult?.groups?.get(1)?.value?.toLongOrNull()
            if (expiry != null) {
                Log.i("TokenManager", "Refresh token expiry extracted: $expiry")
                sharedPreferences.edit {
                    putLong("refresh_token_expiry", expiry)
                }
            }
        }

    }
}