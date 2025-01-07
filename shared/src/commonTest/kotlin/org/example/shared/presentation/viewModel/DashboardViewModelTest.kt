package org.example.shared.presentation.viewModel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.activity.GetWeeklyActivityUseCase
import org.example.shared.domain.use_case.curriculum.FetchActiveCurriculumUseCase
import org.example.shared.domain.use_case.curriculum.FetchCurriculaByUserUseCase
import org.example.shared.domain.use_case.lesson.CountLessonsByStatusUseCase
import org.example.shared.domain.use_case.lesson.FetchLessonsByModuleUseCase
import org.example.shared.domain.use_case.module.CountModulesByStatusUseCase
import org.example.shared.domain.use_case.module.FetchModulesByCurriculumUseCase
import org.example.shared.domain.use_case.profile.FetchProfileUseCase
import org.example.shared.domain.use_case.section.CountSectionsByStatusUseCase
import org.example.shared.domain.use_case.section.FetchSectionsByLessonUseCase
import org.example.shared.domain.use_case.session.FetchSessionsByUserUseCase
import org.example.shared.presentation.util.UIEvent
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    // ViewModel and dependencies
    private lateinit var viewModel: DashboardViewModel
    private lateinit var fetchProfileUseCase: FetchProfileUseCase
    private lateinit var fetchSessionsByUserUseCase: FetchSessionsByUserUseCase
    private lateinit var fetchCurriculaByUserUseCase: FetchCurriculaByUserUseCase
    private lateinit var fetchModulesByCurriculumUseCase: FetchModulesByCurriculumUseCase
    private lateinit var fetchLessonsByModuleUseCase: FetchLessonsByModuleUseCase
    private lateinit var fetchSectionsByLessonUseCase: FetchSectionsByLessonUseCase
    private lateinit var getWeeklyActivityUseCase: GetWeeklyActivityUseCase
    private lateinit var countModulesByStatusUseCase: CountModulesByStatusUseCase
    private lateinit var countLessonsByStatusUseCase: CountLessonsByStatusUseCase
    private lateinit var countSectionsByStatusUseCase: CountSectionsByStatusUseCase
    private lateinit var fetchActiveCurriculumUseCase: FetchActiveCurriculumUseCase
    private lateinit var dispatcher: TestDispatcher
    private lateinit var syncManager: SyncManager<DatabaseRecord>
    private val syncManagers = mutableListOf<SyncManager<DatabaseRecord>>()

    @Before
    fun setUp() {
        // Initialize TestDispatcher and set as Main dispatcher
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)

        // Mock dependencies using MockK
        fetchProfileUseCase = mockk()
        fetchSessionsByUserUseCase = mockk()
        fetchModulesByCurriculumUseCase = mockk()
        fetchLessonsByModuleUseCase = mockk()
        fetchSectionsByLessonUseCase = mockk()
        countModulesByStatusUseCase = mockk()
        countLessonsByStatusUseCase = mockk()
        countSectionsByStatusUseCase = mockk()
        getWeeklyActivityUseCase = mockk()
        fetchCurriculaByUserUseCase = mockk()
        fetchActiveCurriculumUseCase = mockk()
        syncManager = mockk(relaxed = true)
        syncManagers.add(syncManager)

        // Instantiate ViewModel with mocked dependencies
        viewModel = DashboardViewModel(
            fetchProfileUseCase = fetchProfileUseCase,
            fetchSessionsByUserUseCase = fetchSessionsByUserUseCase,
            fetchActiveCurriculumUseCase = fetchActiveCurriculumUseCase,
            fetchCurriculaByUserUseCase = fetchCurriculaByUserUseCase,
            fetchModulesByCurriculumUseCase = fetchModulesByCurriculumUseCase,
            fetchLessonsByModuleUseCase = fetchLessonsByModuleUseCase,
            fetchSectionsByLessonUseCase = fetchSectionsByLessonUseCase,
            getWeeklyActivityUseCase = getWeeklyActivityUseCase,
            countModulesByStatusUseCase = countModulesByStatusUseCase,
            countLessonsByStatusUseCase = countLessonsByStatusUseCase,
            countSectionsByStatusUseCase = countSectionsByStatusUseCase,
            dispatcher = dispatcher,
            syncManagers = syncManagers,
            sharingStarted = SharingStarted.Eagerly,
        )

        // Set up default behaviors for mocks
        every { syncManager.syncStatus } returns MutableStateFlow(SyncManager.SyncStatus.Idle)

        coEvery { fetchProfileUseCase() } returns Result.success(profile)
        coEvery { fetchSessionsByUserUseCase(any()) } returns Result.success(emptyList())
        coEvery { fetchActiveCurriculumUseCase(any()) } returns Result.success(activeCurriculum)
        coEvery { fetchCurriculaByUserUseCase(any()) } returns Result.success(emptyList())

        coEvery { fetchModulesByCurriculumUseCase(any(), any()) } returns Result.success(emptyList())
        coEvery { fetchLessonsByModuleUseCase(any(), any(), any()) } returns Result.success(emptyList())
        coEvery { fetchSectionsByLessonUseCase(any(), any(), any(), any()) } returns Result.success(emptyList())

        coEvery { getWeeklyActivityUseCase(any()) } returns Result.success(emptyMap())

        coEvery { countModulesByStatusUseCase(activeCurriculum.id) } returns Result.success(emptyMap())
        coEvery { countLessonsByStatusUseCase(activeCurriculum.id) } returns Result.success(emptyMap())
        coEvery { countSectionsByStatusUseCase(activeCurriculum.id) } returns Result.success(emptyMap())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getProfileUseCase success should update profile, and set isLoading to false`() = runTest {
        // Given
        coEvery { fetchProfileUseCase() } returns Result.success(profile)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(profile, this.profile)
            assertFalse(isLoading)
        }

        coVerify {
            fetchProfileUseCase()
        }
    }

    @Test
    fun `getProfileUseCase failure should handle error and set isLoading to false`() = runTest {
        // Given
        val exception = Exception("Failed to fetch profile")
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { fetchProfileUseCase() } returns Result.failure(exception)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertNull(profile)
            assertFalse(isLoading)
        }

        coVerify {
            fetchProfileUseCase()
        }

        // Verify UI events
        assertTrue(uiEvents.isNotEmpty())
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `getWeeklyActivityUseCase success should update weeklyActivity`() = runTest {
        // Given
        val weeklyActivity = mapOf(DayOfWeek.MONDAY to Pair(1L, 1))

        coEvery { getWeeklyActivityUseCase(any()) } returns Result.success(weeklyActivity)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(weeklyActivity, this.weeklyActivity)
        }

        coVerify {
            getWeeklyActivityUseCase(any())
        }
    }

    @Test
    fun `getAllCurricula success should update curricula`() = runTest {
        // Given
        val curricula = listOf(mockk<Curriculum>(relaxed = true))
        coEvery { fetchCurriculaByUserUseCase(any()) } returns Result.success(curricula)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(curricula, this.curricula)
        }

        coVerify {
            fetchCurriculaByUserUseCase(any())
        }
    }

    @Test
    fun `getActiveCurriculumData should update active curriculum`() = runTest {
        // Given
        coEvery { fetchActiveCurriculumUseCase(any()) } returns Result.success(activeCurriculum)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(activeCurriculum, this.activeCurriculum)
        }

        coVerify {
            fetchActiveCurriculumUseCase(any())
        }
    }

    @Test
    fun `getAllModulesData should update modules`() = runTest {
        // Given
        val modules = listOf(mockk<Module>(relaxed = true))
        coEvery { fetchModulesByCurriculumUseCase(any(), any()) } returns Result.success(modules)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(modules, this.modules)
        }

        coVerify {
            fetchModulesByCurriculumUseCase(any(), any())
        }
    }

    @Test
    fun `getAllModulesData failure should handle error`() = runTest {
        // Given
        val exception = Exception("Failed to fetch modules")
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { fetchModulesByCurriculumUseCase(any(), any()) } returns Result.failure(exception)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertTrue(modules.isEmpty())
        }

        coVerify {
            fetchModulesByCurriculumUseCase(any(), any())
        }

        // Verify UI events
        assertTrue(uiEvents.isNotEmpty())
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `countModulesByStatus should update modulesByStatus`() = runTest {
        // Given
        val modulesByStatus = mapOf(Status.FINISHED to 1, Status.UNFINISHED to 2)
        coEvery { countModulesByStatusUseCase(activeCurriculum.id) } returns Result.success(modulesByStatus)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(modulesByStatus, this.moduleCountByStatus)
        }

        coVerify {
            countModulesByStatusUseCase(activeCurriculum.id)
        }
    }

    @Test
    fun `countModulesByStatus failure should handle error`() = runTest {
        // Given
        val exception = Exception("Failed to count modules by status")
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { countModulesByStatusUseCase(activeCurriculum.id) } returns Result.failure(exception)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertTrue(moduleCountByStatus.isEmpty())
        }

        coVerify {
            countModulesByStatusUseCase(activeCurriculum.id)
        }

        // Verify UI events
        assertTrue(uiEvents.isNotEmpty())
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `countLessonsByStatus should update lessonsByStatus`() = runTest {
        // Given
        val lessonsByStatus = mapOf(Status.FINISHED to 1, Status.UNFINISHED to 2)

        coEvery { fetchModulesByCurriculumUseCase(any(), any()) } returns Result.success(listOf(mockk(relaxed = true)))
        coEvery { fetchLessonsByModuleUseCase(any(), any(), any()) } returns Result.success(listOf(mockk(relaxed = true)))
        coEvery { countLessonsByStatusUseCase(activeCurriculum.id) } returns Result.success(lessonsByStatus)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(lessonsByStatus, this.lessonCountByStatus)
        }

        coVerify {
            fetchModulesByCurriculumUseCase(any(), any())
            fetchLessonsByModuleUseCase(any(), any(), any())
            countLessonsByStatusUseCase(activeCurriculum.id)
        }
    }

    @Test
    fun `countLessonsByStatus failure should handle error`() = runTest {
        // Given
        val exception = Exception("Failed to count lessons by status")
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { fetchModulesByCurriculumUseCase(any(), any()) } returns Result.success(listOf(mockk(relaxed = true)))
        coEvery { fetchLessonsByModuleUseCase(any(), any(), any()) } returns Result.success(listOf(mockk(relaxed = true)))
        coEvery { countLessonsByStatusUseCase(activeCurriculum.id) } returns Result.failure(exception)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertTrue(lessonCountByStatus.isEmpty())
        }

        coVerify {
            countLessonsByStatusUseCase(activeCurriculum.id)
        }

        // Verify UI events
        assertTrue(uiEvents.isNotEmpty())
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `countSectionsByStatus should update sectionsByStatus`() = runTest {
        // Given
        val sectionsByStatus = mapOf(Status.FINISHED to 1, Status.UNFINISHED to 2)

        coEvery { fetchModulesByCurriculumUseCase(any(), any()) } returns Result.success(listOf(mockk(relaxed = true)))
        coEvery { fetchLessonsByModuleUseCase(any(), any(), any()) } returns Result.success(listOf(mockk(relaxed = true)))
        coEvery { fetchSectionsByLessonUseCase(any(), any(), any(), any()) } returns Result.success(listOf(mockk(relaxed = true)))
        coEvery { countSectionsByStatusUseCase(activeCurriculum.id) } returns Result.success(sectionsByStatus)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(sectionsByStatus, this.sectionCountByStatus)
        }

        coVerify {
            fetchModulesByCurriculumUseCase(any(), any())
            fetchLessonsByModuleUseCase(any(), any(), any())
            fetchSectionsByLessonUseCase(any(), any(), any(), any())
            countSectionsByStatusUseCase(activeCurriculum.id)
        }
    }

    @Test
    fun `countSectionsByStatus failure should handle error`() = runTest {
        // Given
        val exception = Exception("Failed to count sections by status")
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { fetchModulesByCurriculumUseCase(any(), any()) } returns Result.success(listOf(mockk(relaxed = true)))
        coEvery { fetchLessonsByModuleUseCase(any(), any(), any()) } returns Result.success(listOf(mockk(relaxed = true)))
        coEvery { fetchSectionsByLessonUseCase(any(), any(), any(), any()) } returns Result.success(listOf(mockk(relaxed = true)))
        coEvery { countSectionsByStatusUseCase(activeCurriculum.id) } returns Result.failure(exception)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertTrue(sectionCountByStatus.isEmpty())
        }

        coVerify {
            countSectionsByStatusUseCase(activeCurriculum.id)
        }

        // Verify UI events
        assertTrue(uiEvents.isNotEmpty())
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    companion object {
        private val profile = mockk<Profile> {
            every { id } returns "user123"
        }
        private val activeCurriculum = mockk<Curriculum>(relaxed = true)
    }
}
