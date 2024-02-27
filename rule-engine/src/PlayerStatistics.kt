package org.keizar.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.keizar.utils.communication.game.Player

class PlayerStatistics(val player: Player) {
    //TODO: Implement the statistics of the player
    val round1Captured : StateFlow<Int> = MutableStateFlow(0)
    val round2Captured : StateFlow<Int> = MutableStateFlow(0)

    val round1OpponentCaptured : StateFlow<Int> = MutableStateFlow(0)
    val round2OpponentCaptured : StateFlow<Int> = MutableStateFlow(0)

    val round1Moves : StateFlow<Int> = MutableStateFlow(0)
    val round2Moves : StateFlow<Int> = MutableStateFlow(0)

    val round1Time : StateFlow<Int> = MutableStateFlow(0)
    val round2Time : StateFlow<Int> = MutableStateFlow(0)

    val round1AverageTime: StateFlow<Int> = MutableStateFlow(0)
    val round2AverageTime: StateFlow<Int> = MutableStateFlow(0)

}