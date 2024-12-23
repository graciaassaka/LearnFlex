package org.example.shared.domain.use_case.section

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class GetSectionByMinQuizScoreUseCaseTest {
    private lateinit var useCase: GetSectionByMinQuizScoreUseCase
    private lateinit var repository: SectionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetSectionByMinQuizScoreUseCase(repository)
    }

    @Test
    fun `getIdsByMinScore should return ids flow when succeeds`() = runTest {
        val parentId = "parent-id"
        val minScore = 80
        val sections = mockk<List<Section>>()

        coEvery { repository.getByMinScore(parentId, minScore) } returns Result.success(sections)

        val result = useCase(parentId, minScore)

        coVerify(exactly = 1) { repository.getByMinScore(parentId, minScore) }
        assertEquals(sections, result.getOrNull())
    }

    @Test
    fun `getIdsByMinScore should return error flow when fails`() = runTest {
        val parentId = "parent-id"
        val minScore = 80
        val exception = RuntimeException("Failed to get section ids")

        coEvery { repository.getByMinScore(parentId, minScore) } returns Result.failure(exception)

        val result = useCase(parentId, minScore)

        coVerify(exactly = 1) { repository.getByMinScore(parentId, minScore) }
        assertEquals(exception, result.exceptionOrNull())
    }
}