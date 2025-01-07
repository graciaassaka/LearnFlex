package org.example.composeApp.injection

import kotlinx.coroutines.Dispatchers
import org.example.shared.FirebaseInit
import org.example.shared.data.local.database.DatabaseProvider
import org.example.shared.data.remote.firebase.FirebaseAuthClient
import org.example.shared.data.remote.firebase.FirebaseConfig
import org.example.shared.data.remote.firebase.FirebaseStorageClient
import org.example.shared.domain.client.AuthClient
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
    single { FirebaseAuthClient(get(), get(), FirebaseConfig.useEmulator) }
    single<AuthClient> { get<FirebaseAuthClient>() }
}

actual fun getFirebaseStorageServiceModule() = module {
    single { FirebaseStorageClient(get(), get()) }
}

actual fun getDatabaseModule() = module {
    single { DatabaseProvider().getDatabase() }
}