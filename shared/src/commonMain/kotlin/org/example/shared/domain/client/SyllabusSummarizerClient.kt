package org.example.shared.domain.client

import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Interface for a client that summarizes syllabuses.
 */
interface SyllabusSummarizerClient {
    /**
     * Summarizes the given syllabus.
     *
     * @param syllabus The syllabus file to be summarized.
     * @return A Flow emitting the result of the summarization, which is a string.
     */
    fun summarizeSyllabus(syllabus: File): Flow<Result<String>>
}