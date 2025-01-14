package org.example.shared.domain.use_case.lesson

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
        val lessonsResult = Result.success(lessons)
        coEvery { repository.getAll(path) } returns lessonsResult

        // Act
        val result = fetchLessonsByModuleUseCase(USER_ID, CURRICULUM_ID, MODULE_ID)

        // Assert
        coVerify(exactly = 1) { repository.getAll(path) }
        assertEquals(Result.success(lessons), result)
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val exception = RuntimeException("GetAll failed")
        val errorResult = Result.failure<List<Lesson>>(exception)
        coEvery { repository.getAll(path) } returns errorResult

        // Act
        val result = fetchLessonsByModuleUseCase(USER_ID, CURRICULUM_ID, MODULE_ID)

        // Assert
        coVerify(exactly = 1) { repository.getAll(path) }
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
