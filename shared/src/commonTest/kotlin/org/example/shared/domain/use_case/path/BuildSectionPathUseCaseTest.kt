package org.example.shared.domain.use_case.path

import io.mockk.every
import io.mockk.mockk
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BuildSectionPathUseCaseTest {
    private lateinit var buildSectionPathUseCase: BuildSectionPathUseCase
    private lateinit var pathBuilder: PathBuilder

    @Before
    fun setUp() {
        pathBuilder = mockk<PathBuilder>(relaxed = true)
        buildSectionPathUseCase = BuildSectionPathUseCase(pathBuilder)

        every { pathBuilder.buildSectionPath(any(), any(), any(), any()) } returns PATH
    }

    @Test
    fun `invoke with valid userId, curriculumId, moduleId, and lessonId should return the path built by the pathBuilder`() {
        // Arrange
        val userId = "testUserId"
        val curriculumId = "testCurriculumId"
        val moduleId = "testModuleId"
        val lessonId = "testLessonId"

        // Act
        val result = buildSectionPathUseCase(userId, curriculumId, moduleId, lessonId)

        // Assert
        assertEquals(PATH, result)
    }

    @Test
    fun `invoke with empty userId should throw IllegalArgumentException`() {
        // Arrange
        val userId = ""
        val curriculumId = "testCurriculumId"
        val moduleId = "testModuleId"
        val lessonId = "testLessonId"

        // Assert
        assertFailsWith<IllegalArgumentException> {
            // Act
            buildSectionPathUseCase(userId, curriculumId, moduleId, lessonId)
        }
    }

    @Test
    fun `invoke with empty curriculumId should throw IllegalArgumentException`() {
        // Arrange
        val userId = "testUserId"
        val curriculumId = ""
        val moduleId = "testModuleId"
        val lessonId = "testLessonId"

        // Assert
        assertFailsWith<IllegalArgumentException> {
            // Act
            buildSectionPathUseCase(userId, curriculumId, moduleId, lessonId)
        }
    }

    @Test
    fun `invoke with empty moduleId should throw IllegalArgumentException`() {
        // Arrange
        val userId = "testUserId"
        val curriculumId = "testCurriculumId"
        val moduleId = ""
        val lessonId = "testLessonId"

        // Assert
        assertFailsWith<IllegalArgumentException> {
            // Act
            buildSectionPathUseCase(userId, curriculumId, moduleId, lessonId)
        }
    }

    @Test
    fun `invoke with empty lessonId should throw IllegalArgumentException`() {
        // Arrange
        val userId = "testUserId"
        val curriculumId = "testCurriculumId"
        val moduleId = "testModuleId"
        val lessonId = ""

        // Assert
        assertFailsWith<IllegalArgumentException> {
            // Act
            buildSectionPathUseCase(userId, curriculumId, moduleId, lessonId)
        }
    }

    companion object {
        private const val PATH = "test/section/path"
    }
}