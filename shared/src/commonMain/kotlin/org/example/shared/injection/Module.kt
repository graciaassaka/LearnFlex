package org.example.shared.injection

import org.example.shared.domain.use_case.*
import org.example.shared.presentation.viewModel.AuthViewModel
import org.example.shared.presentation.viewModel.BaseViewModel
import org.example.shared.presentation.viewModel.SharedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


expect fun initKoin(context: Any?)

val useCaseModule = module {
    single { SignUpUseCase(get()) }
    single { SignInUseCase(get()) }
    single { GetUserDataUseCase(get()) }
    single { SendVerificationEmailUseCase(get()) }
    single { VerifyEmailUseCase(get()) }
    single { DeleteUserUseCase(get()) }
}

val viewModelModule = module {
    viewModel { BaseViewModel(get()) }
    viewModel { SharedViewModel(get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get(), get(), get(), get()) }
}