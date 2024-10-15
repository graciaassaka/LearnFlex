package org.example.learnflex

import org.example.composeApp.App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import org.example.composeApp.navigation.Navigator
import org.example.composeApp.theme.LearnFlexTheme

/**
 * MainActivity is the entry point of the Android application.
 * It extends ComponentActivity and sets the content view to the App composable.
 */
class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val navHostController = rememberNavController()
            LearnFlexTheme {
                Navigator(navHostController)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview()
{
    App()
}