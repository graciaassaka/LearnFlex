package org.example.shared.domain.use_case.section

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteAllSectionsUseCaseTest {
    private lateinit var useCase: DeleteAllSectionsUseCase
    private lateinit var repository: SectionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = DeleteAllSectionsUseCase(repository)
    }

    @Test
    fun `deleteAll should return success when succeeds`() = runTest {
        val path = "test/path"
        val sections = listOf(mockk<Section>(relaxed = true))
        coEvery { repository.deleteAll(path, sections, any()) } returns Result.success(Unit)

        val result = useCase(path, sections)

        coVerify(exactly = 1) { repository.deleteAll(path, sections, any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `deleteAll should return failure when fails`() = runTest {
        val path = "test/path"
        val sections = listOf(mockk<Section>(relaxed = true))
        val exception = RuntimeException("Delete failed")
        coEvery { repository.deleteAll(path, sections, any()) } returns Result.failure(exception)

        val result = useCase(path, sections)

        coVerify(exactly = 1) { repository.deleteAll(path, sections, any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}