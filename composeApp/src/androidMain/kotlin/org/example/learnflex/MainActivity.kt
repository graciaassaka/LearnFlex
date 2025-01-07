package org.example.learnflex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import org.example.composeApp.App
import org.example.composeApp.injection.initKoin

/**
 * MainActivity is the entry point of the Android application.
 * It extends ComponentActivity and sets the content view to the App composable.
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initKoin(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            App(
                windowSizeClass = calculateWindowSizeClass(this),
                cacheDir = cacheDir.resolve("image_cache")
            )
        }
    }
}
