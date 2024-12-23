package org.example.shared.presentation.viewModel

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.util.UIEvent
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class BaseViewModelTest {
    private lateinit var viewModel: BaseViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = BaseViewModel(testDispatcher)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

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

        viewModel.onExitAnimationFinished()
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

        viewModel.onExitAnimationFinished()
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