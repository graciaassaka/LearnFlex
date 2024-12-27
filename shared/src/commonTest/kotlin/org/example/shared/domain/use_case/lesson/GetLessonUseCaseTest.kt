package org.example.shared.domain.use_case.lesson

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.data.remote.firestore.FirestorePathBuilder
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetLessonUseCaseTest {
    private lateinit var useCase: GetLessonUseCase
    private lateinit var repository: LessonRepository

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = GetLessonUseCase(repository)
    }

    @Test
    fun `invoke should return lesson flow when get succeeds`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildLessonPath("userId", "curriculumId", "moduleId")
        val lesson = mockk<Lesson>()
        val lessonFlow = flowOf(Result.success(lesson))
        every { repository.get(path, "lessonId") } returns lessonFlow

        // Act
        val emissions = mutableListOf<Result<Lesson>>()
        useCase(path, "lessonId").collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.get(path, "lessonId") }
        assertEquals(1, emissions.size)
        assertEquals(Result.success(lesson), emissions.first())
    }

    @Test
    fun `invoke should return error flow when get fails`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildLessonPath("userId", "curriculumId", "moduleId")
        val exception = RuntimeException("Get failed")
        val errorFlow = flowOf(Result.failure<Lesson>(exception))
        every { repository.get(path, "lessonId") } returns errorFlow

        // Act
        val emissions = mutableListOf<Result<Lesson>>()
        useCase(path, "lessonId").collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.get(path, "lessonId") }
        assertEquals(1, emissions.size)
        assert(emissions.first().isFailure)
        assertEquals(exception, emissions.first().exceptionOrNull())
    }

    @Test
    fun `invoke should throw exception when path does not end with LESSONS`() {
        // Arrange
        val path = FirestorePathBuilder().buildLessonPath("userId", "curriculumId", "moduleId") + "/extra"

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            runTest {
                useCase(path, "lessonId").first().getOrThrow()
            }
        }
    }
}