package org.example.composeApp.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.composeApp.dimension.Dimension

/**
 * A composable function that displays a speech bubble.
 *
 * @param modifier The modifier to be applied to the speech bubble.
 * @param content The content to be displayed inside the speech bubble.
 */
@Composable
fun SpeechBubble(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
)
{
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        content = content
    )
}
