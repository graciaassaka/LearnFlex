package org.example.composeApp.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowWidthSizeClass
import org.example.composeApp.dimension.Padding
import org.example.composeApp.navigation.AppDestination
import org.example.shared.presentation.util.SnackbarType

/**
 * Custom scaffold composable function.
 *
 * @param currentDestination AppDestination
 * @param onDestinationSelected Function1<AppDestination, Unit>
 * @param enabled Boolean
 * @param modifier Modifier
 * @param content @Composable () -> Unit
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
        Box(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
            content(
                PaddingValues.Absolute(
                    left = Padding.MEDIUM.dp,
                    right = Padding.MEDIUM.dp
                )
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                CustomSnackbar(it, snackbarType)
            }
        }
    }
}

