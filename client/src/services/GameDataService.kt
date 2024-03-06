package org.keizar.client.services

import org.keizar.utils.communication.game.GameDataGet
import org.keizar.utils.communication.game.GameDataId
import org.keizar.utils.communication.game.GameDataStore
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Service for saving seeds and boards for each user.
 */
interface GameDataService : ClientService {
    /**
     * Uploads a game data to the server.
     */
    @POST("games")
    suspend fun autoSaveData(@Body gameData: GameDataStore): GameDataId

    @POST("games/save/{dataId}")
    suspend fun userSaveData(@Path("dataId") dataId: String): String

    /**
     * Retrieves all the games of the user currently logged in.
     */
    @GET("games")
    suspend fun getGames(): List<GameDataGet>

    /**
     * Deletes a game from the server.
     */
    @DELETE("games/{id}")
    // id is GameData id
    suspend fun deleteGame(@Path("id") id: String)
}