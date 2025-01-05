package org.example.shared.domain.use_case.syllabus

import kotlinx.coroutines.flow.flow
import org.example.shared.domain.client.SyllabusSummarizerClient
import java.io.File

/**
 * Use case for summarizing a syllabus from a given file.
 *
 * @property syllabusSummarizerClient The client responsible for summarizing the syllabus.
 */
class SummarizeSyllabusUseCase(
    private val syllabusSummarizerClient: SyllabusSummarizerClient
) {

    /**
     * Invokes the use case to summarize the syllabus from the given file.
     *
     * @param file The file to be summarized. Must be a PDF or DOCX file.
     * @return The result of the syllabus summarization.
     */
    operator fun invoke(file: File) = flow {
        require(file.exists()) { "File does not exist" }
        require(file.extension in setOf("pdf", "docx")) { "File must be a PDF or DOCX" }

        syllabusSummarizerClient.summarizeSyllabus(file).collect(::emit)
    }
}