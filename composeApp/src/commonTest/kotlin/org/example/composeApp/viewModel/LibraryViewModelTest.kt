package org.example.composeApp.viewModel

import io.mockk.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.example.composeApp.injection.DatabaseSyncManagers
import org.example.composeApp.presentation.action.LibraryAction
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.AppState
import org.example.composeApp.presentation.state.LibraryUIState
import org.example.composeApp.presentation.ui.util.UIEvent
import org.example.composeApp.presentation.viewModel.LearnFlexViewModel
import org.example.composeApp.presentation.viewModel.LibraryViewModel
import org.example.composeApp.presentation.viewModel.util.ResourceProvider
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.util.BundleManager
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.curriculum.DeleteCurriculumUseCase
import org.example.shared.domain.use_case.curriculum.GenerateCurriculumUseCase
import org.example.shared.domain.use_case.library.UploadCurriculumAndModulesUseCase
import org.example.shared.domain.use_case.module.GenerateModuleUseCase
import org.example.shared.domain.use_case.syllabus.SummarizeSyllabusUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.io.File
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {
    private lateinit var viewModel: LibraryViewModel
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var summarizeSyllabusUseCase: SummarizeSyllabusUseCase
    private lateinit var generateCurriculumUseCase: GenerateCurriculumUseCase
    private lateinit var generateModuleUseCase: GenerateModuleUseCase
    private lateinit var uploadCurriculumAndModulesUseCase: UploadCurriculumAndModulesUseCase
    private lateinit var deleteCurriculumUseCase: DeleteCurriculumUseCase
    private lateinit var learnFlexViewModel: LearnFlexViewModel
    private lateinit var appStateFlow: MutableStateFlow<AppState>
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var syncManager: SyncManager<DatabaseRecord>
    private lateinit var syncManagers: MutableList<SyncManager<DatabaseRecord>>
    private lateinit var syncStatus: MutableStateFlow<SyncManager.SyncStatus>

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        syncManager = mockk(relaxed = true)
        summarizeSyllabusUseCase = mockk(relaxed = true)
        generateCurriculumUseCase = mockk(relaxed = true)
        generateModuleUseCase = mockk(relaxed = true)
        uploadCurriculumAndModulesUseCase = mockk(relaxed = true)
        deleteCurriculumUseCase = mockk(relaxed = true)
        learnFlexViewModel = mockk(relaxed = true)
        appStateFlow = MutableStateFlow(AppState())
        resourceProvider = mockk(relaxed = true)
        syncStatus = MutableStateFlow<SyncManager.SyncStatus>(SyncManager.SyncStatus.Idle)
        syncManager = mockk(relaxed = true)
        syncManagers = mutableListOf<SyncManager<DatabaseRecord>>()
        syncManagers.add(syncManager)

        startKoin {
            modules(
                module {
                    single<LearnFlexViewModel> { learnFlexViewModel }
                    single<CoroutineDispatcher> { testDispatcher }
                    single<ResourceProvider> { resourceProvider }
                    single<SyncManager<DatabaseRecord>> { syncManager }
                    single<DatabaseSyncManagers> { syncManagers }
                }
            )
        }

        viewModel = LibraryViewModel(
            deleteCurriculumUseCase,
            generateCurriculumUseCase,
            generateModuleUseCase,
            summarizeSyllabusUseCase,
            uploadCurriculumAndModulesUseCase
        )

        every { syncManager.syncStatus } returns syncStatus
        every { learnFlexViewModel.state } returns appStateFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `handleAction SummarizeSyllabus should update syllabus description when successful`() = runTest {
        // Given
        val file = File.createTempFile("syllabus", ".pdf")
        val expectedDescription = "Syllabus description"

        coEvery { summarizeSyllabusUseCase(file) } returns flowOf(Result.success(expectedDescription))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.SummarizeSyllabus(file))
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(expectedDescription, syllabusDescription)
        }
        coVerify {
            summarizeSyllabusUseCase(file)
        }
    }

    @Test
    fun `handleAction SummarizeSyllabus should handle error when unsuccessful`() = runTest {
        // Given
        val file = File.createTempFile("syllabus", ".pdf")
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }
        advanceUntilIdle()

        coEvery { summarizeSyllabusUseCase(file) } returns flowOf(Result.failure(Exception("Syllabus error")))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.SummarizeSyllabus(file))
        advanceUntilIdle()

        // Then
        coVerify {
            summarizeSyllabusUseCase(file)
        }

        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `handleAction DeleteSyllabusFile should clear syllabus file in state`() = runTest {
        // Given
        val file = File.createTempFile("syllabus", ".pdf")
        val expectedDescription = "Syllabus description"

        coEvery { summarizeSyllabusUseCase(file) } returns flowOf(Result.success(expectedDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.SummarizeSyllabus(file))

        advanceUntilIdle()
        with(viewModel.state.value) {
            assertEquals(expectedDescription, syllabusDescription)
        }

        // When
        viewModel.handleAction(LibraryAction.DeleteSyllabusFile)
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertNull(syllabusFile)
        }
    }

    @Test
    fun `handleAction EditSyllabusDescription should update syllabus description in state`() = runTest {
        // Given
        val expectedDescription = "Syllabus description"

        // When
        viewModel.handleAction(LibraryAction.EditSyllabusDescription(expectedDescription))
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(expectedDescription, syllabusDescription)
        }
    }

    @Test
    fun `handleAction GenerateCurriculum with valid description and profile should generate curriculum successfully`() = runTest {
        // Given
        val syllabusDescription = "Curriculum description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val generatedResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Curriculum title"
            every { description } returns "Curriculum description"
            every { content } returns listOf("module1", "module2")
        }

        appStateFlow.update { it.copy(profile = profile) }
        advanceUntilIdle()

        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedResponse))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        // Then
        coVerify {
            generateCurriculumUseCase(syllabusDescription, profile)
        }
        with(viewModel.state.value) {
            assertEquals(generatedResponse.title, curriculum?.title)
            assertEquals(generatedResponse.description, curriculum?.description)
            assertEquals(generatedResponse.content, curriculum?.content)
        }
    }

    @Test
    fun `handleAction GenerateCurriculum should handle error when unsuccessful`() = runTest {
        // Given
        val syllabusDescription = "Curriculum description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        appStateFlow.update { it.copy(profile = profile) }
        advanceUntilIdle()

        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.failure(Exception("Curriculum error")))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        // Then
        coVerify {
            generateCurriculumUseCase(syllabusDescription, profile)
        }

        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `handleAction GenerateModule with valid curriculum, description and profile should generate module successfully`() = runTest {
        // Given
        val syllabusDescription = "Syllabus description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val generatedCurriculumResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Curriculum title"
            every { description } returns "Curriculum description"
            every { content } returns listOf("Module1 title", "Module2 title")
        }
        val generatedModuleResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Module1 title"
            every { description } returns "Module description"
            every { content } returns listOf("lesson1", "lesson2")
        }
        appStateFlow.update { it.copy(profile = profile) }
        advanceUntilIdle()

        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedCurriculumResponse))
        coEvery { generateModuleUseCase("Module1 title", profile, any()) } returns flowOf(Result.success(generatedModuleResponse))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.GenerateModule("Module1 title"))
        advanceUntilIdle()

        // Then
        coVerify {
            generateModuleUseCase("Module1 title", profile, any())
        }
        with(viewModel.state.value) {
            assertEquals(generatedModuleResponse.title, modules.first().title)
            assertEquals(generatedModuleResponse.description, modules.first().description)
            assertEquals(generatedModuleResponse.content, modules.first().content)
        }
    }

    @Test
    fun `handleAction GenerateModule should handle error when unsuccessful`() = runTest {
        // Given
        val syllabusDescription = "Syllabus description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val generatedCurriculumResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Curriculum title"
            every { description } returns "Curriculum description"
            every { content } returns listOf("Module1 title", "Module2 title")
        }
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        appStateFlow.update { it.copy(profile = profile) }
        advanceUntilIdle()

        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedCurriculumResponse))
        coEvery { generateModuleUseCase("Module1 title", profile, any()) } returns flowOf(Result.failure(Exception("Module error")))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.GenerateModule("Module1 title"))
        advanceUntilIdle()

        // Then
        coVerify {
            generateModuleUseCase("Module1 title", profile, any())
        }

        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `handleAction RemoveModule with valid index should remove module from state`() = runTest {
        // Given
        val syllabusDescription = "Syllabus description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val generatedCurriculumResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Curriculum title"
            every { description } returns "Curriculum description"
            every { content } returns listOf("Module1 title", "Module2 title")
        }
        val generatedModuleResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Module1 title"
            every { description } returns "Module description"
            every { content } returns listOf("lesson1", "lesson2")
        }
        coEvery { learnFlexViewModel.state.value.profile } returns profile
        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedCurriculumResponse))
        coEvery { generateModuleUseCase("Module1 title", profile, any()) } returns flowOf(Result.success(generatedModuleResponse))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateModule("Module1 title"))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.RemoveModule("Module1 title"))
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertTrue(modules.isEmpty())
        }
    }

    @Test
    fun `handleAction RemoveLesson with valid indices and moduleId should remove lesson from module`() = runTest {
        // Given
        val syllabusDescription = "Syllabus description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val generatedCurriculumResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Curriculum title"
            every { description } returns "Curriculum description"
            every { content } returns listOf("Module1 title", "Module2 title")
        }
        val generatedModuleResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Module1 title"
            every { description } returns "Module description"
            every { content } returns listOf("lesson1", "lesson2")
        }

        appStateFlow.update { it.copy(profile = profile) }
        advanceUntilIdle()

        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedCurriculumResponse))
        coEvery { generateModuleUseCase("Module1 title", profile, any()) } returns flowOf(Result.success(generatedModuleResponse))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateModule("Module1 title"))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.RemoveLesson("lesson1", viewModel.state.value.modules.first().id))
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(1, modules.first().content.size)
        }
    }

    @Test
    fun `handleAction SaveContent with valid data should upload curriculum and modules successfully`() = runTest {
        // Given
        val successMessage = "Upload successful"
        val syllabusDescription = "Syllabus description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val generatedCurriculumResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Curriculum title"
            every { description } returns "Curriculum description"
            every { content } returns listOf("Module1 title")
        }
        val generatedModuleResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Module1 title"
            every { description } returns "Module description"
            every { content } returns listOf("lesson1", "lesson2")
        }
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        appStateFlow.update { it.copy(profile = profile) }
        advanceUntilIdle()

        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedCurriculumResponse))
        coEvery { generateModuleUseCase("Module1 title", profile, any()) } returns flowOf(Result.success(generatedModuleResponse))
        coEvery { uploadCurriculumAndModulesUseCase("profileId", any(), any()) } returns Result.success(Unit)
        coEvery { resourceProvider.getString(any()) } returns successMessage
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateModule("Module1 title"))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.SaveContent)
        advanceUntilIdle()

        // Then
        coVerify {
            uploadCurriculumAndModulesUseCase("profileId", any(), any())
        }
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)
        assertEquals(successMessage, (uiEvents.first() as UIEvent.ShowSnackbar).message)

        job.cancel()
    }

    @Test
    fun `handleAction SaveContent with unequal curriculum content size and module size throws exception`() = runTest {
        // Given
        val error = "error"
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }
        val syllabusDescription = "Syllabus description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val generatedCurriculumResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Curriculum title"
            every { description } returns "Curriculum description"
            every { content } returns listOf("Module1 title", "Module2 title")
        }
        val generatedModuleResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Module1 title"
            every { description } returns "Module description"
            every { content } returns listOf("lesson1", "lesson2")
        }

        appStateFlow.update { it.copy(profile = profile) }
        advanceUntilIdle()

        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedCurriculumResponse))
        coEvery { generateModuleUseCase("Module1 title", profile, any()) } returns flowOf(Result.success(generatedModuleResponse))
        coEvery { resourceProvider.getString(any()) } returns error
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateModule("Module1 title"))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.SaveContent)
        advanceUntilIdle()

        // Then
        coVerifyAll(true) {
            uploadCurriculumAndModulesUseCase("profileId", any(), any())
        }

        with(viewModel.state.value) {
            assertEquals(1, modules.size)
        }

        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)
        assertEquals(error, (uiEvents.first() as UIEvent.ShowSnackbar).message)

        job.cancel()
    }

    @Test
    fun `handleAction SaveContent should handle error when unsuccessful`() = runTest {
        // Given
        val syllabusDescription = "Syllabus description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val generatedCurriculumResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Curriculum title"
            every { description } returns "Curriculum description"
            every { content } returns listOf("Module1 title")
        }
        val generatedModuleResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Module1 title"
            every { description } returns "Module description"
            every { content } returns listOf("lesson1", "lesson2")
        }
        val error = "Upload error"
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        appStateFlow.update { it.copy(profile = profile) }
        advanceUntilIdle()

        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedCurriculumResponse))
        coEvery { generateModuleUseCase("Module1 title", profile, any()) } returns flowOf(Result.success(generatedModuleResponse))
        coEvery { uploadCurriculumAndModulesUseCase("profileId", any(), any()) } returns Result.failure(Exception("Upload error"))
        coEvery { resourceProvider.getString(any()) } returns error

        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateModule("Module1 title"))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.SaveContent)
        advanceUntilIdle()

        // Then
        coVerify {
            uploadCurriculumAndModulesUseCase("profileId", any(), any())
        }

        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)
        assertEquals(error, (uiEvents.first() as UIEvent.ShowSnackbar).message)

        job.cancel()
    }

    @Test
    fun `handleAction DiscardContent should clear curriculum and modules from state`() = runTest {
        // Given
        val syllabusDescription = "Syllabus description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val generatedCurriculumResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Curriculum title"
            every { description } returns "Curriculum description"
            every { content } returns listOf("Module1 title")
        }
        val generatedModuleResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Module1 title"
            every { description } returns "Module description"
            every { content } returns listOf("lesson1", "lesson2")
        }

        appStateFlow.update { it.copy(profile = profile) }
        advanceUntilIdle()

        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedCurriculumResponse))
        coEvery { generateModuleUseCase("Module1 title", profile, any()) } returns flowOf(Result.success(generatedModuleResponse))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateModule("Module1 title"))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.DiscardContent)
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertNull(curriculum)
            assertTrue(modules.isEmpty())
        }
    }

    @Test
    fun `handleAction EditSearchQuery with non-empty query should update search query and filter curricula`() = runTest {
        // Given
        val curr1 = Curriculum(
            title = "Intro to Kotlin",
            description = "Basics of Kotlin",
            content = listOf()
        )
        val curr2 = Curriculum(
            title = "Advanced Kotlin",
            description = "Coroutines, Flows, etc.",
            content = listOf()
        )
        val curr3 = Curriculum(
            title = "Intro to Java",
            description = "Basics of Java",
            content = listOf()
        )

        // Pretend our appStateFlow contains a bundleManager returning these curricula.
        val bundleManager = mockk<BundleManager>(relaxed = true) {
            every { getCurricula() } returns listOf(curr1, curr2, curr3)
        }

        // Update the flow so the LibraryViewModel picks up these curricula.
        appStateFlow.update {
            it.copy(bundleManager = bundleManager)
        }
        advanceUntilIdle()

        // Initially, the ViewModel collects from appStateFlow and sets curricula/filteredCurricula.
        // When
        viewModel.handleAction(LibraryAction.EditFilterQuery("Intro"))
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            // The filterQuery is "Intro"
            assertEquals("Intro", filterQuery)
            // We expect only the "Intro to Kotlin" and "Intro to Java" to remain.
            assertEquals(2, filteredCurricula.size)
            assertTrue(filteredCurricula.any { it.title == "Intro to Kotlin" })
            assertTrue(filteredCurricula.any { it.title == "Intro to Java" })
            assertFalse(filteredCurricula.any { it.title == "Advanced Kotlin" })
        }
    }

    @Test
    fun `handleAction EditSearchQuery with empty query should reset filtered curricula`() = runTest {
        // Given
        val curr1 = Curriculum(title = "Intro to Kotlin", description = "", content = listOf())
        val curr2 = Curriculum(title = "Advanced Kotlin", description = "", content = listOf())

        // Mock the bundleManager to return 2 curricula.
        val bundleManager = mockk<BundleManager>(relaxed = true) {
            every { getCurricula() } returns listOf(curr1, curr2)
        }

        // Update the flow
        appStateFlow.update {
            it.copy(bundleManager = bundleManager)
        }
        advanceUntilIdle()

        // Filter them first with a non-empty query
        viewModel.handleAction(LibraryAction.EditFilterQuery("Intro"))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.filteredCurricula.size)

        // When: we now pass an empty query to reset
        viewModel.handleAction(LibraryAction.EditFilterQuery(""))
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals("", filterQuery)
            // filteredCurricula should be the full list
            assertEquals(2, filteredCurricula.size)
            assertTrue(filteredCurricula.any { it.title == "Intro to Kotlin" })
            assertTrue(filteredCurricula.any { it.title == "Advanced Kotlin" })
        }
    }

    @Test
    fun `handleAction ClearSearchQuery should reset search query and filtered curricula`() = runTest {
        // Given
        val curr1 = Curriculum(title = "Intro to Kotlin", description = "", content = listOf())
        val curr2 = Curriculum(title = "Advanced Kotlin", description = "", content = listOf())

        // Mock the bundleManager to return 2 curricula.
        val bundleManager = mockk<BundleManager>(relaxed = true) {
            every { getCurricula() } returns listOf(curr1, curr2)
        }

        // Update the flow
        appStateFlow.update {
            it.copy(bundleManager = bundleManager)
        }
        advanceUntilIdle()

        // Filter them first with a non-empty query
        viewModel.handleAction(LibraryAction.EditFilterQuery("Intro"))
        advanceUntilIdle()
        assertEquals("Intro", viewModel.state.value.filterQuery)
        assertEquals(1, viewModel.state.value.filteredCurricula.size)

        // When
        viewModel.handleAction(LibraryAction.ClearFilterQuery)
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            // The query should be empty, and the filteredCurricula
            // should match the full curricula list again.
            assertEquals("", filterQuery)
            assertEquals(2, filteredCurricula.size)
        }
    }

    @Test
    fun `handleAction Navigate with null curriculum should handle navigation`() = runTest {
        // Given
        val destination = Route.ProfileManagement
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.handleAction(LibraryAction.Navigate(destination))
        advanceUntilIdle()

        // Then
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.Navigate)
        assertEquals(destination, (uiEvents.first() as UIEvent.Navigate).destination)

        job.cancel()
    }

    @Test
    fun `handleAction Navigate with curriculum should set showDiscardWarningDialog to true`() = runTest {
        // Given
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }
        val syllabusDescription = "Curriculum description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val generatedResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Curriculum title"
            every { description } returns "Curriculum description"
            every { content } returns listOf("module1", "module2")
        }
        appStateFlow.update { it.copy(profile = profile) }
        advanceUntilIdle()

        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedResponse))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.Navigate(Route.ProfileManagement))
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertTrue(showDiscardWarningDialog)
        }
        assertEquals(0, uiEvents.size)

        job.cancel()
    }

    @Test
    fun `handleAction HideDiscardWarningDialog should set showDiscardWarningDialog to false`() = runTest {
        // Given
        val syllabusDescription = "Curriculum description"
        val profile = mockk<Profile> { every { id } returns "profileId" }
        val generatedResponse = mockk<ContentGeneratorClient.GeneratedResponse> {
            every { title } returns "Curriculum title"
            every { description } returns "Curriculum description"
            every { content } returns listOf("module1", "module2")
        }

        appStateFlow.update { it.copy(profile = profile) }
        advanceUntilIdle()

        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedResponse))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.Navigate(Route.ProfileManagement))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showDiscardWarningDialog)

        // When
        viewModel.handleAction(LibraryAction.HideDiscardWarningDialog)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.state.value.showDiscardWarningDialog)
    }

    @Test
    fun `handleAction OpenCurriculum should set curriculum and modules in state`() = runTest {
        // Given
        val curriculumId = "abc123"
        val mockCurriculum = Curriculum(
            id = curriculumId,
            title = "Demo Curriculum",
            description = "A sample curriculum",
            content = listOf("Module A", "Module B")
        )
        val mockModules = listOf(
            Module(
                title = "Module A",
                description = "Module A description",
                content = listOf("Lesson 1", "Lesson 2")
            ),
            Module(
                title = "Module B",
                description = "Module B description",
                content = listOf("Lesson 3", "Lesson 4")
            )
        )

        // Mock bundleManager so we return that curriculum + modules for the given ID
        val bundleManager = mockk<BundleManager>(relaxed = true) {
            every { getCurriculumByKey(curriculumId) } returns mockCurriculum
            every { getModulesByCurriculum(curriculumId) } returns mockModules
        }

        // Update the flow so the VM picks up this bundleManager
        appStateFlow.update {
            it.copy(bundleManager = bundleManager)
        }
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.OpenCurriculum(curriculumId))
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(mockCurriculum, curriculum)
            assertEquals(mockModules.size, modules.size)
            assertTrue(modules.any { it.title == "Module A" })
            assertTrue(modules.any { it.title == "Module B" })
            assertEquals(LibraryUIState.DisplayMode.View, displayMode)
        }
    }
}