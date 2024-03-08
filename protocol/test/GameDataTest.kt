import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.keizar.utils.communication.game.*

class GameDataTest {

    @Test
    fun `test GameDataGet serialization and deserialization`() {
        val gameDataGet = GameDataGet(
            selfUsername = "user1",
            opponentUsername = "user2",
            timeStamp = "123456789",
            gameConfiguration = "config",
            round1Stats = RoundStats(
                neutralStats = NeutralStats(
                    whiteCaptured = 5,
                    blackCaptured = 3,
                    whiteAverageTime = 2.5,
                    whiteMoves = 10,
                    blackMoves = 8,
                    blackAverageTime = 2.0,
                    blackTime = 16,
                    whiteTime = 25
                ),
                player = Player.FirstBlackPlayer,
                winner = null
            ),
            round2Stats = RoundStats(
                neutralStats = NeutralStats(
                    whiteCaptured = 5,
                    blackCaptured = 3,
                    whiteAverageTime = 2.5,
                    whiteMoves = 10,
                    blackMoves = 8,
                    blackAverageTime = 2.0,
                    blackTime = 16,
                    whiteTime = 25
                ),
                player = Player.FirstBlackPlayer,
                winner = null
            ),
            dataId = "dataId"
        )

        val jsonString = Json.encodeToString(GameDataGet.serializer(), gameDataGet)
        val result = Json.decodeFromString(GameDataGet.serializer(), jsonString)

        assertEquals(gameDataGet, result)
    }

    @Test
    fun `test GameDataResponse serialization and deserialization`() {
        val gameDataResponse = GameDataResponse(success = true)

        val jsonString = Json.encodeToString(GameDataResponse.serializer(), gameDataResponse)
        val result = Json.decodeFromString(GameDataResponse.serializer(), jsonString)

        assertEquals(gameDataResponse, result)
    }

    @Test
    fun `test jsonElementToRoundStats`() {
        val roundStats = RoundStats(
            neutralStats = NeutralStats(
                whiteCaptured = 5,
                blackCaptured = 3,
                whiteAverageTime = 2.5,
                whiteMoves = 10,
                blackMoves = 8,
                blackAverageTime = 2.0,
                blackTime = 16,
                whiteTime = 25
            ),
            player = Player.FirstBlackPlayer,
            winner = null
        )

        val gameDataStore = GameDataStore(
            id = "id",
            round1Statistics = roundStats,
            round2Statistics = roundStats,
            gameConfiguration = "config",
            currentTimestamp = "123456789"
        )

        val gameDataId = GameDataId(id = "id")
        val gameDataList = GameDataList(gameData = listOf(GameDataGet(
            selfUsername = "user1",
            opponentUsername = "user2",
            timeStamp = "123456789",
            gameConfiguration = "config",
            round1Stats = roundStats,
            round2Stats = roundStats,
            dataId = "dataId"
        )))

        val jsonElement: JsonElement = Json.encodeToJsonElement(roundStats)
        val result = jsonElementToRoundStats(jsonElement)

        assertEquals(roundStats, result)
    }
}