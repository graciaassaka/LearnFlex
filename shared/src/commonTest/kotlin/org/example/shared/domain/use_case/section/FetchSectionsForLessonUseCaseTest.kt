package org.example.shared.domain.use_case.section

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchSectionsForLessonUseCaseTest {
    private lateinit var useCase: FetchSectionsByLessonUseCase
    private lateinit var repository: SectionRepository

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = FetchSectionsByLessonUseCase(repository)
    }

    @Test
    fun `invoke should return sections flow when getAll succeeds`() = runTest {
        // Arrange
        val sections = listOf(mockk<Section>())
        val sectionsResult = Result.success(sections)
        coEvery { repository.getAll(path) } returns sectionsResult

        // Act
        val result = useCase(USER_ID, CURRICULUM_ID, MODULE_ID, LESSON_ID)

        // Assert
        coVerify(exactly = 1) { repository.getAll(path) }
        assertEquals(Result.success(sections), result)
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val exception = RuntimeException("GetAll failed")
        val errorResult = Result.failure<List<Section>>(exception)
        coEvery { repository.getAll(path) } returns errorResult

        // Act
        val result = useCase(USER_ID, CURRICULUM_ID, MODULE_ID, LESSON_ID)

        // Assert
        coVerify(exactly = 1) { repository.getAll(path) }
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }

    companion object {
        private const val USER_ID = "userId"
        private const val CURRICULUM_ID = "curriculumId"
        private const val MODULE_ID = "moduleId"
        private const val LESSON_ID = "lessonId"
        private val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document(USER_ID)
            .collection(Collection.CURRICULA)
            .document(CURRICULUM_ID)
            .collection(Collection.MODULES)
            .document(MODULE_ID)
            .collection(Collection.LESSONS)
            .document(LESSON_ID)
            .collection(Collection.SECTIONS)
            .build()
    }
}