package org.keizar.android.ui.tutorial

import org.keizar.game.snapshot.GameSnapshot
import org.keizar.game.snapshot.GameSnapshotBuilder
import org.keizar.utils.communication.game.BoardPos
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A template of a steps that the user can follow.
 */
class Tutorial(
    val initialGameSnapshot: GameSnapshot,
    val steps: List<Step> = emptyList()
)

sealed class Step

/**
 * Request the user to move a piece from [from] to [to].
 */
class RequestMove(
    val from: BoardPos,
    val to: BoardPos,
) : Step()

/**
 * Automatically move a piece from [from] to [to].
 */
class AutoMove(
    val from: BoardPos,
    val to: BoardPos,
) : Step()

/**
 * Sets the message to be displayed for this step.
 */
class SetMessage(
    val message: String,
) : Step()

class Delay(
    val duration: Duration,
) : Step()


inline fun buildTutorial(
    action: TutorialBuilder.() -> Unit
): Tutorial {
    return TutorialBuilder().apply(action).build()
}


class MoveBuilder {
    @PublishedApi
    internal var from: BoardPos? = null

    @PublishedApi
    internal var to: BoardPos? = null

    fun from(str: String) {
        from = BoardPos.fromString(str)
    }

    fun to(str: String) {
        to = BoardPos.fromString(str)
    }

    fun from(pos: BoardPos) {
        from = pos
    }

    fun to(pos: BoardPos) {
        to = pos
    }

    fun build(): Pair<BoardPos, BoardPos> {
        return Pair(
            from ?: error("from is not set"),
            to ?: error("to is not set")
        )
    }
}

class TutorialBuilder(
) {
    @PublishedApi
    internal val snapshotBuilder: GameSnapshotBuilder = GameSnapshotBuilder()

    @PublishedApi
    internal var steps: MutableList<Step> = mutableListOf()

    inner class StepsBuilder {
        fun requestMove(from: String, to: String) {
            steps.add(RequestMove(BoardPos(from), BoardPos(to)))
        }

        fun requestMove(from: BoardPos, to: BoardPos) {
            steps.add(RequestMove(from, to))
        }

        fun requestMove(moveBuilder: MoveBuilder.() -> Unit) {
            val move = MoveBuilder().apply(moveBuilder).build()
            steps.add(RequestMove(move.first, move.second))

        }

        fun delay(duration: Duration) {
            steps.add(Delay(duration))
        }

        fun delay(millis: Long) {
            steps.add(Delay(millis.milliseconds))
        }

        fun autoMove(from: BoardPos, to: BoardPos) {
            steps.add(AutoMove(from, to))
        }

        fun message(message: String) {
            steps.add(SetMessage(message))
        }
    }

    /**
     * Configures the board
     */
    inline fun board(
        builderAction: GameSnapshotBuilder.() -> Unit
    ) {
        snapshotBuilder.apply(builderAction)
    }

    inline fun steps(builderAction: StepsBuilder.() -> Unit) {
        StepsBuilder().apply(builderAction)
    }

    fun build(): Tutorial {
        return Tutorial(
            initialGameSnapshot = snapshotBuilder.build(),
            steps = steps,
        )
    }
}


private fun test() {
    buildTutorial {
        board {
            tiles {

            }
            round {

            }
        }

        steps {
            requestMove(BoardPos(0, 0), BoardPos(0, 1))
            delay(Duration.ZERO)
            autoMove(BoardPos(0, 1), BoardPos(0, 2))
            message("Hello, world!")
        }
    }
}