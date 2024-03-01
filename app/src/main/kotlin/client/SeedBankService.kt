package org.keizar.android.client

import org.keizar.utils.communication.account.AuthRequest
import org.keizar.utils.communication.account.AuthResponse
import org.keizar.utils.communication.account.User
import org.keizar.utils.communication.account.UsernameValidityResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SeedBankService {
    @GET("seed-bank")
    suspend fun getSeeds(): List<String>

    @POST("seed-bank/{seed}")
    suspend fun addSeed(@Body seed: String)

    @DELETE("seed-bank/{seed}")
    suspend fun removeSeed(@Body seed: String)
}