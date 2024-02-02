package org.keizar.server.training.plugins

import kotlinx.serialization.Serializable

@Serializable
data class AIBoardData(
    val board: List<List<Int>>
)