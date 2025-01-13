package org.example.shared.domain.use_case.section

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UploadSectionUseCaseTest {

    private lateinit var uploadSectionUseCase: UploadSectionUseCase
    private lateinit var repository: SectionRepository

    @Before
    fun setUp() {
        repository = mockk()
        uploadSectionUseCase = UploadSectionUseCase(repository)
    }

    @Test
    fun `invoke should return success when upload succeeds`() = runTest {
        // Arrange
        val userId = "userId"
        val curriculumId = "curriculumId"
        val moduleId = "moduleId"
        val lessonId = "lessonId"

        val section = mockk<Section> {
            every { id } returns "sectionId"
            every { title } returns "Section Title"
            every { description } returns "Section Description"
            every { content } returns listOf("Content 1", "Content 2")
        }

        // Optionally, construct the expected path if you want to verify it
        val expectedPath = PathBuilder()
            .collection(Collection.PROFILES)
            .document(userId)
            .collection(Collection.CURRICULA)
            .document(curriculumId)
            .collection(Collection.MODULES)
            .document(moduleId)
            .collection(Collection.LESSONS)
            .document(lessonId)
            .collection(Collection.SECTIONS)
            .document(section.id)
            .build()

        // Stub the repository.insert to return a successful result
        coEvery { repository.insert(item = section, path = expectedPath, timestamp = any()) } returns Result.success(Unit)

        // Act
        val result = uploadSectionUseCase(section, userId, curriculumId, moduleId, lessonId)

        // Assert
        coVerify(exactly = 1) { repository.insert(item = section, path = expectedPath, timestamp = any()) }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when upload fails`() = runTest {
        // Arrange
        val userId = "userId"
        val curriculumId = "curriculumId"
        val moduleId = "moduleId"
        val lessonId = "lessonId"

        val section = mockk<Section> {
            every { id } returns "sectionId"
            every { title } returns "Section Title"
            every { description } returns "Section Description"
            every { content } returns listOf("Content 1", "Content 2")
        }

        val exception = RuntimeException("Upload failed")

        // Optionally, construct the expected path if you want to verify it
        val expectedPath = PathBuilder()
            .collection(Collection.PROFILES)
            .document(userId)
            .collection(Collection.CURRICULA)
            .document(curriculumId)
            .collection(Collection.MODULES)
            .document(moduleId)
            .collection(Collection.LESSONS)
            .document(lessonId)
            .collection(Collection.SECTIONS)
            .document(section.id)
            .build()

        // Stub the repository.insert to return a failure result
        coEvery { repository.insert(item = section, path = expectedPath, timestamp = any()) } returns Result.failure(exception)

        // Act
        val result = uploadSectionUseCase(section, userId, curriculumId, moduleId, lessonId)

        // Assert
        coVerify(exactly = 1) { repository.insert(item = section, path = expectedPath, timestamp = any()) }
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
