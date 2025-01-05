package org.example.composeApp.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.file_too_large_error
import org.example.shared.domain.constant.FileType
import org.jetbrains.compose.resources.stringResource
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun DocumentUpload(
    onDocumentSelected: (File) -> Unit,
    enabled: Boolean,
    handleError: (Throwable) -> Unit,
    isUploaded: Boolean,
    isUploading: Boolean,
    modifier: Modifier,
    onDocumentDeleted: () -> Unit,
) {
    var showFileDialog by remember { mutableStateOf(false) }

    val fileTooLargeErr = stringResource(Res.string.file_too_large_error)
    var fileName by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showFileDialog) {
            LaunchedEffect(Unit) {
                showFileDialog = false

                val fileChooser = JFileChooser().apply {
                    fileFilter = FileNameExtensionFilter("Document files", "pdf", "docx")
                    isMultiSelectionEnabled = false
                }

                val result = fileChooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile.run {
                    if (length() > FileType.DOCUMENT.value) {
                        handleError(Exception(fileTooLargeErr))
                    } else {
                        onDocumentSelected(this)
                        fileName = name
                    }
                }
            }
        }
        FileUploadCard(
            onUpload = { showFileDialog = true },
            onDelete = onDocumentDeleted,
            enabled = enabled,
            isUploaded = isUploaded,
            isUploading = isUploading,
            modifier = modifier,
            fileName = fileName
        )
    }
}