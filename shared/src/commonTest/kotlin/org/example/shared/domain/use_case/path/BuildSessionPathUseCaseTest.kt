package org.example.shared.domain.use_case.path

import io.mockk.every
import io.mockk.mockk
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BuildSessionPathUseCaseTest {
    private lateinit var buildSessionPathUseCase: BuildSessionPathUseCase
    private lateinit var pathBuilder: PathBuilder

    @Before
    fun setUp() {
        pathBuilder = mockk<PathBuilder>(relaxed = true)
        buildSessionPathUseCase = BuildSessionPathUseCase(pathBuilder)

        every { pathBuilder.buildSessionPath(any()) } returns PATH
    }

    @Test
    fun `invoke with valid userId should return the path built by the pathBuilder`() {
        // Arrange
        val userId = "testUserId"

        // Act
        val result = buildSessionPathUseCase(userId)

        // Assert
        assertEquals(PATH, result)
    }

    @Test
    fun `invoke with empty userId should throw IllegalArgumentException`() {
        // Arrange
        val userId = ""

        // Assert
        assertFailsWith<IllegalArgumentException> {
            // Act
            buildSessionPathUseCase(userId)
        }
    }

    companion object {
        private const val PATH = "test/session/path"
    }
}