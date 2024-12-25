package org.example.shared.domain.model.interfaces

/**
 * Represents a record in the database.
 */
interface ScoreQueryable {
    val quizScore: Int
    val quizScoreMax: Int
}