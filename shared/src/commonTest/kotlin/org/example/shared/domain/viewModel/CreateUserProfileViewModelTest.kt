package org.example.shared.domain.viewModel

import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.example.shared.data.model.Field
import org.example.shared.data.model.Level
import org.example.shared.data.model.User
import org.example.shared.domain.use_case.CreateUserProfileUseCase
import org.example.shared.domain.use_case.UploadProfilePictureUseCase
import org.example.shared.presentation.state.SharedState
import org.example.shared.presentation.util.UIEvent
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import org.example.shared.presentation.viewModel.SharedViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class CreateUserProfileViewModelTest
{
    private lateinit var viewModel: CreateUserProfileViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var createProfileUseCase: CreateUserProfileUseCase
    private lateinit var uploadProfilePictureUseCase: UploadProfilePictureUseCase
    private lateinit var testDispatcher: TestDispatcher
    private val sharedFlow = MutableStateFlow(SharedState())
    private val user = User(
        uid = "user123",
        displayName = "TestUser",
        email = "test@example.com",
        emailVerified = true,
    )

    @Before
    fun setUp()
    {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        sharedViewModel = mockk(relaxed = true)
        createProfileUseCase = mockk(relaxed = true)
        uploadProfilePictureUseCase = mockk(relaxed = true)
        viewModel = CreateUserProfileViewModel(
            sharedViewModel,
            createProfileUseCase,
            uploadProfilePictureUseCase,
            testDispatcher,
            SharingStarted.Eagerly
        )

        every { sharedViewModel.state } returns sharedFlow
        every { sharedViewModel.getUserData() } just runs
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `init should prepare ViewModel to receive updates from SharedViewModel`() = runTest {
        // Given
        sharedFlow.update { it.copy(userData = user) }

        // When
        advanceUntilIdle()

        // Then
        assertEquals(user.displayName, viewModel.state.value.username)
        assertEquals(user.email, viewModel.state.value.email)
    }

    @Test
    fun `init should call SharedViewModel#clearError and display error message when SharedViewModel state contains error`() = runTest {
        // Given
        val error = Exception("An error occurred")
        sharedFlow.update { it.copy(error = error) }

        val uiEvent = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvent)
        }

        // When
        advanceUntilIdle()

        // Then
        verify { sharedViewModel.clearError() }

        assertEquals(1, uiEvent.size)
        assertTrue(uiEvent.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `onUsernameChanged should update username in state when username is valid`() = runTest {
        // Given
        val username = "TestUser"

        // When
        viewModel.onUsernameChanged(username)
        advanceUntilIdle()

        // Then
        assertEquals(username, viewModel.state.value.username)
        assertNull(viewModel.state.value.usernameError)
    }

    @Test
    fun `onUsernameChanged should update username and display error message when username is invalid`() = runTest {
        // Given
        val username = ""

        // When
        viewModel.onUsernameChanged(username)
        advanceUntilIdle()

        // Then
        assertEquals(username, viewModel.state.value.username)
        assertNotNull(viewModel.state.value.usernameError)
    }

    @Test
    fun `onFieldChanged should update field in state`() = runTest {
        // Given
        val field = Field.ComputerScience

        // When
        viewModel.onFieldChanged(field)
        advanceUntilIdle()

        // Then
        assertEquals(field, viewModel.state.value.field)
    }

    @Test
    fun `onLevelChanged should update level in state`() = runTest {
        // Given
        val level = Level.Beginner

        // When
        viewModel.onLevelChanged(level)
        advanceUntilIdle()

        // Then
        assertEquals(level, viewModel.state.value.level)
    }

    @Test
    fun `toggleLevelDropdownVisibility should update isLevelDropdownVisible in state`() = runTest {
        // Given
        val isLevelDropdownVisible = false

        // When
        viewModel.toggleLevelDropdownVisibility()
        advanceUntilIdle()

        // Then
        assertEquals(!isLevelDropdownVisible, viewModel.state.value.isLevelDropdownVisible)
    }


    @Test
    fun `onGoalChanged should update goal in state`() = runTest {
        // Given
        val goal = "Learn something new"

        // When
        viewModel.onGoalChanged(goal)
        advanceUntilIdle()

        // Then
        assertEquals(goal, viewModel.state.value.goal)
    }

    @Test
    fun `onUploadProfilePicture should update state with photoUrl and show successMessage when uploadProfilePictureUseCase returns success`() = runTest {
        // Given
        val imageData = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val successMessage = "Profile picture uploaded successfully"
        val photoUrl = "https://example.com/profile.jpg"

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { uploadProfilePictureUseCase(imageData) } returns Result.success(photoUrl)

        // When
        viewModel.onUploadProfilePicture(imageData, successMessage)
        advanceUntilIdle()

        // Then
        coVerify { uploadProfilePictureUseCase(imageData) }
        assertEquals(photoUrl, viewModel.state.value.photoUrl)
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `onUploadProfilePicture should show error message when uploadProfilePictureUseCase returns failure`() = runTest {
        // Given
        val imageData = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val errorMessage = "Failed to upload profile picture"

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { uploadProfilePictureUseCase(imageData) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.onUploadProfilePicture(imageData, "Success message")
        advanceUntilIdle()

        // Then
        coVerify { uploadProfilePictureUseCase(imageData) }
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `onCreateProfile should show successMessage when createUserProfile returns success`() = runTest {
        // Given
        val successMessage = "Profile created successfully"
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { createProfileUseCase(any()) } returns Result.success(Unit)

        // When
        viewModel.onCreateProfile(successMessage)
        advanceUntilIdle()

        // Then
        coVerify { createProfileUseCase(any()) }
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `onCreateProfile should show error message when createUserProfile returns failure`() = runTest {
        // Given
        val errorMessage = "Failed to create profile"
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { createProfileUseCase(any()) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.onCreateProfile("Success message")
        advanceUntilIdle()

        // Then
        coVerify { createProfileUseCase(any()) }
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }
}