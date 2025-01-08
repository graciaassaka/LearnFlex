package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.presentation.action.LibraryAction
import org.example.composeApp.presentation.state.LibraryUIState
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.composeApp.presentation.viewModel.util.ResourceProvider
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.curriculum.FetchCurriculaByUserUseCase
import org.example.shared.domain.use_case.curriculum.FetchCurriculumBundleUseCase
import org.example.shared.domain.use_case.curriculum.GenerateCurriculumUseCase
import org.example.shared.domain.use_case.library.UploadCurriculumAndModulesUseCase
import org.example.shared.domain.use_case.module.GenerateModuleUseCase
import org.example.shared.domain.use_case.profile.FetchProfileUseCase
import org.example.shared.domain.use_case.syllabus.SummarizeSyllabusUseCase
import java.io.File


/**
 * ViewModel for managing the user's library, including operations such as
 * retrieving curricula, generating new content, and managing modules and lessons.
 *
 * This ViewModel interacts with various use cases to handle business logic
 * and manages the UI state of the Library screen.
 *
 * @property fetchProfileUseCase Use case to fetch the user's profile.
 * @property fetchCurriculaByUserUseCase Use case to fetch curricula associated with the user.
 * @property fetchCurriculumBundleUseCase Use case for retrieving curriculum bundles.
 * @property summarizeSyllabusUseCase Use case for summarizing syllabus files.
 * @property generateCurriculumUseCase Use case for generating curricula.
 * @property generateModuleUseCase Use case for generating modules within a curriculum.
 * @property uploadCurriculumAndModulesUseCase Use case for uploading curriculum and module content.
 * @property resourceProvider Provides application resources such as strings.
 * @property dispatcher Coroutine dispatcher for executing asynchronous tasks.
 * @property syncManagers List of synchronization managers for handling database records.
 * @property sharingStarted Determines the sharing behavior of flows in StateFlow.
 */
class LibraryViewModel(
    private val fetchProfileUseCase: FetchProfileUseCase,
    private val fetchCurriculaByUserUseCase: FetchCurriculaByUserUseCase,
    private val fetchCurriculumBundleUseCase: FetchCurriculumBundleUseCase,
    private val summarizeSyllabusUseCase: SummarizeSyllabusUseCase,
    private val generateCurriculumUseCase: GenerateCurriculumUseCase,
    private val generateModuleUseCase: GenerateModuleUseCase,
    private val uploadCurriculumAndModulesUseCase: UploadCurriculumAndModulesUseCase,
    private val resourceProvider: ResourceProvider,
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
        when (action) {
            is LibraryAction.Refresh -> refresh()
            is LibraryAction.SummarizeSyllabus -> summarizeSyllabus(action.file)
            is LibraryAction.DeleteSyllabusFile -> deleteSyllabusFile()
            is LibraryAction.EditSyllabusDescription -> editSyllabusDescription(action.description)
            is LibraryAction.GenerateCurriculum if displayMode == LibraryUIState.DisplayMode.Edit -> showDiscardWarningDialog()
            is LibraryAction.GenerateCurriculum -> generateJob = generateCurriculum()
            is LibraryAction.CancelGeneration -> generateJob?.cancel()
            is LibraryAction.GenerateModule -> generateJob = generateModule(action.title)
            is LibraryAction.RemoveModule -> removeModule(action.title)
            is LibraryAction.RemoveLesson -> removeLesson(action.lessonTitle, action.moduleId)
            is LibraryAction.SaveContent -> uploadContent()
            is LibraryAction.DiscardContent -> discardContent()
            is LibraryAction.EditFilterQuery -> editFilterQuery(action.query)
            is LibraryAction.ClearFilterQuery -> editFilterQuery("")
            is LibraryAction.OpenCurriculum if displayMode == LibraryUIState.DisplayMode.Edit -> showDiscardWarningDialog()
            is LibraryAction.OpenCurriculum -> openCurriculum(action.curriculumId)
            is LibraryAction.HandleError -> handleError(action.error)
            is LibraryAction.Navigate if displayMode == LibraryUIState.DisplayMode.Edit -> showDiscardWarningDialog()
            is LibraryAction.Navigate -> navigate(action.destination)
            is LibraryAction.HideDiscardWarningDialog -> hideDiscardWarningDialog()
        }
    }

    /**
     * Refreshes the library state by loading profile and curricula data.
     */
    private fun refresh() {
        _state.update { it.copy(isDownloading = true) }
        viewModelScope.launch(dispatcher) {
            try {
                val profile = fetchProfileUseCase().getOrThrow()
                _state.update { it.copy(profile = profile) }

                val curricula = fetchCurriculaByUserUseCase(profile.id).getOrThrow()
                _state.update { it.copy(curricula = curricula, filteredCurricula = curricula) }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _state.update { it.copy(isDownloading = false) }
            }
        }
    }

    /**
     * Summarizes the syllabus from the given file.
     *
     * @param file The file containing the syllabus.
     */
    private fun summarizeSyllabus(file: File) {
        generateJob = viewModelScope.launch(dispatcher) {
            try {
                require(file.exists()) { resourceProvider.getString(Res.string.file_not_found_error) }
                require(file.extension in setOf("pdf", "docx")) { resourceProvider.getString(Res.string.file_unsupported_error) }

                _state.update { it.copy(isUploading = true) }

                summarizeSyllabusUseCase(file).collect { result ->
                    _state.update { it.copy(syllabusDescription = result.getOrThrow(), syllabusFile = file) }
                }
            } catch (_: TimeoutCancellationException) {
                showSnackbar(resourceProvider.getString(Res.string.content_generation_took_too_long_error), SnackbarType.Error)
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _state.update { it.copy(isUploading = false) }
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
    private fun generateCurriculum() = viewModelScope.launch(dispatcher) {
        try {
            val profile = checkNotNull(_state.value.profile)
            val description = _state.value.syllabusDescription

            check(description.isNotBlank()) {
                resourceProvider.getString(Res.string.blank_syllabus_description_warning)
            }
            _state.update { it.copy(isGenerating = true) }

            generateCurriculumUseCase(description, profile).collect { result ->
                val info = result.getOrThrow()
                val curriculum = Curriculum(
                    title = info.title,
                    description = info.description,
                    content = info.content
                )
                _state.update { it.copy(curriculum = curriculum, displayMode = LibraryUIState.DisplayMode.Edit) }
            }
        } catch (_: TimeoutCancellationException) {
            showSnackbar(resourceProvider.getString(Res.string.content_generation_took_too_long_error), SnackbarType.Error)
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _state.update { it.copy(isGenerating = false) }
        }
    }


    /**
     * Generates a module for the curriculum.
     *
     * @param title The title of the module to generate.
     */
    private fun generateModule(title: String) = viewModelScope.launch(dispatcher) {
        try {
            require(_state.value.modules.all { it.title != title }) {
                resourceProvider.getString(Res.string.module_already_exists_warning)
            }
            val profile = checkNotNull(_state.value.profile)
            val curriculum = checkNotNull(_state.value.curriculum)

            _state.update { it.copy(isGenerating = true) }

            generateModuleUseCase(title, profile, curriculum).collect { result ->
                val info = result.getOrThrow()
                val module = Module(
                    title = title,
                    description = info.description,
                    content = info.content
                )
                _state.update { it.copy(modules = it.modules + module) }
            }
        } catch (_: TimeoutCancellationException) {
            showSnackbar(resourceProvider.getString(Res.string.content_generation_took_too_long_error), SnackbarType.Error)
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _state.update { it.copy(isGenerating = false) }
        }
    }

    /**
     * Removes a module from the current curriculum and updates the module list.
     *
     * @param title The title of the module to be removed.
     */
    private fun removeModule(title: String) = _state.update { current ->
        current.copy(
            modules = current.modules.filter { it.title != title },
            curriculum = current.curriculum?.copy(
                content = current.curriculum.content
                    .toMutableList()
                    .apply { removeIf { it == title } }
            )
        )
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
    private fun uploadContent() = viewModelScope.launch(dispatcher) {
        try {
            val successMessage = async { resourceProvider.getString(Res.string.save_content_success) }
            val profile = checkNotNull(_state.value.profile)
            val curriculum = checkNotNull(_state.value.curriculum)
            val modules = _state.value.modules

            check(curriculum.content.size == modules.size) {
                resourceProvider.getString(Res.string.modules_title_mismatch_warning)
            }
            uploadCurriculumAndModulesUseCase(profile.id, curriculum, modules)
                .onSuccess { showSnackbar(successMessage.await(), SnackbarType.Success); discardContent() }
                .onFailure { throw it }
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _state.update { it.copy(isUploading = false) }
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
     *
     * @param curriculumId The ID of the curriculum to open.
     */
    private fun openCurriculum(curriculumId: String) = viewModelScope.launch(dispatcher) {
        try {
            val profile = checkNotNull(_state.value.profile)
            val curriculum = checkNotNull(_state.value.curricula.find { it.id == curriculumId })

            val bundle = fetchCurriculumBundleUseCase(profile.id, curriculum)
            _state.update {
                it.copy(
                    isDownloading = true,
                    curriculum = bundle.curriculum,
                    modules = bundle.modules,
                    displayMode = LibraryUIState.DisplayMode.View
                )
            }
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _state.update { it.copy(isDownloading = false) }
        }
    }
}