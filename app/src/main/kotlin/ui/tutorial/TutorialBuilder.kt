package org.keizar.android.ui.tutorial

import org.keizar.game.snapshot.GameSnapshotBuilder
import org.keizar.utils.communication.game.BoardPos

/**
 * Builds a tutorial.
 *
 * - See [Tutorial] for more information about what is a tutorial.
 *
 * @see Tutorial
 * @sample samples.TutorialSamples.buildTutorialComplete
 */
inline fun buildTutorial(
    action: TutorialBuilder.() -> Unit
): Tutorial = TutorialBuilder().apply(action).build()

/**
 * @see buildTutorial
 */
class TutorialBuilder {
    @PublishedApi
    internal val snapshotBuilder: GameSnapshotBuilder = GameSnapshotBuilder()

    val steps = StepsBuilder()

    /**
     * Modifies the board.
     */
    @TutorialDslMarker
    inline fun board(
        builderAction: GameSnapshotBuilder.() -> Unit
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

    inner class StepsBuilder {
        @PublishedApi
        internal val list: MutableList<Step> = mutableListOf()

        /**
         * Adds a step to the tutorial.
         *
         * Steps are executed in the order they are added.
         */
        fun step(name: String, action: StepAction): Step {
            val step = Step(name, action)
            list.add(step)
            return step
        }

        internal fun toList() = list.toList()
    }


    /**
     * Adds a step to the executed just after this step has been successfully executed.
     *
     * This is useful to add a message after a move has been requested, without specifying the name of the step.
     *
     * @sample samples.TutorialSamples.stepThen
     */
    fun Step.then(
        name: String = this.name + "-then",
        execution: StepAction
    ): Step = steps.step(name, execution)

    fun build(): Tutorial {
        return Tutorial(
            initialGameSnapshot = snapshotBuilder.build(),
            steps = steps.toList(),
        )
    }
}

@DslMarker
annotation class TutorialDslMarker

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
        from(pos)
        to = BoardPos.fromString(pos)
    }

    fun build(): Pair<BoardPos, BoardPos> {
        return Pair(
            from ?: error("from is not set"),
            to ?: error("to is not set")
        )
    }
}

