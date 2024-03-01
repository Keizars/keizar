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
    /**
     * Create a room with a specific room number and board properties.
     * Return OK if the room is created successfully.
     */
    @POST("room/{roomNumber}/create")
    suspend fun createRoom(
        @Path("roomNumber") roomNumber: UInt,
        @Body properties: BoardProperties
    )

    /**
     * Get the room information of a specified room number.
     */
    @GET("room/{roomNumber}")
    suspend fun getRoom(@Path("roomNumber") roomNumber: UInt): RoomInfo

    /**
     * Join a room with given number and user info.
     * Return OK on success.
     */
    @POST("room/{roomNumber}/join")
    suspend fun joinRoom(roomNumber: UInt): Boolean
}