package org.example.composeApp.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import org.example.composeApp.navigation.AppDestination

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

    val (customNavSuiteType, itemFontSize) = with(currentWindowAdaptiveInfo()) {
        if (windowSizeClass.windowWidthSizeClass == androidx.window.core.layout.WindowWidthSizeClass.EXPANDED) {
            Pair(NavigationSuiteType.NavigationDrawer, 16.sp)
        } else {
            Pair(NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(this), 8.sp)
        }
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
        layoutType = customNavSuiteType,
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
        content = { content(PaddingValues.Absolute()) }
    )
}

