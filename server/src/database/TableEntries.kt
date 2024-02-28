package org.keizar.server.database

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

//typealias AttributeMap = Map<String, ColumnValue>
//
//class AttributeMapFormat(
//    override val serializersModule: SerializersModule
//) : SerialFormat {
//    private inner class AttributeMapEncoder(
//        private val output: (AttributeMap) -> Unit,
//    ) : CompositeEncoder {
//        override val serializersModule: SerializersModule get() = this@AttributeMapFormat.serializersModule
//        override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
//            TODO("Not yet implemented")
//        }
//
//        override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
//            TODO("Not yet implemented")
//        }
//
//        override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
//            TODO("Not yet implemented")
//        }
//
//        override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
//            TODO("Not yet implemented")
//        }
//
//        override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
//            TODO("Not yet implemented")
//        }
//
//        override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
//            TODO("Not yet implemented")
//        }
//
//        override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
//            TODO("Not yet implemented")
//        }
//
//        override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
//            TODO("Not yet implemented")
//        }
//
//        @ExperimentalSerializationApi
//        override fun <T : Any> encodeNullableSerializableElement(
//            descriptor: SerialDescriptor,
//            index: Int,
//            serializer: SerializationStrategy<T>,
//            value: T?
//        ) {
//            TODO("Not yet implemented")
//        }
//
//        override fun <T> encodeSerializableElement(
//            descriptor: SerialDescriptor,
//            index: Int,
//            serializer: SerializationStrategy<T>,
//            value: T
//        ) {
//            TODO("Not yet implemented")
//        }
//
//        override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
//            TODO("Not yet implemented")
//        }
//
//        override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
//            TODO("Not yet implemented")
//        }
//
//        override fun endStructure(descriptor: SerialDescriptor) {
//            TODO("Not yet implemented")
//        }
//    }
//
//    private inner class TopLevelEncoder(
//        private val outputs: MutableList<AttributeMap>,
//    ) : AbstractEncoder() {
//        override val serializersModule: SerializersModule get() = this@AttributeMapFormat.serializersModule
//
//        override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
//            return AttributeMapEncoder {
//                outputs.add(it)
//            }
//        }
//
//        override fun encodeValue(value: Any): Nothing {
//            error("Top-level encoding is not allowed. You can only call AttributeMapFormat.encodeToAttributeMap with a class")
//        }
//    }
//
//    fun <T> encodeToAttributeMap(
//        serializer: SerializationStrategy<T>,
//        value: T
//    ): Map<String, ColumnValue> {
//        
//        val outputs = mutableListOf<AttributeMap>()
//        val encoder = TopLevelEncoder(outputs)
//        serializer.serialize(encoder, value)
//        return outputs.singleOrNull() ?: error("Expected a single value")
//    }
//}

