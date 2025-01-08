package org.example.composeApp.viewModel

import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.example.composeApp.presentation.action.LibraryAction
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.ui.util.UIEvent
import org.example.composeApp.presentation.viewModel.LibraryViewModel
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.model.Curriculum
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {
    private lateinit var viewModel: LibraryViewModel
    private lateinit var syncManager: SyncManager<DatabaseRecord>
    private lateinit var fetchProfileUseCase: FetchProfileUseCase
    private lateinit var fetchCurriculaByUserUseCase: FetchCurriculaByUserUseCase
    private lateinit var summarizeSyllabusUseCase: SummarizeSyllabusUseCase
    private lateinit var generateCurriculumUseCase: GenerateCurriculumUseCase
    private lateinit var generateModuleUseCase: GenerateModuleUseCase
    private lateinit var uploadCurriculumUseCase: UploadCurriculumUseCase
    private lateinit var uploadModulesUseCase: UploadModulesUseCase
    private lateinit var deleteCurriculumUseCase: DeleteCurriculumUseCase
    private lateinit var testDispatcher: TestDispatcher
    private val syncStatus = MutableStateFlow<SyncManager.SyncStatus>(SyncManager.SyncStatus.Idle)

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        syncManager = mockk(relaxed = true)
        fetchProfileUseCase = mockk(relaxed = true)
        fetchCurriculaByUserUseCase = mockk(relaxed = true)
        summarizeSyllabusUseCase = mockk(relaxed = true)
        generateCurriculumUseCase = mockk(relaxed = true)
        generateModuleUseCase = mockk(relaxed = true)
        uploadCurriculumUseCase = mockk(relaxed = true)
        uploadModulesUseCase = mockk(relaxed = true)

        viewModel = LibraryViewModel(
            fetchProfileUseCase,
            fetchCurriculaByUserUseCase,
            summarizeSyllabusUseCase,
            generateCurriculumUseCase,
            generateModuleUseCase,
            uploadCurriculumUseCase,
            uploadModulesUseCase,
            deleteCurriculumUseCase,
            testDispatcher,
            listOf(syncManager),
            SharingStarted.Eagerly
        )

        every { syncManager.syncStatus } returns syncStatus
        coEvery { fetchProfileUseCase() } returns Result.success(mockk<Profile> { every { id } returns "profileId" })
        coEvery { fetchCurriculaByUserUseCase(any()) } returns Result.success(emptyList())
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `handleAction Refresh when refresh succeeds should update profile and curricula`() = runTest {
        // Given
        val expectedProfile = mockk<Profile> { every { id } returns "profileId" }
        val expectedCurricula = listOf(mockk<Curriculum>())

        coEvery { fetchProfileUseCase() } returns Result.success(expectedProfile)
        coEvery { fetchCurriculaByUserUseCase(any()) } returns Result.success(expectedCurricula)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(expectedProfile, profile)
            assertEquals(expectedCurricula, curricula)
        }
        coVerify {
            fetchProfileUseCase()
            fetchCurriculaByUserUseCase(any())
        }
    }

    @Test
    fun `handleAction Refresh when refresh fails should handle error and update loading state`() = runTest {
        // Given
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { fetchProfileUseCase() } returns Result.failure(Exception("Profile error"))

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertNull(profile)
        }
        coVerify {

            fetchProfileUseCase()
        }

        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
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

        coEvery { fetchProfileUseCase() } returns Result.success(profile)
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

        coEvery { fetchProfileUseCase() } returns Result.success(profile)
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
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
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

        coEvery { fetchProfileUseCase() } returns Result.success(profile)
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
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
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
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
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
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedCurriculumResponse))
        coEvery { generateModuleUseCase("Module1 title", profile, any()) } returns flowOf(Result.success(generatedModuleResponse))
        coEvery { uploadCurriculumUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { uploadModulesUseCase(any(), any(), any()) } returns Result.success(Unit)
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
            uploadCurriculumUseCase(any(), any())
            uploadModulesUseCase(any(), any(), any())
        }
    }

    @Test
    fun `handleAction SaveContent with unequal curriculum content size and module size throws exception`() = runTest {
        // Given
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
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
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
        viewModel.handleAction(LibraryAction.SaveContent)
        advanceUntilIdle()

        // Then
        coVerifyAll(true) {
            uploadCurriculumUseCase(any(), any())
            uploadModulesUseCase(any(), any(), any())
        }

        with(viewModel.state.value) {
            assertEquals(1, modules.size)
        }

        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

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
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { fetchProfileUseCase() } returns Result.success(profile)
        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedCurriculumResponse))
        coEvery { generateModuleUseCase("Module1 title", profile, any()) } returns flowOf(Result.success(generatedModuleResponse))
        coEvery { uploadCurriculumUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { uploadModulesUseCase(any(), any(), any()) } returns Result.failure(Exception("Module error"))
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
            uploadCurriculumUseCase(any(), any())
            uploadModulesUseCase(any(), any(), any())
        }

        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

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
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
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
        val query = "Curriculum 1"
        val curricula = listOf(
            Curriculum(title = "Curriculum 1", description = "Description 1", content = listOf("Module 1")),
            Curriculum(title = "Curriculum 2", description = "Description 2", content = listOf("Module 2"))
        )
        coEvery { fetchCurriculaByUserUseCase(any()) } returns Result.success(curricula)
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.EditFilterQuery(query))
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(query, filterQuery)
            assertEquals(1, filteredCurricula.size)
            assertContains(filteredCurricula, curricula.first())
        }
    }

    @Test
    fun `handleAction EditSearchQuery with empty query should reset filtered curricula`() = runTest {
        // Given
        val query = ""
        val curricula = listOf(
            Curriculum(title = "Curriculum 1", description = "Description 1", content = listOf("Module 1")),
            Curriculum(title = "Curriculum 2", description = "Description 2", content = listOf("Module 2"))
        )
        coEvery { fetchCurriculaByUserUseCase(any()) } returns Result.success(curricula)
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditFilterQuery(query))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.EditFilterQuery(""))
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(curricula.size, filteredCurricula.size)
        }
    }

    @Test
    fun `handleAction ClearSearchQuery should reset search query and filtered curricula`() = runTest {
        // Given
        val query = "Curriculum 1"
        val curricula = listOf(
            Curriculum(title = "Curriculum 1", description = "Description 1", content = listOf("Module 1")),
            Curriculum(title = "Curriculum 2", description = "Description 2", content = listOf("Module 2"))
        )
        coEvery { fetchCurriculaByUserUseCase(any()) } returns Result.success(curricula)

        viewModel.handleAction(LibraryAction.EditFilterQuery(query))
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.ClearFilterQuery)
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals("", filterQuery)
            assertEquals(curricula.size, filteredCurricula.size)
        }
    }

    @Test
    fun `handleAction Navigate with null curriculum should handle navigation`() = runTest {
        // Given
        val destination = Route.Profile
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
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedResponse))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        // When
        viewModel.handleAction(LibraryAction.Navigate(Route.Profile))
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
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
        coEvery { generateCurriculumUseCase(syllabusDescription, profile) } returns flowOf(Result.success(generatedResponse))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.EditSyllabusDescription(syllabusDescription))
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.GenerateCurriculum)
        advanceUntilIdle()

        viewModel.handleAction(LibraryAction.Navigate(Route.Profile))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showDiscardWarningDialog)

        // When
        viewModel.handleAction(LibraryAction.HideDiscardWarningDialog)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.state.value.showDiscardWarningDialog)
    }
}