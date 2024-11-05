package org.example.composeApp

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import org.example.composeApp.navigation.Navigator
import org.example.composeApp.theme.LearnFlexTheme
import org.example.composeApp.util.LocalComposition
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(windowSizeClass: WindowSizeClass) {
    val navHostController = rememberNavController()
    CompositionLocalProvider(
        LocalComposition.MaxFileSize provides 10L * 1024 * 1024
    ) {
        LearnFlexTheme {
            Navigator(
                navController = navHostController,
                windowSizeClass = windowSizeClass
            )
        }
    }
}