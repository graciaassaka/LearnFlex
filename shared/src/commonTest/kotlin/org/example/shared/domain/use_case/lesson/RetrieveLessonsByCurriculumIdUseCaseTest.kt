package org.example.shared.domain.use_case.lesson

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RetrieveLessonsByCurriculumIdUseCaseTest {
    private lateinit var repository: LessonRepository
    private lateinit var useCase: RetrieveLessonsByCurriculumUseCase

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = RetrieveLessonsByCurriculumUseCase(repository)
    }

    @Test
    fun `invoke should return lessons when repository succeeds`() = runTest {
        // Arrange
        val curriculumId = "curriculum123"
        val lesson1 = mockk<Lesson>()
        val lesson2 = mockk<Lesson>()
        val lessonsList = listOf(lesson1, lesson2)
        coEvery { repository.getByCurriculumId(curriculumId) } returns Result.success(lessonsList)

        // Act
        val result = useCase(curriculumId)

        // Assert
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
        assertEquals(Result.success(lessonsList), result)
    }

    @Test
    fun `invoke should return empty list when repository returns empty list`() = runTest {
        // Arrange
        val curriculumId = "curriculumEmpty"
        val emptyList: List<Lesson> = emptyList()
        coEvery { repository.getByCurriculumId(curriculumId) } returns Result.success(emptyList)

        // Act
        val result = useCase(curriculumId)

        // Assert
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
        assertEquals(Result.success(emptyList), result)
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Arrange
        val curriculumId = "curriculumError"
        val exception = RuntimeException("Failed to fetch lessons")
        coEvery { repository.getByCurriculumId(curriculumId) } returns Result.failure(exception)

        // Act
        val result = useCase(curriculumId)

        // Assert
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke should throw exception when repository throws`() {
        // Arrange
        val curriculumId = "curriculumException"
        val exception = IllegalStateException("Unexpected error")
        coEvery { repository.getByCurriculumId(curriculumId) } throws exception

        // Act & Assert
        assertFailsWith<IllegalStateException> {
            runTest {
                useCase(curriculumId)
            }
        }
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
    }
}
