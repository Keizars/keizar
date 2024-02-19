package org.keizar.android.tutorial

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.keizar.utils.communication.game.BoardPos
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * The context in which a step action is executed.
 *
 * See [Step] for revoking the step.
 *
 * @see Step
 */
interface StepActionContext {
    /**
     * Delays the step for a given [duration].
     */
    suspend fun delay(duration: Duration)

    suspend fun showPossibleMoves(pos: BoardPos, duration: Duration = 1.5.seconds)

    /**
     * Move the player's piece from one position to another.
     *
     * Suspends until the move and relevant animations are complete.
     */
    suspend fun movePlayer(from: BoardPos, to: BoardPos)

    /**
     * Move the opponent's piece from one position to another.
     *
     * Suspends until the move and relevant animations are complete.
     */
    suspend fun moveOpponent(from: BoardPos, to: BoardPos)

    /**
     * Requests the player to move a piece from one position to another.
     *
     * Suspends until the player has made the move correctly.
     * If the player makes an invalid move, tutorial engine will ask the player to try again.
     */
    suspend fun requestMovePlayer(from: BoardPos, to: BoardPos)

    /**
     * Update the message displayed to the user.
     *
     * @param content The composable to be rendered in the board actions area, to the left of the "Next" button.
     */
    suspend fun message(content: @Composable () -> Unit)

    /**
     * Composes a composable to the UI.
     *
     * The composable is displayed in the board actions area.
     * You should only use bottom sheets, dialogs, navigation in the [compose].
     * If you want to display a message, use [message].
     */
    suspend fun compose(content: @Composable (request: TutorialRequest.CompletableTutorialRequest<Unit>) -> Unit)

    /**
     * Suspends until the player has clicked the "Next" button.
     */
    suspend fun awaitNext()
}

/**
 * Shows a bottom sheet with the given [content].
 *
 * Suspend until the bottom sheet is dismissed.
 */
suspend fun StepActionContext.showBottomSheet(
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    return compose { request ->
        ModalBottomSheet(onDismissRequest = { request.respond() }) {
            Column(Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}

suspend inline fun StepActionContext.showPossibleMoves(pos: String, duration: Duration = 3.seconds) =
    this.showPossibleMoves(BoardPos(pos), duration)

/**
 * Update the message displayed to the user.
 */
suspend inline fun StepActionContext.message(message: String) {
    message {
        Text(text = message)
    }
}

/**
 * @see StepActionContext.delay
 */
suspend inline fun StepActionContext.delay(millis: Long) = delay(millis.milliseconds)

/**
 * @see StepActionContext.requestMovePlayer
 */
suspend inline fun StepActionContext.requestMovePlayer(moveBuilder: MoveBuilder.() -> Unit) {
    val builder = MoveBuilder().apply(moveBuilder)
    val (from, to) = builder.build()
    requestMovePlayer(from, to)
}

/**
 * @see StepActionContext.movePlayer
 */
suspend inline fun StepActionContext.movePlayer(moveBuilder: MoveBuilder.() -> Unit) {
    val builder = MoveBuilder().apply(moveBuilder)
    val (from, to) = builder.build()
    movePlayer(from, to)
}

/**
 * @see StepActionContext.moveOpponent
 */
suspend inline fun StepActionContext.moveOpponent(moveBuilder: MoveBuilder.() -> Unit) {
    val builder = MoveBuilder().apply(moveBuilder)
    val (from, to) = builder.build()
    moveOpponent(from, to)
}

