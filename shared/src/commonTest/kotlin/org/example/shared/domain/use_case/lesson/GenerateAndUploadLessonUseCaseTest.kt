package org.example.shared.domain.use_case.lesson

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateAndUploadLessonUseCaseTest {

    private lateinit var generateLesson: GenerateLessonUseCase
    private lateinit var uploadLessonUseCase: UploadLessonUseCase
    private lateinit var generateAndUploadLessonUseCase: GenerateAndUploadLessonUseCase

    @Before
    fun setUp() {
        generateLesson = mockk()
        uploadLessonUseCase = mockk()
        generateAndUploadLessonUseCase = GenerateAndUploadLessonUseCase(generateLesson, uploadLessonUseCase)
    }

    @Test
    fun `invoke returns success when generation and upload succeed`() = runTest {
        // Arrange
        val title = "Lesson Title"
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule

        // Create a dummy response for lesson generation
        val generatedLessonContent = listOf("Content line 1", "Content line 2")
        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = title,
            description = "A generated lesson description",
            content = generatedLessonContent
        )
        // Prepare a Lesson object expected after generation
        val expectedLesson = Lesson(
            title = title,
            description = generatedResponse.description,
            content = generatedResponse.content
        )

        // Mock generateLesson to return a flow emitting a successful result with generatedResponse
        coEvery { generateLesson(title, profile, curriculum, module) } returns flowOf(Result.success(generatedResponse))
        // Mock uploadLessonUseCase to return a successful result when called
        coEvery { uploadLessonUseCase(any(), profile.id, curriculum.id, module.id) } returns Result.success(Unit)

        // Act
        val result = generateAndUploadLessonUseCase(title, profile, curriculum, module)

        // Assert
        coVerify(exactly = 1) { generateLesson(title, profile, curriculum, module) }
        coVerify(exactly = 1) { uploadLessonUseCase(any(), profile.id, curriculum.id, module.id) }
        assertTrue(result.isSuccess)
        assertEquals(expectedLesson.title, result.getOrNull()?.title)
        assertEquals(expectedLesson.description, result.getOrNull()?.description)
        assertEquals(expectedLesson.content, result.getOrNull()?.content)
    }

    @Test
    fun `invoke returns failure when generation fails`() = runTest {
        // Arrange
        val title = "Lesson Title"
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule

        val generationException = Exception("Generation failed")
        coEvery { generateLesson(title, profile, curriculum, module) } returns flowOf(Result.failure(generationException))

        // Act
        val result = generateAndUploadLessonUseCase(title, profile, curriculum, module)

        // Assert
        coVerify(exactly = 1) { generateLesson(title, profile, curriculum, module) }
        // uploadLessonUseCase should not be called if generation fails
        coVerify(exactly = 0) { uploadLessonUseCase(any(), any(), any(), any()) }
        assertTrue(result.isFailure)
        assertEquals("Generation failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns failure when upload fails`() = runTest {
        // Arrange
        val title = "Lesson Title"
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule

        // Create a dummy successful generation response
        val generatedLessonContent = listOf("Content line 1", "Content line 2")
        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = title,
            description = "A generated lesson description",
            content = generatedLessonContent
        )
        Lesson(
            title = title,
            description = generatedResponse.description,
            content = generatedResponse.content
        )

        coEvery { generateLesson(title, profile, curriculum, module) } returns flowOf(Result.success(generatedResponse))

        val uploadException = Exception("Upload failed")
        coEvery { uploadLessonUseCase(any(), profile.id, curriculum.id, module.id) } returns Result.failure(uploadException)

        // Act
        val result = generateAndUploadLessonUseCase(title, profile, curriculum, module)

        // Assert
        coVerify(exactly = 1) { generateLesson(title, profile, curriculum, module) }
        coVerify(exactly = 1) { uploadLessonUseCase(any(), profile.id, curriculum.id, module.id) }
        assertTrue(result.isFailure)
        assertEquals("Upload failed", result.exceptionOrNull()?.message)
    }

    // Dummy objects for testing
    private val dummyProfile = Profile(
        username = "TestUser",
        email = "test@example.com",
        photoUrl = "https://example.com/photo.png",
        preferences = Profile.LearningPreferences(field = "COMPUTER_SCIENCE", level = "BEGINNER", goal = "Learn something")
    )
    private val dummyCurriculum = Curriculum(
        title = "Curriculum Title",
        description = "Curriculum Description",
        content = listOf("Topic 1", "Topic 2")
    )
    private val dummyModule = Module(
        title = "Module Title",
        description = "Module Description",
        content = listOf("Lesson 1", "Lesson 2")
    )
}
