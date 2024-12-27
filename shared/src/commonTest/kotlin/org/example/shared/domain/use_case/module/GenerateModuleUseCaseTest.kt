package org.example.shared.domain.use_case.module

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.constant.ContentType
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Profile
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GenerateModuleUseCaseTest {

    private lateinit var contentGeneratorClient: ContentGeneratorClient
    private lateinit var generateModuleUseCase: GenerateModuleUseCase

    @Before
    fun setUp() {
        // Mock the ContentGeneratorClient with relaxed behavior
        contentGeneratorClient = mockk(relaxed = true)
        // Initialize the use case with the mocked client
        generateModuleUseCase = GenerateModuleUseCase(contentGeneratorClient)
    }

    /**
     * Verifies that the use case calls generateContent with the correct context based on input parameters.
     */
    @Test
    fun `invoke should call generateContent with correct context`() = runTest {
        // Arrange
        val tile = "Module 1: Introduction to Kotlin"
        val profile = Profile(
            username = "testUser",
            email = "test@example.com",
            photoUrl = "https://example.com/photo.jpg",
            preferences = Profile.LearningPreferences(
                field = "Computer Science",
                level = "Beginner",
                goal = "Learn Kotlin"
            ),
            learningStyle = Profile.LearningStyle(
                dominant = "reading",
                breakdown = Profile.LearningStyleBreakdown(
                    reading = 70,
                    kinesthetic = 30
                )
            )
        )
        val curriculum = Curriculum(
            imageUrl = "https://example.com/image.jpg",
            title = "Kotlin Basics",
            description = "A beginner-friendly introduction to Kotlin.",
            status = "active"
        )

        // Mock the generateContent method to return a successful flow
        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = "Module 1: Introduction to Kotlin",
            imagePrompt = "An illustration of Kotlin basics",
            description = "This module covers the fundamentals of Kotlin programming.",
            content = listOf("Variables and Types", "Control Structures", "Functions")
        )
        coEvery {
            contentGeneratorClient.generateContent(any())
        } returns flowOf(Result.success(generatedResponse))

        // Act
        val result = generateModuleUseCase.invoke(tile, profile, curriculum)

        // Assert
        // Verify that generateContent was called exactly once with the correct context
        coVerify(exactly = 1) {
            contentGeneratorClient.generateContent(
                match {
                    it.field == profile.preferences.field &&
                            it.level == profile.preferences.level &&
                            it.goal == profile.preferences.goal &&
                            it.style == profile.learningStyle &&
                            it.type == ContentType.MODULE &&
                            it.contentDescriptors.size == 2 &&
                            it.contentDescriptors[0].type == ContentType.CURRICULUM &&
                            it.contentDescriptors[0].title == curriculum.title &&
                            it.contentDescriptors[0].description == curriculum.description &&
                            it.contentDescriptors[1].type == ContentType.MODULE &&
                            it.contentDescriptors[1].title == tile &&
                            it.contentDescriptors[1].description == "Not provided"
                }
            )
        }

        // Additionally, verify that the result matches the mocked generated response
        assertTrue(result.isSuccess)
        assertEquals(generatedResponse, result.getOrNull())
    }

    /**
     * Tests that the use case returns a success result when generateContent emits a successful Result.
     */
    @Test
    fun `invoke should return success result when generateContent succeeds`() = runTest {
        // Arrange
        val tile = "Module 2: Advanced Kotlin Features"
        val profile = Profile(
            username = "advancedUser",
            email = "advanced@example.com",
            photoUrl = "https://example.com/photo2.jpg",
            preferences = Profile.LearningPreferences(
                field = "Engineering",
                level = "Intermediate",
                goal = "Master Kotlin Coroutines"
            ),
            learningStyle = Profile.LearningStyle(
                dominant = "kinesthetic",
                breakdown = Profile.LearningStyleBreakdown(
                    reading = 40,
                    kinesthetic = 60
                )
            )
        )
        val curriculum = mockk<Curriculum> {
            every { title } returns "Advanced Kotlin Curriculum"
            every { description } returns "An advanced curriculum focusing on Kotlin's concurrency and coroutines."
        }

        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = "Module 2: Advanced Kotlin Features",
            imagePrompt = "A diagram of Kotlin coroutines",
            description = "This module delves into Kotlin's advanced features, including coroutines and concurrency.",
            content = listOf("Coroutines Basics", "Advanced Coroutine Builders", "Error Handling in Coroutines")
        )

        // Mock the generateContent method to return a successful flow
        coEvery {
            contentGeneratorClient.generateContent(any())
        } returns flowOf(Result.success(generatedResponse))

        // Act
        val result = generateModuleUseCase.invoke(tile, profile, curriculum)

        // Assert
        // Verify that the result is successful and matches the mocked response
        assertTrue(result.isSuccess)
        assertEquals(generatedResponse, result.getOrNull())
    }

    /**
     * Tests that the use case returns a failure result when generateContent emits a failure Result.
     */
    @Test
    fun `invoke should return failure result when generateContent fails`() = runTest {
        // Arrange
        val tile = "Module 3: Kotlin Coroutines Deep Dive"
        val profile = Profile(
            username = "expertUser",
            email = "expert@example.com",
            photoUrl = "https://example.com/photo3.jpg",
            preferences = Profile.LearningPreferences(
                field = "Health",
                level = "Advanced",
                goal = "Optimize Kotlin applications"
            ),
            learningStyle = Profile.LearningStyle(
                dominant = "reading",
                breakdown = Profile.LearningStyleBreakdown(
                    reading = 85,
                    kinesthetic = 15
                )
            )
        )
        val curriculum = mockk<Curriculum> {
            every { title } returns "Expert Kotlin Curriculum"
            every { description } returns "An expert-level curriculum for optimizing Kotlin applications."
        }

        val exception = Exception("Content generation failed")

        // Mock the generateContent method to return a failure flow
        coEvery {
            contentGeneratorClient.generateContent(any())
        } returns flowOf(Result.failure(exception))

        // Act
        val result = generateModuleUseCase.invoke(tile, profile, curriculum)

        // Assert
        // Verify that the result is a failure and contains the expected exception
        assertTrue(result.isFailure)
        val thrownException = result.exceptionOrNull()
        assertIs<Exception>(thrownException)
        assertEquals("Content generation failed", thrownException.message)
    }
}
