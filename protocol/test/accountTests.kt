import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.keizar.utils.communication.account.*

class AuthRequestTest {

    @Test
    fun `test UsernameValidityResponse serialization and deserialization`() {
        val usernameValidityResponse = UsernameValidityResponse(validity = true)

        val jsonString = Json.encodeToString(UsernameValidityResponse.serializer(), usernameValidityResponse)
        val result = Json.decodeFromString(UsernameValidityResponse.serializer(), jsonString)
        
    }

    @Test
    fun `test ImageUrlExchange serialization and deserialization`() {
        val imageUrlExchange = ImageUrlExchange(url = "https://example.com/image.png")

        val jsonString = Json.encodeToString(ImageUrlExchange.serializer(), imageUrlExchange)
        val result = Json.decodeFromString(ImageUrlExchange.serializer(), jsonString)

    }
}