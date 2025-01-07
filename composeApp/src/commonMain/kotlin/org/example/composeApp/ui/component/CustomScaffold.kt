package org.example.composeApp.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowWidthSizeClass
import org.example.composeApp.navigation.AppDestination
import org.example.shared.presentation.util.SnackbarType

/**
 * A custom scaffold composable function that provides a structured layout with navigation items, a snackbar, and content.
 * It supports adaptive design based on the window size class.
 *
 * @param snackbarHostState The state of the SnackbarHost that manages snackbar messages.
 * @param snackbarType The type of snackbar to be displayed, such as Info, Success, Error, or Warning.
 * @param currentDestination The current app destination selected from the navigation items.
 * @param onDestinationSelected A callback function invoked when a navigation item is selected.
 * @param enabled A boolean indicating whether the navigation items are enabled.
 * @param modifier An optional modifier to be applied to the CustomScaffold layout.
 * @param content The main content to be displayed within the scaffold, which receives PaddingValues as a parameter.
 */
@Composable
fun CustomScaffold(
    snackbarHostState: SnackbarHostState,
    snackbarType: SnackbarType,
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val navSuiteItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary,
        )
    )

    val itemFontSize = with(currentWindowAdaptiveInfo()) {
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) 16.sp else 8.sp
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestination.entries.forEach {
                item(
                    selected = it == currentDestination,
                    onClick = { onDestinationSelected(it) },
                    icon = { Icon(it.icon, it.contentDescription) },
                    enabled = enabled,
                    label = {
                        Text(
                            text = it.label,
                            fontSize = itemFontSize,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    alwaysShowLabel = false,
                    colors = navSuiteItemColors,
                )
            }
        },
        modifier = modifier,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContentColor = MaterialTheme.colorScheme.onSurface,
            navigationRailContentColor = MaterialTheme.colorScheme.onSurface,
            navigationDrawerContentColor = MaterialTheme.colorScheme.onSurface,
            navigationBarContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            navigationRailContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            navigationDrawerContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Scaffold(
            modifier = Modifier.safeDrawingPadding(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) { CustomSnackbar(it, snackbarType) } },
            content = { content(it) }
        )
    }
}

