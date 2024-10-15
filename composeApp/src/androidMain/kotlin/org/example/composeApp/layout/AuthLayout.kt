package org.example.composeApp.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.tooling.preview.Preview
import org.example.composeApp.component.CustomSnackbar
import org.example.composeApp.component.auth.AnimatedAppBanner
import org.example.composeApp.component.auth.AnimatedBottomSheet
import org.example.composeApp.theme.LearnFlexTheme
import org.example.shared.presentation.util.SnackbarType

/**
 * AuthLayout provides a scaffold for authentication screens, incorporating a customizable
 * snackbar host and animated components for the app banner and bottom sheet.
 *
 * @param snackbarHostState State for managing snackbar messages within the scaffold.
 * @param snackbarType Type of the snackbar (e.g., success, error, warning, info).
 * @param isVisible Controls the visibility of the animated components.
 * @param onAnimationFinished Callback triggered when animations complete.
 * @param content Composable content that appears within the bottom sheet component.
 */
@Composable
fun AuthLayout(
    snackbarHostState: SnackbarHostState,
    snackbarType: SnackbarType,
    isVisible: Boolean,
    onAnimationFinished: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit)
)
{
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) { CustomSnackbar(it, snackbarType) } }) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeLayout { constraints ->
                val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
                val width = constraints.maxWidth
                val height = constraints.maxHeight

                val appNamePlaceable = subcompose("appName") {
                   AnimatedAppBanner(isVisible = isVisible)
                }.map { it.measure(looseConstraints) }

                val contentPlaceable = subcompose("content") {
                    AnimatedBottomSheet(
                        isVisible = isVisible,
                        onAnimationFinished = onAnimationFinished,
                        content = content
                    )
                }.map { it.measure(looseConstraints) }

                layout(width, height) {
                    val appNameY = 200
                    appNamePlaceable.forEach { it.place(x = (width - it.width) / 2, y = appNameY) }

                    val contentY = 500
                    contentPlaceable.forEach { it.place(x = (width - it.width) / 2, y = contentY) }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun AuthLayoutPreview()
{
    LearnFlexTheme {
        AuthLayout(
            snackbarHostState = SnackbarHostState(),
            snackbarType = SnackbarType.Info,
            isVisible = true,
            onAnimationFinished = {},
            content = { Column { Text("Content") } }
        )
    }
}