package org.example.shared.domain.use_case

import org.example.shared.domain.service.AuthService

class SignInUseCase(private val authService: AuthService)
{
    suspend operator fun invoke(email: String, password: String) = authService.signIn(email, password)
}