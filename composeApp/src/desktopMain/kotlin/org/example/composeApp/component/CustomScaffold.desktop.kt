package org.example.composeApp.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.app_name
import learnflex.composeapp.generated.resources.ic_logo
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.dimension.Padding
import org.example.composeApp.navigation.AppDestination
import org.example.shared.presentation.util.SnackbarType
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Custom scaffold composable function.
 *
 * @param widthSizeClass WindowWidthSizeClass
 * @param currentDestination AppDestination
 * @param onDestinationSelected Function1<AppDestination, Unit>
 * @param snackbarHostState SnackbarHostState
 * @param enabled Boolean
 * @param modifier Modifier
 * @param content @Composable () -> Unit
 */
@Composable
actual fun CustomScaffold(
    widthSizeClass: WindowWidthSizeClass,
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    snackbarHostState: SnackbarHostState,
    enabled: Boolean,
    modifier: Modifier,
    content: @Composable (PaddingValues) -> Unit
) = NavigationContainer(
    windowWidthSizeClass = widthSizeClass,
    currentDestination = currentDestination,
    onDestinationSelected = onDestinationSelected,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) { CustomSnackbar(it, SnackbarType.Info) } },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                content = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add") }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        content = content
    )
}

@Composable
private fun NavigationContainer(
    windowWidthSizeClass: WindowWidthSizeClass,
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    content: @Composable () -> Unit
) = when (windowWidthSizeClass) {
    WindowWidthSizeClass.Expanded -> {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(
                    modifier = Modifier.width(250.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(Padding.MEDIUM.dp)
                    )
                    AppDestination.entries.forEach { destination ->
                        NavigationDrawerItem(
                            label = { Text(destination.label) },
                            selected = currentDestination == destination,
                            onClick = { onDestinationSelected(destination) },
                            modifier = Modifier.padding(Padding.SMALL.dp),
                            icon = { Icon(destination.icon, destination.contentDescription) },
                            badge = null,
                            shape = MaterialTheme.shapes.small,
                            colors = NavigationDrawerItemDefaults.colors(),
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    }
                }
            },
            content = content
        )
    }

    else                          -> {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                header = {
                    Image(
                        painter = painterResource(Res.drawable.ic_logo),
                        contentDescription = null,
                        modifier = Modifier.size(Dimension.LOGO_SIZE_SMALL.dp)
                    )
                }
            ) {
                AppDestination.entries.forEach { destination ->
                    NavigationRailItem(
                        selected = currentDestination == destination,
                        onClick = { onDestinationSelected(destination) },
                        icon = { Icon(destination.icon, destination.contentDescription) },
                        label = { Text(destination.label) },
                        alwaysShowLabel = false
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}
