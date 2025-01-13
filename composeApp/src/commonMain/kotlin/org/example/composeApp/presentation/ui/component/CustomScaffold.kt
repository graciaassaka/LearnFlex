package org.example.composeApp.presentation.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowWidthSizeClass
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.refresh_button_label
import org.example.composeApp.presentation.navigation.AppDestination
import org.example.composeApp.presentation.ui.constant.TestTags
import org.example.composeApp.presentation.ui.dimension.Padding
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.jetbrains.compose.resources.stringResource

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
    onRefresh: () -> Unit,
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

    val fabInteractionSource = remember { MutableInteractionSource() }
    val isFabHovered by fabInteractionSource.collectIsHoveredAsState()

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
            content = { content(it) },
            floatingActionButton = {
                if (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED)
                    FloatingActionButton(
                        onClick = onRefresh,
                        interactionSource = fabInteractionSource,
                        modifier = Modifier.Companion
                            .padding(Padding.MEDIUM.dp)
                            .hoverable(interactionSource = fabInteractionSource)
                            .testTag(TestTags.DASHBOARD_REFRESH_FAB.tag)
                    ) {
                        Row(
                            modifier = Modifier.Companion
                                .padding(Padding.SMALL.dp)
                                .animateContentSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Refresh, stringResource(Res.string.refresh_button_label))
                            if (isFabHovered) Text(stringResource(Res.string.refresh_button_label))
                        }
                    }
            }
        )
    }
}

