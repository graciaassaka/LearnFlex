package org.example.shared.injection

import kotlinx.coroutines.Dispatchers
import org.example.shared.FirebaseInit
import org.example.shared.data.firebase.FirebaseAuthService
import org.example.shared.data.firebase.FirebaseConfig
import org.example.shared.domain.service.AuthService
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun initKoin(context: Any?)
{
    startKoin {
        modules(
            firebaseInitModule,
            commonModule
        )
    }
}

val firebaseInitModule = module {
    single { FirebaseInit() }
}

actual fun getDispatcherModule() = module {
    single { Dispatchers.Default }
}

actual fun getFirebaseAuthServiceModule()= module {
    single { FirebaseAuthService(get(), get(), FirebaseConfig.useEmulator()) }
    single<AuthService> { get<FirebaseAuthService>() }
}
