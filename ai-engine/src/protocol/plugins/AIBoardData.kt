package org.keizar.aiengine.protocol.plugins

import kotlinx.serialization.Serializable

@Serializable
data class AIBoardData(
    val board: List<List<Int>>
)