package org.example.composeApp.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.dimension.Padding
import org.example.composeApp.dimension.Spacing

/**
 * Composable function to display a group of selectable cards.
 *
 * @param options A list of strings representing the options to be displayed as selectable cards.
 * @param onOptionSelected A callback invoked when an option is selected, receiving the selected option as a parameter.
 * @param selectedOption A string representing the currently selected option.
 * @param enabled A boolean value indicating whether the selectable cards are enabled or disabled.
 * @param modifier The modifier to be applied to the Column composable containing the selectable cards.
 */
@Composable
fun SelectableCardGroup(
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    selectedOption: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .padding(Padding.MEDIUM.dp)
        .selectableGroup(),
    verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    options.forEach {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selectedOption == it,
                    onClick = { onOptionSelected(it) },
                    enabled = enabled
                ),
            shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp),
            colors = if (selectedOption == it) CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) else CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            content = { Text(text = it, modifier = Modifier.padding(Padding.SMALL.dp)) }
        )
    }
}
