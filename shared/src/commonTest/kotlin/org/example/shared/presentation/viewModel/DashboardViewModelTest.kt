package org.example.shared.presentation.viewModel

import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.example.shared.data.remote.firestore.FirestorePathBuilder
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.curriculum.GetActiveCurriculumUseCase
import org.example.shared.domain.use_case.curriculum.GetAllCurriculaUseCase
import org.example.shared.domain.use_case.lesson.CountLessonsByStatusUseCase
import org.example.shared.domain.use_case.lesson.GetAllLessonsUseCase
import org.example.shared.domain.use_case.module.CountModulesByStatusUseCase
import org.example.shared.domain.use_case.module.GetAllModulesUseCase
import org.example.shared.domain.use_case.other.GetWeeklyActivityUseCase
import org.example.shared.domain.use_case.path.*
import org.example.shared.domain.use_case.profile.GetProfileUseCase
import org.example.shared.domain.use_case.section.CountSectionsByStatusUseCase
import org.example.shared.domain.use_case.section.GetAllSectionsUseCase
import org.example.shared.domain.use_case.session.GetAllSessionsUseCase
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
    private lateinit var buildProfilePathUseCase: BuildProfilePathUseCase
    private lateinit var buildSessionPathUseCase: BuildSessionPathUseCase
    private lateinit var buildCurriculumPathUseCase: BuildCurriculumPathUseCase
    private lateinit var buildModulePathUseCase: BuildModulePathUseCase
    private lateinit var buildLessonPathUseCase: BuildLessonPathUseCase
    private lateinit var buildSectionPathUseCase: BuildSectionPathUseCase
    private lateinit var getProfileUseCase: GetProfileUseCase
    private lateinit var getAllSessionsUseCase: GetAllSessionsUseCase
    private lateinit var getAllCurriculaUseCase: GetAllCurriculaUseCase
    private lateinit var getAllModulesUseCase: GetAllModulesUseCase
    private lateinit var getAllLessonsUseCase: GetAllLessonsUseCase
    private lateinit var getAllSectionsUseCase: GetAllSectionsUseCase
    private lateinit var getWeeklyActivityUseCase: GetWeeklyActivityUseCase
    private lateinit var countModulesByStatusUseCase: CountModulesByStatusUseCase
    private lateinit var countLessonsByStatusUseCase: CountLessonsByStatusUseCase
    private lateinit var countSectionsByStatusUseCase: CountSectionsByStatusUseCase
    private lateinit var getActiveCurriculumUseCase: GetActiveCurriculumUseCase
    private lateinit var dispatcher: TestDispatcher
    private lateinit var syncManager: SyncManager<DatabaseRecord>
    private val syncManagers = mutableListOf<SyncManager<DatabaseRecord>>()

    @Before
    fun setUp() {
        // Initialize TestDispatcher and set as Main dispatcher
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)

        // Mock dependencies using MockK
        buildProfilePathUseCase = mockk()
        buildSessionPathUseCase = mockk()
        buildCurriculumPathUseCase = mockk()
        buildModulePathUseCase = mockk()
        buildLessonPathUseCase = mockk()
        buildSectionPathUseCase = mockk()
        getProfileUseCase = mockk()
        getAllSessionsUseCase = mockk()
        getAllModulesUseCase = mockk()
        getAllLessonsUseCase = mockk()
        getAllSectionsUseCase = mockk()
        countModulesByStatusUseCase = mockk()
        countLessonsByStatusUseCase = mockk()
        countSectionsByStatusUseCase = mockk()
        getWeeklyActivityUseCase = mockk()
        getAllCurriculaUseCase = mockk()
        getActiveCurriculumUseCase = mockk()
        syncManager = mockk(relaxed = true)
        syncManagers.add(syncManager)

        // Instantiate ViewModel with mocked dependencies
        viewModel = DashboardViewModel(
            buildProfilePathUseCase = buildProfilePathUseCase,
            buildSessionPathUseCase = buildSessionPathUseCase,
            buildCurriculumPathUseCase = buildCurriculumPathUseCase,
            buildModulePathUseCase = buildModulePathUseCase,
            buildLessonPathUseCase = buildLessonPathUseCase,
            buildSectionPathUseCase = buildSectionPathUseCase,
            getProfileUseCase = getProfileUseCase,
            getAllSessionsUseCase = getAllSessionsUseCase,
            getActiveCurriculumUseCase = getActiveCurriculumUseCase,
            getAllCurriculaUseCase = getAllCurriculaUseCase,
            getAllLessonsUseCase = getAllLessonsUseCase,
            getAllSectionsUseCase = getAllSectionsUseCase,
            getAllModulesUseCase = getAllModulesUseCase,
            getWeeklyActivityUseCase = getWeeklyActivityUseCase,
            countModulesByStatusUseCase = countModulesByStatusUseCase,
            countLessonsByStatusUseCase = countLessonsByStatusUseCase,
            countSectionsByStatusUseCase = countSectionsByStatusUseCase,
            dispatcher = dispatcher,
            syncManagers = syncManagers,
            sharingStarted = SharingStarted.Eagerly
        )

        // Set up default behaviors for mocks
        every { syncManager.syncStatus } returns MutableStateFlow(SyncManager.SyncStatus.Idle)
        coEvery { buildProfilePathUseCase() } returns profilePath
        coEvery { buildSessionPathUseCase(profile.id) } returns sessionPath
        coEvery { buildCurriculumPathUseCase(profile.id) } returns curriculumPath
        coEvery { buildModulePathUseCase(profile.id, activeCurriculum.id) } returns modulePath
        coEvery { buildLessonPathUseCase(profile.id, activeCurriculum.id, any()) } returns lessonPath
        coEvery { buildSectionPathUseCase(profile.id, activeCurriculum.id, any(), any()) } returns sectionPath

        coEvery { getProfileUseCase(profilePath) } returns flowOf(Result.success(profile))
        coEvery { getAllSessionsUseCase(sessionPath) } returns flowOf(Result.success(emptyList()))
        coEvery { getActiveCurriculumUseCase(curriculumPath) } returns flowOf(Result.success(activeCurriculum))
        coEvery { getAllCurriculaUseCase(curriculumPath) } returns flowOf(Result.success(emptyList()))

        coEvery { getAllModulesUseCase(modulePath) } returns flowOf(Result.success(emptyList()))
        coEvery { getAllLessonsUseCase(lessonPath) } returns flowOf(Result.success(emptyList()))
        coEvery { getAllSectionsUseCase(sectionPath) } returns flowOf(Result.success(emptyList()))

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
        coEvery { getProfileUseCase(profilePath) } returns flowOf(Result.success(profile))

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(profile, this.profile)
            assertFalse(isLoading)
        }

        coVerify {
            getProfileUseCase(profilePath)
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

        coEvery { getProfileUseCase(profilePath) } returns flowOf(Result.failure(exception))

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertNull(profile)
            assertFalse(isLoading)
        }

        coVerifySequence {
            buildProfilePathUseCase()
            getProfileUseCase(profilePath)
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
        coEvery { getAllCurriculaUseCase(curriculumPath) } returns flowOf(Result.success(curricula))

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(curricula, this.curricula)
        }

        coVerify {
            getAllCurriculaUseCase(curriculumPath)
        }
    }

    @Test
    fun `getActiveCurriculumData should update active curriculum`() = runTest {
        // Given
        coEvery { getActiveCurriculumUseCase(curriculumPath) } returns flowOf(Result.success(activeCurriculum))

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(activeCurriculum, this.activeCurriculum)
        }

        coVerify {
            getActiveCurriculumUseCase(curriculumPath)
        }
    }

    @Test
    fun `getAllModulesData should update modules`() = runTest {
        // Given
        val modules = listOf(mockk<Module>(relaxed = true))
        coEvery { getAllModulesUseCase(modulePath) } returns flowOf(Result.success(modules))

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(modules, this.modules)
        }

        coVerify {
            getAllModulesUseCase(modulePath)
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

        coEvery { getAllModulesUseCase(modulePath) } returns flowOf(Result.failure(exception))

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertTrue(modules.isEmpty())
        }

        coVerify {
            getAllModulesUseCase(modulePath)
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

        coEvery { getAllModulesUseCase(modulePath) } returns flowOf(Result.success(listOf(mockk(relaxed = true))))
        coEvery { getAllLessonsUseCase(lessonPath) } returns flowOf(Result.success(listOf(mockk(relaxed = true))))
        coEvery { countLessonsByStatusUseCase(activeCurriculum.id) } returns Result.success(lessonsByStatus)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(lessonsByStatus, this.lessonCountByStatus)
        }

        coVerify {
            getAllModulesUseCase(modulePath)
            getAllLessonsUseCase(lessonPath)
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

        coEvery { getAllModulesUseCase(modulePath) } returns flowOf(Result.success(listOf(mockk(relaxed = true))))
        coEvery { getAllLessonsUseCase(lessonPath) } returns flowOf(Result.success(listOf(mockk(relaxed = true))))
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

        coEvery { getAllModulesUseCase(modulePath) } returns flowOf(Result.success(listOf(mockk(relaxed = true))))
        coEvery { getAllLessonsUseCase(lessonPath) } returns flowOf(Result.success(listOf(mockk(relaxed = true))))
        coEvery { getAllSectionsUseCase(sectionPath) } returns flowOf(Result.success(listOf(mockk(relaxed = true))))
        coEvery { countSectionsByStatusUseCase(activeCurriculum.id) } returns Result.success(sectionsByStatus)

        // When
        advanceUntilIdle()

        // Then
        with(viewModel.state.value) {
            assertEquals(sectionsByStatus, this.sectionCountByStatus)
        }

        coVerify {
            getAllModulesUseCase(modulePath)
            getAllLessonsUseCase(lessonPath)
            getAllSectionsUseCase(sectionPath)
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

        coEvery { getAllModulesUseCase(modulePath) } returns flowOf(Result.success(listOf(mockk(relaxed = true))))
        coEvery { getAllLessonsUseCase(lessonPath) } returns flowOf(Result.success(listOf(mockk(relaxed = true))))
        coEvery { getAllSectionsUseCase(sectionPath) } returns flowOf(Result.success(listOf(mockk(relaxed = true))))
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
        private val profilePath = FirestorePathBuilder().buildProfilePath()
        private val sessionPath = FirestorePathBuilder().buildSessionPath("user123")
        private val curriculumPath = FirestorePathBuilder().buildCurriculumPath("user123")
        private val modulePath = FirestorePathBuilder().buildModulePath("user123", "curriculum123")
        private val lessonPath = FirestorePathBuilder().buildLessonPath("user123", "curriculum123", "module123")
        private val sectionPath = FirestorePathBuilder().buildSectionPath("user123", "curriculum123", "module123", "lesson123")
        private val profile = mockk<Profile>(relaxed = true)
        private val activeCurriculum = mockk<Curriculum>(relaxed = true)
    }
}
