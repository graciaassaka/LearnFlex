package org.example.shared.domain.use_case.module

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Module
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class CountModulesByStatusUseCaseTest {
    private lateinit var countModulesByStatusUseCase: CountModulesByStatusUseCase
    private lateinit var getModulesByCurriculumIdUseCase: GetModulesByCurriculumIdUseCase

    @Before
    fun setUp() {
        getModulesByCurriculumIdUseCase = mockk<GetModulesByCurriculumIdUseCase>(relaxed = true)
        countModulesByStatusUseCase = CountModulesByStatusUseCase(getModulesByCurriculumIdUseCase)
    }

    @Test
    fun `invoke should return a map of ContentStatus to module count when getAllModulesUseCase succeeds`() = runTest {
        // Arrange
        val expected = modules.groupBy {
            if (it.quizScore >= it.quizScoreMax * 0.75) Status.FINISHED
            else Status.UNFINISHED
        }.mapValues { it.value.size }

        coEvery { getModulesByCurriculumIdUseCase(PATH) } returns Result.success(modules)

        // Act
        val result = countModulesByStatusUseCase(PATH)

        // Assert
        assert(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `invoke should return failure when getAllModulesUseCase fails`() = runTest {
        // Arrange
        val exception = Exception("Failed to get modules")

        coEvery { getModulesByCurriculumIdUseCase(PATH) } returns Result.failure(exception)

        // Act
        val result = countModulesByStatusUseCase(PATH)

        // Assert
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    companion object {
        private const val PATH = "test/path"
        private val modules = listOf(
            Module(
                id = "module1",
                imageUrl = "https://example.com/image1.jpg",
                title = "Introduction to Programming",
                description = "Learn the basics of programming.",
                index = 1,
                quizScore = 85,
                quizScoreMax = 100,
                createdAt = 1640995200000,
                lastUpdated = 1641081600000
            ),
            Module(
                id = "module2",
                imageUrl = "https://example.com/image2.jpg",
                title = "Advanced Programming Concepts",
                description = "Explore advanced topics in programming.",
                index = 2,
                quizScore = 90,
                quizScoreMax = 100,
                createdAt = 1643587200000,
                lastUpdated = 1643673600000
            ),
            Module(
                id = "module3",
                imageUrl = "https://example.com/image3.jpg",
                title = "Data Structures",
                description = "Understand data structures and their applications.",
                index = 3,
                quizScore = 95,
                quizScoreMax = 100,
                createdAt = 1646188800000,
                lastUpdated = 1646275200000
            )
        )
    }
}