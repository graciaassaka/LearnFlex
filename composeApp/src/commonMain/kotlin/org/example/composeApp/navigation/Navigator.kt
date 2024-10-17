package org.example.composeApp.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.example.composeApp.screen.AuthScreen
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.viewModel.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Navigator composable function.
 *
 * @param navController NavHostController
 */
@Composable
fun Navigator(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
)
{
    NavHost(
        navController = navController,
        startDestination = Route.Auth
    ) {
        composable<Route.Auth> {
            val viewModel: AuthViewModel = koinViewModel()
            AuthScreen(
                windowSizeClass = windowSizeClass,
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}