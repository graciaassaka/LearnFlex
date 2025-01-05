package org.example.shared.domain.use_case.lesson

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.constant.ContentType
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile
import org.example.shared.domain.use_case.section.GenerateSectionUseCase
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GenerateSectionUseCaseTest {

    private lateinit var contentGeneratorClient: ContentGeneratorClient
    private lateinit var generateSectionUseCase: GenerateSectionUseCase

    @Before
    fun setUp() {
        // Mock the ContentGeneratorClient with relaxed behavior to avoid unnecessary stubbing
        contentGeneratorClient = mockk(relaxed = true)
        // Initialize the use case with the mocked client
        generateSectionUseCase = GenerateSectionUseCase(contentGeneratorClient)
    }

    /**
     * Verifies that the use case calls generateContent with the correct context based on input parameters.
     */
    @Test
    fun `invoke should call generateContent with correct context`() = runTest {
        // Arrange
        val title = "Section 1: Introduction to Kotlin Coroutines"
        val profile = Profile(
            username = "testUser",
            email = "test@example.com",
            photoUrl = "https://example.com/photo.jpg",
            preferences = Profile.LearningPreferences(
                field = "Computer Science",
                level = "Beginner",
                goal = "Learn Kotlin Concurrency"
            ),
            learningStyle = Profile.LearningStyle(
                dominant = "reading",
                breakdown = Profile.LearningStyleBreakdown(
                    reading = 75,
                    kinesthetic = 25
                )
            )
        )
        val curriculum = Curriculum(
            title = "Kotlin Programming Curriculum",
            description = "A comprehensive curriculum for learning Kotlin.",
            content = listOf("Introduction to Kotlin", "Kotlin Basics"),
            status = "active"
        )
        val module = Module(
            title = "Module 1: Coroutines Basics",
            description = "Understanding the fundamentals of coroutines.",
            index = 1,
            content = listOf("Coroutines Setup", "Basic Usage"),
            quizScore = 85,
            quizScoreMax = 100
        )
        val lesson = Lesson(
            title = "Lesson 1: What are Coroutines?",
            description = "An introduction to coroutines in Kotlin.",
            index = 1,
            content = listOf("Coroutines Overview", "Coroutine Lifecycle"),
            quizScore = 90,
            quizScoreMax = 100
        )

        // Mock the generateContent method to return a successful GeneratedResponse
        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = title,
            description = "This section covers the basics of Kotlin coroutines, including setup and simple usage.",
            content = listOf("Introduction to Coroutines", "Coroutine Builders", "Suspending Functions")
        )
        coEvery {
            contentGeneratorClient.generateContent(any())
        } returns flowOf(Result.success(generatedResponse))

        // Act
        val result = generateSectionUseCase.invoke(title, profile, curriculum, module, lesson)

        // Assert
        // Verify that generateContent was called exactly once with the correct context
        coVerify(exactly = 1) {
            contentGeneratorClient.generateContent(
                match { context ->
                    context.field == profile.preferences.field &&
                            context.level == profile.preferences.level &&
                            context.style == profile.learningStyle &&
                            context.type == ContentType.SECTION &&
                            context.contentDescriptors.size == 4 &&
                            context.contentDescriptors[0].type == ContentType.CURRICULUM &&
                            context.contentDescriptors[0].title == curriculum.title &&
                            context.contentDescriptors[0].description == curriculum.description &&
                            context.contentDescriptors[1].type == ContentType.MODULE &&
                            context.contentDescriptors[1].title == module.title &&
                            context.contentDescriptors[1].description == module.description &&
                            context.contentDescriptors[2].type == ContentType.LESSON &&
                            context.contentDescriptors[2].title == lesson.title &&
                            context.contentDescriptors[2].description == lesson.description &&
                            context.contentDescriptors[3].type == ContentType.SECTION &&
                            context.contentDescriptors[3].title == title &&
                            context.contentDescriptors[3].description == "Not provided"
                }
            )
        }

        // Additionally, verify that the result matches the mocked generated response
        assertTrue(result.isSuccess)
        assertEquals(generatedResponse, result.getOrNull())
    }

    /**
     * Tests that the use case returns a success result when contentGeneratorClient emits a successful GeneratedResponse.
     */
    @Test
    fun `invoke should return success result when generateContent succeeds`() = runTest {
        // Arrange
        val title = "Section 2: Advanced Coroutine Builders"
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
        val curriculum = Curriculum(
            title = "Advanced Kotlin Curriculum",
            description = "An advanced curriculum focusing on Kotlin's concurrency features.",
            content = listOf("Advanced Coroutines", "Concurrency Patterns"),
            status = "active"
        )
        val module = Module(
            title = "Module 2: Coroutine Scope and Context",
            description = "Deep dive into coroutine scopes and context management.",
            index = 2,
            content = listOf("CoroutineScope", "CoroutineContext", "Structured Concurrency"),
            quizScore = 90,
            quizScoreMax = 100
        )
        val lesson = Lesson(
            title = "Lesson 2: CoroutineScope",
            description = "Understanding CoroutineScope in Kotlin.",
            index = 2,
            content = listOf("CoroutineScope Basics", "CoroutineScope Hierarchy"),
            quizScore = 95,
            quizScoreMax = 100
        )

        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = title,
            description = "This section explores coroutine scopes, context, and best practices for managing them.",
            content = listOf("CoroutineScope", "CoroutineContext", "Structured Concurrency")
        )

        // Mock the generateContent method to return a successful GeneratedResponse
        coEvery {
            contentGeneratorClient.generateContent(any())
        } returns flowOf(Result.success(generatedResponse))

        // Act
        val result = generateSectionUseCase.invoke(title, profile, curriculum, module, lesson)

        // Assert
        // Verify that the result is successful and matches the mocked response
        assertTrue(result.isSuccess)
        assertEquals(generatedResponse, result.getOrNull())
    }

    /**
     * Tests that the use case returns a failure result when contentGeneratorClient emits a failure.
     */
    @Test
    fun `invoke should return failure result when generateContent fails`() = runTest {
        // Arrange
        val title = "Section 3: Coroutine Exception Handling"
        val profile = Profile(
            username = "expertUser",
            email = "expert@example.com",
            photoUrl = "https://example.com/photo3.jpg",
            preferences = Profile.LearningPreferences(
                field = "Health",
                level = "Advanced",
                goal = "Optimize Kotlin Applications"
            ),
            learningStyle = Profile.LearningStyle(
                dominant = "reading",
                breakdown = Profile.LearningStyleBreakdown(
                    reading = 85,
                    kinesthetic = 15
                )
            )
        )
        val curriculum = Curriculum(
            title = "Expert Kotlin Curriculum",
            description = "An expert-level curriculum for optimizing Kotlin applications.",
            content = listOf("Advanced Coroutines", "Performance Tuning"),
            status = "active"
        )
        val module = Module(
            title = "Module 3: Exception Handling in Coroutines",
            description = "Handling exceptions within coroutines effectively.",
            index = 3,
            content = listOf("Coroutine Exceptions", "SupervisorScope"),
            quizScore = 95,
            quizScoreMax = 100
        )
        val lesson = Lesson(
            title = "Lesson 3: Coroutine Exceptions",
            description = "Advanced techniques for managing exceptions in coroutines.",
            index = 3,
            content = listOf("CoroutineExceptionHandler", "SupervisorJob"),
            quizScore = 100,
            quizScoreMax = 100
        )

        val exception = Exception("Content generation failed due to server error.")

        // Mock the generateContent method to emit a failure
        coEvery {
            contentGeneratorClient.generateContent(any())
        } returns flowOf(Result.failure(exception))

        // Act
        val result = generateSectionUseCase.invoke(title, profile, curriculum, module, lesson)

        // Assert
        // Verify that the result is a failure and contains the expected exception
        assertTrue(result.isFailure)
        val thrownException = result.exceptionOrNull()
        assertIs<Exception>(thrownException)
        assertEquals("Content generation failed due to server error.", thrownException.message)
    }
}
