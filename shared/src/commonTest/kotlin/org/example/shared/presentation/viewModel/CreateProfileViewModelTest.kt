package org.example.shared.presentation.viewModel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.example.shared.domain.client.StyleQuizGeneratorClient
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.User
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.auth.GetUserDataUseCase
import org.example.shared.domain.use_case.path.BuildProfilePathUseCase
import org.example.shared.domain.use_case.profile.*
import org.example.shared.domain.use_case.validation.ValidateUsernameUseCase
import org.example.shared.presentation.util.ProfileCreationForm
import org.example.shared.presentation.util.UIEvent
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class CreateProfileViewModelTest {
    private lateinit var viewModel: CreateUserProfileViewModel
    private lateinit var syncManager: SyncManager<DatabaseRecord>
    private lateinit var getUserDataUseCase: GetUserDataUseCase
    private lateinit var createProfileUseCase: CreateProfileUseCase
    private lateinit var uploadProfilePictureUseCase: UploadProfilePictureUseCase
    private lateinit var deleteProfilePictureUseCase: DeleteProfilePictureUseCase
    private lateinit var getStyleQuestionnaireUseCase: GetStyleQuestionnaireUseCase
    private lateinit var getStyleResultUseCase: GetStyleResultUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase
    private lateinit var validateUsernameUseCase: ValidateUsernameUseCase
    private lateinit var buildProfilePathUseCase: BuildProfilePathUseCase
    private lateinit var testDispatcher: TestDispatcher
    private val syncStatus = MutableStateFlow<SyncManager.SyncStatus>(SyncManager.SyncStatus.Idle)

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        syncManager = mockk(relaxed = true)
        getUserDataUseCase = mockk(relaxed = true)
        createProfileUseCase = mockk(relaxed = true)
        uploadProfilePictureUseCase = mockk(relaxed = true)
        deleteProfilePictureUseCase = mockk(relaxed = true)
        getStyleQuestionnaireUseCase = mockk(relaxed = true)
        getStyleResultUseCase = mockk(relaxed = true)
        updateProfileUseCase = mockk(relaxed = true)
        validateUsernameUseCase = ValidateUsernameUseCase()
        buildProfilePathUseCase = mockk(relaxed = true)
        viewModel = CreateUserProfileViewModel(
            getUserDataUseCase,
            createProfileUseCase,
            uploadProfilePictureUseCase,
            deleteProfilePictureUseCase,
            getStyleQuestionnaireUseCase,
            getStyleResultUseCase,
            updateProfileUseCase,
            validateUsernameUseCase,
            buildProfilePathUseCase,
            testDispatcher,
            listOf(syncManager),
            SharingStarted.Eagerly
        )

        every { syncManager.syncStatus } returns syncStatus
        coEvery { getUserDataUseCase() } returns Result.success(user)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `viewModel should handle error when syncManager returns SyncStatus Error`() = runTest {
        // Given
        val errorMessage = "Failed to sync data"
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        syncStatus.value = SyncManager.SyncStatus.Error(Exception(errorMessage))
        advanceUntilIdle()

        // Then
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents.first() as UIEvent.ShowSnackbar).message)

        job.cancel()
    }

    @Test
    fun `getUserData should update state with user data`() = runTest {
        // Given
        val userData = User(
            localId = "user1234",
            displayName = "TestUserName",
            email = "test@example.com",
            emailVerified = true,
        )
        coEvery { getUserDataUseCase() } returns Result.success(userData)

        // When
        viewModel.getUserData()
        advanceUntilIdle()

        // Then
        assertEquals(userData.localId, viewModel.state.value.userId)
        assertEquals(userData.displayName, viewModel.state.value.username)
        assertEquals(userData.email, viewModel.state.value.email)
    }

    @Test
    fun `getUserData should handle error when getUserDataUseCase returns failure`() = runTest {
        // Given
        val errorMessage = "Failed to fetch user data"
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { getUserDataUseCase() } returns Result.failure(Exception(errorMessage))

        // When
        // getUserData will be call when state is collected
        advanceUntilIdle()

        // Then
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents.first() as UIEvent.ShowSnackbar).message)

        job.cancel()
    }

    @Test
    fun `onUsernameChanged should update username in state when username is valid`() = runTest {
        // Given
        val username = "TestUser_1"

        // When
        advanceUntilIdle()
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
        advanceUntilIdle()
        viewModel.onUsernameChanged(username)
        advanceUntilIdle()

        // Then
        assertEquals(username, viewModel.state.value.username)
        assertNotNull(viewModel.state.value.usernameError)
    }

    @Test
    fun `onFieldChanged should update field in state`() = runTest {
        // Given
        val field = Field.COMPUTER_SCIENCE

        // When
        viewModel.onFieldChanged(field)
        advanceUntilIdle()

        // Then
        assertEquals(field, viewModel.state.value.field)
    }

    @Test
    fun `onLevelChanged should update level in state`() = runTest {
        // Given
        val level = Level.BEGINNER

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
    fun `onUploadProfilePicture should update state with photoUrl and show successMessage when uploadProfilePictureUseCase returns success`() =
        runTest {
            // Given
            val imageData = byteArrayOf(0x00, 0x01, 0x02, 0x03)
            val successMessage = "Profile picture uploaded successfully"
            val photoUrl = "https://example.com/profile.jpg"

            val uiEvents = mutableListOf<UIEvent>()
            val job = launch {
                viewModel.uiEvent.toList(uiEvents)
            }

            coEvery { buildProfilePathUseCase() } returns TEST_PATH
            coEvery { uploadProfilePictureUseCase(TEST_PATH, imageData) } returns Result.success(photoUrl)

            // When
            viewModel.onUploadProfilePicture(imageData, successMessage)
            advanceUntilIdle()

            // Then
            coVerify { uploadProfilePictureUseCase(TEST_PATH, imageData) }
            assertEquals(photoUrl, viewModel.state.value.photoUrl)
            assertEquals(1, uiEvents.size)
            assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

            job.cancel()
        }

    @Test
    fun `onUploadProfilePicture should show error message when uploadProfilePictureUseCase returns failure`() =
        runTest {
            // Given
            val imageData = byteArrayOf(0x00, 0x01, 0x02, 0x03)
            val errorMessage = "Failed to upload profile picture"

            val uiEvents = mutableListOf<UIEvent>()
            val job = launch {
                viewModel.uiEvent.toList(uiEvents)
            }

            coEvery { buildProfilePathUseCase() } returns TEST_PATH
            coEvery { uploadProfilePictureUseCase(TEST_PATH, imageData) } returns Result.failure(Exception(errorMessage))

            // When
            viewModel.onUploadProfilePicture(imageData, "Success message")
            advanceUntilIdle()

            // Then
            coVerify { uploadProfilePictureUseCase(TEST_PATH, imageData) }
            assertEquals(1, uiEvents.size)
            assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

            job.cancel()
        }

    @Test
    fun `onProfilePictureDeleted should update state with empty photoUrl and show successMessage when deleteProfilePictureUseCase returns success`() =
        runTest {
            // Given
            val successMessage = "Profile picture deleted successfully"
            val uiEvents = mutableListOf<UIEvent>()
            val job = launch {
                viewModel.uiEvent.toList(uiEvents)
            }

            coEvery { deleteProfilePictureUseCase(any()) } returns Result.success(Unit)

            // When
            viewModel.onProfilePictureDeleted(successMessage)
            advanceUntilIdle()

            // Then
            coVerify { deleteProfilePictureUseCase(any()) }
            assertEquals("", viewModel.state.value.photoUrl)
            assertEquals(1, uiEvents.size)
            assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

            job.cancel()
        }

    @Test
    fun `onProfilePictureDeleted should show error message when deleteProfilePictureUseCase returns failure`() =
        runTest {
            // Given
            val errorMessage = "Failed to delete profile picture"
            val uiEvents = mutableListOf<UIEvent>()
            val job = launch {
                viewModel.uiEvent.toList(uiEvents)
            }

            coEvery { deleteProfilePictureUseCase(any()) } returns Result.failure(Exception(errorMessage))

            // When
            viewModel.onProfilePictureDeleted("Success message")
            advanceUntilIdle()

            // Then
            coVerify { deleteProfilePictureUseCase(any()) }
            assertEquals(1, uiEvents.size)
            assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

            job.cancel()
        }

    @Test
    fun `onCreateProfile should call createProfileUseCase when username is valid`() = runTest {
        // Given
        val username = "TestUser"
        coEvery { createProfileUseCase(any(), any()) } returns Result.success(Unit)

        // When
        viewModel.onUsernameChanged(username)
        viewModel.onCreateProfile("Success message")
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { createProfileUseCase(any(), any()) }
    }

    @Test
    fun `onCreateProfile should not call createProfileUseCase when username is invalid`() = runTest {
        // Given
        val username = ""
        val path = "users/test-path"
        coEvery { buildProfilePathUseCase() } returns path

        // When
        viewModel.onUsernameChanged(username)
        viewModel.onCreateProfile("Success message")
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { createProfileUseCase(any(), any()) }
    }

    @Test
    fun `onCreateProfile should show successMessage when createUserProfile returns success`() = runTest {
        // Given
        val successMessage = "Profile created successfully"
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { createProfileUseCase(any(), any()) } returns Result.success(Unit)

        // When
        viewModel.onUsernameChanged("TestUser")
        viewModel.onCreateProfile(successMessage)
        advanceUntilIdle()

        // Then
        coVerify { createProfileUseCase(any(), any()) }
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

        coEvery { createProfileUseCase(any(), any()) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.onUsernameChanged("TestUser")
        viewModel.onCreateProfile("Success message")
        advanceUntilIdle()

        // Then
        coVerify { createProfileUseCase(any(), any()) }
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `startStyleQuestionnaire should call getStyleQuestionnaireUseCase and update state with questionnaire`() =
        runTest {
            // Given
            coEvery { getStyleQuestionnaireUseCase(any(), any()) } returns flowOf(Result.success(mockk()))

            // When
            viewModel.startStyleQuestionnaire()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { getStyleQuestionnaireUseCase(any(), any()) }
        }

    @Test
    fun `startStyleQuestionnaire should update state with questionnaire when getStyleQuestionnaireUseCase returns success`() =
        runTest {
            // Given
            val question = mockk<StyleQuizGeneratorClient.StyleQuestion>()

            coEvery { getStyleQuestionnaireUseCase(any(), any()) } returns flowOf(Result.success(question))

            // When
            viewModel.startStyleQuestionnaire()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { getStyleQuestionnaireUseCase(any(), any()) }
            assertEquals(question, viewModel.state.value.styleQuestionnaire.first())
        }

    @Test
    fun `startStyleQuestionnaire should show error message when getStyleQuestionnaireUseCase returns failure`() =
        runTest {
            // Given
            val errorMessage = "Failed to fetch style questionnaire"
            val uiEvents = mutableListOf<UIEvent>()
            val job = launch {
                viewModel.uiEvent.toList(uiEvents)
            }

            coEvery {
                getStyleQuestionnaireUseCase(
                    any(),
                    any()
                )
            } returns flowOf(Result.failure(Exception(errorMessage)))

            // When
            viewModel.startStyleQuestionnaire()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { getStyleQuestionnaireUseCase(any(), any()) }
            assertEquals(1, uiEvents.size)
            assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

            job.cancel()
        }

    @Test
    fun `onQuestionAnswered should update state with style response`() = runTest {
        // Given
        val style = Style.READING

        // When
        viewModel.onQuestionAnswered(style)
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.state.value.styleResponses.size)
        assertEquals(style, viewModel.state.value.styleResponses.first())
    }

    @Test
    fun `onQuestionnaireCompleted should call getStyleResultUseCase and update state with result`() = runTest {
        // Given
        val result = mockk<Profile.LearningStyle>()

        coEvery { getStyleResultUseCase(any()) } returns Result.success(result)

        // When
        viewModel.onQuestionnaireCompleted()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getStyleResultUseCase(any()) }
        assertEquals(result, viewModel.state.value.learningStyle)
    }

    @Test
    fun `onQuestionnaireCompleted should show error message when getStyleResultUseCase returns failure`() = runTest {
        // Given
        val errorMessage = "Failed to fetch style result"
        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        coEvery { getStyleResultUseCase(any()) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.onQuestionnaireCompleted()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getStyleResultUseCase(any()) }
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)

        job.cancel()
    }

    @Test
    fun `displayProfileCreationForm should update state with currentForm when form is PERSONAL_INFO`() = runTest {
        // Given
        val form = ProfileCreationForm.PERSONAL_INFO

        // When
        viewModel.displayProfileCreationForm(form)
        advanceUntilIdle()

        // Then
        assertEquals(form, viewModel.state.value.currentForm)
    }

    @Test
    fun `displayProfileCreationForm should update state with currentForm when form is STYLE_QUESTIONNAIRE`() = runTest {
        // Given
        val form = ProfileCreationForm.STYLE_QUESTIONNAIRE

        // When
        viewModel.displayProfileCreationForm(form)
        advanceUntilIdle()

        // Then
        assertEquals(form, viewModel.state.value.currentForm)
    }

    @Test
    fun `setLearningStyle should call setUserStyleUseCase with userId and styleResult when styleResult is not null`() =
        runTest {
            // Given
            val successMessage = "Style set successfully"

            coEvery { updateProfileUseCase(any(), any()) } returns Result.success(Unit)
            coEvery { getStyleResultUseCase(any()) } returns Result.success(testLearningStyle)

            // When
            viewModel.onQuestionnaireCompleted() // This will set the styleResult
            advanceUntilIdle()
            viewModel.setLearningStyle(successMessage)
            advanceUntilIdle()

            // Then
            coVerify { updateProfileUseCase(any(), any()) }
            assertFalse(viewModel.state.value.showStyleResultDialog)
            assertFalse(viewModel.state.value.isLoading)
        }

    @Test
    fun `setLearningStyle should show success message when setUserStyleUseCase returns success`() = runTest {
        // Given
        val successMessage = "Style set successfully"

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch { viewModel.uiEvent.toList(uiEvents) }

        coEvery { updateProfileUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { getStyleResultUseCase(any()) } returns Result.success(testLearningStyle)

        // When
        viewModel.onQuestionnaireCompleted() // This will set the styleResult
        advanceUntilIdle()
        viewModel.setLearningStyle(successMessage)
        advanceUntilIdle()

        // Then
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)
        assertEquals(successMessage, (uiEvents.first() as UIEvent.ShowSnackbar).message)

        job.cancel()
    }

    @Test
    fun `setLearningStyle should show error message when setUserStyleUseCase returns failure`() = runTest {
        // Given
        val successMessage = "Style set successfully"
        val errorMessage = "Failed to set style"

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch { viewModel.uiEvent.toList(uiEvents) }

        coEvery { updateProfileUseCase(any(), any()) } returns Result.failure(Exception(errorMessage))
        coEvery { getStyleResultUseCase(any()) } returns Result.success(testLearningStyle)


        // When
        viewModel.onQuestionnaireCompleted()
        advanceUntilIdle()
        viewModel.setLearningStyle(successMessage)
        advanceUntilIdle()

        // Then
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)
        assertEquals(errorMessage, (uiEvents.first() as UIEvent.ShowSnackbar).message)

        job.cancel()
    }

    @Test
    fun `setLearningStyle should handle null styleResult and show error message`() = runTest {
        // Given
        val successMessage = "Style set successfully"

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch { viewModel.uiEvent.toList(uiEvents) }


        // When
        viewModel.setLearningStyle(successMessage)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { updateProfileUseCase(any(), any()) }
        assertEquals(1, uiEvents.size)
        assertTrue(uiEvents.first() is UIEvent.ShowSnackbar)
        assertFalse(viewModel.state.value.showStyleResultDialog)
        assertFalse(viewModel.state.value.isLoading)

        job.cancel()
    }

    @Test
    fun `setLearningStyle should update showStyleResultDialog state correctly`() = runTest {
        // Given
        val successMessage = "Style set successfully"

        coEvery { updateProfileUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { getStyleResultUseCase(any()) } returns Result.success(testLearningStyle)

        // When
        viewModel.onQuestionnaireCompleted()
        advanceUntilIdle()
        viewModel.setLearningStyle(successMessage)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.state.value.showStyleResultDialog)
    }

    companion object {
        private const val TEST_PATH = "profiles/user123"
        private val testLearningStyleBreakdown = Profile.LearningStyleBreakdown(reading = 40, kinesthetic = 30)
        private val testLearningStyle = Profile.LearningStyle(
            dominant = Style.READING.value,
            breakdown = testLearningStyleBreakdown
        )
        private val user = User(
            localId = "user123",
            displayName = "TestUser",
            email = "test@example.com",
            emailVerified = true,
        )
    }
}