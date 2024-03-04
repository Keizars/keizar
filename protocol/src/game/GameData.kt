package org.keizar.utils.communication.game

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Serializable
data class GameDataStore(
    val id: String? = null,
    val round1Statistics: RoundStats,
    val round2Statistics: RoundStats,
    val gameConfiguration: String,
    val userId: String? = null,
    val opponentId: String? = null,
    val currentTimestamp: String,
    val userSaved: Boolean = false
)

@Serializable
data class GameDataGet(
    val selfUsername: String,
    val opponentUsername: String,
    val timeStamp: String,
    val gameConfiguration: String,
    val round1Stats: RoundStats,
    val round2Stats: RoundStats,
    val dataId: String,
)

@Serializable
data class NeutralStats(
    val whiteCaptured: Int,
    val blackCaptured: Int,
    val whiteAverageTime: Double,
    val whiteMoves: Int,
    val blackMoves: Int,
    val blackAverageTime: Double,
    val blackTime: Int,
    val whiteTime: Int,
)


@Serializable
data class RoundStats(
    val neutralStats: NeutralStats,
    val player: Player,
    val winner: Player?,
)

fun jsonElementToRoundStats(jsonElement: JsonElement): RoundStats {
    val jsonString = jsonElement.toString()
    return Json.decodeFromString<RoundStats>(jsonString)
}