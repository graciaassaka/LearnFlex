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

@Composable
actual fun ImageUpload(
    isLoading: Boolean,
    onImageSelected: (ByteArray) -> Unit,
    onImageDeleted: () -> Unit,
    handleError: (String) -> Unit,
    modifier: Modifier,
    isUploaded: Boolean
)
{
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
            isLoading = isLoading,
            onFileDeleted = onImageDeleted,
            isUploaded = isUploaded
        )

        if (showFileDialog)
        {
            LaunchedEffect(Unit) {
                showFileDialog = false

                val fileChooser = JFileChooser().apply {
                    fileFilter = FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp")
                    isMultiSelectionEnabled = false
                }

                val result = fileChooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile.run {
                    if (length() > maxFileSize) handleError(fileTooLargeErr)
                    else inputStream().buffered().use { onImageSelected(it.readBytes()) }
                }
            }
        }
    }
}