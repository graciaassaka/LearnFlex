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

class UploadLessonsUseCaseTest {
    private lateinit var uploadLessonsUseCase: UploadLessonsUseCase
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        uploadLessonsUseCase = UploadLessonsUseCase(repository)
    }

    @Test
    fun `invoke should return success when upload succeeds`() = runTest {
        // Arrange
        val lessons = listOf(mockk<Lesson>())
        coEvery { repository.insertAll(lessons, path, any()) } returns Result.success(Unit)

        // Act
        val result = uploadLessonsUseCase(lessons, USER_ID, CURRICULUM_ID, MODULE_ID)

        // Assert
        coVerify(exactly = 1) { repository.insertAll(lessons, path, any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when upload fails`() = runTest {
        // Arrange
        val lessons = listOf(mockk<Lesson>())
        val exception = RuntimeException("Upload failed")
        coEvery { repository.insertAll(lessons, path, any()) } returns Result.failure(exception)

        // Act
        val result = uploadLessonsUseCase(lessons, USER_ID, CURRICULUM_ID, MODULE_ID)

        // Assert
        coVerify(exactly = 1) { repository.insertAll(lessons, path, any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    companion object {
        private const val USER_ID = "userId"
        private const val CURRICULUM_ID = "curriculumId"
        private const val MODULE_ID = "moduleId"
        val path = PathBuilder()
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