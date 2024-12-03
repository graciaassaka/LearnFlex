package org.example.shared.domain.use_case

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.repository.ModuleRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertTrue

class GetModuleIdsByMinQuizScoreUseCaseTest {
    private lateinit var useCase: GetModuleIdsByMinQuizScoreUseCase
    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetModuleIdsByMinQuizScoreUseCase(repository)
    }

    @Test
    fun `getIdsByMinScore should return ids flow when succeeds`() = runTest {
        val path = "test/path"
        val minScore = 80
        val ids = setOf("id1", "id2")
        val idsFlow = flowOf(Result.success(ids))
        every { repository.getIdsByMinScore(path, minScore) } returns idsFlow

        val result = useCase(path, minScore).single()

        verify(exactly = 1) { repository.getIdsByMinScore(path, minScore) }
        assertTrue(result.isSuccess)
        assertEquals(ids, result.getOrNull())
    }

    @Test
    fun `getIdsByMinScore should return error flow when fails`() = runTest {
        val path = "test/path"
        val minScore = 80
        val exception = RuntimeException("Get failed")
        val errorFlow = flowOf(Result.failure<Set<String>>(exception))
        every { repository.getIdsByMinScore(path, minScore) } returns errorFlow

        val result = useCase(path, minScore).single()

        verify(exactly = 1) { repository.getIdsByMinScore(path, minScore) }
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}