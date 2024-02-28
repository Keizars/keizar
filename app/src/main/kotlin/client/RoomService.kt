package org.keizar.android.client

import org.keizar.game.BoardProperties
import org.keizar.game.RoomInfo
import org.keizar.utils.communication.message.UserInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface RoomService {
    @POST("room/create/{roomNumber}")
    suspend fun createRoom(
        @Path("roomNumber") roomNumber: UInt,
        @Body properties: BoardProperties
    )

    @GET("room/{roomNumber}")
    suspend fun getRoom(@Path("roomNumber") roomNumber: UInt): RoomInfo

    @POST("room/join/{roomNumber}")
    suspend fun joinRoom(roomNumber: UInt, @Body userInfo: UserInfo): Boolean

    /**
     * Changes the seed of the room
     */
    @PATCH("room/{roomNumber}/seed/{seed}")
    suspend fun setSeed(roomNumber: UInt, seed: UInt)

    /**
     * Accepts the seed change made by other user
     */
    @POST("room/{roomNumber}/agree")
    suspend fun acceptChange(roomNumber: UInt, seed: UInt)
}