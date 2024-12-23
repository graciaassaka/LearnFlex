package org.example.shared.domain.model.definition

/**
 * Represents a record in the database.
 */
interface ScoreQueryable {
    val quizScore: Int
    val quizScoreMax: Int
}