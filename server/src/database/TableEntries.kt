package org.keizar.server.database

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.internal.JsonToStringWriter
import kotlinx.serialization.json.internal.encodeByWriter
import kotlinx.serialization.modules.SerializersModule

enum class ColumnType {
    STRING,
    NUMBER,
    BOOLEAN,
    BINARY,
}

data class ColumnValue(
    val name: String,
    val type: ColumnType,
    val value: Any,
)

sealed interface TableEntry

data class User(
    val userId: String, // Primary Key
    val userName: String,
) : TableEntry

data class Game(
    val gameId: String, // Primary Key
    val gameSnapshot: String,
) : TableEntry

data class GameCollection(
    val gameCollectionId: Int, // Primary Key
    val gameId: String, // Foreign Key -> Games
    val userId: String, // Foreign Key -> Users, Index
) : TableEntry

data class SeedBank(
    val seedBankId: Int, // Primary Key
    val userId: String, // Foreign Key -> Users, Index
    val gameSeed: String,
) : TableEntry

class AttributeMapFormat(
    override val serializersModule: SerializersModule
) : SerialFormat {
    fun <T> encodeToAttributeMap(
        serializer: SerializationStrategy<T>,
        value: T
    ): Map<String, ColumnValue> {
        val result = mutableMapOf<String, ColumnValue>()
        try {
            encodeByWriter(this@Json, result, serializer, value)
            return result.toString()
        } finally {
            result.release()
        }
    }
}

