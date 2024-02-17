package org.keizar.android.tutorial

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.keizar.utils.communication.game.BoardPos
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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
     * @param content The composable to be rendered in the message area.
     */
    suspend fun message(content: @Composable () -> Unit)

    /**
     * Suspends until the player has clicked the "Next" button.
     */
    suspend fun awaitNext()
}

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
