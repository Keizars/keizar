package org.keizar.android.ui.game.transition

import androidx.annotation.UiThread
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.keizar.game.Piece
import org.keizar.game.Role
import kotlin.time.Duration.Companion.milliseconds

/**
 * Controls the transition of the board, including:
 * - piece movement animation
 * - winning piece animation
 * - turning board animation
 */
interface BoardTransitionController {
    //    /**
//     * Alpha value for the tiles that are viewed by the black player.
//     */
//    val blackViewedTilesAlpha: Float
//        @Composable get
//
//    /**
//     * Alpha value for the tiles that are viewed by the white player.
//     */
//    val whiteViewedTilesAlpha: Float
//        @Composable get
//
    val winningPieceAlpha: Float
        @Composable get

    val boardBackgroundRotation: Float
        @Composable get

    val pieceMovementAnimationSpec: AnimationSpec<Dp>
        @Composable get


    /**
     * Maps the piece offset to support delayed transition when turning the board.
     */
    fun pieceOffset(piece: Piece, arrangedPos: Flow<DpOffset>): Flow<DpOffset>

    val isPlayingTransition: StateFlow<Boolean>

    /**
     * Flash the winning piece.
     *
     * Suspends until the flash animation is complete.
     */
    @UiThread
    suspend fun flashWiningPiece()

    /**
     * Turn the board. This includes the steps:
     * 1. Animate the tiles. [boardBackgroundRotation] will change during this step.
     * 2. Animate the pieces. Pieces will move slowly to their new positions.
     *
     * This function must be called before the piece offsets are changed.
     */
    @UiThread
    suspend fun turnBoard()
}

fun BoardTransitionController(
    initialPlayAs: Role,
    playAs: Flow<Role>,
    backgroundScope: CoroutineScope,
    theirCapturedPieceHostState: CapturedPieceHostState,
    myCapturedPieceHostState: CapturedPieceHostState,
    onTransitionFinished: @DisallowComposableCalls () -> Unit = {},
): BoardTransitionController = BoardTransitionControllerImpl(
    initialPlayAs,
    playAs,
    backgroundScope,
    theirCapturedPieceHostState,
    myCapturedPieceHostState,
    onTransitionFinished
)

private class BoardTransitionControllerImpl(
    initialPlayAs: Role,
    private val playAs: Flow<Role>,
    override val backgroundScope: CoroutineScope,
    private val theirCapturedPieceHostState: CapturedPieceHostState,
    private val myCapturedPieceHostState: CapturedPieceHostState,
    private val onTransitionFinished: @DisallowComposableCalls () -> Unit,
) : BoardTransitionController, HasBackgroundScope {
    companion object {
        private const val TILES_ANIMATION_DURATION = 1000
        private const val PIECES_ANIMATION_DURATION = 1500

        private val MOVEMENT_FAST = tween<Dp>(500)
        private val MOVEMENT_SLOW = tween<Dp>(PIECES_ANIMATION_DURATION)
    }

//    @Stable
//    private val _whiteViewedTilesAlpha = mutableFloatStateOf(if (initialPlayAs == Role.WHITE) 1f else 0f)
//
//    override val blackViewedTilesAlpha: Float
//        @Composable get() {
//            return animateFloatAsState(
//                targetValue = 1 - _whiteViewedTilesAlpha.floatValue,
//                animationSpec = tween(TILES_ANIMATION_DURATION),
//                label = "blackViewedTilesAlpha",
//            ).value
//        }
//
//    override val whiteViewedTilesAlpha: Float
//        @Composable get() {
//            return animateFloatAsState(
//                targetValue = _whiteViewedTilesAlpha.floatValue,
//                animationSpec = tween(TILES_ANIMATION_DURATION),
//                label = "whiteViewedTilesAlpha",
//            ).value
//        }

    @Stable
    private val _flashingWinningPiece = mutableStateOf(true)

    override val winningPieceAlpha: Float
        @Composable get() {
            val alpha by animateFloatAsState(
                targetValue = if (_flashingWinningPiece.value) 1f else 0f,
                animationSpec = tween(500, easing = LinearEasing),
                label = "winningPieceAlpha",
            )
            return alpha
        }

    @Stable
    private val _boardBackgroundRotation = mutableFloatStateOf(if (initialPlayAs == Role.WHITE) 0f else 180f)

    private var boardBackgroundRotationFinished = CompletableDeferred(true)

    override val boardBackgroundRotation: Float
        @Composable get() {
            return animateFloatAsState(
                targetValue = _boardBackgroundRotation.floatValue,
                animationSpec = tween(TILES_ANIMATION_DURATION, easing = LinearOutSlowInEasing),
                label = "boardBackgroundRotation",
                finishedListener = {
                    boardBackgroundRotationFinished.complete(true)
                }
            ).value
        }

    @Stable
    private val _pieceMovementAnimationSpec: MutableState<AnimationSpec<Dp>> = mutableStateOf(MOVEMENT_FAST)

    override val pieceMovementAnimationSpec: AnimationSpec<Dp>
        @Composable
        get() = _pieceMovementAnimationSpec.value

    @Stable
    private val _isTurningBoard = mutableStateOf(false)

    override fun pieceOffset(piece: Piece, arrangedPos: Flow<DpOffset>): Flow<DpOffset> {
        return combine(arrangedPos, piece.isCaptured, playAs) { pos, isCaptured, playAs ->
            if (isCaptured) {
                getCapturedPieceHostState(piece.role, playAs).capture(piece.index)
                    .offsetFromBoard
            } else {
                pos
            }
        }.mapLatest {
            if (_isTurningBoard.value) {
                boardBackgroundRotationFinished.runCatching { await() }
            }
            it
        }
    }

    override val isPlayingTransition: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private fun getCapturedPieceHostState(pieceBeingCaptured: Role, myRole: Role): CapturedPieceHostState {
        if (pieceBeingCaptured == myRole) return theirCapturedPieceHostState
        return myCapturedPieceHostState
    }

    private inline fun <R> playTransition(block: () -> R): R {
        try {
            isPlayingTransition.value = true
            return block()
        } finally {
            isPlayingTransition.value = false
        }
    }

    override suspend fun flashWiningPiece() = playTransition {
        repeat(3) {
            this._flashingWinningPiece.value = false
            delay(600)
            this._flashingWinningPiece.value = true
            delay(600)
        }
    }

//    init {
//        playAs.mapLatest {
//            _whiteViewedTilesAlpha.floatValue = if (it == Role.WHITE) 1f else 0f
//            delay(TILES_ANIMATION_DURATION.milliseconds)
//        }.flowOn(Dispatchers.Main).launchIn(backgroundScope)
//    }

    override suspend fun turnBoard() = playTransition {
        _isTurningBoard.value = true
        boardBackgroundRotationFinished = CompletableDeferred()
        _pieceMovementAnimationSpec.value = MOVEMENT_SLOW
        _boardBackgroundRotation.floatValue = (_boardBackgroundRotation.floatValue + 180f) % 360
        awaitFrame()
        delay(
            TILES_ANIMATION_DURATION.milliseconds
                .plus(PIECES_ANIMATION_DURATION.milliseconds)
        )
        _isTurningBoard.value = false
        _pieceMovementAnimationSpec.value = MOVEMENT_FAST
        onTransitionFinished()
    }
}
