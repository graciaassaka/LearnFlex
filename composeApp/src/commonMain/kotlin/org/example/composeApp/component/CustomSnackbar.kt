package org.example.composeApp.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.shared.presentation.util.SnackbarType

/**
 * Displays a custom snackbar with different color schemes based on its type.
 *
 * @param snackbarData Data to be displayed in the snackbar.
 * @param snackbarType Type of the snackbar (e.g., success, error, warning, info).
 * @param modifier Modifier to be applied to the snackbar.
 */
@Composable
fun CustomSnackbar(
    snackbarData: SnackbarData,
    snackbarType: SnackbarType,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (snackbarType) {
        SnackbarType.Success -> MaterialTheme.colorScheme.primaryContainer
        SnackbarType.Error -> MaterialTheme.colorScheme.errorContainer
        SnackbarType.Warning -> MaterialTheme.colorScheme.tertiaryContainer
        SnackbarType.Info -> MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = when (snackbarType) {
        SnackbarType.Success -> MaterialTheme.colorScheme.onPrimaryContainer
        SnackbarType.Error -> MaterialTheme.colorScheme.onErrorContainer
        SnackbarType.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
        SnackbarType.Info -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        contentColor = contentColor,
        containerColor = backgroundColor,
        actionColor = contentColor
    )
}