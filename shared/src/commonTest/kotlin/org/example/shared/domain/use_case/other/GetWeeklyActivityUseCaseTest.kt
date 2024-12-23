package org.example.shared.domain.use_case.other

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Session
import org.example.shared.domain.use_case.session.GetSessionsByDateRangeUseCase
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetWeeklyActivityUseCaseTest {
    private lateinit var getWeeklyActivityUseCase: GetWeeklyActivityUseCase
    private lateinit var getSessionsByDateRangeUseCase: GetSessionsByDateRangeUseCase

    @Before
    fun setUp() {
        getSessionsByDateRangeUseCase = mockk<GetSessionsByDateRangeUseCase>(relaxed = true)
        getWeeklyActivityUseCase = GetWeeklyActivityUseCase(getSessionsByDateRangeUseCase)
    }

    @Test
    fun `invoke should return correct activity map on successful retrieval`() = runTest {
        // Given
        val timestamp = Instant.parse("2024-12-24T10:15:30Z").toEpochMilli()
        val startTimestamp = Instant.parse("2024-12-17T10:15:30Z").toEpochMilli()

        coEvery { getSessionsByDateRangeUseCase(startTimestamp, timestamp) } returns Result.success(sessions)

        val expected = mapOf(
            Instant.parse("2024-12-17T10:15:30Z").atZone(ZoneId.systemDefault()).dayOfWeek to Pair(
                (Instant.parse("2024-12-23T10:15:30Z").toEpochMilli() - Instant.parse("2024-12-17T10:15:30Z")
                    .toEpochMilli()) / 60000, 1
            ),
            Instant.parse("2024-12-18T09:30:00Z").atZone(ZoneId.systemDefault()).dayOfWeek to Pair(
                (Instant.parse("2024-12-18T11:15:30Z").toEpochMilli() - Instant.parse("2024-12-18T09:30:00Z")
                    .toEpochMilli()) / 60000, 1
            ),
            Instant.parse("2024-12-20T13:00:00Z").atZone(ZoneId.systemDefault()).dayOfWeek to Pair(
                (Instant.parse("2024-12-20T14:45:00Z").toEpochMilli() - Instant.parse("2024-12-20T13:00:00Z")
                    .toEpochMilli()) / 60000, 1
            )
        )

        // When
        val result = getWeeklyActivityUseCase(timestamp)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `invoke should return failure when getSessionByDateRangeUseCase fails`() = runTest {
        // Given
        val timestamp = Instant.parse("2024-12-24T10:15:30Z").toEpochMilli()
        val startTimestamp = Instant.parse("2024-12-17T10:15:30Z").toEpochMilli()
        val exception = RuntimeException("Failed to get sessions")

        coEvery { getSessionsByDateRangeUseCase(startTimestamp, timestamp) } returns Result.failure(exception)

        // When
        val result = getWeeklyActivityUseCase(timestamp)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    companion object {
        val sessions = listOf(
            Session(
                id = "id1",
                endTime = Instant.parse("2024-12-23T10:15:30Z").toEpochMilli(),
                createdAt = Instant.parse("2024-12-17T10:15:30Z").toEpochMilli(),
                lastUpdated = Instant.parse("2024-12-23T10:15:30Z").toEpochMilli()
            ),
            Session(
                id = "id2",
                endTime = Instant.parse("2024-12-18T11:15:30Z").toEpochMilli(),
                createdAt = Instant.parse("2024-12-18T09:30:00Z").toEpochMilli(),
                lastUpdated = Instant.parse("2024-12-18T11:15:30Z").toEpochMilli()
            ),
            Session(
                id = "id3",
                endTime = Instant.parse("2024-12-20T14:45:00Z").toEpochMilli(),
                createdAt = Instant.parse("2024-12-20T13:00:00Z").toEpochMilli(),
                lastUpdated = Instant.parse("2024-12-20T14:45:00Z").toEpochMilli()
            )
        )
    }
}