package org.keizar.android.tutorial

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow

/**
 * UI components to be displayed to the user.
 */
interface TutorialPresentation {
    /**
     * The composable message to be displayed to the user.
     */
    @Stable
    val message: StateFlow<(@Composable () -> Unit)?>

    /**
     * The tooltip message displayed in the top center of the board.
     */
    @Stable
    val tooltip: StateFlow<(@Composable RowScope.() -> Unit)?>
}
