package org.example.composeApp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import org.example.composeApp.ui.dimension.Padding
import org.example.shared.domain.constant.interfaces.ValuableEnum

/**
 * A composable function that displays a dropdown menu for selecting values from an enumeration.
 *
 * @param T The type of the enumeration. It must implement both `Enum<T>` and `ValuableEnum`.
 * @param selected The currently selected value from the enumeration.
 * @param isDropDownVisible A boolean flag indicating whether the dropdown menu is currently visible.
 * @param onDropDownVisibilityChanged A callback function triggered when the dropdown visibility is toggled.
 * @param onSelected A callback function triggered when a new value is selected from the dropdown.
 * @param enabled A boolean flag indicating whether the dropdown is enabled or disabled.
 * @param modifier The modifier to be applied to the dropdown menu layout.
 * @param label Optional string resource ID for the dropdown label. Defaults to null.
 */
@Composable
inline fun <reified T> EnumDropdown(
    label: String,
    selected: T,
    isDropDownVisible: Boolean,
    noinline onDropDownVisibilityChanged: () -> Unit,
    crossinline onSelected: (T) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) where T : Enum<T>, T : ValuableEnum<String>
{
    val density = LocalDensity.current
    var rowSize by remember { mutableStateOf(Size.Zero) }

    Row(
        modifier = modifier
            .padding(vertical = Padding.SMALL.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(end = Padding.SMALL.dp)
        )
        Box(
            modifier = Modifier
                .padding(start = Padding.SMALL.dp)
                .weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .clip(shape = MaterialTheme.shapes.medium)
                    .clickable(
                        enabled = enabled,
                        onClick = onDropDownVisibilityChanged
                    ).background(MaterialTheme.colorScheme.surfaceVariant)
                    .onGloballyPositioned { coordinates -> rowSize = coordinates.size.toSize() }
                    .testTag("${T::class.simpleName}_dropdown_button"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selected.value,
                    modifier = Modifier.padding(Padding.SMALL.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (isDropDownVisible) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.padding(Padding.SMALL.dp)
                )
            }
            DropdownMenu(
                expanded = isDropDownVisible,
                onDismissRequest = onDropDownVisibilityChanged,
                offset = DpOffset(0.dp, 4.dp),
                modifier = Modifier
                    .width(with(density) { rowSize.width.toDp() })
                    .testTag("${T::class.simpleName}_dropdown_menu"),
            ) {
                enumValues<T>().sorted().forEachIndexed { i, entry ->
                    DropdownMenuItem(
                        text = { Text(entry.value) },
                        onClick = {
                            onDropDownVisibilityChanged()
                            onSelected(entry)
                        },
                        enabled = enabled,
                    )
                    if (i < enumValues<T>().size.dec()) FadingDivider(
                        startColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        endColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0f),
                        modifier = Modifier.padding(horizontal = Padding.SMALL.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FadingDivider(
    startColor: Color,
    endColor: Color,
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp
)
{
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(brush = Brush.horizontalGradient(colors = listOf(startColor, endColor)))
    )
}
