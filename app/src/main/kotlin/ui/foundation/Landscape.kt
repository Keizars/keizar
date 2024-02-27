package org.keizar.android.ui.foundation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration


@Composable
fun isSystemInLandscape() = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
