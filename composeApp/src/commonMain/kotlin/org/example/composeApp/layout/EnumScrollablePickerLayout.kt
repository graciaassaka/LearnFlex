package org.example.composeApp.layout

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.composeApp.dimension.Padding
import kotlin.enums.enumEntries
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A composable function that displays a scrollable picker for selecting a value from an enumeration.
 *
 * @param T The type of the enumeration. It must implement `Enum<T>`.
 * @param label The label to be displayed above the picker.
 * @param onChange Lambda function to be called when the selected value changes.
 * @param enabled Boolean indicating whether the picker is enabled or not.
 * @param modifier Modifier to be applied to the picker layout.
 */
@Composable
inline fun <reified T> EnumScrollablePickerLayout(
    label: String,
    crossinline onChange: (T) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) where T : Enum<T> {
    val itemHeight = Padding.LARGE.dp
    var offset by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    val entries = enumEntries<T>()

    val maxOffset = (entries.size - 1) * itemHeight.value

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(itemHeight * 4),
        horizontalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            modifier = Modifier.offset(y = itemHeight / 2)
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .testTag("${T::class.simpleName}_picker")
                .height(itemHeight * 3)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        offset = (offset - delta * 0.5f).coerceIn(0f, maxOffset)
                        updateSelection(offset, itemHeight.value, entries, onChange)
                    },
                    enabled = enabled,
                    onDragStopped = {
                        coroutineScope.launch {
                            val targetOffset = (offset / itemHeight.value).roundToInt() * itemHeight.value
                            offset = targetOffset.coerceIn(0f, maxOffset)
                            updateSelection(offset, itemHeight.value, entries, onChange)
                        }
                    }
                )
        ) {
            Layout(
                content = {
                    repeat(enumEntries<T>().size) { index ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.height(itemHeight).testTag(entries[index].name),
                            content = { Text(text = entries[index].name, fontSize = 16.sp) }
                        )
                    }
                }
            ) { measurables, constraints ->
                val itemWidth = constraints.maxWidth
                val itemHeightPx = itemHeight.roundToPx()
                val midPoint = constraints.maxHeight / 2

                val placeables = measurables.map { measurable ->
                    measurable.measure(Constraints.fixed(itemWidth, itemHeightPx))
                }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    val firstVisibleIndex = (offset / itemHeight.value).roundToInt()

                    for (i in 0 until 3) {
                        val index = firstVisibleIndex + i - 1
                        if (index in entries.indices) {
                            val placeable = placeables[index]
                            val y = midPoint + (i - 1) * itemHeightPx + (offset % itemHeight.value).roundToInt()
                            val alpha = 1f - abs(y - midPoint) / (itemHeightPx * 1.5f)
                            placeable.placeWithLayer(0, y) {
                                this.alpha = alpha.coerceIn(0f, 1f)
                            }
                        }
                    }
                }
            }
        }
    }
}

inline fun <T> updateSelection(offset: Float, itemHeight: Float, entries: List<T>, onValueChange: (T) -> Unit) {
    val centerIndex = (offset / itemHeight).roundToInt().coerceIn(0, entries.lastIndex)
    onValueChange(entries[centerIndex])
}
