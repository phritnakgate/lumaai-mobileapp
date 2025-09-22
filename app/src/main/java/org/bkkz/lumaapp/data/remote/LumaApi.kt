package org.bkkz.lumaapp.data.remote

import org.bkkz.lumaapp.data.entity.auth.EmailRegistrationRequest
import org.bkkz.lumaapp.data.entity.auth.EmailSignInRequest
import org.bkkz.lumaapp.data.entity.auth.EmailSignInResponse
import org.bkkz.lumaapp.data.entity.auth.GoogleSignInRequest
import org.bkkz.lumaapp.data.entity.auth.LogoutRequest
import org.bkkz.lumaapp.data.entity.auth.TokenRequest
import org.bkkz.lumaapp.data.entity.auth.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

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
    ) : TokenResponse

    @POST("auth/register")
    suspend fun registerWithEmail(
        @Body emailRegistrationRequest: EmailRegistrationRequest
    ) : TokenResponse

    @POST("auth/logout")
    suspend fun logout(
        @Body logoutRequest: LogoutRequest
    ) : Response<Unit>
}