package org.example.shared.injection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import org.example.shared.FirebaseInitializer
import org.example.shared.data.firebase.FirebaseAuthService
import org.example.shared.data.firebase.TokenStorageImpl
import org.example.shared.domain.TokenStorage
import org.example.shared.domain.service.AuthService
import org.example.shared.domain.use_case.GetUserDataUseCase
import org.example.shared.domain.use_case.SignInUseCase
import org.example.shared.domain.use_case.SignUpUseCase
import org.example.shared.presentation.viewModel.SharedViewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

actual fun initKoin(context: Any?)
{
    startKoin {
        modules(
            firebaseModule,
            tokenStorageModule,
            dispatcherModule,
            sharingStartedModule,
            authModule,
            authServiceModule,
            useCaseModule,
            viewModelModule
        )
    }
}

val firebaseModule = module {
    single { FirebaseInitializer() }
}

val tokenStorageModule = module {
    single<TokenStorage> { TokenStorageImpl() }
}

val dispatcherModule = module {
    single { Dispatchers.IO }
}

val sharingStartedModule = module {
    single { SharingStarted.WhileSubscribed() }
}

val authServiceModule = module {
    single<AuthService> { get<FirebaseAuthService>() }
}

val authModule = module {
    single { FirebaseInitializer() }
    single { FirebaseAuthService(get()) }
}

val useCaseModule = module {
    single { SignUpUseCase(get()) }
    single { SignInUseCase(get()) }
    single { GetUserDataUseCase(get()) }
}

val viewModelModule = module {
    factory { SharedViewModel(get(), get(), get()) }
}