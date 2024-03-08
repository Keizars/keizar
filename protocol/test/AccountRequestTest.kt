import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.keizar.utils.communication.account.*

class AccountRequestTest {

    @Test
    fun `test EditUserRequest serialization and deserialization`() {
        val editUserRequest = EditUserRequest(username = "newUsername", nickname = "newNickname")

        val jsonString = Json.encodeToString(EditUserRequest.serializer(), editUserRequest)
        val result = Json.decodeFromString(EditUserRequest.serializer(), jsonString)

        assertEquals(editUserRequest, result)
    }

    @Test
    fun `test EditUserResponse serialization and deserialization`() {
        val editUserResponse = EditUserResponse(success = true)

        val jsonString = Json.encodeToString(EditUserResponse.serializer(), editUserResponse)
        val result = Json.decodeFromString(EditUserResponse.serializer(), jsonString)

        assertEquals(editUserResponse, result)
    }

    @Test
    fun `test ChangePasswordRequest serialization and deserialization`() {
        val changePasswordRequest = ChangePasswordRequest(password = "newPassword")

        val jsonString = Json.encodeToString(ChangePasswordRequest.serializer(), changePasswordRequest)
        val result = Json.decodeFromString(ChangePasswordRequest.serializer(), jsonString)

        assertEquals(changePasswordRequest, result)
    }

    @Test
    fun `test ChangePasswordResponse serialization and deserialization`() {
        val changePasswordResponse = ChangePasswordResponse(success = true)

        val jsonString = Json.encodeToString(ChangePasswordResponse.serializer(), changePasswordResponse)
        val result = Json.decodeFromString(ChangePasswordResponse.serializer(), jsonString)

        assertEquals(changePasswordResponse, result)
    }
}