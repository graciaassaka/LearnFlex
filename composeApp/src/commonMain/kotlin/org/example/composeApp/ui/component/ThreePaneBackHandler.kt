package org.example.composeApp.ui.component

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable

/**
 * A composable function for handling back navigation in a three-pane scaffold layout.
 *
 * @param navigator The navigator instance used to manage and handle back stack navigation for the three-pane scaffold.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
expect fun ThreePaneBackHandler(
    navigator: ThreePaneScaffoldNavigator<Nothing>
)