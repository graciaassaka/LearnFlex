package org.example.shared.injection

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import org.example.shared.data.firebase.FirebaseAuthService
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

actual fun initKoin(context: Any?) {
    require(context is Context) { "Android context required" }
    startKoin {
        androidContext(context)
        modules(
            platformAuthModule(),
            authServiceModule
        )
    }
}

actual fun platformAuthModule() = module{
    single { FirebaseAuth.getInstance()}
    single { FirebaseAuthService(get()) }
}