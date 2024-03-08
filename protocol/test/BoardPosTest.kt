import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromJsonElement
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.keizar.utils.communication.game.*

class BoardPosTest {

    @Test
    fun `test AsArraySerializer serialize and deserialize`() {
        val boardPos = BoardPos(3, 5)

        val json = Json { encodeDefaults = true }

        val serialized = json.encodeToJsonElement(BoardPos.AsArraySerializer, boardPos)
        val deserialized = json.decodeFromJsonElement(BoardPos.AsArraySerializer, serialized)

        assertEquals(boardPos, deserialized)
    }
}