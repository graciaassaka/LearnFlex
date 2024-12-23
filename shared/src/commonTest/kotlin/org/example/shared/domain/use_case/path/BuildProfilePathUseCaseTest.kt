package org.example.shared.domain.use_case.path

import io.mockk.every
import io.mockk.mockk
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildProfilePathUseCaseTest {
    private lateinit var buildProfilePathUseCase: BuildProfilePathUseCase
    private lateinit var pathBuilder: PathBuilder

    @Before
    fun setUp() {
        pathBuilder = mockk<PathBuilder>(relaxed = true)
        buildProfilePathUseCase = BuildProfilePathUseCase(pathBuilder)

        every { pathBuilder.buildProfilePath() } returns PATH
    }

    @Test
    fun `invoke should return the path built by the pathBuilder`() {
        // Act
        val result = buildProfilePathUseCase()

        // Assert
        assertEquals(PATH, result)
    }

    companion object {
        private const val PATH = "test/profile/path"
    }
}