package org.example.shared.injection

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import io.ktor.http.*
import kotlinx.coroutines.flow.SharingStarted
import org.example.shared.data.assistant.OpenAIAssistantClient
import org.example.shared.data.assistant.StyleQuizServiceImpl
import org.example.shared.data.firebase.FirebaseAuthService
import org.example.shared.data.firebase.FirebaseStorageService
import org.example.shared.data.firestore.LearningStyleReposImpl
import org.example.shared.data.firestore.UserProfileReposImpl
import org.example.shared.data.util.HttpClientConfig
import org.example.shared.data.util.OpenAIConstants
import org.example.shared.domain.repository.LearningStyleRepos
import org.example.shared.domain.repository.UserProfileRepos
import org.example.shared.domain.service.AIAssistantClient
import org.example.shared.domain.service.AuthService
import org.example.shared.domain.service.StorageService
import org.example.shared.domain.service.StyleQuizService
import org.example.shared.domain.use_case.*
import org.example.shared.presentation.viewModel.AuthViewModel
import org.example.shared.presentation.viewModel.BaseViewModel
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import org.example.shared.presentation.viewModel.SharedViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect fun initKoin(context: Any?)

expect fun getDispatcherModule(): Module

expect fun getFirebaseAuthServiceModule(): Module

expect fun getFirebaseStorageServiceModule(): Module

val commonModule = module {
    single { SharingStarted.WhileSubscribed(5000) }

    includes(getDispatcherModule())

    single {
        HttpClientConfig.create().also { Runtime.getRuntime().addShutdownHook(Thread { it.close() }) }
    }

    single { Firebase.firestore }

    includes(getFirebaseAuthServiceModule())

    single<AuthService> { get<FirebaseAuthService>() }

    includes(getFirebaseStorageServiceModule())

    single<StorageService> { get<FirebaseStorageService>() }

    single<AIAssistantClient> {
        OpenAIAssistantClient(
            get(), URLBuilder(protocol = URLProtocol.HTTPS, host = "api.openai.com").build(), OpenAIConstants.API_KEY
        )
    }

    single<StyleQuizService> { StyleQuizServiceImpl(get()) }

    single { SignUpUseCase(get()) }
    single { SignInUseCase(get()) }
    single { GetUserDataUseCase(get()) }
    single { SendVerificationEmailUseCase(get()) }
    single { VerifyEmailUseCase(get()) }
    single { DeleteUserUseCase(get()) }
    single { SendPasswordResetEmailUseCase(get()) }
    single { CreateUserProfileUseCase(get()) }
    single { UploadProfilePictureUseCase(get(), get()) }
    single { DeleteProfilePictureUseCase(get(), get()) }
    single { GetStyleQuestionnaireUseCase(get()) }
    single { GetStyleResultUseCase(get()) }
    single { GetUserStyleUseCase(get()) }

    single<UserProfileRepos> { UserProfileReposImpl(get()) }
    single<LearningStyleRepos> { LearningStyleReposImpl(get()) }

    viewModel { BaseViewModel(get()) }
    viewModel { SharedViewModel(get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { CreateUserProfileViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
}