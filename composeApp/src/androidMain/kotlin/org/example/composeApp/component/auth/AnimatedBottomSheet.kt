package org.example.composeApp.component.auth

import android.graphics.RuntimeShader
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import org.example.composeApp.component.BACKGROUND_SHADER
import org.example.composeApp.theme.LearnFlexTheme
import org.example.learnflex.R


/**
 * Displays an animated bottom sheet that slides in and out vertically.
 *
 * @param isVisible Determines the visibility state of the bottom sheet.
 * @param onAnimationFinished Lambda to be invoked when the animation finishes.
 * @param modifier Modifier to be applied to the bottom sheet layout.
 * @param content Composable content of the bottom sheet.
 */
@Composable
fun AnimatedBottomSheet(
    isVisible: Boolean,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
)
{
    val transitionState = remember { MutableTransitionState(false) }

    val time by produceState(0f) { while (true) withInfiniteAnimationFrameMillis { value = it / 1000f } }

    val color = MaterialTheme.colorScheme.surfaceContainerHighest
    val color2 = MaterialTheme.colorScheme.surfaceContainerLowest

    LaunchedEffect(isVisible) {
        transitionState.targetState = isVisible
    }

    LaunchedEffect(transitionState) {
        snapshotFlow { transitionState.isIdle }.collect {
            if (it && !transitionState.currentState && !transitionState.targetState) onAnimationFinished()
        }
    }

    AnimatedVisibility(
        visibleState = transitionState,
        enter = slideInVertically(tween(durationMillis = 500, easing = LinearOutSlowInEasing)) { it },
        exit = slideOutVertically(tween(durationMillis = 500, easing = LinearOutSlowInEasing)) { it },
    ) {
        Box(
            modifier = modifier
                .drawWithCache {
                    val shader = RuntimeShader(BACKGROUND_SHADER)
                    val shaderBrush = ShaderBrush(shader)
                    shader.setFloatUniform("resolution", size.width, size.height)
                    onDrawBehind {
                        shader.setFloatUniform("time", time)
                        shader.setColorUniform("color", color.toArgb())
                        shader.setColorUniform("color2", color2.toArgb())
                        drawRect(shaderBrush)
                    }
                }
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(
                        topStart = dimensionResource(R.dimen.corner_radius_large),
                        topEnd = dimensionResource(R.dimen.corner_radius_large)
                    )
                ),
            content = { Column(modifier = Modifier.fillMaxSize(), content = content) }
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun AnimatedBottomSheetPreview()
{
    LearnFlexTheme {
        AnimatedBottomSheet(isVisible = true, onAnimationFinished = {}) {
            Column(modifier = Modifier.fillMaxSize()) { Text("Hello, World!") }
        }
    }
}