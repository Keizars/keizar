package org.keizar.android.tutorial

import org.keizar.game.snapshot.GameSnapshotBuilder
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.Player

/**
 * Builds a tutorial.
 *
 * - See [Tutorial] for more information about what is a tutorial.
 *
 * @see Tutorial
 * @sample samples.TutorialSamples.buildTutorialComplete
 */
inline fun buildTutorial(
    id: String,
    action: TutorialBuilder.() -> Unit,
): Tutorial = TutorialBuilder(id).apply(action).build()

/**
 * @see buildTutorial
 */
@TutorialDslMarker
class TutorialBuilder(
    private val id: String,
    private var player: Player = Player.FirstWhitePlayer,
) {
    @PublishedApi
    internal val snapshotBuilder: GameSnapshotBuilder = GameSnapshotBuilder()

    val steps = StepsBuilder()

    @TutorialDslMarker
    fun playerStartAsWhite() {
        player = Player.FirstWhitePlayer
    }

    @TutorialDslMarker
    fun playerStartAsBlack() {
        player = Player.FirstBlackPlayer
    }

    /**
     * Modifies the board.
     */
    @TutorialDslMarker
    inline fun board(
        builderAction: (@TutorialDslMarker GameSnapshotBuilder).() -> Unit,
    ) {
        snapshotBuilder.apply(builderAction)
    }

    /**
     * Modifies the steps.
     */
    @TutorialDslMarker
    inline fun steps(execution: StepsBuilder.() -> Unit) {
        steps.apply(execution)
    }

    @TutorialDslMarker
    inner class StepsBuilder {
        @PublishedApi
        internal val list: MutableList<Step> = mutableListOf()

        /**
         * Adds a savepoint.
         *
         * [onRestored] is only invoked when the player clicks "Back", i.e. when the tutorial session goes back to the last savepoint.
         */
        @TutorialDslMarker
        fun savepoint(name: String, onRestored: StepAction = { awaitNext() }): Savepoint {
            val save = Savepoint(name, onRestored)
            list.add(save)
            return save
        }

        /**
         * Adds a step.
         *
         * Steps are executed in the order they are added.
         *
         * @param name Name for debugging purposes. Not necessarily (but recommended) to be unique.
         * @param savepoint `true` to create a savepoint, with which the player can go "Back" to this step.
         * @param awaitNext `true` to add a step that awaits the player to click "Next".
         */
        @TutorialDslMarker
        fun step(
            name: String,
            savepoint: Boolean = true,
            awaitNext: Boolean = true,
            action: StepAction = {}
        ): Step {
            val step = Action(name, action)
            if (savepoint) {
                list.add(Savepoint("$name-savepoint") { awaitNext() })
            }
            list.add(step)
            if (awaitNext) {
                list.add(Action("$name-awaitNext") { awaitNext() })
            }
            return step
        }

        /**
         * Adds an action to the executed just after this step has been successfully executed.
         *
         * This is useful to add a message after a move has been requested, without specifying the name of the step.
         *
         * @sample samples.TutorialSamples.stepThen
         * @see step
         */
        @TutorialDslMarker
        fun Step.then(
            name: String = this.name + "-then",
            awaitNext: Boolean = true,
            execution: StepAction,
        ): Step {
            list.removeIf { it.name == "${this.name}-awaitNext" } // a new one will be added by 
            return step(name, savepoint = false, awaitNext = awaitNext, execution)
        }

        internal fun toList() = list.toList()
    }

    fun build(): Tutorial {
        return Tutorial(
            id = id,
            initialGameSnapshot = snapshotBuilder.build(),
            player = player,
            steps = steps.toList(),
        )
    }
}

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class TutorialDslMarker

/**
 * Builds a move.
 *
 * @sample samples.MoveBuilderSamples.specifySeparately
 * @sample samples.MoveBuilderSamples.naturally
 */
inline fun buildMove(action: MoveBuilder.() -> Unit): Pair<BoardPos, BoardPos> =
    MoveBuilder().apply(action).build()

/**
 * A builder that builds a move. Use [buildMove] instead.
 */
@TutorialDslMarker
class MoveBuilder {
    private var from: BoardPos? = null

    private var to: BoardPos? = null

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

    @TutorialDslMarker
    infix fun BoardPos.to(pos: BoardPos) {
        from = this
        to = pos
    }

    @TutorialDslMarker
    infix fun String.to(pos: String) {
        from(this)
        to = BoardPos.fromString(pos)
    }

    fun build(): Pair<BoardPos, BoardPos> {
        return Pair(
            from ?: error("from is not set"),
            to ?: error("to is not set")
        )
    }
}

