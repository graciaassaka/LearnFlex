package org.example.composeApp.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.composeApp.navigation.AppDestination

/**
 * Custom scaffold composable function.
 *
 * @param currentDestination AppDestination
 * @param onDestinationSelected Function1<AppDestination, Unit>
 * @param snackbarHostState SnackbarHostState
 * @param enabled Boolean
 * @param modifier Modifier
 * @param content @Composable () -> Unit
 */
@Composable
expect fun CustomScaffold(
    widthSizeClass: WindowWidthSizeClass,
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    snackbarHostState: SnackbarHostState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
)