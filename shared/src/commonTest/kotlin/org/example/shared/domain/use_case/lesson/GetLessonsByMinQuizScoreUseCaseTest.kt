package org.example.shared.domain.use_case.lesson

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class GetLessonsByMinQuizScoreUseCaseTest {
    private lateinit var getLessonsByMinQuizScoreUseCase: GetLessonsByMinQuizScoreUseCase
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        getLessonsByMinQuizScoreUseCase = GetLessonsByMinQuizScoreUseCase(repository)
    }

    @Test
    fun `invoke should return lesson ids flow when get succeeds`() = runTest {
        val path = "test/path"
        val minScore = 80
        val lessons = mockk<List<Lesson>>()

        coEvery { repository.getByMinScore(path, minScore) } returns Result.success(lessons)

        val result = getLessonsByMinQuizScoreUseCase(path, minScore)

        coVerify(exactly = 1) { repository.getByMinScore(path, minScore) }
        assertEquals(lessons, result.getOrNull())
    }

    @Test
    fun `invoke should return error flow when get fails`() = runTest {
        val path = "test/path"
        val minScore = 80
        val exception = RuntimeException("Get failed")

        coEvery { repository.getByMinScore(path, minScore) } returns Result.failure(exception)

        val result = getLessonsByMinQuizScoreUseCase(path, minScore)

        coVerify(exactly = 1) { repository.getByMinScore(path, minScore) }
        assertEquals(exception, result.exceptionOrNull())
    }
}