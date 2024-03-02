package org.keizar.android.client
import org.keizar.utils.communication.game.AnonymousGameData
import retrofit2.http.Body
import retrofit2.http.POST

interface AnonymousDataService {
    @POST("game/anonymous/data")
    suspend fun sendAnonymousData(@Body gameData: AnonymousGameData): Boolean

}