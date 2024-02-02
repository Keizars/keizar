package org.keizar.android.ui.game.transition

import androidx.annotation.UiThread
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.keizar.game.Role
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
    fun pieceOffset(original: Flow<DpOffset>): Flow<DpOffset>

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
//    playAs: Flow<Role>,
    backgroundScope: CoroutineScope,
): BoardTransitionController = BoardTransitionControllerImpl(
    initialPlayAs,
    backgroundScope,
)

private class BoardTransitionControllerImpl(
    initialPlayAs: Role,
//    playAs: Flow<Role>,
    override val backgroundScope: CoroutineScope,
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
    private val _winningPieceAlpha = mutableFloatStateOf(1f)

    override val winningPieceAlpha: Float
        @Composable get() {
            return animateFloatAsState(
                targetValue = _winningPieceAlpha.floatValue,
                animationSpec = repeatable(3, tween(1000)),
                label = "winningPieceAlpha",
            ).value
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

    override fun pieceOffset(original: Flow<DpOffset>): Flow<DpOffset> {
        return original.mapLatest {
            if (_isTurningBoard.value) {
                boardBackgroundRotationFinished.runCatching { await() }
            }
            it
        }
    }

    override suspend fun flashWiningPiece() {
        _winningPieceAlpha.floatValue = 0f
        awaitFrame()
        _winningPieceAlpha.floatValue = 1f
        delay(3.seconds)
    }

//    init {
//        playAs.mapLatest {
//            _whiteViewedTilesAlpha.floatValue = if (it == Role.WHITE) 1f else 0f
//            delay(TILES_ANIMATION_DURATION.milliseconds)
//        }.flowOn(Dispatchers.Main).launchIn(backgroundScope)
//    }

    override suspend fun turnBoard() {
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
    }
}
