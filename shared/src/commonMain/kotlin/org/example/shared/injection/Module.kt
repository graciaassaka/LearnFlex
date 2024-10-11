package org.example.shared.injection

import org.example.shared.data.firebase.FirebaseAuthService
import org.example.shared.domain.service.AuthService
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

expect fun initKoin(context: Any?)

expect fun platformAuthModule(): org.koin.core.module.Module

val authServiceModule = module {
    includes(platformAuthModule())
    single<AuthService> { get<FirebaseAuthService>() }
}