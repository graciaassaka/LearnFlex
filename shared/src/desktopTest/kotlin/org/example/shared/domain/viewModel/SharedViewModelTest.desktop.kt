package org.example.shared.domain.viewModel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.test.*
import org.example.shared.data.model.User
import org.example.shared.domain.use_case.GetUserDataUseCase
import org.example.shared.presentation.viewModel.SharedViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
actual class SharedViewModelTest {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var getUserDataUseCase: GetUserDataUseCase
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userData: User

    @Before
    actual fun setup() {
        Dispatchers.setMain(testDispatcher)
        getUserDataUseCase = mockk(relaxed = true)
        sharedViewModel = SharedViewModel(getUserDataUseCase, testDispatcher, SharingStarted.Eagerly)

        userData = User(
            createdAt = "2023-10-01T00:00:00Z",
            customAuth = false,
            disabled = false,
            displayName = "John Doe",
            email = "john.doe@example.com",
            emailVerified = true,
            lastLoginAt = "2023-10-01T12:00:00Z",
            localId = "123456789",
            passwordHash = "hashed_password",
            passwordUpdatedAt = 1696156800000,
            photoUrl = "http://example.com/photo.jpg",
            providerUserInfo = listOf(),
            validSince = "2023-10-01T00:00:00Z"
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

    @Test
    fun `onCleared should cancel viewModelScope`() = runTest {
        // Given
        coEvery { getUserDataUseCase() } returns Result.success(userData)
        sharedViewModel.getUserData()
        advanceUntilIdle()

        // When
        sharedViewModel.onCleared()
        advanceUntilIdle()

        // Then
        val job = sharedViewModel.viewModelScope.coroutineContext[Job]
        assertNotNull(job, )
        assertTrue(job.isCancelled)
        assertFalse(job.isActive)

        // Additionally, verify that further coroutines cannot be launched
        var wasExecuted = false
        sharedViewModel.viewModelScope.launch {
            wasExecuted = true
        }
        advanceUntilIdle()
        assertFalse(wasExecuted)
    }
}