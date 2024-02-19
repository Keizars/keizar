package org.keizar.android.tutorial

import kotlinx.coroutines.flow.StateFlow

interface StepSession {
    val index: Int
    
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
