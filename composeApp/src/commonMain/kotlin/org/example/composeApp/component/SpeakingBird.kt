package org.example.composeApp.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.learnflexbird
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.dimension.Padding
import org.example.composeApp.util.Orientation
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
    val (height, bubbleAlignment, birdAlignment) = when (orientation) {
        Orientation.Vertical -> Triple(Dimension.SPEAKING_BIRD_HEIGHT_VER.dp, Alignment.TopEnd, Alignment.BottomStart)
        Orientation.Horizontal -> Triple(Dimension.SPEAKING_BIRD_HEIGHT_HOR.dp, Alignment.TopCenter, Alignment.BottomCenter)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(Padding.MEDIUM.dp),
    ) {
        SpeechBubble(
            modifier = Modifier
                .align(bubbleAlignment)
                .padding(end = Padding.SMALL.dp)
                .zIndex(1f)
                .sizeIn(
                    maxWidth = Dimension.SPEECH_BUBBLE_MAX_WIDTH.dp,
                    maxHeight = Dimension.SPEECH_BUBBLE_MAX_HEIGHT.dp,
                    minWidth = Dimension.SPEECH_BUBBLE_MIN_WIDTH.dp,
                    minHeight = Dimension.SPEECH_BUBBLE_MIN_HEIGHT.dp
                )
        ) {
            Column(
                modifier = Modifier.padding(Padding.MEDIUM.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                content = { if (!enabled) TypingIndicator(modifier = Modifier.padding(Padding.MEDIUM.dp)) else content() }
            )
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
