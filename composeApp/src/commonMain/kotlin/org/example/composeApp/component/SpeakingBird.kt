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
 * A composable function that displays a speaking bird with a speech bubble.
 *
 * @param isLoading A boolean flag indicating whether the bird is currently speaking.
 * @param modifier The modifier to be applied to the speaking bird layout.
 * @param content The content to be displayed inside the speech bubble.
 */
@Composable
fun SpeakingBird(
    orientation: Orientation,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val (bubbleAlignment, birdAlignment) = when (orientation) {
        Orientation.Vertical -> Alignment.TopEnd to Alignment.BottomStart
        Orientation.Horizontal -> Alignment.TopCenter to Alignment.BottomCenter
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimension.SPEAKING_BIRD_HEIGHT.dp)
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
                content = { if (isLoading) TypingIndicator(modifier = Modifier.padding(Padding.MEDIUM.dp)) else content() }
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
