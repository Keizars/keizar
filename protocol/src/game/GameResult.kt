package org.keizar.utils.communication.game

import kotlinx.serialization.Serializable

@Serializable
sealed class GameResult {
    @Serializable
    data object Draw : GameResult()

    @Serializable
    data class Winner(val player: Player) : GameResult()
    companion object {
        fun values(): Array<GameResult> {
            return arrayOf(Draw, Winner(Player.FirstWhitePlayer), Winner(Player.FirstBlackPlayer))
        }

        fun valueOf(value: String): GameResult {
            return when (value) {
                "DRAW" -> Draw
                "WINNER1" -> Winner(Player.FirstWhitePlayer)
                "WINNER2" -> Winner(Player.FirstBlackPlayer)
                else -> throw IllegalArgumentException("No object org.keizar.game.GameResult.$value")
            }
        }
    }
}