package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Test

class GetModuleUseCaseTest {
    private lateinit var useCase: GetModuleUseCase
    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetModuleUseCase(repository)
    }

    @Test
    fun `get should return success when succeeds`() = runTest {
        val path = "test/path"
        val id = "test-id"
        val module = mockk<Module>()
        coEvery { repository.get(path, id) } returns flowOf(Result.success(module))

        val result = useCase(path, id).single()

        coVerify(exactly = 1) { repository.get(path, id) }
        assert(result.isSuccess)
        assertEquals(module, result.getOrNull())
    }

    @Test
    fun `get should return failure when fails`() = runTest {
        val path = "test/path"
        val id = "test-id"
        val exception = RuntimeException("Get failed")
        coEvery { repository.get(path, id) } returns flowOf(Result.failure(exception))

        val result = useCase(path, id).single()

        coVerify(exactly = 1) { repository.get(path, id) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
