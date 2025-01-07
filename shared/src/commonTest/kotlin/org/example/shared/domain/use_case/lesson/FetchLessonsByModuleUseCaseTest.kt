package org.example.shared.domain.use_case.lesson

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchLessonsByModuleUseCaseTest {
    private lateinit var fetchLessonsByModuleUseCase: FetchLessonsByModuleUseCase
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        fetchLessonsByModuleUseCase = FetchLessonsByModuleUseCase(repository)
    }

    @Test
    fun `invoke should return lessons flow when getAll succeeds`() = runTest {
        // Arrange
        val lessons = listOf(mockk<Lesson>(), mockk<Lesson>())
        val lessonsFlow = flowOf(Result.success(lessons))
        every { repository.getAll(path) } returns lessonsFlow

        // Act
        val result = fetchLessonsByModuleUseCase(USER_ID, CURRICULUM_ID, MODULE_ID)

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(Result.success(lessons), result)
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val exception = RuntimeException("GetAll failed")
        val errorFlow = flowOf(Result.failure<List<Lesson>>(exception))
        every { repository.getAll(path) } returns errorFlow

        // Act
        val result = fetchLessonsByModuleUseCase(USER_ID, CURRICULUM_ID, MODULE_ID)

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }

    companion object {
        private const val USER_ID = "userId"
        private const val CURRICULUM_ID = "curriculumId"
        private const val MODULE_ID = "moduleId"
        private val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document(USER_ID)
            .collection(Collection.CURRICULA)
            .document(CURRICULUM_ID)
            .collection(Collection.MODULES)
            .document(MODULE_ID)
            .collection(Collection.LESSONS)
            .build()
    }
}
