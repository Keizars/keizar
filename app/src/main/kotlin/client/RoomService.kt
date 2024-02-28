package org.keizar.android.client

import retrofit2.http.PATCH
import retrofit2.http.POST

interface RoomService {
//    @POST("room/create/{roomNumber}")
//    suspend fun createRoom(
//        @Path("roomNumber") roomNumber: UInt,
//        @Body properties: BoardProperties
//    )
//
//    @GET("room/{roomNumber}")
//    suspend fun getRoom(@Path("roomNumber") roomNumber: UInt): RoomInfo
//
//    @POST("room/join/{roomNumber}")
//    suspend fun joinRoom(roomNumber: UInt, userInfo: UserInfo): Boolean

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