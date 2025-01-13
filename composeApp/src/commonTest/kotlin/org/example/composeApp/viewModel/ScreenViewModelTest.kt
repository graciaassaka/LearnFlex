package org.example.composeApp.viewModel

import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.composeApp.injection.DatabaseSyncManagers
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.composeApp.presentation.ui.util.UIEvent
import org.example.composeApp.presentation.viewModel.LearnFlexViewModel
import org.example.composeApp.presentation.viewModel.ScreenViewModel
import org.example.composeApp.presentation.viewModel.util.ResourceProvider
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.sync.SyncManager
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@ExperimentalCoroutinesApi
class ScreenViewModelTest {
    private lateinit var viewModel: ScreenViewModel
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var learnFlexViewModel: LearnFlexViewModel
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var syncManager: SyncManager<DatabaseRecord>
    private lateinit var syncManagers: MutableList<SyncManager<DatabaseRecord>>
    private lateinit var syncStatus: MutableStateFlow<SyncManager.SyncStatus>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        learnFlexViewModel = mockk(relaxed = true)
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

        every { syncManager.syncStatus } returns syncStatus

        viewModel = ScreenViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `initial state is correct`() {
        assertTrue(viewModel.isScreenVisible.value)
    }

    @Test
    fun `navigate without waiting for animation emits Navigate event`() = runTest {
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        val destination = Route.Auth
        viewModel.navigate(destination, false)

        testScheduler.advanceUntilIdle()

        assertTrue(uiEvents.size == 1)
        val event = uiEvents.first()
        assertTrue(event is UIEvent.Navigate)
        assertTrue((event as UIEvent.Navigate).destination == destination)

        job.cancel()
    }

    @Test
    fun `navigate with waiting for animation sets screen visibility to false`() {
        val destination = Route.Auth
        viewModel.navigate(destination, true)
        assertTrue(!viewModel.isScreenVisible.value)
    }

    @Test
    fun `onExitAnimationFinished emits Navigate event when destination is set`() = runTest {
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        val destination = Route.Auth

        viewModel.navigate(destination, true)
        testScheduler.advanceUntilIdle()

        viewModel.handleExitAnimationFinished()
        testScheduler.advanceUntilIdle()

        assertTrue(uiEvents.size == 1)
        val event = uiEvents.last()
        assertTrue(event is UIEvent.Navigate)
        assertEquals(destination, (event as UIEvent.Navigate).destination)

        job.cancel()
    }

    @Test
    fun `onExitAnimationFinished does not emit Navigate event when destination is not set`() = runTest {
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        viewModel.handleExitAnimationFinished()
        testScheduler.advanceUntilIdle()

        assertTrue(uiEvents.isEmpty())

        job.cancel()
    }

    @Test
    fun `handleError with message shows emit ShowSnackbar event with error message`() = runTest {
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        val errorMessage = "An error occurred"
        viewModel.handleError(Exception(errorMessage))

        testScheduler.advanceUntilIdle()

        assertTrue(uiEvents.size == 1)
        val event = uiEvents.first()
        assertTrue(event is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (event as UIEvent.ShowSnackbar).message)

        job.cancel()
    }

    @Test
    fun `showSnackbar emits ShowSnackbar event with message`() = runTest {
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        val message = "Test message"
        viewModel.showSnackbar(message, SnackbarType.Info)

        testScheduler.advanceUntilIdle()

        assertTrue(uiEvents.size == 1)
        val event = uiEvents.first()
        assertTrue(event is UIEvent.ShowSnackbar)
        assertEquals(message, (event as UIEvent.ShowSnackbar).message)

        job.cancel()
    }
}