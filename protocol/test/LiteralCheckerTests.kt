import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.keizar.utils.communication.*
import org.keizar.utils.communication.account.AuthStatus
import org.keizar.utils.communication.account.ModelConstraints

class LiteralCheckerTest {

    @Test
    fun `test check function`() {
        val string = "testString"
        val regexStr = "test.*"

        val result = LiteralChecker.check(string, regexStr)

        assertTrue(result)
    }

    @Test
    fun `test checkUsername function`() {
        val validUsername = "validUsername"
        val invalidUsername = "invalidUsername!"
        val tooLongUsername = "a".repeat(ModelConstraints.USERNAME_MAX_LENGTH + 1)

        val validResult = LiteralChecker.checkUsername(validUsername)
        val invalidResult = LiteralChecker.checkUsername(invalidUsername)

        assertEquals(AuthStatus.SUCCESS, validResult)
        assertEquals(AuthStatus.INVALID_USERNAME, invalidResult)
        assertEquals(AuthStatus.USERNAME_TOO_LONG, LiteralChecker.checkUsername(tooLongUsername))
    }

    @Test
    fun `test checkNickname function`() {
        val validNickname = "validNickname"
        val invalidNickname = "invalidNicknameaaaaaaaa"

        val validResult = LiteralChecker.checkNickname(validNickname)
        val invalidResult = LiteralChecker.checkNickname(invalidNickname)

        assertEquals(AuthStatus.SUCCESS, validResult)
        assertEquals(AuthStatus.NICKNAME_TOO_LONG, invalidResult)
    }
}