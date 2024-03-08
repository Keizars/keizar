import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.keizar.utils.communication.game.GameResult
import org.keizar.utils.communication.game.Player

class GameResultTest {

    @Test
    fun `test values function`() {
        val values = GameResult.values()

        assertTrue(values.contains(GameResult.Draw))
        assertTrue(values.contains(GameResult.Winner(Player.FirstWhitePlayer)))
        assertTrue(values.contains(GameResult.Winner(Player.FirstBlackPlayer)))
    }

    @Test
    fun `test valueOf function`() {
        assertEquals(GameResult.Draw, GameResult.valueOf("DRAW"))
        assertEquals(GameResult.Winner(Player.FirstWhitePlayer), GameResult.valueOf("WINNER1"))
        assertEquals(GameResult.Winner(Player.FirstBlackPlayer), GameResult.valueOf("WINNER2"))
    }

    @Test
    fun `test valueOf function with invalid input`() {
        assertThrows<IllegalArgumentException> {
            GameResult.valueOf("INVALID")
        }
    }
}