package org.example.composeApp.presentation.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.cancel_button_label
import org.jetbrains.compose.resources.stringResource

private const val INDICATOR_SIZE = 80

/**
 * A composable function that displays a loading indicator with an optional cancel button.
 *
 * @param isCancellable Determines whether the cancel button should be displayed.
 * @param cancel Callback invoked when the cancel button is clicked.
 * @param modifier Modifier to be applied to the container of the loading indicator and button.
 */
@Composable
fun LoadingWithCancelButton(
    isCancellable: Boolean,
    cancel: () -> Unit,
    modifier: Modifier = Modifier
) = Box(modifier = modifier) {
    CircularProgressIndicator(
        modifier = Modifier.align(Alignment.Center).size(INDICATOR_SIZE.dp),
        color = MaterialTheme.colorScheme.primary,
    )
    if (isCancellable) IconButton(
        onClick = cancel,
        modifier = Modifier.align(Alignment.Center)
    ) {
        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = stringResource(Res.string.cancel_button_label)
        )
    }
}