package org.keizar.android

import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.keizar.android.ui.game.configuration.GameStartConfiguration

object GameStartConfigurationEncoder {
    fun encode(
        configuration: GameStartConfiguration
    ): String = ProtoBuf.encodeToHexString(GameStartConfiguration.serializer(), configuration)

    fun decode(configurationSeed: String): GameStartConfiguration? =
        kotlin.runCatching {
            ProtoBuf.decodeFromHexString(GameStartConfiguration.serializer(), configurationSeed)
        }.getOrNull()
}

fun GameStartConfiguration.encode() = GameStartConfigurationEncoder.encode(this)