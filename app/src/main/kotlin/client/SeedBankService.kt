package org.keizar.android.client

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SeedBankService {
    @GET("seed-bank")
    suspend fun getSeeds(): List<String>

    @POST("seed-bank/{seed}")
    suspend fun addSeed(@Path("seed") seed: String)

    @DELETE("seed-bank/{seed}")
    suspend fun removeSeed(@Path("seed") seed: String)
}