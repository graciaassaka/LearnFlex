package org.example.composeApp.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.composeApp.ui.dimension.Dimension
import org.example.composeApp.ui.dimension.Padding

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun ToolTip(
    text: String,
    modifier: Modifier,
    content: @Composable (() -> Unit)
) = TooltipArea(
    tooltip = {
        Surface(
            modifier = Modifier.shadow(4.dp).width(200.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp)
        ) {
            Column(
                modifier = Modifier.padding(Padding.SMALL.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    },
    modifier = modifier.padding(start = Padding.XLARGE.dp),
    delayMillis = 500,
    tooltipPlacement = TooltipPlacement.CursorPoint(alignment = Alignment.BottomEnd),
    content = content
)
