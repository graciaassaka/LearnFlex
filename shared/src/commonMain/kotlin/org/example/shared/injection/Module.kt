package org.example.shared.injection

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import org.example.shared.data.local.dao.LearningStyleDao
import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.database.LearnFlexDatabase
import org.example.shared.data.remote.assistant.OpenAIAssistantClient
import org.example.shared.data.remote.assistant.StyleQuizClientImpl
import org.example.shared.data.remote.firebase.FirebaseAuthClient
import org.example.shared.data.remote.firebase.FirebaseStorageClient
import org.example.shared.data.remote.firestore.LearningStyleRemoteDataSource
import org.example.shared.data.remote.firestore.UserProfileRemoteDataSource
import org.example.shared.data.remote.util.HttpClientConfig
import org.example.shared.data.repository.LearningStyleRepository
import org.example.shared.data.repository.UserRepository
import org.example.shared.data.sync.handler.LearningStyleSyncHandler
import org.example.shared.data.sync.handler.UserProfileSyncHandler
import org.example.shared.data.sync.manager.SyncManagerImpl
import org.example.shared.data.util.OpenAIConstants
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.Repository
import org.example.shared.domain.service.AIAssistantClient
import org.example.shared.domain.service.AuthClient
import org.example.shared.domain.service.StorageClient
import org.example.shared.domain.service.StyleQuizClient
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.*
import org.example.shared.presentation.viewModel.AuthViewModel
import org.example.shared.presentation.viewModel.BaseViewModel
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Platform-specific module declarations
expect fun initKoin(context: Any?)
expect fun getDispatcherModule(): Module
expect fun getDatabaseModule(): Module
expect fun getFirebaseAuthServiceModule(): Module
expect fun getFirebaseStorageServiceModule(): Module

// Qualifier constants
private const val USER_PROFILE_SCOPE = "user_profile_scope"
private const val LEARNING_STYLE_SCOPE = "learning_style_scope"

val commonModule = module {
    // Core Dependencies
    single {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).also { scope ->
            Runtime.getRuntime().addShutdownHook(Thread { scope.cancel() })
        }
    }
    single { SharingStarted.WhileSubscribed(5000) }

    // Include Platform-Specific Modules
    includes(
        getDispatcherModule(),
        getDatabaseModule(),
        getFirebaseAuthServiceModule(),
        getFirebaseStorageServiceModule()
    )

    // HTTP Client Configuration
    single {
        HttpClientConfig.create().also { client ->
            Runtime.getRuntime().addShutdownHook(Thread { client.close() })
        }
    }

    // Firebase Services
    single<FirebaseFirestore> { Firebase.firestore }
    single<AuthClient> { get<FirebaseAuthClient>() }
    single<StorageClient> { get<FirebaseStorageClient>() }

    // OpenAI Services
    single<AIAssistantClient> {
        OpenAIAssistantClient(
            httpClient = get(),
            baseUrl = URLBuilder(
                protocol = URLProtocol.HTTPS,
                host = "api.openai.com"
            ).build(),
            apiKey = OpenAIConstants.API_KEY
        )
    }

    // Style Quiz Service
    single<StyleQuizClient> { StyleQuizClientImpl(assistant = get()) }

    // Database DAOs
    single<UserProfileDao> { get<LearnFlexDatabase>().userProfileDao() }
    single<LearningStyleDao> { get<LearnFlexDatabase>().learningStyleDao() }

    // Remote Data Sources
    single<RemoteDataSource<UserProfile>>(named(USER_PROFILE_SCOPE)) {
        UserProfileRemoteDataSource(firestore = get())
    }
    single<RemoteDataSource<LearningStyle>>(named(LEARNING_STYLE_SCOPE)) {
        LearningStyleRemoteDataSource(firestore = get())
    }

    // Sync Handlers
    single<SyncHandler<UserProfile>>(named(USER_PROFILE_SCOPE)) {
        UserProfileSyncHandler(
            remoteDataSource = get(named(USER_PROFILE_SCOPE)),
            userProfileDao = get()
        )
    }

    single<SyncHandler<LearningStyle>>(named(LEARNING_STYLE_SCOPE)) {
        LearningStyleSyncHandler(
            remoteDataSource = get(named(LEARNING_STYLE_SCOPE)),
            learningStyleDao = get()
        )
    }

    // Sync Managers
    single<SyncManager<UserProfile>>(named(USER_PROFILE_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(),
            syncHandler = get<SyncHandler<UserProfile>>(named(USER_PROFILE_SCOPE)),
            maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    single<SyncManager<LearningStyle>>(named(LEARNING_STYLE_SCOPE)) {
        SyncManagerImpl(
            syncScope = get(),
            syncHandler = get<SyncHandler<LearningStyle>>(named(LEARNING_STYLE_SCOPE)),
            maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread { syncManager.close() })
        }
    }

    // Repositories
    single<Repository<UserProfile>>(named(USER_PROFILE_SCOPE)) {
        UserRepository(
            remoteDataSource = get(named(USER_PROFILE_SCOPE)),
            userProfileDao = get(),
            syncManager = get(named(USER_PROFILE_SCOPE))
        )
    }

    single<Repository<LearningStyle>>(named(LEARNING_STYLE_SCOPE)) {
        LearningStyleRepository(
            remoteDataSource = get(named(LEARNING_STYLE_SCOPE)),
            learningStyleDao = get(),
            syncManager = get(named(LEARNING_STYLE_SCOPE))
        )
    }

    // Use Cases - Auth
    singleOf(::SignUpUseCase)
    singleOf(::SignInUseCase)
    singleOf(::GetUserDataUseCase)
    singleOf(::SendVerificationEmailUseCase)
    singleOf(::VerifyEmailUseCase)
    singleOf(::DeleteUserUseCase)
    singleOf(::SendPasswordResetEmailUseCase)

    // Use Cases - User Profile
    single {
        CreateUserProfileUseCase(repository = get(named(USER_PROFILE_SCOPE)))
    }
    singleOf(::UploadProfilePictureUseCase)
    singleOf(::DeleteProfilePictureUseCase)

    // Use Cases - Learning Style
    single {
        CreateUserStyleUseCase(repository = get(named(LEARNING_STYLE_SCOPE)))
    }
    single {
        GetUserStyleUseCase(repository = get(named(LEARNING_STYLE_SCOPE)))
    }
    singleOf(::GetStyleQuestionnaireUseCase)
    singleOf(::GetStyleResultUseCase)

    // ViewModels
    viewModel { BaseViewModel(dispatcher = get()) }

    viewModel {
        AuthViewModel(
            signUpUseCase = get(),
            signInUseCase = get(),
            sendVerificationEmailUseCase = get(),
            verifyEmailUseCase = get(),
            deleteUserUseCase = get(),
            sendPasswordResetEmailUseCase = get(),
            dispatcher = get()
        )
    }

    viewModel {
        CreateUserProfileViewModel(
            getUserDataUseCase = get(),
            createUserProfileUseCase = get(),
            uploadProfilePictureUseCase = get(),
            deleteProfilePictureUseCase = get(),
            getStyleQuestionnaireUseCase = get(),
            getStyleResultUseCase = get(),
            createUserStyleUseCase = get(),
            syncManager = get(named(USER_PROFILE_SCOPE)),
            dispatcher = get(),
            sharingStarted = get()
        )
    }
}