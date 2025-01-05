package org.example.composeApp.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.io.File

/**
 * A composable function for handling document upload functionality.
 *
 * @param onDocumentSelected Callback triggered when a document is selected, providing the selected document as a File.
 * @param enabled Controls whether the document upload components are enabled or not.
 * @param handleError Callback for handling errors that may occur during document upload or deletion.
 * @param isUploaded Indicates whether a document has already been uploaded.
 * @param isUploading Indicates whether a document is in the process of being uploaded.
 * @param modifier Modifier to be applied to the document upload component.
 * @param onDocumentDeleted Callback triggered when a document is deleted.
 */
@Composable
expect fun DocumentUpload(
    onDocumentSelected: (File) -> Unit,
    enabled: Boolean,
    handleError: (Throwable) -> Unit,
    isUploaded: Boolean,
    isUploading: Boolean,
    modifier: Modifier = Modifier,
    onDocumentDeleted: () -> Unit = {}
)