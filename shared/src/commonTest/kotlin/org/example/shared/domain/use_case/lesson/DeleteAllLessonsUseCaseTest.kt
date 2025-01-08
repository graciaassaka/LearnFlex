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

class DeleteAllLessonsUseCaseTest {
    private lateinit var deleteLessonsByModuleUseCase: DeleteLessonsByModuleUseCase
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        deleteLessonsByModuleUseCase = DeleteLessonsByModuleUseCase(repository)
    }

    @Test
    fun `invoke should return success when deleteAll succeeds`() = runTest {
        val lessons = listOf(mockk<Lesson>())
        coEvery { repository.deleteAll(lessons, path, any()) } returns Result.success(Unit)

        val result = deleteLessonsByModuleUseCase(lessons, USER_ID, CURRICULUM_ID, MODULE_ID)

        coVerify(exactly = 1) { repository.deleteAll(lessons, path, any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when deleteAll fails`() = runTest {
        val lessons = listOf(mockk<Lesson>())
        val exception = RuntimeException("DeleteAll failed")
        coEvery { repository.deleteAll(lessons, path, any()) } returns Result.failure(exception)

        val result = deleteLessonsByModuleUseCase(lessons, USER_ID, CURRICULUM_ID, MODULE_ID)

        coVerify(exactly = 1) { repository.deleteAll(lessons, path, any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
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