import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.keizar.game.BoardProperties
import org.keizar.game.protocol.PlayerInfo
import org.keizar.game.protocol.RoomInfo
import org.keizar.utils.communication.message.UserInfo
import kotlin.test.assertEquals

class ProtocolTest {

    @Test
    fun RoomInfoTest() = runTest {
        val roomNumber = 1u
        val properties = BoardProperties.getStandardProperties()
        val playerInfo = listOf(
            PlayerInfo(UserInfo("user1"), true, true),
            PlayerInfo(UserInfo("user2"), false, true),
        )
        val roomInfo = RoomInfo(roomNumber, properties, playerInfo)
        assertEquals(roomNumber, roomInfo.roomNumber)
    }
}