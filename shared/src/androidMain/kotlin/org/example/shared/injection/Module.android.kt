package org.example.shared.injection

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import org.example.shared.data.local.database.DatabaseProvider
import org.example.shared.data.remote.firebase.FirebaseAuthClient
import org.example.shared.data.remote.firebase.FirebaseStorageClient
import org.example.shared.domain.service.AuthClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

actual fun initKoin(context: Any?) {
    require(context is Context) { "Android context required" }
    startKoin {
        androidContext(context)
        modules(
            commonModule
        )
    }
}

actual fun getDispatcherModule() = module {
    single { Dispatchers.IO }
}

actual fun getFirebaseAuthServiceModule() = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseAuthClient(get()) }
    single<AuthClient> { get<FirebaseAuthClient>() }
}

actual fun getFirebaseStorageServiceModule() = module {
    single { FirebaseStorage.getInstance() }
    single { FirebaseStorageClient(get()) }
}

actual fun getDatabaseModule()= module {
    single { DatabaseProvider(androidContext()).getDatabase() }
}