package org.example.shared.domain.use_case.curriculum

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.constant.ContentType
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GenerateCurriculumUseCaseTest {

    private lateinit var generateCurriculumUseCase: GenerateCurriculumUseCase
    private lateinit var contentGeneratorClient: ContentGeneratorClient

    @Before
    fun setUp() {
        contentGeneratorClient = mockk(relaxed = true)
        generateCurriculumUseCase = GenerateCurriculumUseCase(contentGeneratorClient)
    }

    /**
     * Verifies that the use case calls the client's generateContent method with the expected context.
     */
    @Test
    fun `use case should call generateContent with correct context`() = runTest {
        // Arrange
        val syllabusDescription = "An advanced Kotlin syllabus covering coroutines and flows."
        val profile = Profile(
            username = "testUser",
            email = "test@example.com",
            photoUrl = "",
            preferences = Profile.LearningPreferences(
                field = Field.COMPUTER_SCIENCE,
                level = Level.BEGINNER,
                goal = "Become a Kotlin expert"
            ),
            learningStyle = Profile.LearningStyle(
                dominant = Style.READING.name,
                breakdown = Profile.LearningStyleBreakdown(
                    reading = 60,
                    kinesthetic = 40
                )
            )
        )

        // Mock the client to return a flow of generated content
        coEvery {
            contentGeneratorClient.generateContent(any())
        } returns flowOf(Result.success(mockk()))

        // Act
        generateCurriculumUseCase(syllabusDescription, profile).first()

        // Assert
        coVerify(exactly = 1) {
            contentGeneratorClient.generateContent(
                match {
                    it.field.name == profile.preferences.field &&
                            it.level.name == profile.preferences.level &&
                            it.style == profile.learningStyle &&
                            it.type == ContentType.CURRICULUM &&
                            it.contentDescriptors.size == 1 &&
                            it.contentDescriptors.first().type == ContentType.SYLLABUS &&
                            it.contentDescriptors.first().title == "No title provided" &&
                            it.contentDescriptors.first().description == syllabusDescription
                }
            )
        }
    }

    /**
     * Tests that the use case returns a success result when contentGeneratorClient returns a success flow.
     */
    @Test
    fun `use case should return success result when generateContent succeeds`() = runTest {
        // Arrange
        val syllabusDescription = "Kotlin basics syllabus."
        val profile = Profile(
            username = "testUser",
            email = "test@example.com",
            photoUrl = "",
            preferences = Profile.LearningPreferences(
                field = Field.COMPUTER_SCIENCE.name,
                level = Level.BEGINNER.name,
                goal = "Learn Kotlin"
            ),
            learningStyle = Profile.LearningStyle(
                dominant = Style.READING.name,
                breakdown = Profile.LearningStyleBreakdown(
                    reading = 80,
                    kinesthetic = 20
                )
            )
        )

        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = "Generated Curriculum",
            description = "A short description",
            content = listOf("Module 1", "Module 2")
        )

        // Mock the flow returned by the client
        coEvery {
            contentGeneratorClient.generateContent(any())
        } returns flowOf(Result.success(generatedResponse))

        // Act
        val result = generateCurriculumUseCase(syllabusDescription, profile).first()

        // Assert
        // runCatching { ... } in the use case returns a Result<GeneratedResponse>
        assertTrue(result.isSuccess)
        assertEquals(generatedResponse, result.getOrNull())
    }

    /**
     * Tests that the use case returns a failure result when contentGeneratorClient returns a failure flow.
     */
    @Test
    fun `use case should return failure result when generateContent fails`() = runTest {
        // Arrange
        val syllabusDescription = "A difficult syllabus"
        val profile = Profile(
            username = "testUser",
            email = "test@example.com",
            photoUrl = "",
            preferences = Profile.LearningPreferences(
                field = Field.COMPUTER_SCIENCE.name,
                level = Level.ADVANCED.name,
                goal = "Master Kotlin internals"
            ),
            learningStyle = Profile.LearningStyle(
                dominant = Style.KINESTHETIC.name,
                breakdown = Profile.LearningStyleBreakdown(
                    reading = 20,
                    kinesthetic = 80
                )
            )
        )

        val exception = Exception("Generation failed")
        // Mock the flow to emit a failure
        coEvery {
            contentGeneratorClient.generateContent(any())
        } returns flowOf(Result.failure(exception))

        // Act
        val result = generateCurriculumUseCase(syllabusDescription, profile).first()

        // Assert
        assertTrue(result.isFailure)
        val thrownException = result.exceptionOrNull()
        assertIs<Exception>(thrownException)
        assertEquals("Generation failed", thrownException.message)
    }
}
