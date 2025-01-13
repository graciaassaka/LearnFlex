package org.example.shared.domain.model.interfaces

/**
 * Represents a record in the database.
 */
interface ScorableRecord {
    val quizScore: Int
    val quizScoreMax: Int

    fun isCompleted() = quizScore >= (0.8 * quizScoreMax).toInt()
}