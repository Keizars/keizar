package org.keizar.android

import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.keizar.android.ui.game.configuration.GameStartConfiguration

/**
 * Encoder for [GameStartConfiguration]
 */
object GameStartConfigurationEncoder {
    /**
     * Encodes the [configuration] into a configuration seed.
     */
    fun encode(
        configuration: GameStartConfiguration
    ): String = ProtoBuf.encodeToHexString(GameStartConfiguration.serializer(), configuration)

    /**
     * Decodes the [configurationSeed] into a [GameStartConfiguration].
     * Returns `null` if the [configurationSeed] is invalid.
     */
    fun decode(configurationSeed: String): GameStartConfiguration? =
        kotlin.runCatching {
            ProtoBuf.decodeFromHexString(GameStartConfiguration.serializer(), configurationSeed)
        }.getOrNull()
}

/**
 * Encodes the [GameStartConfiguration] into a configuration seed.
 */
fun GameStartConfiguration.encode() = GameStartConfigurationEncoder.encode(this)