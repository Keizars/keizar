package org.keizar.android.ui.tutorial

import kotlinx.coroutines.flow.StateFlow

interface StepSession {
    val step: Step

    val state: StateFlow<StepState>
}

enum class StepState {
    /**
     * Not invoked or revoked.
     */
    NOT_INVOKED,

    INVOKING,

    INVOKED,

    REVOKING,
}
