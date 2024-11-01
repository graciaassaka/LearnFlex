package org.example.shared.injection

import com.google.firebase.firestore.FirebaseFirestore
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.serialization.json.Json
import org.example.shared.data.assistant.OpenAIAssistantClient
import org.example.shared.data.firebase.FirebaseAuthService
import org.example.shared.data.repository.UserProfileReposImpl
import org.example.shared.domain.repository.UserProfileRepos
import org.example.shared.domain.service.AIAssistantClient
import org.example.shared.domain.service.AuthService
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

val commonModule = module {
    single { SharingStarted.WhileSubscribed(5000) }

    includes(getDispatcherModule())

    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.NONE
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 60000L
                    connectTimeoutMillis = 60000L
                    socketTimeoutMillis = 60000L
                }
            }
        }.also { client ->
            Runtime.getRuntime().addShutdownHook(Thread { client.close() })
        }
    }

    single { FirebaseFirestore.getInstance() }

    includes(getFirebaseAuthServiceModule())

    single<AuthService> { get<FirebaseAuthService>() }

    single<AIAssistantClient> { OpenAIAssistantClient(get()) }

    single { SignUpUseCase(get()) }
    single { SignInUseCase(get()) }
    single { GetUserDataUseCase(get()) }
    single { SendVerificationEmailUseCase(get()) }
    single { VerifyEmailUseCase(get()) }
    single { DeleteUserUseCase(get()) }
    single { SendPasswordResetEmailUseCase(get()) }
    single { CreateUserProfileUseCase(get()) }
    single { UploadProfilePictureUseCase(get(), get()) }

    single<UserProfileRepos> { UserProfileReposImpl(get()) }

    viewModel { BaseViewModel(get()) }
    viewModel { SharedViewModel(get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { CreateUserProfileViewModel(get(), get(), get(), get(), get()) }
}