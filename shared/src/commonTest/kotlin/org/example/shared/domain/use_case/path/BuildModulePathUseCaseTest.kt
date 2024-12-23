package org.example.shared.domain.use_case.path

import io.mockk.every
import io.mockk.mockk
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BuildModulePathUseCaseTest {
    private lateinit var buildModulePathUseCase: BuildModulePathUseCase
    private lateinit var pathBuilder: PathBuilder

    @Before
    fun setUp() {
        pathBuilder = mockk<PathBuilder>(relaxed = true)
        buildModulePathUseCase = BuildModulePathUseCase(pathBuilder)

        every { pathBuilder.buildModulePath(any(), any()) } returns PATH
    }

    @Test
    fun `invoke with valid userId and curriculumId should return the path built by the pathBuilder`() {
        // Arrange
        val userId = "testUserId"
        val curriculumId = "testCurriculumId"

        // Act
        val result = buildModulePathUseCase(userId, curriculumId)

        // Assert
        assertEquals(PATH, result)
    }

    @Test
    fun `invoke with empty userId should throw IllegalArgumentException`() {
        // Arrange
        val userId = ""
        val curriculumId = "testCurriculumId"

        // Assert
        assertFailsWith<IllegalArgumentException> {
            // Act
            buildModulePathUseCase(userId, curriculumId)
        }
    }

    @Test
    fun `invoke with empty curriculumId should throw IllegalArgumentException`() {
        // Arrange
        val userId = "testUserId"
        val curriculumId = ""

        // Assert
        assertFailsWith<IllegalArgumentException> {
            // Act
            buildModulePathUseCase(userId, curriculumId)
        }
    }

    companion object {
        private const val PATH = "test/module/path"
    }
}