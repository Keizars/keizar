package org.keizar.android.ui.game

//import org.keizar.android.client.GameDataService
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import org.keizar.aiengine.AIParameters
import org.keizar.aiengine.AlgorithmAI
import org.keizar.android.BuildConfig
import org.keizar.android.data.SavedState
import org.keizar.android.data.SavedStateRepository
import org.keizar.android.data.SessionManager
import org.keizar.android.data.encode
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.ErrorMessage
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.foundation.launchInMain
import org.keizar.android.ui.foundation.runUntilSuccess
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.transition.BoardTransitionController
import org.keizar.android.ui.game.transition.CapturedPieceHostState
import org.keizar.android.ui.game.transition.PieceArranger
import org.keizar.client.ClientPlayer
import org.keizar.client.services.GameDataService
import org.keizar.client.services.SeedBankService
import org.keizar.client.services.UserService
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.game.Piece
import org.keizar.game.Role
import org.keizar.game.RoundSession
import org.keizar.utils.communication.account.User
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.Difficulty
import org.keizar.utils.communication.game.GameDataStore
import org.keizar.utils.communication.game.GameResult
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.game.RoundStats
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant

interface GameBoardViewModel : HasBackgroundScope {
    @Stable
    val startConfiguration: GameStartConfiguration

    @Stable
    val boardProperties: BoardProperties

    @Stable
    val pieceArranger: PieceArranger

    @Stable
    val myCapturedPieceHostState: CapturedPieceHostState

    @Stable
    val theirCapturedPieceHostState: CapturedPieceHostState

    @Stable
    val boardTransitionController: BoardTransitionController

    @Stable
    val selfRole: StateFlow<Role>

    @Stable
    val selfPlayer: Player

    /**
     * List of the pieces on the board.
     */
    @Stable
    val pieces: SharedFlow<List<UiPiece>>

    /**
     * Currently picked piece. `null` if no piece is picked.
     */
    @Stable
    val currentPick: StateFlow<Pick?>

    /**
     * Currently available **logical** positions where the picked piece can move to. `null` if no piece is picked.
     */
    @Stable
    val availablePositions: SharedFlow<List<BoardPos>?>

    /**
     * `true` if the last move is done by [onRelease].
     */
    @Stable
    val lastMoveIsDrag: MutableStateFlow<Boolean>

    @Stable
    val winningCounter: StateFlow<Int>

    @Stable
    val blackCapturedPieces: StateFlow<Int>

    @Stable
    val whiteCapturedPieces: StateFlow<Int>

    @Stable
    val winner: StateFlow<Role?>

    @Stable
    val finalWinner: StateFlow<GameResult?>

    @Stable
    val currentRound: SharedFlow<RoundSession>

    @Stable
    val currentRoundCount: StateFlow<Int>

    @Stable
    val round1Winner: StateFlow<Role?>

    @Stable
    val round2Winner: StateFlow<Role?>

    /**
     * `true` if the end of the first round is announced.
     */
    @Stable
    val endRoundOneAnnounced: MutableStateFlow<Boolean>

    /**
     * `true` if the end of the second round is announced.
     */
    @Stable
    val endRoundTwoAnnounced: MutableStateFlow<Boolean>

    @Stable
    val gameOverReadyToBeAnnounced: MutableStateFlow<Boolean>

    /**
     * `true` if the game over results are ready to be announced.
     */
    @Stable
    val showGameOverResults: MutableStateFlow<Boolean>

    /**
     * flag to indicate if the the undo button should be clickable
     */
    @Stable
    val canUndo: StateFlow<Boolean>

    @Stable
    val singlePlayerMode: Boolean

    @Stable
    val sessionManager: SessionManager

    @Stable
    var round1Statistics: Flow<RoundStats>

    @Stable
    var round2Statistics: Flow<RoundStats>

    @Stable
    val latestRoundStats: Flow<RoundStats>

    val currentGameDataId: StateFlow<String>


    // clicking

    /**
     * Called when the player single-clicks the piece.
     */
    fun onClickPiece(piece: UiPiece)

    /**
     * Called when the player single-clicks the empty tile.
     */
    fun onClickTile(logicalPos: BoardPos)


    // dragging

    /**
     * Called when the player long-presses the piece.
     */
    fun onHold(piece: UiPiece)

    /**
     * The offset of the piece from the point where the player stared holding the piece.
     *
     * If the player is not holding any piece, the flow emits [DpOffset.Zero].
     */
    val draggingOffset: StateFlow<DpOffset>

    /**
     * Add the given [offset] to the current [draggingOffset].
     *
     * This is called when the player drags the piece.
     */
    fun addDraggingOffset(offset: DpOffset)

    /**
     * Called when the player releases the piece after long-pressing it.
     */
    fun onRelease(piece: UiPiece)

    /**
     * Called when the the first round is finished by the players to start the next one.
     */
    fun startNextRound(selfPlayer: Player)

    /**
     * Get the count of pieces of the given [role] in [roundNo] round.
     */
    fun getRoundPieceCount(roundNo: Int, role: Role): StateFlow<Int>

    fun replayCurrentRound()

    fun replayGame()

    fun setEndRoundAnnouncement(flag: Boolean, setAll: Boolean = false)

    fun setGameOverReadyToBeAnnouncement(flag: Boolean)

    fun setShowGameOverResults(flag: Boolean)

    fun undo()

    suspend fun addSeed(seed: String)


    suspend fun removeSavedState() {}
}

@Composable
fun rememberSinglePlayerGameBoardViewModel(
    session: GameSession,
    selfPlayer: Player,
    difficulty: Difficulty = Difficulty.EASY,
): GameBoardViewModel {
    return remember(session, selfPlayer) {
        SinglePlayerGameBoardViewModel(session, selfPlayer, difficulty)
    }
}

/**
 * Can be [SinglePlayerGameBoardViewModel] or [MultiplayerGameBoardViewModel].
 */
sealed class PlayableGameBoardViewModel(
    game: GameSession,
    selfPlayer: Player,
) : BaseGameBoardViewModel(game, selfPlayer) {

    override val currentGameDataId: StateFlow<String> = MutableStateFlow("")
    suspend fun userSave(): Boolean {
        val gameDataService: GameDataService by inject()
        val result = viewModelScope.async(Dispatchers.IO) {
            gameDataService.userSaveData(currentGameDataId.value).success
        }
        return result.await()
    }

    suspend fun autoSave() {
        var opponentName: String? = "Computer"
        var userName = sessionManager.self.value?.username
        val gameDataService: GameDataService by inject()
        if (this is MultiplayerGameBoardViewModel) {
            opponentName = opponentUser.first().username
        }
        if (userName == null) {
            userName = "Unknown"
        }
        val gameData = GameDataStore(
            null,
            round1Statistics.first(),
            round2Statistics.first(),
            startConfiguration.encode(),
            userName,
            opponentName,
            Instant.now().toString(),
        )
        val id = gameDataService.autoSaveData(gameData)
        (currentGameDataId as MutableStateFlow).value = id.id
    }


}

class SinglePlayerGameBoardViewModel(
    game: GameSession,
    selfPlayer: Player,
    private val difficulty: Difficulty,
) : PlayableGameBoardViewModel(
    game,
    selfPlayer,
), KoinComponent {
    private val savedStateRepository: SavedStateRepository by inject()
//    private val GameDataService: GameDataService by inject()

    private val gameAi =
        when (difficulty) {
            Difficulty.EASY -> AlgorithmAI(
                game,
                Player.entries.first { it != selfPlayer },
                backgroundScope.coroutineContext,
                disableDelay = !BuildConfig.ENABLE_AI_DELAY,
                aiParameters = AIParameters(
                    keizarThreshold = 1,
                    possibleMovesThreshold = 4,
                    noveltyLevel = 0.6,
                    allowCaptureKeizarThreshold = 0.6
                )
            )

            else -> AlgorithmAI(
                game,
                Player.entries.first { it != selfPlayer },
                backgroundScope.coroutineContext,
                disableDelay = !BuildConfig.ENABLE_AI_DELAY,
            )
        }

    init {
        launchInBackground {
//            if (BuildConfig.ENABLE_AI_DELAY) {
//                delay(5.seconds) // Wait a few seconds before computer starts as white
//            }
            gameAi.start()
        }

        launchInBackground {
            game.currentRound.flatMapLatest { it.curRole }.distinctUntilChanged()
                .combine(game.finalWinner) { role, winner ->
                    role to winner
                }
                .filter { it.second == null }
                .collect { (_, _) ->
                    savedStateRepository.save(
                        SavedState.SinglePlayerGame(
                            startConfiguration,
                            game.getSnapshot()
                        )
                    )
                }
        }

        launchInBackground {
            game.finalWinner.distinctUntilChanged().collect {
                if (it != null) {
                    savedStateRepository.save(SavedState.Empty)
                }
            }
        }

        launchInBackground {
            game.finalWinner.distinctUntilChanged().collect {
                if (it != null) {
                    autoSave()
                }
            }
        }
    }

    override val startConfiguration: GameStartConfiguration
        get() = GameStartConfiguration(
            playAs = selfRole.value,
            difficulty = difficulty,
            layoutSeed = boardProperties.seed ?: 0,
        )

    override suspend fun removeSavedState() {
        savedStateRepository.save(SavedState.Empty)
    }
}

class MultiplayerGameBoardViewModel(
    game: GameSession,
    selfPlayer: Player,
    selfClientPlayer: ClientPlayer,
    private val opponentClientPlayer: StateFlow<ClientPlayer?>,
) : PlayableGameBoardViewModel(game, selfPlayer), KoinComponent {
    private val userService: UserService by inject()

    override val startConfiguration: GameStartConfiguration
        get() = GameStartConfiguration(
            playAs = selfRole.value,
            difficulty = Difficulty.EASY,
            layoutSeed = boardProperties.seed ?: 0,
        )

    override val singlePlayerMode = false
    val error = MutableStateFlow<ErrorMessage?>(null)

    @Stable
    val myUser: SharedFlow<User> = flow {
        emit(runUntilSuccess { userService.getUser(selfClientPlayer.username) })
    }.shareInBackground()

    @Stable
    val opponentUser: SharedFlow<User> = opponentClientPlayer.filterNotNull().map {
        runUntilSuccess { userService.getUser(it.username) }
    }.shareInBackground()

    init {
        launchInBackground {
            game.finalWinner.distinctUntilChanged().collect {
                if (it != null) {
                    autoSave()
                }
            }
        }
    }


    val isWaitingOpponentNext = MutableStateFlow(false)
    override fun startNextRound(selfPlayer: Player) {
        if (isWaitingOpponentNext.value) {
            return
        }
        logger.info { "startNextRound, selfPlayer = $selfPlayer" }
        // UNDISPATCHED is necessary here
        launchInMain(start = CoroutineStart.UNDISPATCHED) {
            isWaitingOpponentNext.value = true
            try {
                val curr = game.currentRoundNo.first()
                if (currentRoundCount.value != 1) {
                    game.currentRoundNo.filter { curr != it }.first()
                    boardTransitionController.turnBoard()
                }
            } finally {
                isWaitingOpponentNext.value = false
            }
        }
        launchInBackground(Dispatchers.IO) {
            try {
                game.confirmNextRound(selfPlayer)
            } catch (e: Exception) {
                logger.error(e) { "Failed to confirm next round" }
                error.value = ErrorMessage.networkError()
            }
        }
    }
}

@Suppress("LeakingThis")
abstract class BaseGameBoardViewModel(
    protected val game: GameSession,
    @Stable override val selfPlayer: Player,
) : AbstractViewModel(), GameBoardViewModel, KoinComponent {

    open var arePiecesClickable: Boolean = true

    override val boardProperties = game.properties

    @Stable
    override val selfRole: StateFlow<Role> = game.currentRole(selfPlayer)

    @Stable
    override val pieceArranger = PieceArranger(
        boardProperties = boardProperties,
        viewedAs = selfRole
    )

    override val myCapturedPieceHostState: CapturedPieceHostState = CapturedPieceHostState()
    override val theirCapturedPieceHostState: CapturedPieceHostState = CapturedPieceHostState()

    @Stable
    override val boardTransitionController = BoardTransitionController(
        initialPlayAs = selfRole.value,
        playAs = selfRole,
        backgroundScope = backgroundScope,
        myCapturedPieceHostState = myCapturedPieceHostState,
        theirCapturedPieceHostState = theirCapturedPieceHostState,
        onTransitionFinished = {
            myCapturedPieceHostState.clear()
            theirCapturedPieceHostState.clear()
        }
    )

    @Stable
    override val pieces: SharedFlow<List<UiPiece>> =
        game.currentRound.map { it.pieces }.map { list ->
            list.map {
                UiPiece(
                    enginePiece = it,
                    offsetInBoard = boardTransitionController.pieceOffset(
                        it,
                        pieceArranger.offsetFor(it.pos)
                    ),
                    backgroundScope,
                )
            }
        }.shareInBackground()

    @Stable
    override val currentPick: MutableStateFlow<Pick?> = MutableStateFlow(null)

    @Stable
    private val currentRole: StateFlow<Role> = selfRole
//        game.currentRound.flatMapLatest { it.curRole }.stateInBackground(Role.WHITE)

    @Stable
    override val winningCounter: StateFlow<Int> = game.currentRound
        .flatMapLatest { it.winningCounter }
        .stateInBackground(0)

    @Stable
    override val blackCapturedPieces: StateFlow<Int> =
        game.currentRound.flatMapLatest { it.getLostPiecesCount(Role.BLACK) }.stateInBackground(0)

    @Stable
    override val whiteCapturedPieces: StateFlow<Int> =
        game.currentRound.flatMapLatest { it.getLostPiecesCount(Role.WHITE) }.stateInBackground(0)


    @Stable
    override val winner: StateFlow<Role?> = game.currentRound.flatMapLatest { it.winner }
        .stateInBackground(null)


    @Stable
    override val finalWinner: StateFlow<GameResult?> = game.finalWinner.stateInBackground(null)

    @Stable
    override val currentRound: SharedFlow<RoundSession> = game.currentRound
        .onEach {
            logger.info { "[game] currentRound: $it" }
        }
        .shareInBackground()

    @Stable
    override val currentRoundCount: StateFlow<Int> = game.currentRoundNo

    @Stable
    override val round1Winner: StateFlow<Role?> = game.rounds[0].winner

    @Stable
    override val round2Winner: StateFlow<Role?> = game.rounds[1].winner

    private val _endRoundOneAnnounced = MutableStateFlow(false)

    private val _endRoundTwoAnnounced = MutableStateFlow(false)

    @Stable
    override val endRoundOneAnnounced = _endRoundOneAnnounced

    @Stable
    override val endRoundTwoAnnounced = _endRoundTwoAnnounced

    private val _gameOverReadyToBeAnnounced = MutableStateFlow(false)

    @Stable
    override val gameOverReadyToBeAnnounced = _gameOverReadyToBeAnnounced

    private val _showGameOverResults = MutableStateFlow(false)

    @Stable
    override val showGameOverResults = _showGameOverResults

    @Stable
    override val singlePlayerMode = true

    @Stable
    override val sessionManager: SessionManager by inject()

    override var round1Statistics: Flow<RoundStats> = game.getRoundStats(0, selfPlayer)

    override var round2Statistics: Flow<RoundStats> = game.getRoundStats(1, selfPlayer)

    override val latestRoundStats: Flow<RoundStats>
        get() {
            val stats = game.currentRoundNo.flatMapLatest { game.getRoundStats(it, selfPlayer) }
            return stats
        }

    @Stable
    override val availablePositions: SharedFlow<List<BoardPos>?> =
        game.currentRound.flatMapLatest { turn ->
            currentPick.flatMapLatest { pick ->
                if (pick == null) {
                    flowOf(emptyList())
                } else {
                    flowOf(turn.getAvailableTargets(pieceArranger.viewToLogical(pick.viewPos).first()))
                }
            }.map { list ->
                list
            }
        }.shareInBackground()

    override val lastMoveIsDrag: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val canUndo: StateFlow<Boolean> =
        game.currentRound.flatMapLatest { it.canUndo }.stateInBackground(false)

    private val seedBankService: SeedBankService by inject()

    override suspend fun addSeed(seed: String) {
        seedBankService.addSeed(seed)
    }

    init {
        backgroundScope.launch {
            winner.collect { winner ->
                if (winner != null) {
                    boardTransitionController.flashWiningPiece()
                }
            }
        }
    }

    override fun onClickPiece(piece: UiPiece) {
        if (!arePiecesClickable) return

        val currentPick = currentPick.value
        if (currentPick == null) {
            if (piece.role != selfRole.value) return
            launchInBackground(start = CoroutineStart.UNDISPATCHED) {
                startPick(piece)
            }
        } else {
            // Pick another piece
            completePick(isDrag = false)
            launchInBackground(start = CoroutineStart.UNDISPATCHED) {
                movePiece(currentPick.logicalPos, piece.pos.value)
            }
            return
        }
    }

    override fun onClickTile(logicalPos: BoardPos) {
        if (!arePiecesClickable) return

        val pick = currentPick.value ?: return
        completePick(isDrag = false)
        launchInBackground(start = CoroutineStart.UNDISPATCHED) {
            movePiece(pick.logicalPos, logicalPos)
        }
    }

    override val draggingOffset = MutableStateFlow(DpOffset.Zero)

    override fun onHold(piece: UiPiece) {
        if (piece.role != currentRole.value) return
        launchInBackground(start = CoroutineStart.UNDISPATCHED) {
            startPick(piece)
        }
    }


    override fun addDraggingOffset(offset: DpOffset) {
        this.draggingOffset.value += offset
    }

    override fun onRelease(piece: UiPiece) {
        val currentPick = currentPick.value ?: return
        if (currentPick.piece != piece) return

        val dragOffset = draggingOffset.value

        piece.hide() // hide it now to avoid flickering
        launchInBackground(start = CoroutineStart.UNDISPATCHED) {
            try {
                movePiece(
                    currentPick.logicalPos,
                    pieceArranger.getNearestPos(dragOffset, from = piece.pos.value).first()
                )
                completePick(isDrag = true)
            } finally {
                piece.cancelHide()
            }
        }
        return
    }


    suspend fun startPick(logicalPos: BoardPos) {
        val piece = pieces.first().firstOrNull { it.pos.value == logicalPos }
            ?: error("No piece at $logicalPos")
        startPick(piece)
    }

    private suspend fun startPick(piece: UiPiece) {
        this.currentPick.value = Pick(piece, pieceArranger.logicalToView(piece.pos.value).first())
    }

    fun completePick(isDrag: Boolean) {
        this.currentPick.value = null
        lastMoveIsDrag.value = isDrag
        draggingOffset.value = DpOffset.Zero
    }

    private suspend fun movePiece(from: BoardPos, to: BoardPos) {
        game.currentRound.first().move(from, to).also {
            logger.info { "[board] move $from to $to: $it" }
        }
    }

    override fun startNextRound(selfPlayer: Player) {
        logger.info { "startNextRound, selfPlayer = $selfPlayer" }
        launchInMain(start = CoroutineStart.UNDISPATCHED) {
            if (currentRoundCount.value != 1) {
                boardTransitionController.turnBoard()
            }
        }
        launchInBackground(Dispatchers.IO) {
            game.confirmNextRound(selfPlayer)
        }
    }

    override fun getRoundPieceCount(roundNo: Int, role: Role): StateFlow<Int> {
        return game.rounds[roundNo].getLostPiecesCount(role)
    }

    override fun replayCurrentRound() {
        setEndRoundAnnouncement(false)
        game.replayCurrentRound()
        myCapturedPieceHostState.clear()
        theirCapturedPieceHostState.clear()
    }

    override fun replayGame() {
        setEndRoundAnnouncement(false, setAll = true)
        launchInMain(start = CoroutineStart.UNDISPATCHED) {
            boardTransitionController.turnBoard()
        }
        game.replayGame()
    }

    override fun setEndRoundAnnouncement(flag: Boolean, setAll: Boolean) {
        if (setAll) {
            _endRoundOneAnnounced.value = flag
            _endRoundTwoAnnounced.value = flag
        } else {
            if (currentRoundCount.value == 0) {
                _endRoundOneAnnounced.value = flag
            } else {
                _endRoundTwoAnnounced.value = flag
            }
        }
    }


    override fun setGameOverReadyToBeAnnouncement(flag: Boolean) {
        _gameOverReadyToBeAnnounced.value = flag
    }

    override fun setShowGameOverResults(flag: Boolean) {
        _showGameOverResults.value = flag
    }

    override fun undo() {
        viewModelScope.launch {
            currentRound.collect { roundSession ->
                roundSession.undo(selfRole.value)
            }
        }
    }


}

@Immutable
class Pick(
    val piece: UiPiece,
    val viewPos: BoardPos
)

val Pick.logicalPos: BoardPos
    get() = piece.pos.value


/**
 * A wrapper of [Piece] that is aware of the UI states.
 */
@Stable
class UiPiece internal constructor(
    private val enginePiece: Piece,
    /**
     * The offset of the piece on the board, starting from the top-left corner.
     */
    val offsetInBoard: Flow<DpOffset>,
    override val backgroundScope: CoroutineScope,
) : Piece by enginePiece, HasBackgroundScope {

    private val _overrideVisible = MutableStateFlow<Boolean?>(null)
    val isVisible = combine(_overrideVisible, isCaptured) { override, isCaptured ->
        override ?: true //!isCaptured
    }.shareInBackground()

    fun hide() {
        _overrideVisible.value = false
    }

    fun cancelHide() {
        _overrideVisible.value = null
    }
}