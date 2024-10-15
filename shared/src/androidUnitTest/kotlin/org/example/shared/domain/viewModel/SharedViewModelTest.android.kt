package org.example.shared.domain.viewModel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.shared.data.model.User
import org.example.shared.domain.use_case.GetUserDataUseCase
import org.example.shared.presentation.viewModel.SharedViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
actual class SharedViewModelTest
{
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var getUserDataUseCase: GetUserDataUseCase
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userData: User

    @Before
    actual fun setup()
    {
        Dispatchers.setMain(testDispatcher)
        getUserDataUseCase = mockk(relaxed = true)
        sharedViewModel = SharedViewModel(getUserDataUseCase, testDispatcher, SharingStarted.Eagerly)

        userData = User(
            displayName = "John Doe",
            email = "john.doe@example.com",
            photoUrl = "https://example.com/photo.jpg",
            emailVerified = true,
            uid = "123456789"
        )
    }

    @After
    actual fun tearDown() = Dispatchers.resetMain()

    @Test
    actual fun `getUserData should call getUserDataUseCase`() = runTest {
        // Given
        coEvery { getUserDataUseCase() } returns Result.success(userData)
        // When
        sharedViewModel.getUserData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { getUserDataUseCase() }
    }

    @Test
    actual fun `getUserData should update state with user data on success`() = runTest {
        // Given
        coEvery { getUserDataUseCase() } returns Result.success(userData)

        // When
        sharedViewModel.getUserData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(userData, sharedViewModel.state.value.userData)
    }

    @Test
    actual fun `getUserData should update state with error on failure`() = runTest {
        // Given
        val exception = Exception("An error occurred")
        coEvery { getUserDataUseCase() } returns Result.failure(exception)

        // When
        sharedViewModel.getUserData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(exception.message, sharedViewModel.state.value.errorMessage)
    }

    @Test
    actual fun `clearError should update state with null error`() = runTest {
        // Given
        val exception = Exception("An error occurred")
        coEvery { getUserDataUseCase() } returns Result.failure(exception)

        sharedViewModel.getUserData()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(sharedViewModel.state.value.errorMessage)

        // When
        sharedViewModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(null, sharedViewModel.state.value.errorMessage)
    }
}