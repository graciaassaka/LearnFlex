package org.example.shared.domain.use_case.curriculum

import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.client.SyllabusSummarizerClient
import org.example.shared.domain.model.Profile
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GenerateCurriculumFromFileUseCaseTest {

    private lateinit var syllabusSummarizerClient: SyllabusSummarizerClient
    private lateinit var generateCurriculumFromDescriptionUseCase: GenerateCurriculumFromDescriptionUseCase
    private lateinit var generateCurriculumFromFileUseCase: GenerateCurriculumFromFileUseCase

    @Before
    fun setUp() {
        syllabusSummarizerClient = mockk(relaxed = true)
        generateCurriculumFromDescriptionUseCase = mockk(relaxed = true)
        generateCurriculumFromFileUseCase = GenerateCurriculumFromFileUseCase(
            syllabusSummarizerClient,
            generateCurriculumFromDescriptionUseCase
        )
    }

    /**
     * Verifies that the use case calls summarizeSyllabus with the correct file
     * and then calls generateCurriculumFromDescriptionUseCase with the obtained description and profile.
     */
    @Test
    fun `invoke should call summarizeSyllabus and then generateCurriculumFromDescriptionUseCase with correct parameters`() = runTest {
        // Arrange
        val syllabusFile = File.createTempFile("syllabus", ".pdf")

        val profile = mockk<Profile>(relaxed = true)
        val description = "Summary of the syllabus"
        val generatedCurriculum = ContentGeneratorClient.GeneratedResponse(
            title = "Generated Curriculum",
            imagePrompt = "An illustration of Kotlin concurrency",
            description = "A short description",
            content = listOf("Module 1", "Module 2")
        )

        // Mock summarizeSyllabus to return a successful summary
        coEvery { syllabusSummarizerClient.summarizeSyllabus(syllabusFile) } returns flowOf(Result.success(description))

        // Mock generateCurriculumFromDescriptionUseCase to return a successful curriculum
        coEvery { generateCurriculumFromDescriptionUseCase.invoke(description, profile) } returns Result.success(generatedCurriculum)

        // Act
        val result = generateCurriculumFromFileUseCase.invoke(syllabusFile, profile)

        // Assert
        coVerify(exactly = 1) {
            syllabusSummarizerClient.summarizeSyllabus(syllabusFile)
            generateCurriculumFromDescriptionUseCase.invoke(description, profile)
        }

        // Verify that the result is successful and matches the generated curriculum
        assertTrue(result.isSuccess)
        assertEquals(generatedCurriculum, result.getOrNull())
    }

    /**
     * Tests that the use case returns a success result when both summarizeSyllabus
     * and generateCurriculumFromDescriptionUseCase succeed.
     */
    @Test
    fun `invoke should return success when summarizeSyllabus and generateCurriculumFromDescriptionUseCase succeed`() = runTest {
        // Arrange
        val syllabusFile = File.createTempFile("syllabus", ".pdf")

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
                    reading = 80,
                    kinesthetic = 20
                )
            )
        )

        val description = "Comprehensive syllabus on Kotlin programming."
        val generatedCurriculum = ContentGeneratorClient.GeneratedResponse(
            title = "Kotlin Programming Curriculum",
            imagePrompt = "A diagram of Kotlin coroutines",
            description = "An in-depth curriculum for Kotlin developers.",
            content = listOf("Introduction", "Advanced Topics", "Projects")
        )

        coEvery { syllabusSummarizerClient.summarizeSyllabus(syllabusFile) } returns flowOf(Result.success(description))
        coEvery { generateCurriculumFromDescriptionUseCase.invoke(description, profile) } returns Result.success(generatedCurriculum)

        // Act
        val result = generateCurriculumFromFileUseCase.invoke(syllabusFile, profile)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(generatedCurriculum, result.getOrNull())
    }

    /**
     * Tests that the use case throws an IllegalArgumentException when the file does not exist.
     */
    @Test
    fun `invoke should throw IllegalArgumentException when file does not exist`() = runTest {
        // Arrange
        val syllabusFile = mockk<File>(relaxed = true) {
            every { exists() } returns false
        }

        val profile = mockk<Profile>(relaxed = true)

        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            generateCurriculumFromFileUseCase.invoke(syllabusFile, profile).getOrThrow()
        }
        assertEquals("File does not exist", exception.message)

        // Verify that summarizeSyllabus and generateCurriculumFromDescriptionUseCase are never called
        coVerifyAll(inverse = true) {
            syllabusSummarizerClient.summarizeSyllabus(any())
            generateCurriculumFromDescriptionUseCase.invoke(any(), any())
        }
    }

    /**
     * Tests that the use case throws an IllegalArgumentException when the file has an unsupported extension.
     */
    @Test
    fun `invoke should throw IllegalArgumentException when file has unsupported extension`() = runTest {
        // Arrange
        val syllabusFile = File.createTempFile("syllabus", ".txt")

        val profile = mockk<Profile>(relaxed = true)

        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            generateCurriculumFromFileUseCase.invoke(syllabusFile, profile).getOrThrow()
        }
        assertEquals("File must be a PDF or DOCX", exception.message)

        // Verify that summarizeSyllabus and generateCurriculumFromDescriptionUseCase are never called
        coVerifyAll(inverse = true) {
            syllabusSummarizerClient.summarizeSyllabus(any())
            generateCurriculumFromDescriptionUseCase.invoke(any(), any())
        }
    }

    /**
     * Tests that the use case returns a failure result when summarizeSyllabus fails.
     */
    @Test
    fun `invoke should return failure when summarizeSyllabus fails`() = runTest {
        // Arrange
        val syllabusFile = File.createTempFile("syllabus", ".pdf")

        val profile = mockk<Profile>(relaxed = true)
        val exception = Exception("Summarization failed")

        coEvery { syllabusSummarizerClient.summarizeSyllabus(syllabusFile) } returns flowOf(Result.failure(exception))

        // Act
        val result = generateCurriculumFromFileUseCase.invoke(syllabusFile, profile)

        // Assert
        assertTrue(result.isFailure)
        val thrownException = result.exceptionOrNull()
        assertIs<Exception>(thrownException)
        assertEquals("Summarization failed", thrownException.message)

        // Verify that generateCurriculumFromDescriptionUseCase is never called
        coVerify(exactly = 1) { syllabusSummarizerClient.summarizeSyllabus(syllabusFile) }
        coVerify(exactly = 0) { generateCurriculumFromDescriptionUseCase.invoke(any(), any()) }
    }

    /**
     * Tests that the use case returns a failure result when generateCurriculumFromDescriptionUseCase fails.
     */
    @Test
    fun `invoke should return failure when generateCurriculumFromDescriptionUseCase fails`() = runTest {
        // Arrange
        val syllabusFile = File.createTempFile("syllabus", ".docx")

        val profile = Profile(
            username = "testUser",
            email = "test@example.com",
            photoUrl = "https://example.com/photo.jpg",
            preferences = Profile.LearningPreferences(
                field = "Engineering",
                level = "Intermediate",
                goal = "Enhance Kotlin skills"
            ),
            learningStyle = Profile.LearningStyle(
                dominant = "kinesthetic",
                breakdown = Profile.LearningStyleBreakdown(
                    reading = 30,
                    kinesthetic = 70
                )
            )
        )

        val description = "Intermediate syllabus on Kotlin for engineering applications."
        val exception = Exception("Curriculum generation failed")

        coEvery { syllabusSummarizerClient.summarizeSyllabus(syllabusFile) } returns flowOf(Result.success(description))
        coEvery { generateCurriculumFromDescriptionUseCase.invoke(description, profile) } returns Result.failure(exception)

        // Act
        val result = generateCurriculumFromFileUseCase.invoke(syllabusFile, profile)

        // Assert
        assertTrue(result.isFailure)
        val thrownException = result.exceptionOrNull()
        assertIs<Exception>(thrownException)
        assertEquals("Curriculum generation failed", thrownException.message)

        // Verify interactions
        coVerifyOrder {
            syllabusSummarizerClient.summarizeSyllabus(syllabusFile)
            generateCurriculumFromDescriptionUseCase.invoke(description, profile)
        }
    }
}
