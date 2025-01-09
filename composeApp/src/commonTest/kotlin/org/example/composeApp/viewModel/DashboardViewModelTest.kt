package org.example.composeApp.viewModel

import androidx.lifecycle.SavedStateHandle
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.example.composeApp.presentation.ui.util.UIEvent
import org.example.composeApp.presentation.viewModel.DashboardViewModel
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Bundle
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.activity.FetchWeeklyActivityByUserUseCase
import org.example.shared.domain.use_case.curriculum.FetchActiveCurriculumUseCase
import org.example.shared.domain.use_case.curriculum.FetchCurriculumBundleUseCase
import org.example.shared.domain.use_case.lesson.CountLessonsByStatusUseCase
import org.example.shared.domain.use_case.module.CountModulesByStatusUseCase
import org.example.shared.domain.use_case.profile.FetchProfileUseCase
import org.example.shared.domain.use_case.section.CountSectionsByStatusUseCase
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
    private lateinit var fetchActiveCurriculumUseCase: FetchActiveCurriculumUseCase
    private lateinit var fetchCurriculumBundleUseCase: FetchCurriculumBundleUseCase
    private lateinit var fetchWeeklyActivityByUserUseCase: FetchWeeklyActivityByUserUseCase
    private lateinit var countModulesByStatusUseCase: CountModulesByStatusUseCase
    private lateinit var countLessonsByStatusUseCase: CountLessonsByStatusUseCase
    private lateinit var countSectionsByStatusUseCase: CountSectionsByStatusUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var dispatcher: TestDispatcher
    private lateinit var syncManager: SyncManager<DatabaseRecord>
    private lateinit var syncManagers: MutableList<SyncManager<DatabaseRecord>>

    @Before
    fun setUp() {
        // Initialize TestDispatcher and set as Main dispatcher
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)

        // Mock dependencies using MockK
        fetchProfileUseCase = mockk()
        fetchActiveCurriculumUseCase = mockk()
        fetchCurriculumBundleUseCase = mockk()
        countModulesByStatusUseCase = mockk()
        countLessonsByStatusUseCase = mockk()
        countSectionsByStatusUseCase = mockk()
        fetchWeeklyActivityByUserUseCase = mockk()
        fetchActiveCurriculumUseCase = mockk()
        savedStateHandle = mockk()
        syncManagers = mutableListOf<SyncManager<DatabaseRecord>>()
        syncManager = mockk(relaxed = true)
        syncManagers.add(syncManager)

        // Instantiate ViewModel with mocked dependencies
        viewModel = DashboardViewModel(
            fetchProfileUseCase = fetchProfileUseCase,
            fetchWeeklyActivityByUserUseCase = fetchWeeklyActivityByUserUseCase,
            fetchActiveCurriculumUseCase = fetchActiveCurriculumUseCase,
            fetchCurriculumBundleUseCase = fetchCurriculumBundleUseCase,
            countModulesByStatusUseCase = countModulesByStatusUseCase,
            countLessonsByStatusUseCase = countLessonsByStatusUseCase,
            countSectionsByStatusUseCase = countSectionsByStatusUseCase,
            dispatcher = dispatcher,
            savedStateHandle = savedStateHandle,
            syncManagers = syncManagers,
            sharingStarted = SharingStarted.Eagerly,
        )

        // Set up default behaviors for mocks
        every { syncManager.syncStatus } returns MutableStateFlow(SyncManager.SyncStatus.Idle)

        coEvery { fetchWeeklyActivityByUserUseCase(any(), any()) } returns Result.success(emptyMap())

        every { savedStateHandle.get<String>(any()) } returns "user123"
        coEvery { fetchProfileUseCase() } returns Result.success(profile)
        coEvery { fetchActiveCurriculumUseCase(any()) } returns Result.success(activeCurriculum)
        coEvery { fetchCurriculumBundleUseCase(any(), any()) } returns Bundle(activeCurriculum, emptyList(), emptyList(), emptyList())

        coEvery { countModulesByStatusUseCase(activeCurriculum.id) } returns Result.success(emptyMap())
        coEvery { countLessonsByStatusUseCase(activeCurriculum.id) } returns Result.success(emptyMap())
        coEvery { countSectionsByStatusUseCase(activeCurriculum.id) } returns Result.success(emptyMap())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `getProfileUseCase success should update profile, and set isLoading to false`() = runTest {
        // Given
        every { savedStateHandle.get<String>(any()) } returns null
        coEvery { fetchProfileUseCase() } returns Result.success(profile)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(profileId, this.profileId)
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

        every { savedStateHandle.get<String>(any()) } returns null
        coEvery { fetchProfileUseCase() } returns Result.failure(exception)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
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

        coEvery { fetchWeeklyActivityByUserUseCase(any(), any()) } returns Result.success(weeklyActivity)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(weeklyActivity, this.weeklyActivity)
        }

        coVerify {
            fetchWeeklyActivityByUserUseCase(any(), any())
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
    fun `fetchActiveCurriculumData should update curriculum and modules`() = runTest {
        // Given
        val modules = listOf(mockk<Module>(relaxed = true))
        coEvery { fetchCurriculumBundleUseCase(any(), any()) } returns Bundle(activeCurriculum, modules, emptyList(), emptyList())

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(modules, this.modules)
        }

        coVerify {
            fetchCurriculumBundleUseCase(any(), any())
        }
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

        coEvery { countLessonsByStatusUseCase(activeCurriculum.id) } returns Result.success(lessonsByStatus)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(lessonsByStatus, this.lessonCountByStatus)
        }

        coVerify {
            fetchCurriculumBundleUseCase(any(), any())
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

        coEvery { countSectionsByStatusUseCase(activeCurriculum.id) } returns Result.success(sectionsByStatus)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(sectionsByStatus, this.sectionCountByStatus)
        }

        coVerify {
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
