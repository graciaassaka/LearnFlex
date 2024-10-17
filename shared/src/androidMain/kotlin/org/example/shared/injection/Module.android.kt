package org.example.shared.injection

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import org.example.shared.data.firebase.FirebaseAuthService
import org.example.shared.domain.service.AuthService
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

actual fun initKoin(context: Any?) {
    require(context is Context) { "Android context required" }
    startKoin {
        androidContext(context)
        modules(
            dispatcherModule,
            sharingStartedModule,
            firebaseModule,
            authServiceModule,
            useCaseModule,
            viewModelModule
        )
    }
}

val dispatcherModule = module {
    single { Dispatchers.IO }
}

val sharingStartedModule = module {
    single { SharingStarted.WhileSubscribed() }
}

val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseAuthService(get()) }
}

val authServiceModule = module {
    single<AuthService> { get<FirebaseAuthService>() }
}




