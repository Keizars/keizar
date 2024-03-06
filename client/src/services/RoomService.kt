package org.keizar.client.services

import org.keizar.client.AccessTokenProvider
import org.keizar.client.Room
import org.keizar.client.annotations.InternalClientApi
import org.keizar.client.exception.RoomFullException
import org.keizar.client.internal.KeizarHttpClient
import org.keizar.client.internal.KeizarHttpClientImpl
import org.keizar.game.BoardProperties
import org.keizar.game.RoomInfo
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import kotlin.coroutines.CoroutineContext

/**
 * Service for creating and joining game rooms.
 *
 * See also [BaseRoomService] for available APIs.
 */
@OptIn(InternalClientApi::class)
interface RoomService : BaseRoomService {
    /**
     * Connect to server by websocket and create a game room session
     * @throws RoomFullException if the join room failed
     */
    suspend fun connect(
        roomNumber: UInt,
        parentCoroutineContext: CoroutineContext,
    ): Room
}

/**
 * Internal interface for Retrofit to generate implementations.
 * Always use [RoomService] instead.
 */
@InternalClientApi
interface BaseRoomService : ClientService {
    /**
     * Create a room with a specific room number and board properties.
     * Return OK if the room is created successfully.
     */
    @POST("room/{roomNumber}/create")
    suspend fun createRoom(
        @Path("roomNumber") roomNumber: String,
        @Body properties: BoardProperties = BoardProperties.getStandardProperties()
    )

    /**
     * Get the room information of a specified room number.
     */
    @GET("room/{roomNumber}")
    suspend fun getRoom(@Path("roomNumber") roomNumber: String): RoomInfo

    /**
     * Join a room with given number.
     * Return OK on success.
     */
    @POST("room/{roomNumber}/join")
    suspend fun joinRoom(@Path("roomNumber") roomNumber: String)
}

@OptIn(InternalClientApi::class)
internal class RoomServiceImpl
@OptIn(InternalClientApi::class)
constructor(
    baseUrl: String,
    private val generated: BaseRoomService,
) : RoomService, BaseRoomService by generated, KoinComponent {
    private val client: KeizarHttpClient = KeizarHttpClientImpl(baseUrl)
    private val accessTokenProvider: AccessTokenProvider by inject()

    override suspend fun connect(
        roomNumber: UInt,
        parentCoroutineContext: CoroutineContext,
    ): Room {
        val token = accessTokenProvider.getAccessToken() ?: throw IllegalStateException("User token not available")
        if (!client.postRoomJoin(roomNumber, token)) {
            throw RoomFullException()
        }
        val self = client.getSelf(token)
        val websocketSession = client.getRoomWebsocketSession(roomNumber, token)
        val roomInfo = client.getRoom(roomNumber, token)
        return Room.create(
            self = self,
            roomInfo = roomInfo,
            websocketSession = websocketSession,
            parentCoroutineContext = parentCoroutineContext
        )
    }
}