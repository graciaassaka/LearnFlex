package org.example.composeApp.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.app_name
import org.example.composeApp.dimension.Padding
import org.example.composeApp.navigation.AppDestination
import org.example.shared.presentation.util.SnackbarType
import org.jetbrains.compose.resources.stringResource

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
actual fun CustomScaffold(
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    snackbarHostState: SnackbarHostState,
    enabled: Boolean,
    modifier: Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
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
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        badge = null,
                        shape = MaterialTheme.shapes.small,
                        colors = NavigationDrawerItemDefaults.colors(),
                        interactionSource = remember { MutableInteractionSource() }
                    )
                }
            }
        }
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
}