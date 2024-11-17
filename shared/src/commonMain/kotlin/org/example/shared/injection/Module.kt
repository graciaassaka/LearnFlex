package org.example.shared.injection

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import org.example.shared.data.local.database.LearnFlexDatabase
import org.example.shared.data.remote.assistant.OpenAIAssistantClient
import org.example.shared.data.remote.assistant.StyleQuizServiceImpl
import org.example.shared.data.remote.firebase.FirebaseAuthService
import org.example.shared.data.remote.firebase.FirebaseStorageService
import org.example.shared.data.remote.firestore.LearningStyleRemoteDataSourceImpl
import org.example.shared.data.remote.firestore.UserProfileRemoteDataSourceImpl
import org.example.shared.data.repository.UserProfileRepositoryImpl
import org.example.shared.data.sync.handler.UserProfileSyncHandler
import org.example.shared.data.sync.manager.SyncManagerImpl
import org.example.shared.data.util.HttpClientConfig
import org.example.shared.data.util.OpenAIConstants
import org.example.shared.domain.data_source.LearningStyleRemoteDataSource
import org.example.shared.domain.data_source.UserProfileRemoteDataSource
import org.example.shared.domain.model.UserProfile
import org.example.shared.domain.repository.UserProfileRepository
import org.example.shared.domain.service.AIAssistantClient
import org.example.shared.domain.service.AuthService
import org.example.shared.domain.service.StorageService
import org.example.shared.domain.service.StyleQuizService
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.*
import org.example.shared.presentation.viewModel.AuthViewModel
import org.example.shared.presentation.viewModel.BaseViewModel
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect fun initKoin(context: Any?)

expect fun getDispatcherModule(): Module

expect fun getDatabaseModule(): Module

expect fun getFirebaseAuthServiceModule(): Module

expect fun getFirebaseStorageServiceModule(): Module

val commonModule = module {
    // Core dependencies
    single {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).also { scope ->
            Runtime.getRuntime().addShutdownHook(Thread {
                scope.cancel()
            })
        }
    }
    single { SharingStarted.WhileSubscribed(5000) }

    // Include platform-specific modules
    includes(
        getDispatcherModule(),
        getDatabaseModule(),
        getFirebaseAuthServiceModule(),
        getFirebaseStorageServiceModule()
    )

    // HTTP Client
    single {
        HttpClientConfig.create().also { client ->
            Runtime.getRuntime().addShutdownHook(Thread {
                client.close()
            })
        }
    }

    // Firebase Services
    single { Firebase.firestore }
    single<AuthService> { get<FirebaseAuthService>() }
    single<StorageService> { get<FirebaseStorageService>() }

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
    single<StyleQuizService> { StyleQuizServiceImpl(get()) }

    // Database DAOs
    single { get<LearnFlexDatabase>().userProfileDao() }

    // Remote Data Sources
    single<UserProfileRemoteDataSource> { UserProfileRemoteDataSourceImpl(get()) }
    single<LearningStyleRemoteDataSource> { LearningStyleRemoteDataSourceImpl(get()) }

    // Sync-related dependencies
    single<SyncHandler<UserProfile>> {
        UserProfileSyncHandler(get(), get())
    }

    single<SyncManager<UserProfile>> {
        SyncManagerImpl<UserProfile>(
            syncScope = get(),
            syncHandler = get(),
            maxRetries = 3
        ).also { syncManager ->
            Runtime.getRuntime().addShutdownHook(Thread {
                syncManager.close()
            })
        }
    }

    // Repositories
    single<UserProfileRepository> {
        UserProfileRepositoryImpl(
            remoteDataSource = get(),
            userProfileDao = get(),
            syncManager = get()
        )
    }

    // Use Cases
    singleOf(::SignUpUseCase)
    singleOf(::SignInUseCase)
    singleOf(::GetUserDataUseCase)
    singleOf(::SendVerificationEmailUseCase)
    singleOf(::VerifyEmailUseCase)
    singleOf(::DeleteUserUseCase)
    singleOf(::SendPasswordResetEmailUseCase)
    singleOf(::CreateUserProfileUseCase)
    singleOf(::UploadProfilePictureUseCase)
    singleOf(::DeleteProfilePictureUseCase)
    singleOf(::GetStyleQuestionnaireUseCase)
    singleOf(::GetStyleResultUseCase)
    singleOf(::GetUserStyleUseCase)
    singleOf(::SetUserStyleUseCase)

    // ViewModels
    viewModel { BaseViewModel(get()) }
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
            setUserStyleUseCase = get(),
            syncManager = get(),
            dispatcher = get(),
            sharingStarted = get()
        )
    }
}