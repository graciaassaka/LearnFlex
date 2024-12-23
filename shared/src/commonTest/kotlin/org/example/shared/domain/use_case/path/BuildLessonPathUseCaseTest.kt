package org.example.shared.domain.use_case.path

import io.mockk.every
import io.mockk.mockk
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BuildLessonPathUseCaseTest {
    private lateinit var buildLessonPathUseCase: BuildLessonPathUseCase
    private lateinit var pathBuilder: PathBuilder

    @Before
    fun setUp() {
        pathBuilder = mockk<PathBuilder>(relaxed = true)
        buildLessonPathUseCase = BuildLessonPathUseCase(pathBuilder)

        every { pathBuilder.buildLessonPath(any(), any(), any()) } returns PATH
    }

    @Test
    fun `invoke with valid userId, curriculumId, and moduleId should return the path built by the pathBuilder`() {
        // Arrange
        val userId = "testUserId"
        val curriculumId = "testCurriculumId"
        val moduleId = "testModuleId"

        // Act
        val result = buildLessonPathUseCase(userId, curriculumId, moduleId)

        // Assert
        assertEquals(PATH, result)
    }

    @Test
    fun `invoke with empty userId should throw IllegalArgumentException`() {
        // Arrange
        val userId = ""
        val curriculumId = "testCurriculumId"
        val moduleId = "testModuleId"

        // Assert
        assertFailsWith<IllegalArgumentException> {
            // Act
            buildLessonPathUseCase(userId, curriculumId, moduleId)
        }
    }

    @Test
    fun `invoke with empty curriculumId should throw IllegalArgumentException`() {
        // Arrange
        val userId = "testUserId"
        val curriculumId = ""
        val moduleId = "testModuleId"

        // Assert
        assertFailsWith<IllegalArgumentException> {
            // Act
            buildLessonPathUseCase(userId, curriculumId, moduleId)
        }
    }

    @Test
    fun `invoke with empty moduleId should throw IllegalArgumentException`() {
        // Arrange
        val userId = "testUserId"
        val curriculumId = "testCurriculumId"
        val moduleId = ""

        // Assert
        assertFailsWith<IllegalArgumentException> {
            // Act
            buildLessonPathUseCase(userId, curriculumId, moduleId)
        }
    }

    companion object {
        private const val PATH = "test/lesson/path"
    }
}