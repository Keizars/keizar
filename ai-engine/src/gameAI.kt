package org.keizar.aiengine

import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.game.Move
import org.keizar.game.Player
import org.keizar.game.boardPoses

interface GameAI {
    val game: GameSession
    val myplayer: Player

    fun FindBestMove():Move
}