package org.example.composeApp.viewModel

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.*
import org.example.composeApp.injection.DatabaseSyncManagers
import org.example.composeApp.presentation.action.LearnFlexAction
import org.example.composeApp.presentation.state.AppState
import org.example.composeApp.presentation.viewModel.EditUserProfileViewModel
import org.example.composeApp.presentation.viewModel.LearnFlexViewModel
import org.example.composeApp.presentation.viewModel.util.ResourceProvider
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.util.BundleManager
import org.example.shared.domain.model.util.SessionManager
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.auth.SignOutUseCase
import org.example.shared.domain.use_case.profile.*
import org.example.shared.domain.use_case.validation.ValidateUsernameUseCase
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import org.example.composeApp.presentation.action.EditUserProfileAction as Action

@OptIn(ExperimentalCoroutinesApi::class)
class EditUserProfileViewModelTest {
    private lateinit var viewModel: EditUserProfileViewModel

    private lateinit var deleteProfileUseCase: DeleteProfileUseCase
    private lateinit var deleteProfilePictureUseCase: DeleteProfilePictureUseCase
    private lateinit var fetchProfilePhotoDownloadUrl: FetchProfilePhotoDownloadUrl
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase
    private lateinit var uploadProfilePictureUseCase: UploadProfilePictureUseCase
    private lateinit var validateUsernameUseCase: ValidateUsernameUseCase

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

        deleteProfileUseCase = mockk(relaxed = true)
        deleteProfilePictureUseCase = mockk(relaxed = true)
        fetchProfilePhotoDownloadUrl = mockk(relaxed = true)
        signOutUseCase = mockk(relaxed = true)
        updateProfileUseCase = mockk(relaxed = true)
        uploadProfilePictureUseCase = mockk(relaxed = true)
        validateUsernameUseCase = mockk(relaxed = true)

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

        appStateFlow.update {
            it.copy(
                profile = profile,
                sessionManager = dummySessionManager,
                bundleManager = dummyBundleManager
            )
        }

        every { syncManager.syncStatus } returns syncStatus
        every { learnFlexViewModel.state } returns appStateFlow

        viewModel = EditUserProfileViewModel(
            deleteProfileUseCase,
            deleteProfilePictureUseCase,
            fetchProfilePhotoDownloadUrl,
            signOutUseCase,
            updateProfileUseCase,
            uploadProfilePictureUseCase,
            validateUsernameUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `init update the state with the profile`() = runTest {
        // Assert
        advanceUntilIdle()
        assertEquals(viewModel.state.value.profile, profile)
    }

    @Test
    fun `handleAction Refresh should call learnFlexViewModel handleAction Refresh`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.handleAction(Action.Refresh)
        advanceUntilIdle()

        // Then
        verify { learnFlexViewModel.handleAction(LearnFlexAction.Refresh) }
    }

    @Test
    fun `handleAction SignOut should call signOutUseCase`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.handleAction(Action.SignOut)
        advanceUntilIdle()

        // Then
        coVerify { signOutUseCase() }
    }

    companion object {
        val profile = Profile(
            username = "TestUser",
            email = "test@example.com",
            photoUrl = "https://example.com/photo.png",
            preferences = Profile.LearningPreferences(
                field = "ENGINEERING",
                level = Level.ADVANCED.name,
                goal = "SomeGoal"
            )
        )
    }
}