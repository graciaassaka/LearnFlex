package org.example.shared.injection

import kotlinx.coroutines.Dispatchers
import org.example.shared.FirebaseInit
import org.example.shared.data.local.database.DatabaseProvider
import org.example.shared.data.remote.firebase.FirebaseAuthService
import org.example.shared.data.remote.firebase.FirebaseConfig
import org.example.shared.data.remote.firebase.FirebaseStorageService
import org.example.shared.domain.service.AuthService
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual fun initKoin(context: Any?) {
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
    single(named("Main")) { Dispatchers.Main }
}

actual fun getFirebaseAuthServiceModule() = module {
    single { FirebaseAuthService(get(), get(), FirebaseConfig.useEmulator) }
    single<AuthService> { get<FirebaseAuthService>() }
}

actual fun getFirebaseStorageServiceModule() = module {
    single { FirebaseStorageService(get(), get()) }
}

actual fun getDatabaseModule() = module {
    single { DatabaseProvider().getDatabase() }
}