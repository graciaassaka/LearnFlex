package org.example.composeApp.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.file_too_large_error
import org.example.composeApp.util.LocalComposition
import org.jetbrains.compose.resources.stringResource
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * A composable function for handling image uploads. It provides a user interface to select an image file,
 * handles file size validation, and manages the loading state.
 *
 * @param enabled A boolean flag indicating whether the image is currently being uploaded.
 * @param onImageSelected A callback function that receives the selected image as a ByteArray.
 * @param onImageDeleted A callback function to handle the deletion of the uploaded image.
 * @param handleError A callback function to handle errors that occur during the image upload process.
 * @param modifier A Modifier for styling this composable.
 * @param isUploaded A boolean flag indicating whether the image has been successfully uploaded.
 */
@Composable
actual fun ImageUpload(
    enabled: Boolean,
    onImageSelected: (ByteArray) -> Unit,
    onImageDeleted: () -> Unit,
    handleError: (Throwable) -> Unit,
    modifier: Modifier,
    isUploaded: Boolean
) {
    var showFileDialog by remember { mutableStateOf(false) }
    val maxFileSize = LocalComposition.MaxFileSize.current

    val fileTooLargeErr = stringResource(Res.string.file_too_large_error)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FileUploadBox(
            uploadText = "Upload Photo",
            onClick = { showFileDialog = true },
            enabled = enabled,
            onFileDeleted = onImageDeleted,
            isUploaded = isUploaded
        )

        if (showFileDialog) {
            LaunchedEffect(Unit) {
                showFileDialog = false

                val fileChooser = JFileChooser().apply {
                    fileFilter = FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp")
                    isMultiSelectionEnabled = false
                }

                val result = fileChooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile.run {
                    if (length() > maxFileSize) handleError(Exception(fileTooLargeErr))
                    else inputStream().buffered().use { onImageSelected(it.readBytes()) }
                }
            }
        }
    }
}