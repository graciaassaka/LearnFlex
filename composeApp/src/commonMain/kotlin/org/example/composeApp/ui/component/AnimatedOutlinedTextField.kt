package org.example.composeApp.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * A composable function that displays an animated outlined text field.
 *
 * @param value The current text to be displayed in the text field.
 * @param onValueChange A callback that is triggered when the text is changed.
 * @param enabled Whether the text field is enabled or not.
 * @param modifier The modifier to be applied to the text field.
 * @param label The label to be displayed inside the text field.
 * @param placeholder The placeholder to be displayed when the text field is empty.
 * @param leadingIcon The leading icon to be displayed at the start of the text field.
 * @param trailingIcon The trailing icon to be displayed at the end of the text field.
 * @param supportingText The supporting text to be displayed below the text field.
 * @param isError Whether the text field is in an error state.
 * @param visualTransformation The visual transformation to be applied to the text field.
 * @param keyboardOptions The keyboard options to be applied to the text field.
 * @param keyboardActions The keyboard actions to be applied to the text field.
 * @param singleLine Whether the text field is a single line or not.
 */
@Composable
fun AnimatedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
) {
    // Create an infinite transition for the animation
    val infiniteTransition = rememberInfiniteTransition(label = "animatedOutlinedTextField")

    // Animate the fraction value from 0 to 1 and back to 0
    val animatedFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedFraction"
    )

    // Interpolate the color for the focused state
    val focusedColor = lerp(
        OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor,
        MaterialTheme.colorScheme.primaryContainer,
        animatedFraction
    )

    // Display the outlined text field with the animated color
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        shape = RoundedCornerShape(32.dp),
        colors = OutlinedTextFieldDefaults.colors().copy(
            focusedIndicatorColor = focusedColor,
            focusedLabelColor = focusedColor,
        )
    )
}