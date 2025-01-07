package org.example.shared.presentation.state

import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile
import java.io.File

/**
 * Represents the UI state for the library.
 *
 * @property profile The user's profile.
 * @property syllabusFile The file containing the syllabus, if available.
 * @property syllabusDescription A description of the syllabus.
 * @property curricula A list of curricula.
 * @property filteredCurricula A list of curricula that match the search query.
 * @property curriculum The currently selected curriculum, if any.
 * @property modules A list of modules.
 * @property filterQuery The current search query.
 * @property showDiscardWarningDialog Indicates if the discard warning dialog should be shown.
 * @property isRefreshing Indicates if the data is currently loading.
 * @property isUploading Indicates if an upload is in progress.
 * @property isDownloading Indicates if a download is in progress.
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
    val isRefreshing: Boolean = false,
    val isUploading: Boolean = false,
    val isDownloading: Boolean = false,
)