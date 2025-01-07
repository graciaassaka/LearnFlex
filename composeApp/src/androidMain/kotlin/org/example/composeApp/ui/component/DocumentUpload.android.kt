package org.example.composeApp.ui.component

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.file_not_found_error
import learnflex.composeapp.generated.resources.file_too_large_error
import org.example.shared.domain.constant.FileType
import org.jetbrains.compose.resources.stringResource
import java.io.File

@Composable
actual fun DocumentUpload(
    onDocumentSelected: (File) -> Unit,
    enabled: Boolean,
    handleError: (Throwable) -> Unit,
    isUploaded: Boolean,
    isUploading: Boolean,
    modifier: Modifier,
    onDocumentDeleted: () -> Unit
) {
    val context = LocalContext.current

    val fileNotFoundErr = stringResource(Res.string.file_not_found_error)
    val fileTooLargeErr = stringResource(Res.string.file_too_large_error)
    var fileName by remember { mutableStateOf<String?>(null) }

    val pickDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                val fileSize = descriptor.statSize
                if (fileSize > FileType.DOCUMENT.value) handleError(Exception(fileTooLargeErr))
                else fileName = saveDocument(context, uri, onDocumentSelected, handleError)
            } ?: run {
                handleError(Exception(fileNotFoundErr))
            }
        }
    }
    FileUploadCard(
        onUpload = { pickDocument.launch("application/pdf") },
        onDelete = onDocumentDeleted,
        enabled = enabled,
        isUploaded = isUploaded,
        isUploading = isUploading,
        modifier = modifier,
        fileName = fileName
    )
}

private fun saveDocument(
    context: Context,
    uri: Uri,
    onDocumentSelected: (File) -> Unit,
    handleError: (Throwable) -> Unit
) = try {
    val fileName = getFileName(context, uri) ?: "document.pdf"
    val file = File(context.filesDir, fileName)
    file.outputStream()
        .use { output ->
            context.contentResolver
                .openInputStream(uri)
                ?.copyTo(output)
        }
    onDocumentSelected(file)
    fileName
} catch (e: Exception) {
    handleError(e)
    ""
}

private fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) fileName = it.getString(nameIndex)
        }
    }
    return fileName
}
