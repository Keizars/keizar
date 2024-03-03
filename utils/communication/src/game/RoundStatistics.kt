package org.keizar.utils.communication.game
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

import kotlinx.serialization.Serializable

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