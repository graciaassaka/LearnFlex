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
import org.example.shared.data.local.dao.UserProfileDao
import org.example.shared.data.local.database.LearnFlexDatabase
import org.example.shared.data.local.entity.UserProfileEntity
import org.example.shared.data.remote.assistant.OpenAIAssistantClient
import org.example.shared.data.remote.assistant.StyleQuizClientImpl
import org.example.shared.data.remote.custom_search.GoogleImageSearchClient
import org.example.shared.data.remote.firebase.FirebaseAuthClient
import org.example.shared.data.remote.firebase.FirebaseStorageClient
import org.example.shared.data.remote.firestore.FirestorePathBuilder
import org.example.shared.data.remote.firestore.RemoteDataSourceImpl
import org.example.shared.data.remote.util.HttpClientConfig
import org.example.shared.data.repository.RepositoryImpl
import org.example.shared.data.repository.util.ModelMapper
import org.example.shared.data.sync.handler.SyncHandlerDelegate
import org.example.shared.data.sync.manager.SyncManagerImpl
import org.example.shared.data.util.GoogleConstants
import org.example.shared.data.util.OpenAIConstants
import org.example.shared.domain.client.*
import org.example.shared.domain.data_source.PathBuilder
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.Repository
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.sync.SyncOperation
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

    // Firebase Clients
    single<FirebaseFirestore> { Firebase.firestore }
    single<AuthClient> { get<FirebaseAuthClient>() }
    single<StorageClient> { get<FirebaseStorageClient>() }

    // OpenAI Client
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

    // Google Image Search Client
    single<ImageSearchClient> {
        GoogleImageSearchClient(
            httpClient = get(),
            baseUrl = URLBuilder(
                protocol = URLProtocol.HTTPS,
                host = "www.googleapis.com",
                pathSegments = listOf("customsearch", "v1")
            ).build(),
            apiKey = GoogleConstants.CUSTOM_SEARCH_API_KEY,
            searchEngineId = GoogleConstants.CUSTOM_IMAGES_SEARCH_ENGINE_ID
        )
    }

    // Style Quiz Service
    single<StyleQuizClient> { StyleQuizClientImpl(assistant = get()) }

    // Path Builder
    single<PathBuilder> { FirestorePathBuilder() }

    // Database DAOs
    single<UserProfileDao>(named(USER_PROFILE_SCOPE)) {
        get<LearnFlexDatabase>().userProfileDao()
    }

    // Remote Data Sources
    single<RemoteDataSource<UserProfile>>(named(USER_PROFILE_SCOPE)) {
        object : RemoteDataSourceImpl<UserProfile>(
            firestore = get(),
            serializer = UserProfile.serializer()
        ) {}
    }

    // Model Mappers
    single<ModelMapper<UserProfile, UserProfileEntity>>(named(USER_PROFILE_SCOPE)) {
        object : ModelMapper<UserProfile, UserProfileEntity> {
            override fun toModel(entity: UserProfileEntity) = with(entity) {
                UserProfile(id, username, email, photoUrl, preferences, learningStyle, createdAt, lastUpdated)
            }

            override fun toEntity(model: UserProfile) = with(model) {
                UserProfileEntity(id, username, email, photoUrl, preferences, learningStyle, createdAt, lastUpdated)
            }
        }
    }

    // Sync Handlers
    single<SyncHandler<UserProfile>>(named(USER_PROFILE_SCOPE)) {
        object : SyncHandler<UserProfile> by SyncHandlerDelegate(
            remoteDataSource = get(named(USER_PROFILE_SCOPE)),
            dao = get<UserProfileDao>(named(USER_PROFILE_SCOPE)),
            modelMapper = get(named(USER_PROFILE_SCOPE))
        ) {}
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

    // Repositories
    single<Repository<UserProfile>>(named(USER_PROFILE_SCOPE)) {
        object : RepositoryImpl<UserProfile, UserProfileEntity>(
            remoteDataSource = get(named(USER_PROFILE_SCOPE)),
            dao = get<UserProfileDao>(named(USER_PROFILE_SCOPE)),
            getStrategy = { id -> get<UserProfileDao>(named(USER_PROFILE_SCOPE)).get(id) },
            syncManager = get(named(USER_PROFILE_SCOPE)),
            syncOperationFactory = { type, path, profile -> SyncOperation(type, path, profile) },
            modelMapper = get(named(USER_PROFILE_SCOPE))
        ) {}
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
        CreateUserProfileUseCase(
            repository = get(named(USER_PROFILE_SCOPE)),
            authClient = get(),
            pathBuilder = get()
        )
    }
    single {
        UpdateUserProfileUseCase(
            repository = get(named(USER_PROFILE_SCOPE)),
            authClient = get(),
            pathBuilder = get()
        )
    }
    singleOf(::UploadProfilePictureUseCase)
    singleOf(::DeleteProfilePictureUseCase)
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
            updateUserProfileUseCase = get(),
            syncManager = get(named(USER_PROFILE_SCOPE)),
            dispatcher = get(),
            sharingStarted = get()
        )
    }
}