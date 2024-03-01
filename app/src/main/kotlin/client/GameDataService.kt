package org.keizar.android.client
import retrofit2.http.Body
import retrofit2.http.POST
import org.keizar.utils.communication.game.PlayerStatistics

interface GameDataService {
    @POST("game/data")
    suspend fun sendGameData(@Body gameData: PlayerStatistics): Boolean
}