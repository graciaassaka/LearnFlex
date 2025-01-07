package org.example.shared.domain.use_case.module

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertTrue

class RetrieveModulesByMinQuizScoreUseCaseTest {
    private lateinit var useCase: RetrieveModulesByMinQuizScoreUseCase
    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = RetrieveModulesByMinQuizScoreUseCase(repository)
    }

    @Test
    fun `getIdsByMinScore should return ids flow when succeeds`() = runTest {
        val path = "test/path"
        val minScore = 80
        val modules = mockk<List<Module>>()

        coEvery { repository.getByMinScore(path, minScore) } returns Result.success(modules)

        val result = useCase(path, minScore)

        coVerify(exactly = 1) { repository.getByMinScore(path, minScore) }
        assertTrue(result.isSuccess)
        assertEquals(modules, result.getOrNull())
    }

    @Test
    fun `getIdsByMinScore should return error flow when fails`() = runTest {
        val path = "test/path"
        val minScore = 80
        val exception = RuntimeException("Get failed")

        coEvery { repository.getByMinScore(path, minScore) } returns Result.failure(exception)

        val result = useCase(path, minScore)

        coVerify(exactly = 1) { repository.getByMinScore(path, minScore) }
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}