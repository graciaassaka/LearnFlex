package org.example.shared.injection

import android.content.Context
import kotlinx.coroutines.Dispatchers
import org.example.shared.data.firebase.FirebaseAuthService
import org.example.shared.domain.service.AuthService
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun initKoin(context: Any?)
{
    require(context is Context) { "Android context required" }
    startKoin {
        androidContext(context)
        modules(
            commonModule
        )
    }
}

actual fun getDispatcherModule() = module {
    single { Dispatchers.IO}
}

actual fun getFirebaseAuthServiceModule() = module {
    single { FirebaseAuthService(get()) }
    single<AuthService> { get<FirebaseAuthService>() }
}

