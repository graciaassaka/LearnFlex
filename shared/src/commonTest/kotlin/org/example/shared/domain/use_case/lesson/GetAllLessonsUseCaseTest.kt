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
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetAllLessonsUseCaseTest {
    private lateinit var getAllLessonsUseCase: GetAllLessonsUseCase
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        getAllLessonsUseCase = GetAllLessonsUseCase(repository)
    }

    @Test
    fun `invoke should return lessons flow when getAll succeeds`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildLessonPath("userId", "curriculumId", "moduleId")
        val lessons = listOf(mockk<Lesson>(), mockk<Lesson>())
        val lessonsFlow = flowOf(Result.success(lessons))
        every { repository.getAll(path) } returns lessonsFlow

        // Act
        val emissions = mutableListOf<Result<List<Lesson>>>()
        getAllLessonsUseCase(path).collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(1, emissions.size)
        assertEquals(Result.success(lessons), emissions.first())
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildLessonPath("userId", "curriculumId", "moduleId")
        val exception = RuntimeException("GetAll failed")
        val errorFlow = flowOf(Result.failure<List<Lesson>>(exception))
        every { repository.getAll(path) } returns errorFlow

        // Act
        val emissions = mutableListOf<Result<List<Lesson>>>()
        getAllLessonsUseCase(path).collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
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
                getAllLessonsUseCase(path).first().getOrThrow()
            }
        }
    }
}
