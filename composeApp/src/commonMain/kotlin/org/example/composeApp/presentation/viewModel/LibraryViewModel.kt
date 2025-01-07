package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.curriculum.DeleteCurriculumUseCase
import org.example.shared.domain.use_case.curriculum.FetchCurriculaByUserUseCase
import org.example.shared.domain.use_case.curriculum.GenerateCurriculumUseCase
import org.example.shared.domain.use_case.curriculum.UploadCurriculumUseCase
import org.example.shared.domain.use_case.module.GenerateModuleUseCase
import org.example.shared.domain.use_case.module.UploadModulesUseCase
import org.example.shared.domain.use_case.profile.FetchProfileUseCase
import org.example.shared.domain.use_case.syllabus.SummarizeSyllabusUseCase
import org.example.composeApp.presentation.action.LibraryAction
import org.example.composeApp.presentation.state.LibraryUIState
import org.example.composeApp.presentation.ui.util.SnackbarType
import java.io.File

/**
 * ViewModel for managing the library's state and actions.
 *
 * @property fetchProfileUseCase Use case for retrieving profiles.
 * @property fetchCurriculaByUserUseCase Use case for retrieving all curricula.
 * @property summarizeSyllabusUseCase Use case for summarizing syllabi.
 * @property generateCurriculumUseCase Use case for generating curricula.
 * @property generateModuleUseCase Use case for generating modules.
 * @property uploadCurriculumUseCase Use case for uploading curricula.
 * @property uploadModulesUseCase Use case for uploading modules.
 * @property deleteCurriculumUseCase Use case for deleting curricula.
 * @property dispatcher Coroutine dispatcher for managing background tasks.
 * @property syncManagers List of sync managers for database records.
 * @property sharingStarted SharingStarted strategy for state flow.
 */
class LibraryViewModel(
    private val fetchProfileUseCase: FetchProfileUseCase,
    private val fetchCurriculaByUserUseCase: FetchCurriculaByUserUseCase,
    private val summarizeSyllabusUseCase: SummarizeSyllabusUseCase,
    private val generateCurriculumUseCase: GenerateCurriculumUseCase,
    private val generateModuleUseCase: GenerateModuleUseCase,
    private val uploadCurriculumUseCase: UploadCurriculumUseCase,
    private val uploadModulesUseCase: UploadModulesUseCase,
    private val deleteCurriculumUseCase: DeleteCurriculumUseCase,
    private val dispatcher: CoroutineDispatcher,
    syncManagers: List<SyncManager<DatabaseRecord>>,
    sharingStarted: SharingStarted
) : BaseViewModel(dispatcher, syncManagers) {
    private val _state = MutableStateFlow(LibraryUIState())
    val state = _state.onStart { refresh() }.stateIn(viewModelScope, sharingStarted, _state.value)

    private var generateJob: Job? = null

    /**
     * Handles various actions related to the library.
     *
     * @param action The action to handle.
     */
    fun handleAction(action: LibraryAction) = with(_state.value) {
        try {
            when (action) {
                is LibraryAction.Refresh -> refresh()
                is LibraryAction.SummarizeSyllabus -> summarizeSyllabus(action.file)
                is LibraryAction.DeleteSyllabusFile -> deleteSyllabusFile()
                is LibraryAction.EditSyllabusDescription -> editSyllabusDescription(action.description)
                is LibraryAction.GenerateCurriculum -> generateCurriculum(syllabusDescription, profile!!)
                is LibraryAction.CancelGeneration -> generateJob?.cancel()
                is LibraryAction.GenerateModule -> generateModule(profile!!, curriculum!!, action.title)
                is LibraryAction.RemoveModule -> removeModule(action.title, modules)
                is LibraryAction.RemoveLesson -> removeLesson(action.lessonTitle, action.moduleId)
                is LibraryAction.SaveContent -> saveContent(profile!!, curriculum!!, modules, action.successMessage)
                is LibraryAction.DiscardContent -> discardContent()
                is LibraryAction.EditFilterQuery -> editFilterQuery(action.query)
                is LibraryAction.ClearFilterQuery -> editFilterQuery("")
                is LibraryAction.OpenCurriculum -> openCurriculum(action.curriculumId)
                is LibraryAction.HandleError -> handleError(action.error)
                is LibraryAction.Navigate if curriculum == null -> navigate(action.destination)
                is LibraryAction.Navigate -> showDiscardWarningDialog()
                is LibraryAction.HideDiscardWarningDialog -> hideDiscardWarningDialog()
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    /**
     * Refreshes the library state by loading profile and curricula data.
     */
    private fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        viewModelScope.launch(dispatcher) {
            try {
                val profile = fetchProfileUseCase().getOrThrow()
                _state.update { it.copy(profile = profile) }

                val curricula = fetchCurriculaByUserUseCase(profile.id).getOrThrow()
                _state.update { it.copy(curricula = curricula, filteredCurricula = curricula) }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _state.update { it.copy(isRefreshing = false) }
            }
        }
    }

    /**
     * Summarizes the syllabus from the given file.
     *
     * @param file The file containing the syllabus.
     */
    private fun summarizeSyllabus(file: File) = with(_state) {
        update { it.copy(isUploading = true) }
        generateJob = viewModelScope.launch(dispatcher) {
            try {
                withTimeout(TIMEOUT) {
                    summarizeSyllabusUseCase(file).collect { result ->
                        result.fold(
                            onSuccess = { description -> update { it.copy(syllabusFile = file, syllabusDescription = description) } },
                            onFailure = ::handleError
                        )
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                update { it.copy(isUploading = false) }
            }
        }
    }

    /**
     * Deletes the syllabus file.
     */
    private fun deleteSyllabusFile() = _state.update { it.copy(syllabusFile = null) }

    /**
     * Edits the syllabus description.
     *
     * @param description The new description for the syllabus.
     */
    private fun editSyllabusDescription(description: String) = _state.update { it.copy(syllabusDescription = description) }

    /**
     * Generates content for the library.
     */
    private fun generateCurriculum(syllabusDescription: String, profile: Profile) = with(_state) {
        require(value.syllabusDescription.isNotBlank()) { "The syllabus description must not be blank." }
        update { it.copy(isDownloading = true) }

        generateJob = viewModelScope.launch(dispatcher) {
            try {
                withTimeout(TIMEOUT) {
                    generateCurriculumUseCase(syllabusDescription, profile).collect { result ->
                        val info = result.getOrThrow()
                        val curriculum = Curriculum(
                            title = info.title,
                            description = info.description,
                            content = info.content
                        )
                        update { it.copy(curriculum = curriculum) }
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                update { it.copy(isDownloading = false) }
            }

        }
    }

    /**
     * Generates a module for the curriculum.
     *
     * @param profile The profile to generate the module for.
     * @param curriculum The curriculum to generate the module for.
     * @param title The title of the module.
     */
    private fun generateModule(
        profile: Profile,
        curriculum: Curriculum,
        title: String
    ) = with(_state) {
        if (value.modules.any { it.title == title }) {
            showSnackbar("A module with the same title already exists.", SnackbarType.Error)
            return
        }
        update { it.copy(isDownloading = true) }
        generateJob = viewModelScope.launch(dispatcher) {
            try {
                withTimeout(TIMEOUT) {
                    generateModuleUseCase(title, profile, curriculum).collect { result ->
                        val info = result.getOrThrow()
                        val module = Module(
                            title = info.title,
                            description = info.description,
                            content = info.content
                        )
                        update { it.copy(modules = it.modules + module) }
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                update { it.copy(isDownloading = false) }
            }
        }
    }


    /**
     * Removes a module from the current curriculum and updates the module list.
     *
     * @param title The title of the module to be removed.
     * @param modules The current list of modules associated with the curriculum.
     */
    private fun removeModule(title: String, modules: List<Module>) = with(_state) {
        update { current ->
            current.copy(
                curriculum = current.curriculum?.copy(content = current.curriculum.content.toMutableList().apply { removeIf { it == title } }),
                modules = modules.filter { it.title != title }
            )
        }
    }

    /**
     * Removes a lesson from a specific module based on the given index and module ID.
     *
     * @param title The title of the lesson to be removed.
     * @param moduleId The unique identifier of the module from which the lesson should be removed.
     */
    private fun removeLesson(title: String, moduleId: String) = with(_state) {
        update { current ->
            current.copy(
                modules = current.modules.map { module ->
                    if (module.id != moduleId) module
                    else module.copy(content = module.content.toMutableList().apply { removeIf { it == title } })
                }
            )
        }
    }

    /**
     * Saves the generated content to the database.
     */
    private fun saveContent(
        profile: Profile,
        curriculum: Curriculum,
        modules: List<Module>,
        successMessage: String
    ) = with(_state) {
        require(curriculum.content.size == modules.size) {
            "The number of modules must match the number of module titles."
        }
        update { it.copy(isDownloading = true) }

        viewModelScope.launch(dispatcher) {
            var isCurriculumUploaded = false
            try {
                uploadCurriculumUseCase(curriculum, profile.id)
                    .onSuccess { isCurriculumUploaded = true }
                    .onFailure { throw it }

                uploadModulesUseCase(modules, profile.id, curriculum.id).getOrThrow()
                showSnackbar(successMessage, SnackbarType.Success)
            } catch (e: Exception) {
                handleError(e)
                if (isCurriculumUploaded) deleteCurriculumUseCase(curriculum, profile.id).onFailure(::handleError)
            } finally {
                discardContent()
                update { it.copy(isDownloading = false) }
            }
        }
    }

    /**
     * Discards the generated content.
     */
    private fun discardContent() = with(_state) { update { it.copy(curriculum = null, modules = emptyList()) } }

    /**
     * Updates the state to display the discard warning dialog.
     */
    private fun showDiscardWarningDialog() = _state.update { it.copy(showDiscardWarningDialog = true) }

    /**
     * Updates the current state to hide the discard warning dialog.
     */
    private fun hideDiscardWarningDialog() = _state.update { it.copy(showDiscardWarningDialog = false) }

    /**
     * Edits the search query.
     *
     * @param query The new search query.
     */
    private fun editFilterQuery(query: String) = _state.update { current ->
        current.copy(
            filterQuery = query,
            filteredCurricula = if (query.isBlank()) current.curricula
            else current.curricula.filter { it.title.contains(query, ignoreCase = true) }
        )
    }

    /**
     * Opens the curriculum.
     */
    private fun openCurriculum(curriculumId: String) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val TIMEOUT = 60000L
    }
}