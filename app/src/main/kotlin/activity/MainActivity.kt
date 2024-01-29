package org.keizar.android.activity

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import org.keizar.android.ui.KeizarApp
import org.keizar.android.ui.home.MainScreen

class MainActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            // transparent status bar
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            // transparent navigation bar
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )

        // allow drawing to system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//        startKoin {
//            modules(module {
//                single<SavedGameRepository> { SavedGameRepositoryImpl(savedGameStore) }
//            })
//        }

        setContent {
            KeizarApp(currentColorScheme) {
                MainScreen()
            }
        }
    }
}
