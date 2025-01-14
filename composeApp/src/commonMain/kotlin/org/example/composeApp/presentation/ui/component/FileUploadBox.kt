package org.example.composeApp.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.composeApp.presentation.ui.dimension.Padding
import org.example.composeApp.presentation.ui.dimension.Spacing
import java.text.SimpleDateFormat
import java.util.*

/**
 * A composable function that displays a file upload UI component with an optional delete function.
 *
 * @param uploadText The text to display when no file is uploaded.
 * @param onClick A callback function invoked when the user clicks to upload a file.
 * @param enabled A boolean flag indicating whether the file upload button is enabled.
 * @param onDelete A callback function invoked when the user deletes an uploaded file.
 * @param modifier A Modifier for styling this composable.
 * @param isUploaded A boolean flag indicating whether a file has been successfully uploaded.
 */
@Composable
fun FileUploadBox(
    uploadText: String,
    onClick: () -> Unit,
    enabled: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isUploaded: Boolean = false,
) {
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val borderWidth = 1.dp
    val dashLength = 10.dp
    val gapLength = 10.dp

    Surface(
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.drawBehind {
            drawRoundRect(
                color = borderColor,
                style = Stroke(
                    width = borderWidth.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(dashLength.toPx(), gapLength.toPx()),
                        phase = 0f
                    )
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TextFieldDefaults.colors().focusedContainerColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Padding.SMALL.dp)
                    .clickable(enabled = enabled) { if (isUploaded) onDelete() else onClick() },
                horizontalArrangement = Arrangement.spacedBy(Padding.SMALL.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = if (isUploaded) onDelete else onClick,
                    enabled = enabled,
                ) {
                    Icon(
                        imageVector = if (isUploaded) Icons.Default.Delete else Icons.Default.UploadFile,
                        contentDescription = null
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
                ) {
                    if (isUploaded) Text(
                        text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    else Text(uploadText)
                }
            }
        }
    }
}