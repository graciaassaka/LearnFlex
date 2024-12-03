package org.example.shared.domain.use_case

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.example.shared.domain.repository.LessonRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class GetLessonIdsByMinQuizScoreTest {
    private lateinit var getLessonIdsByMinQuizScore: GetLessonIdsByMinQuizScore
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        getLessonIdsByMinQuizScore = GetLessonIdsByMinQuizScore(repository)
    }

    @Test
    fun `invoke should return lesson ids flow when get succeeds`() {
        val path = "test/path"
        val minScore = 80
        val lessonIds = setOf("id1", "id2", "id3")
        val idsFlow = flowOf(Result.success(lessonIds))
        every { repository.getIdsByMinScore(path, minScore) } returns idsFlow

        val result = getLessonIdsByMinQuizScore(path, minScore)

        verify(exactly = 1) { repository.getIdsByMinScore(path, minScore) }
        assertEquals(idsFlow, result)
    }

    @Test
    fun `invoke should return error flow when get fails`() {
        val path = "test/path"
        val minScore = 80
        val exception = RuntimeException("Get failed")
        val errorFlow = flowOf(Result.failure<Set<String>>(exception))
        every { repository.getIdsByMinScore(path, minScore) } returns errorFlow

        val result = getLessonIdsByMinQuizScore(path, minScore)

        verify(exactly = 1) { repository.getIdsByMinScore(path, minScore) }
        assertEquals(errorFlow, result)
    }
}