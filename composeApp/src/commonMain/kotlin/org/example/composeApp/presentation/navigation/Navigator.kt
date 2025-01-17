package org.example.composeApp.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.example.composeApp.presentation.ui.screen.*
import org.example.composeApp.presentation.viewModel.*
import org.koin.compose.viewmodel.koinViewModel

private const val TRANSITION_DURATION = 500

/**
 * Navigator composable function.
 *
 * @param navController NavHostController
 */
@Composable
fun Navigator(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
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
        composable<Route.CreateProfile> {
            val viewModel: CreateUserProfileViewModel = koinViewModel()
            CreateProfileScreen(
                windowSizeClass = windowSizeClass,
                navController = navController,
                viewModel = viewModel
            )
        }
        composable<Route.Dashboard>(
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
        ) {
            val viewModel = koinViewModel<DashboardViewModel>()
            DashboardScreen(
                windowSizeClass = windowSizeClass,
                navController = navController,
                viewModel = viewModel
            )
        }
        composable<Route.Library>(
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
        ) {
            val viewModel = koinViewModel<LibraryViewModel>()
            LibraryScreen(
                windowSizeClass = windowSizeClass,
                navController = navController,
                viewModel = viewModel
            )
        }
        composable<Route.Study>(
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
        ) {
            val viewModel = koinViewModel<StudyViewModel>()
            StudyScreen(
                windowSizeClass = windowSizeClass,
                navController = navController,
                viewModel = viewModel
            )
        }
        composable<Route.EditProfile>(
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
        ) {
            val viewModel = koinViewModel<EditUserProfileViewModel>()
            EditUserProfileScreen(
                windowSizeClass = windowSizeClass,
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}