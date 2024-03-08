import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.keizar.game.BoardProperties
import org.keizar.game.BoardPropertiesBuilder
import org.keizar.game.BoardPropertiesPrototypes
import org.keizar.game.Role
import org.keizar.game.TileType
import org.keizar.utils.communication.game.BoardPos
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoardPropertiesBuilderTest {
    @Test
    fun `test BoardPropertiesBuilder with plain tiles`() = runTest {
        val boardProperties = BoardPropertiesBuilder(BoardPropertiesPrototypes.Plain) {
            width { 10 }
            height { 10 }
            keizarTilePos { BoardPos("d1") }
            winningCount { 5 }
            startingRole { Role.BLACK }
            roundsCount { 10 }
            piecesStartingPos { mutableMapOf() }
            tiles {
                plain()
            }
        }.build()
        assertEquals(10, boardProperties.width)
        assertEquals(10, boardProperties.height)
        assertEquals(BoardPos("d1"), boardProperties.keizarTilePos)
        assertEquals(5, boardProperties.winningCount)
        assertEquals(Role.BLACK, boardProperties.startingRole)
        assertEquals(10, boardProperties.rounds)
        assertEquals(mutableMapOf(), boardProperties.piecesStartingPos)
        assertTrue(boardProperties.tileArrangement.values.all { it == TileType.PLAIN || it == TileType.KEIZAR })
    }

    @Test
    fun `test BoardPropertiesBuilder with standard tiles`() = runTest {
        val boardProperties = BoardPropertiesBuilder(BoardPropertiesPrototypes.Plain) {
            width { 10 }
            height { 10 }
            keizarTilePos { BoardPos("d1") }
            winningCount { 5 }
            startingRole { Role.BLACK }
            roundsCount { 10 }
            piecesStartingPos { mutableMapOf() }
            tiles {
                standard { 0 }
            }
        }.build()
        assertEquals(10, boardProperties.width)
        assertEquals(10, boardProperties.height)
        assertEquals(BoardPos("d1"), boardProperties.keizarTilePos)
        assertEquals(5, boardProperties.winningCount)
        assertEquals(Role.BLACK, boardProperties.startingRole)
        assertEquals(10, boardProperties.rounds)
        assertEquals(mutableMapOf(), boardProperties.piecesStartingPos)
        assertEquals(
            BoardProperties.getStandardProperties(0).tileArrangement,
            boardProperties.tileArrangement
        )
    }

    @Test
    fun `test BoardPropertiesBuilder with tiles factory`() = runTest {
        val boardProperties = BoardPropertiesBuilder(BoardPropertiesPrototypes.Plain) {
            width { 10 }
            height { 10 }
            keizarTilePos { BoardPos("d1") }
            winningCount { 5 }
            startingRole { Role.BLACK }
            roundsCount { 10 }
            piecesStartingPos { mutableMapOf() }
            tiles {
                factory {
                    fillWith(TileType.KING) { BoardPos.range("a1" to "h8") }
                }
            }
        }.build()
        assertEquals(10, boardProperties.width)
        assertEquals(10, boardProperties.height)
        assertEquals(BoardPos("d1"), boardProperties.keizarTilePos)
        assertEquals(5, boardProperties.winningCount)
        assertEquals(Role.BLACK, boardProperties.startingRole)
        assertEquals(10, boardProperties.rounds)
        assertEquals(mutableMapOf(), boardProperties.piecesStartingPos)
        assertTrue(boardProperties.tileArrangement.values.all { it == TileType.KING })
    }
}