package org.keizar.android.client

import org.keizar.utils.communication.account.AuthRequest
import org.keizar.utils.communication.account.AuthResponse
import org.keizar.utils.communication.account.User
import org.keizar.utils.communication.account.UsernameValidityResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserService {
    @POST("users/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("users/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("users/available")
    suspend fun isAvailable(@Query("username") username: String): UsernameValidityResponse

    @GET("users/self")
    suspend fun self(): User
}