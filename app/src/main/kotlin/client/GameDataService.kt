package org.keizar.android.client

import org.keizar.utils.communication.game.GameDataGet
import org.keizar.utils.communication.game.GameDataStore
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GameDataService {
    /**
     * Uploads a game data to the server.
     */
    @POST("games")
    suspend fun sendGameData(@Body gameData: GameDataStore)

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