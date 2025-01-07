package org.example.composeApp.presentation.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.learnflexbird
import org.example.composeApp.presentation.ui.dimension.Dimension
import org.example.composeApp.presentation.ui.dimension.Padding
import org.example.composeApp.presentation.ui.constant.Orientation
import org.jetbrains.compose.resources.painterResource

/**
 * A composable function that displays a "SpeakingBird" component with a speech bubble and a bird image.
 *
 * @param orientation Specifies the orientation of the component, either Vertical or Horizontal.
 * @param enabled A Boolean flag indicating whether the component is enabled or not, showing a typing indicator if true.
 * @param modifier A [Modifier] to be applied to the component.
 * @param content The composable content to be displayed inside the speech bubble when enabled is false.
 */
@Composable
fun SpeakingBird(
    orientation: Orientation,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val (height, bubbleAlignment, birdAlignment) = when (orientation) {
        Orientation.Vertical -> Triple(Dimension.SPEAKING_BIRD_HEIGHT_VER.dp, Alignment.TopEnd, Alignment.BottomStart)
        Orientation.Horizontal -> Triple(
            Dimension.SPEAKING_BIRD_HEIGHT_HOR.dp,
            Alignment.TopCenter,
            Alignment.BottomCenter
        )
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(Padding.MEDIUM.dp),
    ) {
        Card(
            modifier = Modifier
                .align(bubbleAlignment)
                .padding(end = Padding.SMALL.dp)
                .zIndex(1f)
                .sizeIn(
                    maxWidth = Dimension.SPEECH_BUBBLE_MAX_WIDTH.dp,
                    maxHeight = Dimension.SPEECH_BUBBLE_MAX_HEIGHT.dp,
                    minWidth = Dimension.SPEECH_BUBBLE_MIN_WIDTH.dp,
                    minHeight = Dimension.SPEECH_BUBBLE_MIN_HEIGHT.dp
                ),
            shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Box(modifier = Modifier.wrapContentSize()) {
                if (scrollState.maxValue > 0) CustomVerticalScrollbar(
                    scrollState = scrollState,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
                Column(
                    modifier = Modifier.Companion
                        .padding(Padding.MEDIUM.dp)
                        .verticalScroll(
                            state = scrollState,
                            enabled = enabled
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = { if (!enabled) TypingIndicator(modifier = Modifier.Companion.padding(Padding.MEDIUM.dp)) else content() }
                )
            }
        }
        Image(
            painter = painterResource(Res.drawable.learnflexbird),
            contentDescription = null,
            modifier = Modifier
                .align(birdAlignment)
                .size(Dimension.BIRD_SIZE.dp),
            alignment = Alignment.BottomStart,
            contentScale = ContentScale.Crop,
        )
    }
}
