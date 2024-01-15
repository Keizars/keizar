package org.keizar.android.activity

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import org.keizar.android.ui.KeizarApp
import org.keizar.android.ui.game.MainScreen

class MainActivity : BaseComponentActivity() {
    private enum class AuthorizationState {
        PROCESSING,
        SUCCESS,
        CANCELLED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            // 透明状态栏
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            // 透明导航栏
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )

        // 允许画到 system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            KeizarApp(currentColorScheme) {
                MainScreen()
            }
        }
    }
}
