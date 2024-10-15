package org.example.shared.domain.viewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
expect class SharedViewModelTest
{
    @Before
    fun setup()

    @After
    fun tearDown()

    @Test
    fun `getUserData should call getUserDataUseCase`()

    @Test
    fun `getUserData should update state with user data on success`()

    @Test
    fun `getUserData should update state with error on failure`()

    @Test
    fun `clearError should update state with null error`()
}