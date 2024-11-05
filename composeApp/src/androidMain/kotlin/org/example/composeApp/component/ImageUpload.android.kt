package org.example.composeApp.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.file_not_found_error
import learnflex.composeapp.generated.resources.file_too_large_error
import learnflex.composeapp.generated.resources.upload_photo_text
import org.example.composeApp.util.LocalComposition
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ImageUpload(
    isLoading: Boolean,
    onImageSelected: (ByteArray) -> Unit,
    onImageDeleted: () -> Unit,
    handleError: (String) -> Unit,
    modifier: Modifier,
    isUploaded: Boolean,
) {
    val context = LocalContext.current
    val maxFileSize = LocalComposition.MaxFileSize.current

    val fileNotFoundErr = stringResource(Res.string.file_not_found_error)
    val fileTooLargeErr = stringResource(Res.string.file_too_large_error)

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            with(context.contentResolver) {
                openFileDescriptor(uri, "r")?.use { descriptor ->
                    if (descriptor.statSize > maxFileSize) handleError(fileTooLargeErr)
                    else openInputStream(uri)?.use { onImageSelected(it.readBytes()) } ?: handleError(fileNotFoundErr)
                }
            }
        }
    }

    FileUploadBox(
        uploadText = stringResource(Res.string.upload_photo_text),
        onClick = { pickImage.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        isLoading = isLoading,
        onFileDeleted = onImageDeleted,
        modifier = modifier,
        isUploaded = isUploaded
    )
}