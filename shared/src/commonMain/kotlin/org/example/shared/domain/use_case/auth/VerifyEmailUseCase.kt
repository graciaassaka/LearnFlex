package org.example.shared.domain.use_case.auth

/**
 * Use case for verifying if a user's email is verified.
 *
 * @property getUserDataUseCase The use case to get user data.
 */
class VerifyEmailUseCase(private val getUserDataUseCase: GetUserDataUseCase) {

    /**
     * Invokes the use case to verify the user's email.
     *
     * @throws Exception if the email is not verified.
     */
    suspend operator fun invoke() = runCatching {
        getUserDataUseCase().getOrThrow().run {
            if (emailVerified == false) throw Exception("Email is not verified.")
        }
    }
}