package org.example.composeApp.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.delete_button_label
import learnflex.composeapp.generated.resources.upload_document
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.dimension.Padding
import org.example.composeApp.dimension.Spacing
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

private const val IMAGE_SIZE = 140

/**
 * Displays a file upload card with options to upload, delete, and view the status of a file.
 *
 * @param onUpload Callback invoked when the upload action is triggered.
 * @param onDelete Callback invoked when the delete action is triggered.
 * @param enabled Determines if the card is enabled and interactive.
 * @param isUploaded Indicates if the file has been successfully uploaded.
 * @param isUploading Indicates if a file upload process is currently ongoing.
 * @param modifier Modifier to apply to the card layout.
 * @param fileName Optional name of the file to be displayed when uploaded.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun FileUploadCard(
    onUpload: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean,
    isUploaded: Boolean,
    isUploading: Boolean,
    modifier: Modifier = Modifier,
    fileName: String? = null
) {
    val composition = rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/file_upload.json").decodeToString()
        )
    }

    Card(
        onClick = onUpload,
        modifier = modifier,
        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_MEDIUM.dp),
        enabled = enabled
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(Spacing.MEDIUM.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    isUploading -> {
                        Image(
                            painter = rememberLottiePainter(
                                composition = composition.value,
                                isPlaying = true,
                                iterations = Compottie.IterateForever
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(IMAGE_SIZE.dp)
                        )
                    }

                    isUploaded -> {
                        Image(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(IMAGE_SIZE.dp)
                        )
                        Text(
                            text = fileName ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    else -> {
                        Image(
                            painter = rememberLottiePainter(
                                composition = composition.value,
                                isPlaying = false
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(IMAGE_SIZE.dp)
                        )
                        Text(
                            text = stringResource(Res.string.upload_document),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (isUploaded) IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Padding.SMALL.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.delete_button_label)
                )
            }
        }
    }
}