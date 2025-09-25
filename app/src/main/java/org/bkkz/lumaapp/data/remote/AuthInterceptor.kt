package org.bkkz.lumaapp.data.remote

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.local.TokenManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthInterceptor() : Interceptor, KoinComponent {

    private val tokenManager : TokenManager by inject()
    private val repository : Repository by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        Log.i("AuthInterceptor","AuthInterceptor Called!")
        val originalRequest = chain.request()
        val accessToken = tokenManager.getAccessToken()

        val requestWithAuth = if (accessToken != null && !originalRequest.url.encodedPath.contains("auth/")) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else originalRequest

        val response = chain.proceed(requestWithAuth)

        if(response.code == 401){
            response.close()
            //Log.i("AuthInterceptor","Token Expired!")
            synchronized(this) {
                val currentToken = tokenManager.getAccessToken()
                if (currentToken == null || currentToken == accessToken) {
                    runBlocking {
                        repository.refreshToken()
                    }
                }
            }
            val newToken = tokenManager.getAccessToken()
            if(newToken != null){
                val newRequest = originalRequest.newBuilder().addHeader("Authorization","Bearer $accessToken").build()
                //Log.i("AuthInterceptor","New Request ${newRequest.url.encodedPath} Send!")
                return chain.proceed(newRequest)
            }
        }

        return response
    }
}