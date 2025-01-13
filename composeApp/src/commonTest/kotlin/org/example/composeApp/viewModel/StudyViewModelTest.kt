@file:OptIn(ExperimentalCoroutinesApi::class)

package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.example.composeApp.injection.DatabaseSyncManagers
import org.example.composeApp.presentation.action.LearnFlexAction
import org.example.composeApp.presentation.action.StudyAction
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.AppState
import org.example.composeApp.presentation.ui.util.UIEvent
import org.example.composeApp.presentation.viewModel.util.ResourceProvider
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.*
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.util.BundleManager
import org.example.shared.domain.sync.SyncManager
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@ExperimentalCoroutinesApi
class StudyViewModelTest {

    private lateinit var viewModel: StudyViewModel

    // Coroutines and Test Dispatchers
    private lateinit var testDispatcher: TestDispatcher

    // Mocks for dependencies
    private lateinit var learnFlexViewModel: LearnFlexViewModel
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var syncManager: SyncManager<DatabaseRecord>
    private lateinit var syncManagers: MutableList<SyncManager<DatabaseRecord>>

    // Mocks for our Use Cases
    private lateinit var fetchLessonQuizQuestionsUseCase: FetchLessonQuizQuestionsUseCase
    private lateinit var fetchModuleQuizQuestionsUseCase: FetchModuleQuizQuestionsUseCase
    private lateinit var fetchSectionQuizQuestionsUseCase: FetchSectionQuizQuestionsUseCase
    private lateinit var generateAndUploadLessonUseCase: GenerateAndUploadLessonUseCase
    private lateinit var generateAndUploadSectionUseCase: GenerateAndUploadSectionUseCase
    private lateinit var regenerateAndUpdateLessonUseCase: RegenerateAndUpdateLessonUseCase
    private lateinit var regenerateAndUpdateSectionUseCase: RegenerateAndUpdateSectionUseCase
    private lateinit var updateModuleUseCase: UpdateModuleUseCase
    private lateinit var updateLessonUseCase: UpdateLessonUseCase
    private lateinit var updateSectionUseCase: UpdateSectionUseCase
    private lateinit var uploadSessionUseCase: UploadSessionUseCase

    // State
    private lateinit var savedState: SavedStateHandle
    private lateinit var appStateFlow: MutableStateFlow<AppState>
    private lateinit var syncStatus: MutableStateFlow<SyncManager.SyncStatus>

    @Before
    fun setup() {
        // Set the main dispatcher to a TestCoroutineDispatcher
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        // Initialize the flows
        appStateFlow = MutableStateFlow(AppState())
        syncStatus = MutableStateFlow(SyncManager.SyncStatus.Idle)

        // Create relaxed mocks so we only have to stub what we use
        learnFlexViewModel = mockk(relaxed = true)
        resourceProvider = mockk(relaxed = true)
        syncManager = mockk(relaxed = true)
        syncManagers = mutableListOf(syncManager)

        // Use Case Mocks
        fetchLessonQuizQuestionsUseCase = mockk(relaxed = true)
        fetchModuleQuizQuestionsUseCase = mockk(relaxed = true)
        fetchSectionQuizQuestionsUseCase = mockk(relaxed = true)
        generateAndUploadLessonUseCase = mockk(relaxed = true)
        generateAndUploadSectionUseCase = mockk(relaxed = true)
        regenerateAndUpdateLessonUseCase = mockk(relaxed = true)
        regenerateAndUpdateSectionUseCase = mockk(relaxed = true)
        updateModuleUseCase = mockk(relaxed = true)
        updateLessonUseCase = mockk(relaxed = true)
        updateSectionUseCase = mockk(relaxed = true)
        uploadSessionUseCase = mockk(relaxed = true)

        // SavedStateHandle
        // Provide initial navigation arguments if desired
        savedState = SavedStateHandle(
            mapOf(
                "curriculumId" to "testCurriculumId",
                "moduleId" to "testModuleId",
                "lessonId" to "testLessonId"
            )
        )

        // Stub any needed behavior
        every { syncManager.syncStatus } returns syncStatus
        every { learnFlexViewModel.state } returns appStateFlow

        // Start Koin with the necessary modules
        startKoin {
            modules(
                module {
                    // Provide the dispatcher and mocks
                    single<CoroutineDispatcher> { testDispatcher }
                    single { resourceProvider }
                    single<SyncManager<DatabaseRecord>> { syncManager }
                    single<DatabaseSyncManagers> { syncManagers }
                    single { learnFlexViewModel }

                    // Provide use cases
                    single { fetchLessonQuizQuestionsUseCase }
                    single { fetchModuleQuizQuestionsUseCase }
                    single { fetchSectionQuizQuestionsUseCase }
                    single { generateAndUploadLessonUseCase }
                    single { generateAndUploadSectionUseCase }
                    single { regenerateAndUpdateLessonUseCase }
                    single { regenerateAndUpdateSectionUseCase }
                    single { updateModuleUseCase }
                    single { updateLessonUseCase }
                    single { updateSectionUseCase }
                    single { uploadSessionUseCase }
                }
            )
        }

        // Finally, instantiate the ViewModel with the mocks
        viewModel = StudyViewModel(
            fetchLessonQuizQuestionsUseCase,
            fetchModuleQuizQuestionsUseCase,
            fetchSectionQuizQuestionsUseCase,
            generateAndUploadLessonUseCase,
            generateAndUploadSectionUseCase,
            regenerateAndUpdateLessonUseCase,
            regenerateAndUpdateSectionUseCase,
            updateModuleUseCase,
            updateLessonUseCase,
            updateSectionUseCase,
            uploadSessionUseCase,
            savedState
        )

        appStateFlow.update {
            it.copy(
                profile = profile,
                bundleManager = bundleManager
            )
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    /**
     * Example test:
     * Verifies that when the app state emits a profile,
     * the ViewModel updates its internal state accordingly.
     */
    @Test
    fun `viewModel updates state when profile is provided`() = runTest {
        // When
        advanceUntilIdle() // let the flow collect

        // Then
        // Verify that the viewModel's state has the profile
        val currentState = viewModel.state.value
        assertEquals(profile, currentState.profile)
    }

    @Test
    fun `handleAction(GoBack) clears lesson`() = runTest {
        // Given
        advanceUntilIdle()
        viewModel.handleAction(StudyAction.SelectLesson(lessons[1].id))
        assertEquals(lessons[1], viewModel.state.value.lesson)

        // When
        viewModel.handleAction(StudyAction.GoBack)
        advanceUntilIdle()

        // Then
        advanceTimeBy(300)
        assertEquals(viewModel.state.value.lesson, null)
    }

    @Test
    fun `handleAction GenerateLesson should invoke generateAndUploadLessonUseCase and update UI state`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val title = "Lesson 3"
        val expectedLesson = Lesson(
            id = "newLessonId",
            title = "Lesson 3",
            description = "Description 1",
            content = listOf("section1"),
            createdAt = 3L,
            lastUpdated = 3L
        )
        coEvery { generateAndUploadLessonUseCase(title, profile, curricula[1], modules[1]) } returns Result.success(expectedLesson)

        val successMessage = "Lesson generated successfully"
        coEvery { resourceProvider.getString(any()) } returns successMessage

        every { learnFlexViewModel.handleAction(LearnFlexAction.Refresh) } answers {
            appStateFlow.update {
                it.copy(
                    bundleManager =
                        bundleManager.copy(
                            bundles = bundleManager.bundles - bundles[1] + Bundle(
                                curriculum = curricula[1],
                                modules = mapOf(Bundle.ModuleKey(curricula[1].id, modules[1].id) to modules[1]),
                                lessons = mapOf(Bundle.LessonKey(curricula[1].id, modules[1].id, expectedLesson.id) to expectedLesson),
                                sections = mapOf(
                                    Bundle.SectionKey(
                                        curricula[1].id,
                                        modules[1].id,
                                        expectedLesson.id,
                                        sections[1].id
                                    ) to sections[1]
                                )
                            )
                        )
                )
            }
        }

        // When
        viewModel.handleAction(StudyAction.GenerateLesson(title))
        advanceUntilIdle()

        // Then
        assertEquals(expectedLesson, viewModel.state.value.lesson)
        assertEquals(successMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction GenerateLesson with error shows UIEvent ShowSnackbar`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val errorMessage = "Error generating lesson"
        val title = "Lesson 3"
        coEvery { generateAndUploadLessonUseCase(title, profile, curricula[1], modules[1]) } returns Result.failure(
            RuntimeException(
                errorMessage
            )
        )

        // When
        viewModel.handleAction(StudyAction.GenerateLesson(title))
        advanceUntilIdle()

        // Then
        assertTrue(uiEvents[0] is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction GenerateSection should invoke generateAndUploadSectionUseCase and update UI state`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val title = "Section 3"
        val expectedSection = Section(
            id = "newSectionId",
            title = "Section 3",
            description = "Description 1",
            content = listOf("quiz1"),
            createdAt = 3L
        )
        coEvery { generateAndUploadSectionUseCase(title, profile, curricula[1], modules[1], lessons[1]) } returns Result.success(
            expectedSection
        )

        val successMessage = "Section generated successfully"
        coEvery { resourceProvider.getString(any()) } returns successMessage

        every { learnFlexViewModel.handleAction(LearnFlexAction.Refresh) } answers {
            appStateFlow.update {
                it.copy(
                    bundleManager =
                        bundleManager.copy(
                            bundles = bundleManager.bundles - bundles[1] + Bundle(
                                curriculum = curricula[1],
                                modules = mapOf(Bundle.ModuleKey(curricula[1].id, modules[1].id) to modules[1]),
                                lessons = mapOf(Bundle.LessonKey(curricula[1].id, modules[1].id, lessons[1].id) to lessons[1]),
                                sections = mapOf(
                                    Bundle.SectionKey(
                                        curricula[1].id,
                                        modules[1].id,
                                        lessons[1].id,
                                        expectedSection.id
                                    ) to expectedSection
                                )
                            )
                        )
                )
            }
        }

        // When
        viewModel.handleAction(StudyAction.GenerateSection(title))
        advanceUntilIdle()

        // Then
        assertEquals(successMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction GenerateSection with error shows UIEvent ShowSnackbar`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val errorMessage = "Error generating section"
        val title = "Section 3"
        coEvery { generateAndUploadSectionUseCase(title, profile, curricula[1], modules[1], lessons[1]) } returns Result.failure(
            RuntimeException(errorMessage)
        )

        // When
        viewModel.handleAction(StudyAction.GenerateSection(title))
        advanceUntilIdle()

        // Then
        assertTrue(uiEvents[0] is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction RegenerateLesson should call regenerateAndUpdateLessonUseCase and update lesson selection`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val lessonId = lessons[1].id
        val expectedLesson = lessons[1].copy(
            lastUpdated = 3L
        )
        coEvery { regenerateAndUpdateLessonUseCase(profile, curricula[1], modules[1], lessons[1]) } returns Result.success(expectedLesson)

        val successMessage = "Lesson regenerated successfully"
        coEvery { resourceProvider.getString(any()) } returns successMessage

        every { learnFlexViewModel.handleAction(LearnFlexAction.Refresh) } answers {
            appStateFlow.update {
                it.copy(
                    bundleManager =
                        bundleManager.copy(
                            bundles = bundleManager.bundles - bundles[1] + Bundle(
                                curriculum = curricula[1],
                                modules = mapOf(Bundle.ModuleKey(curricula[1].id, modules[1].id) to modules[1]),
                                lessons = mapOf(Bundle.LessonKey(curricula[1].id, modules[1].id, expectedLesson.id) to expectedLesson),
                                sections = mapOf(
                                    Bundle.SectionKey(
                                        curricula[1].id,
                                        modules[1].id,
                                        expectedLesson.id,
                                        sections[1].id
                                    ) to sections[1]
                                )
                            )
                        )
                )
            }
        }

        // When
        viewModel.handleAction(StudyAction.RegenerateLesson(lessonId))
        advanceUntilIdle()

        // Then
        assertEquals(expectedLesson, viewModel.state.value.lesson)
        assertEquals(successMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction RegenerateLesson with error shows UIEvent ShowSnackbar`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val errorMessage = "Error regenerating lesson"
        val lessonId = lessons[1].id
        coEvery { regenerateAndUpdateLessonUseCase(profile, curricula[1], modules[1], lessons[1]) } returns Result.failure(
            RuntimeException(
                errorMessage
            )
        )

        // When
        viewModel.handleAction(StudyAction.RegenerateLesson(lessonId))
        advanceUntilIdle()

        // Then
        assertTrue(uiEvents[0] is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction RegenerateSection should call regenerateAndUpdateSectionUseCase and refresh data`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val sectionId = sections[1].id
        val expectedSection = sections[1].copy(
            lastUpdated = 3L
        )
        coEvery { regenerateAndUpdateSectionUseCase(profile, curricula[1], modules[1], lessons[1], sections[1]) } returns Result.success(
            expectedSection
        )

        val successMessage = "Section regenerated successfully"
        coEvery { resourceProvider.getString(any()) } returns successMessage

        every { learnFlexViewModel.handleAction(LearnFlexAction.Refresh) } answers {
            appStateFlow.update {
                it.copy(
                    bundleManager =
                        bundleManager.copy(
                            bundles = bundleManager.bundles - bundles[1] + Bundle(
                                curriculum = curricula[1],
                                modules = mapOf(Bundle.ModuleKey(curricula[1].id, modules[1].id) to modules[1]),
                                lessons = mapOf(Bundle.LessonKey(curricula[1].id, modules[1].id, lessons[1].id) to lessons[1]),
                                sections = mapOf(
                                    Bundle.SectionKey(
                                        curricula[1].id,
                                        modules[1].id,
                                        lessons[1].id,
                                        expectedSection.id
                                    ) to expectedSection
                                )
                            )
                        )
                )
            }
        }

        // When
        viewModel.handleAction(StudyAction.RegenerateSection(sectionId))
        advanceUntilIdle()

        // Then
        assertEquals(successMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction RegenerateSection with error shows UIEvent ShowSnackbar`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val errorMessage = "Error regenerating section"
        val sectionId = sections[1].id
        coEvery { regenerateAndUpdateSectionUseCase(profile, curricula[1], modules[1], lessons[1], sections[1]) } returns Result.failure(
            RuntimeException(errorMessage)
        )

        // When
        viewModel.handleAction(StudyAction.RegenerateSection(sectionId))
        advanceUntilIdle()

        // Then
        assertTrue(uiEvents[0] is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction GenerateModuleQuiz should call fetchModuleQuizQuestionsUseCase and update UI state`() = runTest {
        // Given
        advanceUntilIdle()
        val moduleTitle = modules[1].title
        val expectedQuestion = Question.TrueFalse(
            text = "Is this a question?",
            correctAnswer = "True",
        )

        coEvery { fetchModuleQuizQuestionsUseCase(moduleTitle, Level.valueOf(profile.preferences.level)) } returns flowOf(
            Result.success(
                expectedQuestion
            )
        )

        // When
        viewModel.handleAction(StudyAction.GenerateModuleQuiz)
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.state.value.quiz.questions.size)
        assertEquals(expectedQuestion, viewModel.state.value.quiz.questions[0])
    }

    @Test
    fun `handleAction GenerateModuleQuiz should handle error from fetchModuleQuizQuestionsUseCase`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val errorMessage = "Error fetching module quiz questions"
        val moduleTitle = modules[1].title
        coEvery { fetchModuleQuizQuestionsUseCase(moduleTitle, Level.valueOf(profile.preferences.level)) } returns flowOf(
            Result.failure(
                RuntimeException(errorMessage)
            )
        )

        // When
        viewModel.handleAction(StudyAction.GenerateModuleQuiz)
        advanceUntilIdle()

        // Then
        assertTrue(uiEvents[0] is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction GenerateLessonQuiz should call fetchLessonQuizQuestionsUseCase and update UI state`() = runTest {
        // Given
        advanceUntilIdle()
        val lessonTitle = lessons[1].title
        val expectedQuestion = Question.TrueFalse(
            text = "Is this a question?",
            correctAnswer = "True",
        )

        coEvery { fetchLessonQuizQuestionsUseCase(lessonTitle, Level.valueOf(profile.preferences.level)) } returns flowOf(
            Result.success(
                expectedQuestion
            )
        )

        // When
        viewModel.handleAction(StudyAction.GenerateLessonQuiz)
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.state.value.quiz.questions.size)
        assertEquals(expectedQuestion, viewModel.state.value.quiz.questions[0])
    }

    @Test
    fun `handleAction GenerateLessonQuiz should handle error from fetchLessonQuizQuestionsUseCase`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val errorMessage = "Error fetching lesson quiz questions"
        val lessonTitle = lessons[1].title
        coEvery { fetchLessonQuizQuestionsUseCase(lessonTitle, Level.valueOf(profile.preferences.level)) } returns flowOf(
            Result.failure(
                RuntimeException(errorMessage)
            )
        )

        // When
        viewModel.handleAction(StudyAction.GenerateLessonQuiz)
        advanceUntilIdle()

        // Then
        assertTrue(uiEvents[0] is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction GenerateSectionQuiz should call fetchSectionQuizQuestionsUseCase and update UI state`() = runTest {
        // Given
        advanceUntilIdle()
        val sectionId = sections[1].id
        val sectionTitle = sections[1].title
        val expectedQuestion = Question.TrueFalse(
            text = "Is this a question?",
            correctAnswer = "True",
        )

        coEvery { fetchSectionQuizQuestionsUseCase(sectionTitle, Level.valueOf(profile.preferences.level)) } returns flowOf(
            Result.success(
                expectedQuestion
            )
        )

        // When
        viewModel.handleAction(StudyAction.GenerateSectionQuiz(sectionId))
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.state.value.quiz.questions.size)
        assertEquals(expectedQuestion, viewModel.state.value.quiz.questions[0])
    }

    @Test
    fun `handleAction GenerateSectionQuiz should handle error from fetchSectionQuizQuestionsUseCase`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val errorMessage = "Error fetching section quiz questions"
        val sectionId = sections[1].id
        val sectionTitle = sections[1].title
        coEvery { fetchSectionQuizQuestionsUseCase(sectionTitle, Level.valueOf(profile.preferences.level)) } returns flowOf(
            Result.failure(
                RuntimeException(errorMessage)
            )
        )

        // When
        viewModel.handleAction(StudyAction.GenerateSectionQuiz(sectionId))
        advanceUntilIdle()

        // Then
        assertTrue(uiEvents[0] is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction SaveQuizResult with Collection Modules should call updateModuleUseCase`() = runTest {
        // Given
        advanceUntilIdle()
        val expectedModule = modules[1].copy(
            quizScore = 1,
            quizScoreMax = 1
        )
        coEvery { fetchModuleQuizQuestionsUseCase(any(), Level.valueOf(profile.preferences.level)) } returns flowOf(Result.success(mockk()))
        coEvery { learnFlexViewModel.handleAction(LearnFlexAction.Refresh) } answers {
            appStateFlow.update {
                it.copy(
                    bundleManager =
                        bundleManager.copy(
                            bundles = bundleManager.bundles - bundles[1] + Bundle(
                                curriculum = curricula[1],
                                modules = mapOf(Bundle.ModuleKey(curricula[1].id, modules[1].id) to expectedModule),
                                lessons = mapOf(Bundle.LessonKey(curricula[1].id, modules[1].id, lessons[1].id) to lessons[1]),
                                sections = mapOf(
                                    Bundle.SectionKey(
                                        curricula[1].id,
                                        modules[1].id,
                                        lessons[1].id,
                                        sections[1].id
                                    ) to sections[1]
                                )
                            )
                        )
                )
            }
        }
        coEvery { updateModuleUseCase(modules[1], profile.id, curricula[1].id) } returns Result.success(Unit)

        // When
        viewModel.handleAction(StudyAction.GenerateModuleQuiz)
        advanceUntilIdle()

        viewModel.handleAction(StudyAction.SaveQuizResult)
        advanceUntilIdle()

        // Then
        assertEquals(expectedModule, viewModel.state.value.module)
    }

    @Test
    fun `handleAction GenerateModuleQuiz should handle error from updateModuleUseCase`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val errorMessage = "Error updating module"
        coEvery { fetchModuleQuizQuestionsUseCase(any(), Level.valueOf(profile.preferences.level)) } returns flowOf(Result.success(mockk()))
        coEvery { updateModuleUseCase(modules[1], profile.id, curricula[1].id) } returns Result.failure(RuntimeException(errorMessage))

        // When
        viewModel.handleAction(StudyAction.GenerateModuleQuiz)
        advanceUntilIdle()

        viewModel.handleAction(StudyAction.SaveQuizResult)
        advanceUntilIdle()

        // Then
        assertTrue(uiEvents[0] is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction SaveQuizResult with Collection Lessons should call updateLessonUseCase`() = runTest {
        // Given
        advanceUntilIdle()
        val expectedLesson = lessons[1].copy(
            quizScore = 1,
            quizScoreMax = 1
        )
        coEvery { fetchLessonQuizQuestionsUseCase(any(), Level.valueOf(profile.preferences.level)) } returns flowOf(Result.success(mockk()))
        coEvery { learnFlexViewModel.handleAction(LearnFlexAction.Refresh) } answers {
            appStateFlow.update {
                it.copy(
                    bundleManager =
                        bundleManager.copy(
                            bundles = bundleManager.bundles - bundles[1] + Bundle(
                                curriculum = curricula[1],
                                modules = mapOf(Bundle.ModuleKey(curricula[1].id, modules[1].id) to modules[1]),
                                lessons = mapOf(Bundle.LessonKey(curricula[1].id, modules[1].id, expectedLesson.id) to expectedLesson),
                                sections = mapOf(
                                    Bundle.SectionKey(
                                        curricula[1].id,
                                        modules[1].id,
                                        expectedLesson.id,
                                        sections[1].id
                                    ) to sections[1]
                                )
                            )
                        )
                )
            }
        }
        coEvery { updateLessonUseCase(lessons[1], profile.id, curricula[1].id, modules[1].id) } returns Result.success(Unit)

        // When
        viewModel.handleAction(StudyAction.GenerateLessonQuiz)
        advanceUntilIdle()

        viewModel.handleAction(StudyAction.SaveQuizResult)
        advanceUntilIdle()

        // Then
        assertEquals(expectedLesson, viewModel.state.value.lesson)
    }

    @Test
    fun `handleAction GenerateLessonQuiz should handle error from updateLessonUseCase`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val errorMessage = "Error updating lesson"
        coEvery { fetchLessonQuizQuestionsUseCase(any(), Level.valueOf(profile.preferences.level)) } returns flowOf(Result.success(mockk()))
        coEvery { updateLessonUseCase(lessons[1], profile.id, curricula[1].id, modules[1].id) } returns Result.failure(
            RuntimeException(
                errorMessage
            )
        )

        // When
        viewModel.handleAction(StudyAction.GenerateLessonQuiz)
        advanceUntilIdle()

        viewModel.handleAction(StudyAction.SaveQuizResult)
        advanceUntilIdle()

        // Then
        assertTrue(uiEvents[0] is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction SaveQuizResult with Collection Sections should call updateSectionUseCase`() = runTest {
        // Given
        advanceUntilIdle()
        val expectedSection = sections[1].copy(
            quizScore = 1,
            quizScoreMax = 1
        )
        coEvery {
            fetchSectionQuizQuestionsUseCase(
                any(),
                Level.valueOf(profile.preferences.level)
            )
        } returns flowOf(Result.success(mockk()))
        coEvery { learnFlexViewModel.handleAction(LearnFlexAction.Refresh) } answers {
            appStateFlow.update {
                it.copy(
                    bundleManager =
                        bundleManager.copy(
                            bundles = bundleManager.bundles - bundles[1] + Bundle(
                                curriculum = curricula[1],
                                modules = mapOf(Bundle.ModuleKey(curricula[1].id, modules[1].id) to modules[1]),
                                lessons = mapOf(Bundle.LessonKey(curricula[1].id, modules[1].id, lessons[1].id) to lessons[1]),
                                sections = mapOf(
                                    Bundle.SectionKey(
                                        curricula[1].id,
                                        modules[1].id,
                                        lessons[1].id,
                                        expectedSection.id
                                    ) to expectedSection
                                )
                            )
                        )
                )
            }
        }
        coEvery {
            updateSectionUseCase(
                sections[1],
                profile.id,
                curricula[1].id,
                modules[1].id,
                lessons[1].id
            )
        } returns Result.success(Unit)

        // When
        viewModel.handleAction(StudyAction.GenerateSectionQuiz(sections[1].id))
        advanceUntilIdle()

        viewModel.handleAction(StudyAction.SaveQuizResult)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value.sections.contains(expectedSection))
    }

    @Test
    fun `handleAction GenerateSectionQuiz should handle error from updateSectionUseCase`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }
        val errorMessage = "Error updating section"
        coEvery {
            fetchSectionQuizQuestionsUseCase(
                any(),
                Level.valueOf(profile.preferences.level)
            )
        } returns flowOf(Result.success(mockk()))
        coEvery { updateSectionUseCase(sections[1], profile.id, curricula[1].id, modules[1].id, lessons[1].id) } returns Result.failure(
            RuntimeException(errorMessage)
        )

        // When
        viewModel.handleAction(StudyAction.GenerateSectionQuiz(sections[1].id))
        advanceUntilIdle()

        viewModel.handleAction(StudyAction.SaveQuizResult)
        advanceUntilIdle()

        // Then
        assertTrue(uiEvents[0] is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents[0] as UIEvent.ShowSnackbar).message)
        job.cancel()
    }

    @Test
    fun `handleAction SubmitQuiz should update state with quiz grade`() = runTest {
        // Given
        advanceUntilIdle()
        val answers = listOf("True", "True")
        val quiz = Quiz(
            owner = sections[1].id,
            questionNumber = 2,
            questions = listOf(
                Question.TrueFalse(
                    text = "Is this a question?",
                    correctAnswer = "True"
                ),
                Question.TrueFalse(
                    text = "Is this another question?",
                    correctAnswer = "True"
                )
            ),
            answers = answers
        )
        val expectedQuiz = quiz.grade()
        val sectionId = sections[1].id
        val sectionTitle = sections[1].title
        coEvery { fetchSectionQuizQuestionsUseCase(sectionTitle, Level.valueOf(profile.preferences.level)) } returns flowOf(
            Result.success(quiz.questions[0]),
            Result.success(quiz.questions[1])
        )

        // When
        viewModel.handleAction(StudyAction.GenerateSectionQuiz(sectionId))
        advanceUntilIdle()

        answers.forEach {
            viewModel.handleAction(StudyAction.AnswerQuizQuestion(it))
            advanceUntilIdle()
        }

        viewModel.handleAction(StudyAction.SubmitQuiz)
        advanceUntilIdle()

        // Then
        assertEquals(expectedQuiz, viewModel.state.value.quiz)
    }

    @Test
    fun `handleAction Navigate should update state with navigation event`() = runTest {
        // Given
        advanceUntilIdle()
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.collect {
                uiEvents.add(it)
            }
        }

        // When
        viewModel.handleAction(StudyAction.Navigate(Route.Dashboard))

        // Then
        advanceUntilIdle()
        assertTrue(uiEvents[0] is UIEvent.Navigate)
        assertEquals(Route.Dashboard, (uiEvents[0] as UIEvent.Navigate).destination)
        job.cancel()
    }

    @Test
    fun `handleAction Refresh should call learnFlexViewModel handleAction Refresh`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.handleAction(StudyAction.Refresh)
        advanceUntilIdle()

        // Then
        verify { learnFlexViewModel.handleAction(LearnFlexAction.Refresh) }
    }

    companion object {
        val profile = Profile(
            username = "TestUser",
            email = "test@example.com",
            photoUrl = "https://example.com/photo.png",
            preferences = Profile.LearningPreferences(
                field = "ENGINEERING",
                level = Level.ADVANCED.name,
                goal = "SomeGoal"
            )
        )

        val curricula = listOf(
            Curriculum(
                title = "Curriculum 1",
                description = "Description 1",
                content = listOf("module1"),
                lastUpdated = 1L
            ),
            Curriculum(
                title = "Curriculum 2",
                description = "Description 2",
                content = listOf("module2"),
                lastUpdated = 2L
            )
        )

        val modules = listOf(
            Module(
                title = "Module 1",
                description = "Description 1",
                content = listOf("lesson1"),
                lastUpdated = 1L
            ),
            Module(
                title = "Module 2",
                description = "Description 2",
                content = listOf("lesson2"),
                lastUpdated = 2L
            )
        )

        val lessons = listOf(
            Lesson(
                title = "Lesson 1",
                description = "Description 1",
                content = listOf("section1"),
                lastUpdated = 1L
            ),
            Lesson(
                title = "Lesson 2",
                description = "Description 2",
                content = listOf("section2"),
                lastUpdated = 2L
            )
        )

        val sections = listOf(
            Section(
                title = "Section 1",
                description = "Description 1",
                content = listOf("quiz1")
            ),
            Section(
                title = "Section 2",
                description = "Description 2",
                content = listOf("quiz2")
            )
        )

        val bundles = listOf(
            Bundle(
                curriculum = curricula[0],
                modules = mapOf(Bundle.ModuleKey(curricula[0].id, modules[0].id) to modules[0]),
                lessons = mapOf(Bundle.LessonKey(curricula[0].id, modules[0].id, lessons[0].id) to lessons[0]),
                sections = mapOf(Bundle.SectionKey(curricula[0].id, modules[0].id, lessons[0].id, sections[0].id) to sections[0])
            ),
            Bundle(
                curriculum = curricula[1],
                modules = mapOf(Bundle.ModuleKey(curricula[1].id, modules[1].id) to modules[1]),
                lessons = mapOf(Bundle.LessonKey(curricula[1].id, modules[1].id, lessons[1].id) to lessons[1]),
                sections = mapOf(Bundle.SectionKey(curricula[1].id, modules[1].id, lessons[1].id, sections[1].id) to sections[1])
            )
        )

        val bundleManager = BundleManager(bundles)
    }
}
