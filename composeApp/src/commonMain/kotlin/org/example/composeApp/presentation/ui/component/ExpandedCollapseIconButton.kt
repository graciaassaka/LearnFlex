package org.example.composeApp.presentation.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.collapse_button_label
import learnflex.composeapp.generated.resources.expand_button_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpandCollapseIconButton(
    expanded: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (expanded) {
                Icons.Default.ExpandLess
            } else {
                Icons.Default.ExpandMore
            },
            contentDescription = if (expanded) {
                stringResource(Res.string.collapse_button_label)
            } else {
                stringResource(Res.string.expand_button_label)
            }
        )
    }
}