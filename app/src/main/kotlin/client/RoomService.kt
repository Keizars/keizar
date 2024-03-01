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
    @POST("room/{roomNumber}/create")
    suspend fun createRoom(
        @Path("roomNumber") roomNumber: UInt,
        @Body properties: BoardProperties
    )

    @GET("room/{roomNumber}")
    suspend fun getRoom(@Path("roomNumber") roomNumber: UInt): RoomInfo

    @POST("room/{roomNumber}/join")
    suspend fun joinRoom(roomNumber: UInt, @Body userInfo: UserInfo): Boolean
}