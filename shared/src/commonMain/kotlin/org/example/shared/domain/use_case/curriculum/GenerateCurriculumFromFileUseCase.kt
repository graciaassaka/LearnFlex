package org.example.shared.domain.use_case.curriculum

import kotlinx.coroutines.flow.first
import org.example.shared.domain.client.SyllabusSummarizerClient
import org.example.shared.domain.model.Profile
import java.io.File

/**
 * Use case for generating a curriculum from a file.
 *
 * @property syllabusSummarizerClient Client to summarize the syllabus from the file.
 * @property generateCurriculumFromDescriptionUseCase Use case to generate curriculum from the description.
 */
class GenerateCurriculumFromFileUseCase(
    private val syllabusSummarizerClient: SyllabusSummarizerClient,
    private val generateCurriculumFromDescriptionUseCase: GenerateCurriculumFromDescriptionUseCase
) {
    /**
     * Invokes the use case to generate a curriculum from the given file and profile.
     *
     * @param file The file containing the syllabus (must be a PDF or DOCX).
     * @param profile The profile for which the curriculum is generated.
     * @return The result of the curriculum generation.
     * @throws IllegalArgumentException if the file does not exist or is not a PDF or DOCX.
     */
    suspend operator fun invoke(file: File, profile: Profile) = runCatching {
        require(file.exists()) { "File does not exist" }
        require(file.extension in setOf("pdf", "docx")) { "File must be a PDF or DOCX" }

        syllabusSummarizerClient.summarizeSyllabus(file).first().getOrThrow().let { description ->
            generateCurriculumFromDescriptionUseCase(description, profile).getOrThrow()
        }
    }
}