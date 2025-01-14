@file:OptIn(ExperimentalCoroutinesApi::class)

package org.example.composeApp.viewModel

import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.example.composeApp.injection.DatabaseSyncManagers
import org.example.composeApp.presentation.action.DashboardAction
import org.example.composeApp.presentation.action.LearnFlexAction
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.AppState
import org.example.composeApp.presentation.ui.util.UIEvent
import org.example.composeApp.presentation.viewModel.DashboardViewModel
import org.example.composeApp.presentation.viewModel.LearnFlexViewModel
import org.example.composeApp.presentation.viewModel.util.ResourceProvider
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.util.BundleManager
import org.example.shared.domain.model.util.SessionManager
import org.example.shared.domain.sync.SyncManager
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.time.DayOfWeek

@ExperimentalCoroutinesApi
class DashboardViewModelTest {
    private lateinit var viewModel: DashboardViewModel
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var learnFlexViewModel: LearnFlexViewModel
    private lateinit var appStateFlow: MutableStateFlow<AppState>
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var syncManager: SyncManager<DatabaseRecord>
    private lateinit var syncManagers: MutableList<SyncManager<DatabaseRecord>>
    private lateinit var syncStatus: MutableStateFlow<SyncManager.SyncStatus>

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

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
                    single<CoroutineDispatcher> { testDispatcher }
                    single<ResourceProvider> { resourceProvider }
                    single<SyncManager<DatabaseRecord>> { syncManager }
                    single<DatabaseSyncManagers> { syncManagers }
                    single<LearnFlexViewModel> { learnFlexViewModel }
                }
            )
        }

        val dummySessionManager = mockk<SessionManager>(relaxed = true) {
            every { calculateWeeklyActivity(any()) } returns emptyMap()
        }
        val dummyBundleManager = mockk<BundleManager>(relaxed = true)

        appStateFlow.value = appStateFlow.value.copy(
            sessionManager = dummySessionManager,
            bundleManager = dummyBundleManager
        )

        every { syncManager.syncStatus } returns syncStatus
        every { learnFlexViewModel.state } returns appStateFlow

        viewModel = DashboardViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    /**
     * Example test:
     * Verify that when LearnFlexViewModel's state has an error,
     * the DashboardViewModel calls `handleError` and shows a snackbar.
     */
    @Test
    fun `should handle error from learnFlexViewModel state`() = runTest {
        // Given
        val error = Exception("Test Error")
        val uiEvents = mutableListOf<UIEvent>()

        // Collect UI events
        val job = launch { viewModel.uiEvent.collect { uiEvents.add(it) } }

        // When: We emit a new AppState that has the error
        appStateFlow.update { it.copy(error = error) }
        advanceUntilIdle()

        // Then: The dashboard should handle the error by showing a snackbar.
        assertTrue(uiEvents.isNotEmpty())
        val event = uiEvents.first()
        assertTrue(event is UIEvent.ShowSnackbar)
        assertEquals("Test Error", (event as UIEvent.ShowSnackbar).message)

        job.cancel()
    }

    /**
     * Example test:
     * Verifies that calling `handleAction(Refresh)` will invoke the super.refresh()
     * which calls `learnFlexViewModel.handleAction(LearnFlexAction.Refresh)`.
     */
    @Test
    fun `handleAction(Refresh) should invoke learnFlexViewModel refresh`() = runTest {
        // Given: no special state needed; we'll just verify calls
        coEvery { learnFlexViewModel.handleAction(any()) } just Runs
        advanceUntilIdle()

        // When
        viewModel.handleAction(DashboardAction.Refresh)
        advanceUntilIdle()

        // Then
        coVerify { learnFlexViewModel.handleAction(LearnFlexAction.Refresh) }
    }

    /**
     * Example test:
     * Verifies that calling `refresh()` calculates weekly activity and updates the ViewModel state.
     */
    @Test
    fun `refresh should update weekly activity in DashboardUIState`() = runTest {
        // Given
        val sessionManager = mockk<SessionManager> {
            every { calculateWeeklyActivity(any()) } returns mapOf(
                DayOfWeek.MONDAY to (120L to 2)
            )
        }
        appStateFlow.update { old ->
            old.copy(
                sessionManager = sessionManager,
                bundleManager = mockk(relaxed = true)
            )
        }
        advanceUntilIdle()

        // When
        viewModel.handleAction(DashboardAction.Refresh)
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(1, state.weeklyActivity.size)
        assertEquals(120L, state.weeklyActivity[DayOfWeek.MONDAY]?.first)
        assertEquals(2, state.weeklyActivity[DayOfWeek.MONDAY]?.second)
        assertEquals(120, state.totalMinutes)
        assertEquals(120, state.averageMinutes)
    }

    /**
     * Example test:
     * Verifies that handleAction(Navigate) triggers a UIEvent.Navigate immediately.
     */
    @Test
    fun `handleAction Navigate should emit UIEvent Navigate`() = runTest {
        // Given
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch { viewModel.uiEvent.collect { uiEvents.add(it) } }

        // When
        viewModel.handleAction(DashboardAction.Navigate(Route.EditProfile))
        advanceUntilIdle()

        // Then
        assertEquals(1, uiEvents.size)
        val event = uiEvents.first()
        assertTrue(event is UIEvent.Navigate)
        assertEquals(Route.EditProfile, (event as UIEvent.Navigate).destination)

        job.cancel()
    }

    /**
     * Example test:
     * Verifies that calling refresh updates the itemsCompletion field based on the
     * counts from bundleManager. This checks that updateItemsCompletion() is triggered.
     */
    @Test
    fun `refresh with active curriculum updates itemsCompletion`() = runTest {
        // Given
        // Create a BundleManager mock that returns some statuses
        val bundleManager = mockk<BundleManager>(relaxed = true) {
            every { getCurricula() } returns listOf(mockk(relaxed = true))
            every { getCurriculumByKey(any()) } returns null
            every { getModulesByCurriculum(any()) } returns emptyList()
            every { countCurriculumModulesByStatus(any()) } returns mapOf(
                Status.FINISHED to 3,
                Status.UNFINISHED to 2
            )
            every { countCurriculumLessonsByStatus(any()) } returns mapOf(
                Status.FINISHED to 10,
                Status.UNFINISHED to 5
            )
            every { countCurriculumSectionsByStatus(any()) } returns mapOf(
                Status.FINISHED to 20,
                Status.UNFINISHED to 4
            )
        }
        // Update the AppState with the bundleManager
        appStateFlow.update { it.copy(bundleManager = bundleManager) }
        advanceUntilIdle()

        // When
        viewModel.handleAction(DashboardAction.Refresh)
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(3 + 2, state.moduleCountByStatus.values.sum())
        assertEquals(10 + 5, state.lessonCountByStatus.values.sum())
        assertEquals(20 + 4, state.sectionCountByStatus.values.sum())

        val itemsCompletion = state.itemsCompletion
        assertEquals(3, itemsCompletion.size)

        // Modules
        assertEquals(Collection.MODULES.value, itemsCompletion[0].first)
        assertEquals(3, itemsCompletion[0].second)
        assertEquals(5, itemsCompletion[0].third)

        // Lessons
        assertEquals(Collection.LESSONS.value, itemsCompletion[1].first)
        assertEquals(10, itemsCompletion[1].second)
        assertEquals(15, itemsCompletion[1].third)

        // Sections
        assertEquals(Collection.SECTIONS.value, itemsCompletion[2].first)
        assertEquals(20, itemsCompletion[2].second)
        assertEquals(24, itemsCompletion[2].third)
    }
}
