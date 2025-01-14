package org.example.composeApp.presentation.ui.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import learnflex.composeapp.generated.resources.*
import org.example.shared.domain.constant.FileType
import org.jetbrains.compose.resources.stringResource

/**
 * A composable function for handling image upload functionality.
 *
 * @param enabled Indicates whether the image upload process is ongoing.
 * @param onImageSelected A callback function that is invoked when an image is selected. Provides the selected image as a ByteArray.
 * @param onImageDeleted A callback function that is invoked when the selected image is deleted.
 * @param handleError A callback function to handle errors that occur during image selection or upload.
 * @param modifier A Modifier instance to be applied to the composable.
 * @param isUploaded A boolean flag indicating whether an image has already been uploaded.
 */
@Composable
actual fun ImageUpload(
    enabled: Boolean,
    onImageSelected: (ByteArray) -> Unit,
    onImageDeleted: () -> Unit,
    handleError: (Throwable) -> Unit,
    modifier: Modifier,
    isUploaded: Boolean,
    currentImageUrl: String?
) {
    val context = LocalContext.current

    val fileNotFoundErr = stringResource(Res.string.file_not_found_error)
    val fileTooLargeErr = stringResource(Res.string.file_too_large_error)

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            with(context.contentResolver) {
                openFileDescriptor(uri, "r")?.use { descriptor ->
                    if (descriptor.statSize > FileType.IMAGE.value) handleError(Exception(fileTooLargeErr))
                    else openInputStream(uri)?.use { onImageSelected(it.readBytes()) } ?: handleError(Exception(fileNotFoundErr))
                }
            }
        }
    }

    if (currentImageUrl.isNullOrBlank()) FileUploadBox(
        uploadText = stringResource(Res.string.upload_photo_text),
        onClick = { pickImage.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        enabled = enabled,
        onDelete = onImageDeleted,
        modifier = modifier,
        isUploaded = isUploaded
    ) else AsyncImage(
        model = currentImageUrl,
        contentDescription = stringResource(Res.string.profile_photo),
        modifier = modifier.size(100.dp)
            .clip(shape = CircleShape)
            .clickable(enabled = enabled) {
                if (isUploaded) {
                    onImageDeleted()
                } else {
                    pickImage.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
    )
}