package org.example.shared.domain.use_case

import org.junit.After
import org.junit.Before
import org.junit.Test

expect class GetUserDataUseCaseTest {
    @Before
    fun setUp()

    @After
    fun tearDown()

    @Test
    fun `GetUserDataUseCase should call AuthService#getUserData`()

    @Test
    fun `GetUserDataUseCase should return success when AuthService#getUserData returns success`()

    @Test
    fun `GetUserDataUseCase should return failure with the exception when AuthService#getUserData returns failure`()
}