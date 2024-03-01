package org.keizar.android.client
import org.keizar.utils.communication.game.GameData
import org.keizar.utils.communication.game.GameDataRequestData
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface GameDataService {
    @POST("game/data")
    suspend fun sendGameData(@Body gameData: GameData): Boolean
    
    @GET("game/data")
    suspend fun getGameData(@Body userName: String): List<GameData>
    
    @DELETE("game/data")
    suspend fun deleteGameData(@Body gameDataRequestData: GameDataRequestData): Boolean
    
}