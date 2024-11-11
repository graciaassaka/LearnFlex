package org.example.composeApp.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A composable function for handling image upload functionality.
 *
 * @param enabled Controls whether the image upload components are enabled or not.
 * @param onImageSelected Callback triggered when an image is selected, providing the image as a ByteArray.
 * @param onImageDeleted Callback triggered when an image is deleted.
 * @param handleError Callback for handling errors that may occur during image upload or deletion.
 * @param modifier Modifier to be applied to the image upload component.
 * @param isUploaded Indicates whether an image has already been uploaded.
 */
@Composable
expect fun ImageUpload(
    enabled: Boolean,
    onImageSelected: (ByteArray) -> Unit,
    onImageDeleted: () -> Unit,
    handleError: (Throwable) -> Unit,
    modifier: Modifier,
    isUploaded: Boolean
)