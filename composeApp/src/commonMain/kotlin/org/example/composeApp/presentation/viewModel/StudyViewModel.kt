package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.generate_lesson_success
import learnflex.composeapp.generated.resources.generate_section_success
import learnflex.composeapp.generated.resources.save_quiz_result_success
import org.example.composeApp.presentation.action.StudyAction
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.StudyUIState
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.*
import org.example.shared.domain.model.util.BundleManager
import org.example.shared.domain.use_case.lesson.FetchLessonQuizQuestionsUseCase
import org.example.shared.domain.use_case.lesson.GenerateAndUploadLessonUseCase
import org.example.shared.domain.use_case.lesson.RegenerateAndUpdateLessonUseCase
import org.example.shared.domain.use_case.lesson.UpdateLessonUseCase
import org.example.shared.domain.use_case.module.FetchModuleQuizQuestionsUseCase
import org.example.shared.domain.use_case.module.UpdateModuleUseCase
import org.example.shared.domain.use_case.section.FetchSectionQuizQuestionsUseCase
import org.example.shared.domain.use_case.section.GenerateAndUploadSectionUseCase
import org.example.shared.domain.use_case.section.RegenerateAndUpdateSectionUseCase
import org.example.shared.domain.use_case.section.UpdateSectionUseCase
import org.example.shared.domain.use_case.session.UploadSessionUseCase

/**
 * ViewModel responsible for handling the study-related functionality, including generating and managing quizzes,
 * lessons, sections, and modules within a curriculum. This class uses a range of use cases to perform these tasks
 * while maintaining and updating the application state accordingly.
 *
 * @property fetchLessonQuizQuestionsUseCase Use case for fetching quiz questions specific to a lesson.
 * @property fetchModuleQuizQuestionsUseCase Use case for fetching quiz questions specific to a module.
 * @property fetchSectionQuizQuestionsUseCase Use case for fetching quiz questions specific to a section.
 * @property generateAndUploadLessonUseCase Use case for generating and uploading lessons.
 * @property generateAndUploadSectionUseCase Use case for generating and uploading sections.
 * @property regenerateAndUpdateLessonUseCase Use case for regenerating and updating lessons.
 * @property regenerateAndUpdateSectionUseCase Use case for regenerating and updating sections.
 * @property updateModuleUseCase Use case for updating module details.
 * @property updateLessonUseCase Use case for updating lesson details.
 * @property updateSectionUseCase Use case for updating section details.
 * @property uploadSessionUseCase Use case for uploading sessions.
 * @property savedState Manages state restoration for the ViewModel.
 * @property _state Represents the internal mutable state of the ViewModel.
 * @property state Represents the immutable external state of the ViewModel.
 * @property bundleManager Manages storage and passing of data between activities or fragments.
 * @property generateJob Tracks running generation jobs to ensure proper lifecycle handling.
 */
class StudyViewModel(
    private val fetchLessonQuizQuestionsUseCase: FetchLessonQuizQuestionsUseCase,
    private val fetchModuleQuizQuestionsUseCase: FetchModuleQuizQuestionsUseCase,
    private val fetchSectionQuizQuestionsUseCase: FetchSectionQuizQuestionsUseCase,
    private val generateAndUploadLessonUseCase: GenerateAndUploadLessonUseCase,
    private val generateAndUploadSectionUseCase: GenerateAndUploadSectionUseCase,
    private val regenerateAndUpdateLessonUseCase: RegenerateAndUpdateLessonUseCase,
    private val regenerateAndUpdateSectionUseCase: RegenerateAndUpdateSectionUseCase,
    private val updateModuleUseCase: UpdateModuleUseCase,
    private val updateLessonUseCase: UpdateLessonUseCase,
    private val updateSectionUseCase: UpdateSectionUseCase,
    private val uploadSessionUseCase: UploadSessionUseCase,
    private val savedState: SavedStateHandle
) : ScreenViewModel() {

    private val _state = MutableStateFlow(StudyUIState())
    val state = _state.asStateFlow()
    private lateinit var bundleManager: BundleManager
    private var generateJob: Job? = null

    init {
        val curriculumId = savedState.get<String>("curriculumId").orEmpty()
        val moduleId = savedState.get<String>("moduleId").orEmpty()
        val lessonId = savedState.get<String>("lessonId").orEmpty()

        viewModelScope.launch(dispatcher) {
            learnFlexViewModel.state.collect { appState ->
                val isRefreshing = appState.isLoading
                bundleManager = appState.bundleManager

                appState.error?.let(::handleError)
                appState.profile?.let { profile -> updateState(isRefreshing, profile, curriculumId, moduleId, lessonId) }
            }
        }
    }

    /**
     * Updates the current state of the study view model with the provided user profile, curriculum, module, and lesson information.
     *
     * @param isRefreshing Indicator flag to denote if the data is being refreshed. Default is false.
     * @param profile The user profile information.
     * @param curriculumId The unique identifier of the curriculum to load.
     * @param moduleId The unique identifier of the module to load.
     * @param lessonId The unique identifier of the lesson to load.
     */
    private fun updateState(
        isRefreshing: Boolean = false,
        profile: Profile,
        curriculumId: String,
        moduleId: String,
        lessonId: String,
    ) = with(bundleManager) {
        val curriculum = getCurriculumByKeyOrLatest(curriculumId)
        val modules = curriculum?.id?.let(::getModulesByCurriculum).orEmpty()
        val module = curriculum?.id?.let { getModuleByKeyOrLatest(key = Bundle.ModuleKey(it, moduleId)) }
        val lessons = getLessonsByModule(key = Bundle.ModuleKey(curriculum?.id.orEmpty(), module?.id.orEmpty()))
        val lesson = module?.id?.let { getLessonByKeyOrLatest(Bundle.LessonKey(curriculum.id, it, lessonId)) }
        val lessonKey = Bundle.LessonKey(curriculum?.id.orEmpty(), module?.id.orEmpty(), lesson?.id.orEmpty())
        val sections = lessonKey.let(::getSectionsByLesson)

        savedState["curriculumId"] = curriculum?.id
        savedState["moduleId"] = module?.id
        savedState["lessonId"] = lesson?.id

        if (curriculum != null) _state.update { it.copy(session = Session()) }

        _state.update {
            it.copy(
                profile = profile,
                curricula = getCurricula(),
                curriculum = curriculum,
                modules = modules,
                module = module,
                lessons = lessons,
                lesson = lesson,
                sections = sections,
                isRefreshing = isRefreshing
            )
        }
    }

    /**
     * Handles various study-related actions and triggers the appropriate response
     * based on the type of action provided.
     *
     * @param action The action to be handled. It defines the specific operation
     *               or task to execute, such as answering a quiz question,
     *               generating content, navigation, or other study-related activities.
     */
    fun handleAction(action: StudyAction) = when (action) {
        is StudyAction.AnswerQuizQuestion  -> handleQuizQuestionAnswered(action.answer)
        is StudyAction.CancelGeneration    -> cancelGeneration()
        is StudyAction.GenerateLesson      -> generateJob = generateLesson(action.title)
        is StudyAction.GenerateLessonQuiz  -> generateJob = generateLessonQuiz()
        is StudyAction.GenerateModuleQuiz  -> generateJob = generateModuleQuiz()
        is StudyAction.GenerateSection     -> generateJob = generateSection(action.title)
        is StudyAction.GenerateSectionQuiz -> generateJob = generateSectionQuiz(action.id)
        is StudyAction.GoBack              -> goBack()
        is StudyAction.Navigate            -> navigate(action.route)
        is StudyAction.Refresh             -> refresh()
        is StudyAction.RegenerateLesson    -> regenerateAndUpdateLesson(action.id)
        is StudyAction.RegenerateSection   -> regenerateAndUpdateSection(action.id)
        is StudyAction.SaveQuizResult      -> saveQuizResult()
        is StudyAction.SelectCurriculum    -> selectCurriculum(action.curriculumId)
        is StudyAction.SelectLesson        -> selectLesson(action.lessonId)
        is StudyAction.SelectModule        -> selectModule(action.moduleId)
        is StudyAction.SubmitQuiz          -> submitQuiz()
    }

    /**
     * Handles the user's navigation to the previous screen or state within the study workflow.
     *
     * This function performs the following actions:
     * - Introduces a delay of 300 milliseconds to allow for UI consistency or state transitions.
     * - Removes the "lessonId" from the saved state, effectively resetting the associated lesson context.
     * - Updates the state to reset the current lesson to null.
     *
     * Utilizes the `viewModelScope` to launch a coroutine, ensuring the operations run on a separate thread
     * and do not block the main thread.
     */
    private fun goBack() = viewModelScope.launch {
        delay(300)
        _state.update { it.copy(lesson = null) }
    }

    /**
     * Cancels the ongoing generation process and updates the state to reflect the cancellation.
     *
     * This method terminates the current generation job, if it exists, and updates the
     * state to stop indicating that generation is in progress. Additionally, it hides the quiz pane.
     */
    private fun cancelGeneration() {
        generateJob?.cancel()
        _state.update { it.copy(isGenerating = false, showQuizPane = false) }
    }

    /**
     * Generates a module quiz based on the current state and user's level preferences.
     *
     * This function performs the following operations:
     * - Retrieves the current module and user profile from the state.
     * - Determines the quiz level based on the user's preferences.
     * - Updates the UI state to indicate that quiz generation has started.
     * - Creates a quiz object associated with the current module.
     * - Invokes the `updateQuizQuestions` method to populate the quiz with questions.
     * - Handles any exceptions that occur during the process using the `handleError` method.
     *
     * Execution is performed within a coroutine scope (`viewModelScope`) on a specified dispatcher.
     */
    private fun generateModuleQuiz() = with(_state) {
        viewModelScope.launch(dispatcher) {
            try {
                val module = checkNotNull(value.module)
                val level = Level.valueOf(checkNotNull(value.profile).preferences.level)
                update {
                    it.copy(
                        isGenerating = true,
                        showQuizPane = true,
                        quizCollection = Collection.MODULES,
                        quiz = Quiz(
                            owner = module.id,
                            questionNumber = FetchModuleQuizQuestionsUseCase.NUMBER_OF_QUESTIONS
                        )
                    )
                }
                updateQuizQuestions(module.title, level, fetchModuleQuizQuestionsUseCase::invoke)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * Generates a quiz for the currently selected lesson.
     *
     * The method uses the lesson details from the current state and the user's profile preferences
     * to determine the level and fetch quiz questions accordingly. It updates the state to reflect
     * the quiz generation process status and associated details. Upon successful quiz generation, the
     * method updates the state with the generated quiz questions. In case of an error, it handles the
     * error gracefully by utilizing the error handler.
     *
     * Key steps include:
     * - Ensuring the lesson and user profile data are not null.
     * - Determining the quiz level based on user profile preferences.
     * - Updating the state to indicate the start of quiz generation.
     * - Fetching quiz questions through a suspendable method and updating the state with the quiz.
     * - Handling exceptions by invoking the error handler.
     *
     * This method operates within the lifecycle-aware scope of `viewModelScope`.
     */
    private fun generateLessonQuiz() = with(_state) {
        viewModelScope.launch(dispatcher) {
            try {
                val lesson = checkNotNull(value.lesson)
                val level = Level.valueOf(checkNotNull(value.profile).preferences.level)
                update {
                    it.copy(
                        isGenerating = true,
                        showQuizPane = true,
                        quizCollection = Collection.LESSONS,
                        quiz = Quiz(
                            owner = lesson.id,
                            questionNumber = FetchLessonQuizQuestionsUseCase.NUMBER_OF_QUESTIONS
                        )
                    )
                }
                updateQuizQuestions(lesson.title, level, fetchLessonQuizQuestionsUseCase::invoke)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * Generates a quiz for a specific section identified by the provided ID.
     * The quiz generation process retrieves the section's title, determines the user's learning level,
     * and updates the state with a new quiz object configured for the section. It then fetches the quiz
     * questions asynchronously and updates the state accordingly. Handles any errors that may occur during
     * this process.
     *
     * @param id The unique identifier of the section for which the quiz is to be generated.
     */
    private fun generateSectionQuiz(id: String) = with(_state) {
        viewModelScope.launch(dispatcher) {
            try {
                val title = checkNotNull(value.sections.find { it.id == id }).title
                val level = Level.valueOf(checkNotNull(value.profile).preferences.level)
                update {
                    it.copy(
                        isGenerating = true,
                        showQuizPane = true,
                        quizCollection = Collection.SECTIONS,
                        quiz = Quiz(
                            owner = id,
                            questionNumber = FetchSectionQuizQuestionsUseCase.NUMBER_OF_QUESTIONS
                        )
                    )
                }
                updateQuizQuestions(title, level, fetchSectionQuizQuestionsUseCase::invoke)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * Updates the quiz questions by fetching a new question and appending it to the current quiz.
     *
     * @param title The title used for fetching quiz questions.
     * @param level The level of the quiz (e.g., Beginner, Intermediate, Advanced).
     * @param fetchFunction A function that fetches a flow of questions based on the title and level.
     */
    private suspend fun updateQuizQuestions(
        title: String,
        level: Level,
        fetchFunction: (String, Level) -> Flow<Result<Question>>
    ) = with(_state) {
        fetchFunction(title, level)
            .collect { result ->
                val question = result.getOrThrow()
                val quiz = value.quiz.copy(questions = value.quiz.questions + question)
                update { it.copy(quiz = quiz, isGenerating = false) }
            }
    }

    /**
     * Handles the event when a quiz question is answered by the user.
     * Updates the quiz state by appending the provided answer to the list of answers.
     *
     * @param answer The answer provided by the user for the current quiz question.
     */
    private fun handleQuizQuestionAnswered(answer: Any) = _state.update { current ->
        current.copy(quiz = current.quiz.copy(answers = current.quiz.answers + answer))
    }

    /**
     * Submits the current quiz and updates the state to reflect the submission result.
     *
     * This method performs the following actions:
     * - Grades the current quiz by calculating the score based on the user's answers.
     * - Updates the state with the graded quiz.
     * - Shows the quiz result dialog to the user.
     * - Hides the quiz pane from the user interface.
     */
    private fun submitQuiz() = _state.update {
        it.copy(quiz = it.quiz.grade(), showQuizResultDialog = true, showQuizPane = false)
    }

    /**
     * Saves the quiz result based on the active quiz's associated collection type.
     *
     * This method determines the current quiz collection type from the state
     * and delegates the save process to the corresponding method:
     * - `saveModuleQuizResult` for module quizzes
     * - `saveLessonQuizResult` for lesson quizzes
     * - `saveSectionQuizResult` for section quizzes
     *
     * If the collection type is not recognized, an `IllegalArgumentException` is thrown.
     * Regardless of the outcome, the `refresh` method is called to update the relevant state.
     */
    private fun saveQuizResult() = try {
        when (_state.value.quizCollection) {
            Collection.MODULES  -> saveModuleQuizResult()
            Collection.LESSONS  -> saveLessonQuizResult()
            Collection.SECTIONS -> saveSectionQuizResult()
            else                -> throw IllegalArgumentException()
        }
    } finally {
        refresh()
    }

    /**
     * Saves the result of a module quiz and updates the state accordingly.
     *
     * This method performs the following steps:
     * - Retrieves the profile ID, curriculum ID, and module from the current state.
     * - Updates the module's quiz score with the latest score from the state.
     * - Displays a success message upon successfully saving the quiz result.
     * - Updates the state to reflect the ongoing upload and resets it afterwards.
     *
     * If an error occurs during the process, it handles the error by showing an
     * appropriate snackbar with the error message.
     *
     * The method is executed within a coroutine scope of `viewModelScope` and uses
     * a dispatcher to manage threading.
     */
    private fun saveModuleQuizResult() = viewModelScope.launch(dispatcher) {
        try {
            val profileId = checkNotNull(_state.value.profile).id
            val curriculumId = checkNotNull(_state.value.curriculum).id
            val module = checkNotNull(_state.value.module).updateQuizScore(_state.value.quiz.score)
            val successMessage = async { resourceProvider.getString(Res.string.save_quiz_result_success) }

            _state.update { it.copy(isUploading = true) }

            updateModuleUseCase(module, profileId, curriculumId).getOrThrow()
            showSnackbar(successMessage.await(), SnackbarType.Success)
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _state.update { it.copy(isUploading = false, showQuizResultDialog = false) }
        }
    }

    /**
     * Persists the result of a lesson quiz and provides user feedback upon completion.
     *
     * This function performs the following operations:
     * - Retrieves the current profile, curriculum, module, and lesson from the state. If any of these
     *   values are null, a `NullPointerException` is thrown.
     * - Updates the lesson with the latest quiz score if the new score exceeds the current score.
     * - Updates the UI state to indicate that an upload operation is in progress.
     * - Executes the `updateLessonUseCase` to save the updated lesson details in the repository.
     * - Shows a success snackbar to indicate the successful save operation.
     *
     * In case of an exception, it:
     * - Handles the error by invoking the `handleError` function, which shows an error snackbar.
     *
     * Once the operations are completed, regardless of success or failure, it:
     * - Updates the UI state to indicate that the upload operation has ended and hides the quiz result dialog.
     *
     * All actions that involve coroutine operations are performed on the `viewModelScope` using the
     * provided dispatcher.
     */
    private fun saveLessonQuizResult() = viewModelScope.launch(dispatcher) {
        try {
            val profileId = checkNotNull(_state.value.profile).id
            val curriculumId = checkNotNull(_state.value.curriculum).id
            val moduleId = checkNotNull(_state.value.module).id
            val lesson = checkNotNull(_state.value.lesson).updateQuizScore(_state.value.quiz.score)
            val successMessage = async { resourceProvider.getString(Res.string.save_quiz_result_success) }

            _state.update { it.copy(isUploading = true) }

            updateLessonUseCase(lesson, profileId, curriculumId, moduleId).getOrThrow()
            showSnackbar(successMessage.await(), SnackbarType.Success)
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _state.update { it.copy(isUploading = false, showQuizResultDialog = false) }
        }
    }

    /**
     * Saves the result of the current section quiz and updates the associated state and backend data.
     *
     * This function performs the following operations:
     * - Ensures the current profile, curriculum, module, lesson, and related section are not null.
     * - Updates the quiz score for the corresponding section if the new score is higher.
     * - Displays a success message on successful update of the quiz result.
     * - Handles errors and displays an error message if any exception occurs during the process.
     * - Updates the UI state to indicate the uploading process and hides the quiz result dialog once completed.
     *
     * The state management and backend update are executed asynchronously in the `viewModelScope`.
     *
     * @throws IllegalStateException if any required state value (profile, curriculum, module, lesson, or section) is null.
     */
    private fun saveSectionQuizResult() = viewModelScope.launch(dispatcher) {
        try {
            val profileId = checkNotNull(_state.value.profile).id
            val curriculumId = checkNotNull(_state.value.curriculum).id
            val moduleId = checkNotNull(_state.value.module).id
            val lessonId = checkNotNull(_state.value.lesson).id
            val section = checkNotNull(_state.value.sections.find { it.id == _state.value.quiz.owner })
                .updateQuizScore(_state.value.quiz.score)

            val successMessage = async { resourceProvider.getString(Res.string.save_quiz_result_success) }

            _state.update { it.copy(isUploading = true) }

            updateSectionUseCase(section, profileId, curriculumId, moduleId, lessonId).getOrThrow()
            showSnackbar(successMessage.await(), SnackbarType.Success)
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _state.update { it.copy(isUploading = false, showQuizResultDialog = false) }
        }
    }

    /**
     * Generates a lesson with the specified title, uploads it, and updates the application state.
     * Handles errors appropriately by displaying a snackbar message and ensures state updates
     * in both success and failure cases.
     *
     * @param title The title of the lesson to be generated.
     */
    private fun generateLesson(title: String) = viewModelScope.launch(dispatcher) {
        try {
            val profile = checkNotNull(_state.value.profile)
            val curriculum = checkNotNull(_state.value.curriculum)
            val module = checkNotNull(_state.value.module)

            _state.update { it.copy(isGenerating = true, isUploading = true) }

            val successMessage = async { resourceProvider.getString(Res.string.generate_lesson_success) }
            val lesson = generateAndUploadLessonUseCase(title, profile, curriculum, module).getOrThrow()

            showSnackbar(successMessage.await(), SnackbarType.Success)
            refresh()
            selectLesson(lesson.id)
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _state.update { it.copy(isGenerating = false, isUploading = false) }
        }
    }

    /**
     * Generates and uploads a section for a lesson within a module, curriculum, and profile,
     * while managing the UI state for the generation and upload process.
     *
     * @param title The title of the section to be generated.
     */
    private fun generateSection(title: String) = viewModelScope.launch(dispatcher) {
        try {
            val profile = checkNotNull(_state.value.profile)
            val curriculum = checkNotNull(_state.value.curriculum)
            val module = checkNotNull(_state.value.module)
            val lesson = checkNotNull(_state.value.lesson)
            _state.update { it.copy(isGenerating = true, isUploading = true) }

            val successMessage = async { resourceProvider.getString(Res.string.generate_section_success) }
            generateAndUploadSectionUseCase(title, profile, curriculum, module, lesson).getOrThrow()

            showSnackbar(successMessage.await(), SnackbarType.Success)
            refresh()
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _state.update { it.copy(isGenerating = false, isUploading = false) }
        }
    }

    /**
     * Regenerates and updates the lesson with the provided lesson ID. This method also handles state updates
     * for generating and uploading processes, error handling, and refreshing of data upon success.
     *
     * @param lessonId The unique identifier of the lesson to be regenerated and updated.
     */
    private fun regenerateAndUpdateLesson(lessonId: String) = viewModelScope.launch(dispatcher) {
        try {
            val profile = checkNotNull(_state.value.profile)
            val curriculum = checkNotNull(_state.value.curriculum)
            val module = checkNotNull(_state.value.module)
            var lesson = checkNotNull(_state.value.lessons.find { it.id == lessonId })
            _state.update { it.copy(isGenerating = true, isUploading = true) }

            val successMessage = async { resourceProvider.getString(Res.string.generate_lesson_success) }
            lesson = regenerateAndUpdateLessonUseCase(profile, curriculum, module, lesson).getOrThrow()

            showSnackbar(successMessage.await(), SnackbarType.Success)
            refresh()
            selectLesson(lesson.id)
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _state.update { it.copy(isGenerating = false, isUploading = false) }
        }
    }

    /**
     * Regenerates and updates a specific section by its ID. Updates the state of the UI to indicate
     * the section is being generated and uploaded during the operation. Upon success, displays a
     * success message via a snackbar and refreshes the current data. If an error occurs, it is handled
     * and displayed to the user.
     *
     * @param sectionId The unique identifier of the section to be regenerated and updated.
     */
    private fun regenerateAndUpdateSection(sectionId: String) = viewModelScope.launch(dispatcher) {
        try {
            val profile = checkNotNull(_state.value.profile)
            val curriculum = checkNotNull(_state.value.curriculum)
            val module = checkNotNull(_state.value.module)
            val lesson = checkNotNull(_state.value.lesson)
            val section = checkNotNull(_state.value.sections.find { it.id == sectionId })
            _state.update { it.copy(isGenerating = true, isUploading = true) }

            val successMessage = async { resourceProvider.getString(Res.string.generate_section_success) }
            regenerateAndUpdateSectionUseCase(profile, curriculum, module, lesson, section).getOrThrow()

            showSnackbar(successMessage.await(), SnackbarType.Success)
            refresh()
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _state.update { it.copy(isGenerating = false, isUploading = false) }
        }
    }

    /**
     * Selects a curriculum by its ID and updates the state with the curriculum, its modules, and lessons.
     *
     * @param curriculumId The unique identifier of the curriculum to be selected.
     */
    private fun selectCurriculum(curriculumId: String) {
        with(bundleManager) {
            val curriculum = getCurriculumByKey(curriculumId)
            val modules = getModulesByCurriculum(curriculumId)
            val module = modules.firstOrNull()
            val lessons = getLessonsByModule(Bundle.ModuleKey(curriculumId, module?.id.orEmpty()))

            _state.update { it.copy(curriculum = curriculum, modules = modules, module = module, lessons = lessons, lesson = null) }
        }
    }

    /**
     * Updates the state of the study view by selecting a specific module and its associated lessons,
     * based on the provided module ID. If successful, the selected module and its lessons are
     * stored in the UI state. If an error occurs during the retrieval process, the error is handled.
     *
     * @param moduleId The unique identifier of the module to select.
     */
    private fun selectModule(moduleId: String) = try {
        with(bundleManager) {
            val curriculum = checkNotNull(_state.value.curriculum)
            val module = getModuleByKey(Bundle.ModuleKey(curriculum.id, moduleId))
            val lessons = getLessonsByModule(Bundle.ModuleKey(curriculum.id, moduleId))

            _state.update { it.copy(module = module, lessons = lessons, lesson = null) }
        }
    } catch (e: Exception) {
        handleError(e)
    }

    /**
     * Selects a lesson by its identifier, updates the state with the selected lesson and its sections,
     * and handles any potential errors that occur during the process.
     *
     * @param lessonId The unique identifier of the lesson to select.
     */
    private fun selectLesson(lessonId: String) = try {
        val curriculum = checkNotNull(_state.value.curriculum)
        val module = checkNotNull(_state.value.module)

        val key = Bundle.LessonKey(curriculum.id, module.id, lessonId)
        val lesson = bundleManager.getLessonByKey(key)
        val sections = lesson?.let { bundleManager.getSectionsByLesson(key) }.orEmpty()

        _state.update { it.copy(lesson = lesson, sections = sections) }
    } catch (e: Exception) {
        handleError(e)
    }

    /**
     * Navigates to the specified destination while performing additional session upload and error handling operations.
     *
     * @param destination The route to navigate to.
     * @param waitForAnimation Whether to wait for the exit animation to finish before navigating.
     */
    override fun navigate(destination: Route, waitForAnimation: Boolean) {
        viewModelScope.launch(dispatcher) {
            _state.value.session?.let {
                try {
                    val profile = checkNotNull(_state.value.profile)
                    val session = it.copy(lastUpdated = System.currentTimeMillis())
                    uploadSessionUseCase(session, profile.id).getOrThrow()
                } catch (e: Exception) {
                    handleError(e)
                }
            }

            super.navigate(destination, waitForAnimation)
        }
    }
}