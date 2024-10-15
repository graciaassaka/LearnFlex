package org.example.shared.domain.use_case

import org.example.shared.domain.service.AuthService

class GetUserDataUseCase(private val authService: AuthService)
{
    suspend operator fun invoke() = authService.getUserData()
}
