package org.example.composeApp.presentation.state

import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile
import java.io.File

/**
 * Represents the state of the library UI.
 *
 * @property profile The user's profile information.
 * @property syllabusFile The syllabus file.
 * @property syllabusDescription The description of the syllabus.
 * @property curricula The user's curricula.
 * @property filteredCurricula The filtered curricula.
 * @property curriculum The user's curriculum.
 * @property modules The user's modules.
 * @property filterQuery The filter query.
 * @property showDiscardWarningDialog Indicates if the discard warning dialog should be shown.
 * @property isUploading Indicates if the syllabus is currently uploading.
 * @property isDownloading Indicates if the syllabus is currently downloading.
 * @property isGenerating Indicates if the curriculum is currently generating.
 * @property displayMode The display mode of the library.
 */
data class LibraryUIState(
    val profile: Profile? = null,
    val syllabusFile: File? = null,
    val syllabusDescription: String = "",
    val curricula: List<Curriculum> = emptyList(),
    val filteredCurricula: List<Curriculum> = emptyList(),
    val curriculum: Curriculum? = null,
    val modules: List<Module> = emptyList(),
    val filterQuery: String = "",
    val showDiscardWarningDialog: Boolean = false,
    val isUploading: Boolean = false,
    val isDownloading: Boolean = false,
    val isGenerating: Boolean = false,
    val displayMode: DisplayMode = DisplayMode.View
) {
    enum class DisplayMode {
        View,
        Edit
    }
}