package org.example.composeApp.presentation.ui.component

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
actual fun <T> ThreePaneBackHandler(navigator: ThreePaneScaffoldNavigator<T>, onBack: () -> Unit) {
    // no-op
}