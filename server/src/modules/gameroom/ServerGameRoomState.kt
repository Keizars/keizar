package org.keizar.server.modules.gameroom

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.utils.communication.GameRoomState
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.UserInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

sealed interface ServerGameRoomState {
    val players: Map<UserInfo, PlayerSession>

    sealed interface StateWithModifiableBoardPropertiesServer: ServerGameRoomState {
        var boardProperties: BoardProperties
    }

    data class Started(
        override var boardProperties: BoardProperties,
    ) : ServerGameRoomState, StateWithModifiableBoardPropertiesServer {
        private val _players = mutableMapOf<UserInfo, PlayerSession>()
        private val _playersMutex = Mutex()
        override val players: Map<UserInfo, PlayerSession>
            get() = _players.toMap()


        suspend fun <T> players(action: suspend MutableMap<UserInfo, PlayerSession>.() -> T): T {
            return _playersMutex.withLock { _players.action() }
        }

        fun toAllConnected(): AllConnected {
            return AllConnected(_players.toMap(), boardProperties)
        }

        /**
         * Determine which player is the FirstBlackPlayer and which is the FirstWhitePlayer.
         */
        private val allPlayers = Player.entries.shuffled()
        private val allPlayersIndex = AtomicInteger(0)
        private val registeredAllocation = ConcurrentHashMap<UserInfo, Player>()
        fun allocatePlayer(user: UserInfo): Player? {
            if (user in registeredAllocation.keys) return registeredAllocation[user]

            val index = allPlayersIndex.getAndIncrement()
            if (index >= allPlayers.size) return null
            val allocation = allPlayers[index]
            registeredAllocation[user] = allocation
            return allocation
        }
    }

    data class AllConnected(
        override val players: Map<UserInfo, PlayerSession>,
        override var boardProperties: BoardProperties,
    ) : ServerGameRoomState, StateWithModifiableBoardPropertiesServer {
        fun toPlaying(): Playing {
            return Playing(players, boardProperties)
        }
    }

    data class Playing(
        override val players: Map<UserInfo, PlayerSession>,
        val boardProperties: BoardProperties,
    ) : ServerGameRoomState {
        val serverGame: GameSession = GameSession.create(boardProperties)

        fun toFinished(): Finished {
            return Finished(players)
        }
    }

    data class Finished(
        override val players: Map<UserInfo, PlayerSession>
    ) : ServerGameRoomState

    fun toGameRoomState(): GameRoomState {
        return when(this) {
            is Started -> GameRoomState.STARTED
            is AllConnected -> GameRoomState.ALL_CONNECTED
            is Playing -> GameRoomState.PLAYING
            is Finished -> GameRoomState.FINISHED
        }
    }
}