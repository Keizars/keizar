package org.keizar.android.ui.foundation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration


/**
 * Returns `true` if the system is currently in landscape mode.
 *
 * This composable function is aware of configuration changes.
 * When the system switches between landscape and portrait modes, this function will (change to) return `false`/`true` accordingly.
 */
@Composable
fun isSystemInLandscape() = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
