package org.example.shared.injection

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import org.example.shared.data.firebase.FirebaseAuthService
import org.example.shared.domain.service.AuthService
import org.example.shared.domain.use_case.GetUserDataUseCase
import org.example.shared.domain.use_case.SignInUseCase
import org.example.shared.domain.use_case.SignUpUseCase
import org.example.shared.presentation.viewModel.AuthViewModel
import org.example.shared.presentation.viewModel.BaseViewModel
import org.example.shared.presentation.viewModel.SharedViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.viewModel
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

val useCaseModule = module {
    single { SignUpUseCase(get()) }
    single { SignInUseCase(get()) }
    single { GetUserDataUseCase(get()) }
}

val viewModelModule = module {
    viewModel { BaseViewModel() }
    viewModel { SharedViewModel(get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
}


