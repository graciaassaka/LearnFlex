package org.example.composeApp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.example.composeApp.screen.AuthScreen
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.viewModel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Displays the navigation host for managing different routes in the application.
 *
 * @param any An object expected to be of type [NavHostController], used for controlling the navigation stack.
 */
@Composable
actual fun Navigator(any: Any)
{
    val navController = any as NavHostController

    NavHost(
        navController = navController,
        startDestination = Route.Auth
    ) {
        composable<Route.Auth> {
            val viewModel: AuthViewModel = koinViewModel()
            AuthScreen(navController, viewModel)
        }
    }
}