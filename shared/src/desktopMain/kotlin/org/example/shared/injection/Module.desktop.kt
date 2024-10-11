package org.example.shared.injection

import org.example.shared.FirebaseInitializer
import org.example.shared.data.firebase.FirebaseAuthService
import org.example.shared.data.firebase.TokenStorageImpl
import org.example.shared.domain.TokenStorage
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

actual fun initKoin(context: Any?) {
    startKoin {
        modules(
            platformAuthModule(),
            authServiceModule,
            appModule
        )
    }
}

actual fun platformAuthModule() = module {
    single { FirebaseInitializer() }
    single { FirebaseAuthService(get()) }
}

val appModule = module {
    single { FirebaseInitializer() }
    single<TokenStorage> { TokenStorageImpl() }
}