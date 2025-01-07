package org.example.composeApp.ui.component

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
actual fun ThreePaneBackHandler(navigator: ThreePaneScaffoldNavigator<Nothing>) {
    // no-op
}