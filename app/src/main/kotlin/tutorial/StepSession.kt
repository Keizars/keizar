package org.keizar.android.tutorial

import kotlinx.coroutines.flow.StateFlow

interface StepSession {
    val index: Int

    val step: Step

    val state: StateFlow<StepState>
}

sealed class StepState(
    /**
     * Is the [Action.action] running or not.
     */
    val isJobRunning: Boolean,
    /**
     * Whether the step has had any effect on the game.
     */
    val hasEffect: Boolean,
) {
    /**
     * Not invoked or revoked.
     */
    data object NotInvoked : StepState(isJobRunning = false, hasEffect = false)
    data object Invoking : StepState(isJobRunning = true, hasEffect = true)

    sealed class Invoked : StepState(isJobRunning = false, hasEffect = true)
    data object FullyInvoked : Invoked()
    data object PartiallyInvoked : Invoked()
    data object NothingInvoked : Invoked()

    data object Revoking : StepState(isJobRunning = false, hasEffect = true)
}
