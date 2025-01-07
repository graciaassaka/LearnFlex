package org.example.shared.domain.use_case.section

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class UploadSectionsUseCaseTest {
    private lateinit var useCase: UploadSectionsUseCase
    private lateinit var repository: SectionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UploadSectionsUseCase(repository)
    }

    @Test
    fun `upload should return success when succeeds`() = runTest {
        val section = listOf(mockk<Section>())
        coEvery { repository.insertAll(section, path, any()) } returns Result.success(Unit)

        val result = useCase(section, USER_ID, CURRICULUM_ID, MODULE_ID, LESSON_ID)

        coVerify(exactly = 1) { repository.insertAll(section, path, any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `upload should return failure when fails`() = runTest {
        val section = listOf(mockk<Section>())
        val exception = RuntimeException("Upload failed")
        coEvery { repository.insertAll(section, path, any()) } returns Result.failure(exception)

        val result = useCase(section, USER_ID, CURRICULUM_ID, MODULE_ID, LESSON_ID)

        coVerify(exactly = 1) { repository.insertAll(section, path, any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
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