package org.example.composeApp.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A composable function for uploading images.
 *
 * @param isLoading A boolean indicating if the upload is in progress.
 * @param onImageSelected A callback function to handle the selected image as a ByteArray.
 * @param onImageDeleted A callback function to handle the deletion of an image.
 * @param handleError A callback function to handle errors, receiving an error message as a String.
 * @param modifier A Modifier for styling the composable.
 * @param isUploaded A boolean indicating if the image has been uploaded.
 */
@Composable
expect fun ImageUpload(
    isLoading: Boolean,
    onImageSelected: (ByteArray) -> Unit,
    onImageDeleted: () -> Unit,
    handleError: (String) -> Unit,
    modifier: Modifier,
    isUploaded: Boolean
)