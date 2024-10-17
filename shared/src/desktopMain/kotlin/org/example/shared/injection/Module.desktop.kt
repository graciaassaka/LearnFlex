package org.example.shared.injection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import org.example.shared.FirebaseInitializer
import org.example.shared.data.firebase.FirebaseAuthService
import org.example.shared.data.firebase.TokenStorageImpl
import org.example.shared.domain.TokenStorage
import org.example.shared.domain.service.AuthService
import org.koin.core.context.startKoin
import org.koin.dsl.module

actual fun initKoin(context: Any?) {
    startKoin {
        modules(
            dispatcherModule,
            sharingStartedModule,
            firebaseModule,
            tokenStorageModule,
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
    single { Dispatchers.Default }
}

val sharingStartedModule = module {
    single { SharingStarted.WhileSubscribed(5000) }
}

val authServiceModule = module {
    single<AuthService> { get<FirebaseAuthService>() }
}

val authModule = module {
    single { FirebaseInitializer() }
    single { FirebaseAuthService(get()) }
}