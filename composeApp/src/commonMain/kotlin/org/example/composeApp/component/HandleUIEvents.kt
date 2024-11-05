package org.example.composeApp.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.util.UIEvent
import org.example.shared.presentation.viewModel.BaseViewModel

/**
 * Handles UI events such as navigation and displaying snackbars.
 *
 * @param route Current route of the application.
 * @param navController The navigation controller to handle navigation events.
 * @param viewModel The view model that emits UI events.
 * @param snackbarHostState The state of the snackbar host to display snackbar messages.
 * @param processSnackbarType Lambda function to process the type of snackbar event.
 */
@Composable
fun HandleUIEvents(
    route: Route,
    navController: NavController,
    viewModel: BaseViewModel,
    snackbarHostState: SnackbarHostState,
    processSnackbarType: (SnackbarType) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(true) {
        viewModel.uiEvent.collect { event: UIEvent ->
            when (event) {
                is UIEvent.Navigate -> {
                    if (route is Route.Auth) navController.navigateAndClearBackStack(event.destination)
                    else navController.navigate(event.destination)
                }

                is UIEvent.ShowSnackbar -> {
                    processSnackbarType(event.type)
                    coroutineScope.launch { snackbarHostState.showSnackbar(event.message) }
                }
            }
        }
    }
}

fun NavController.navigateAndClearBackStack(route: Route) =
    navigate(route) { popUpTo(graph.startDestinationId) { inclusive = true } }
