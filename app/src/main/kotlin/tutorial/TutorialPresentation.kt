package org.keizar.android.tutorial

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

/**
 * UI components to be displayed to the user.
 */
interface TutorialPresentation {
    /**
     * The composable message to be displayed to the user.
     */
    val message: StateFlow<(@Composable () -> Unit)?>
}
