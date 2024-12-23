package org.example.shared.domain.use_case.auth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.User
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class VerifyEmailUseCaseTest {
    private lateinit var verifyEmailUseCase: VerifyEmailUseCase
    private lateinit var getUserDataUseCase: GetUserDataUseCase
    private lateinit var user: User

    @Before
    fun setUp() {
        getUserDataUseCase = mockk<GetUserDataUseCase>()
        verifyEmailUseCase = VerifyEmailUseCase(getUserDataUseCase)

        user = mockk<User>()
    }

    @Test
    fun `VerifyEmailUseCase should call GetUserDataUseCase`() = runTest {
        // Given
        coEvery { getUserDataUseCase() } returns Result.success(user)

        // When
        verifyEmailUseCase()

        // Then
        coVerify(exactly = 1) { getUserDataUseCase() }
    }

    @Test
    fun `VerifyEmailUseCase should throw an exception when the user's email is not verified`() = runTest {
        // Given
        every { user.emailVerified } returns false
        coEvery { getUserDataUseCase() } returns Result.success(user)

        // When
        val result = verifyEmailUseCase()

        // Then
        assert(result.isFailure)
    }

    @Test
    fun `VerifyEmailUseCase should return success when the user's email is verified`() = runTest {
        // Given
        every { user.emailVerified } returns true
        coEvery { getUserDataUseCase() } returns Result.success(user)

        // When
        val result = verifyEmailUseCase()

        // Then
        assert(result.isSuccess)
    }
}