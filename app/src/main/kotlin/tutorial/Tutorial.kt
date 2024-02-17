package org.keizar.android.tutorial

import kotlinx.coroutines.Job
import org.keizar.game.GameSession
import org.keizar.game.snapshot.GameSnapshot
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * A group of a steps that the user can follow to learn how to play the game.
 *
 * [Tutorial] is state-less: it describe what is going to happen in when the player plays the tutorial.
 * When the player plays the tutorial, a [TutorialSession] is created.
 *
 * Use [newSession] to create a stateful session which tracks current progress when the player plays the tutorial.
 */
class Tutorial
/**
 * It's recommended to use [buildTutorial] instead.
 */
constructor(
    /**
     * Game snapshot that will be used to create a [GameSession] when this tutorial is being played (i.e. [newSession]).
     */
    val initialGameSnapshot: GameSnapshot,
    /**
     * Ordered steps of this tutorial. A tutorial should have at least one step.
     */
    val steps: List<Step>,
) {
    init {
        require(steps.isNotEmpty()) { "A tutorial should have at least one step" }
    }
}

/**
 * Starts a new [TutorialSession] that tracks the current progress of the player playing the tutorial.
 *
 * @param parentCoroutineContext Parent coroutine context for the session.
 * Returned [TutorialSession] will attach a child [Job] to the [parentCoroutineContext]'s [Job].
 */
fun Tutorial.newSession(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
): TutorialSession = TutorialSessionImpl(this, parentCoroutineContext)


/**
 * Represents a step in the tutorial.
 *
 * The [Step] is state-less: it describe what is going to happen in when the player plays the tutorial.
 * When the player plays the tutorial, a [TutorialSession] is created.
 *
 * ## Invoking a step
 *
 * [action] describes what is going to happen in this step, e.g. requesting the player to move a piece, showing a message, etc.
 *
 * When the tutorial goes to this step, the [action] should be executed.
 *
 * ## Revoking a step
 *
 * When the [action] is invoked, the [TutorialSession] of which this step is a part of tracks what has been done in this step, i.e. piece movements.
 * By revoking the step, all the tracked actions should be undone.
 */
class Step(
    /**
     * Name for debugging purposes. Not necessarily (but recommended) to be unique.
     */
    val name: String,
    /**
     * The action to be executed in this step.
     *
     * Note that when the steps are executed, they are executed in the order they are added.
     */
    val action: StepAction,
) {
    override fun toString(): String = "Step(name=$name)"
}

/**
 * A function that represents an action that is executed in a step.
 * @see StepActionContext
 * @see Step
 */
typealias StepAction = suspend StepActionContext.() -> Unit
