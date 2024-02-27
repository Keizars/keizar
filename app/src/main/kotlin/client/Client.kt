package org.keizar.android.client

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import org.keizar.utils.communication.CommunicationModule
import org.keizar.utils.communication.account.AuthRequest
import org.keizar.utils.communication.account.AuthResponse
import org.keizar.utils.communication.account.UsernameValidityResponse
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

class Client {
    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = CommunicationModule
    }

    private val contentType = MediaType.get("application/json")

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/")
        .addConverterFactory(Json.asConverterFactory(contentType))
        .build()

    val auth: AuthService = retrofit.create(AuthService::class.java)
}

interface AuthService {
    @POST("users/register")
    fun register(@Body request: AuthRequest): AuthResponse

    @POST("users/login")
    fun login(@Body request: AuthRequest): AuthResponse

    @POST("users/available")
    fun isAvailable(@Query("username") username: String): UsernameValidityResponse
}
