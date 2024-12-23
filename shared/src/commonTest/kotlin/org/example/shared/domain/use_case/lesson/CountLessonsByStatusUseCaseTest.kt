package org.example.shared.domain.use_case.lesson

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.ContentStatus
import org.example.shared.domain.model.Lesson
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals

class CountLessonsByStatusUseCaseTest {
    private lateinit var countLessonsByStatusUseCase: CountLessonsByStatusUseCase
    private lateinit var getAllLessonsByCurriculumIdUseCase: GetLessonsByCurriculumIdUseCase

    @Before
    fun setUp() {
        getAllLessonsByCurriculumIdUseCase = mockk<GetLessonsByCurriculumIdUseCase>()
        countLessonsByStatusUseCase = CountLessonsByStatusUseCase(getAllLessonsByCurriculumIdUseCase)
    }

    @Test
    fun `invoke should return Result#success encapsulating a map of ContentStatus to lesson number when getAllLessons returns Result#success`() =
        runTest {
            // Arrange
            val lessonProgress = lessons.groupBy {
                if (it.quizScore >= it.quizScoreMax * 0.75) ContentStatus.FINISHED
                else ContentStatus.UNFINISHED
            }.mapValues { it.value.size }

            coEvery { getAllLessonsByCurriculumIdUseCase(PATH) } returns Result.success(lessons)

            // Act
            val result = countLessonsByStatusUseCase(PATH)

            // Assert
            assert(result.isSuccess)
            assertEquals(lessonProgress, result.getOrNull())
        }

    @Test
    fun `invoke should return Result#failure when getAllLessons returns Result#failure`() = runTest {
        // Arrange
        val exception = Exception("An error occurred")
        coEvery { getAllLessonsByCurriculumIdUseCase(PATH) } returns Result.failure(exception)

        // Act
        val result = countLessonsByStatusUseCase(PATH)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }

    companion object {
        private const val PATH = "test/path"
        private val lessons = listOf(
            Lesson(
                id = "1",
                title = "Lesson 1",
                description = "Description 1",
                imageUrl = "https://example.com/image1.png",
                index = 1,
                quizScore = 5,
                quizScoreMax = 10,
                createdAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                lastUpdated = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ),
            Lesson(
                id = "2",
                title = "Lesson 2",
                description = "Description 2",
                imageUrl = "https://example.com/image2.png",
                index = 2,
                quizScore = 6,
                quizScoreMax = 10,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            ),
            Lesson(
                id = "3",
                title = "Lesson 3",
                description = "Description 3",
                imageUrl = "https://example.com/image3.png",
                index = 3,
                quizScore = 7,
                quizScoreMax = 10,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )
    }
}