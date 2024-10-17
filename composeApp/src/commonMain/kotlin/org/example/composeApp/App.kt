package org.example.composeApp

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import org.example.composeApp.navigation.Navigator
import org.example.composeApp.theme.LearnFlexTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(windowSizeClass: WindowSizeClass)
{
    val navHostController = rememberNavController()
    LearnFlexTheme {
        Navigator(
            navController = navHostController,
            windowSizeClass = windowSizeClass
        )
    }
}