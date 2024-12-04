package org.example.shared.domain.use_case

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.example.shared.domain.repository.SectionRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class GetSectionIdsByMinQuizScoreUseCaseTest {
    private lateinit var useCase: GetSectionIdsByMinQuizScoreUseCase
    private lateinit var repository: SectionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetSectionIdsByMinQuizScoreUseCase(repository)
    }

    @Test
    fun `getIdsByMinScore should return ids flow when succeeds`() {
        val parentId = "parent-id"
        val minScore = 80
        val sectionIds = setOf("id1", "id2", "id3")
        val idsFlow = flowOf(Result.success(sectionIds))
        every { repository.getIdsByMinScore(parentId, minScore) } returns idsFlow

        val result = useCase(parentId, minScore)

        verify(exactly = 1) { repository.getIdsByMinScore(parentId, minScore) }
        assertEquals(idsFlow, result)
    }

    @Test
    fun `getIdsByMinScore should return error flow when fails`() {
        val parentId = "parent-id"
        val minScore = 80
        val exception = RuntimeException("Failed to get section ids")
        val errorFlow = flowOf(Result.failure<Set<String>>(exception))
        every { repository.getIdsByMinScore(parentId, minScore) } returns errorFlow

        val result = useCase(parentId, minScore)

        verify(exactly = 1) { repository.getIdsByMinScore(parentId, minScore) }
        assertEquals(errorFlow, result)
    }
}