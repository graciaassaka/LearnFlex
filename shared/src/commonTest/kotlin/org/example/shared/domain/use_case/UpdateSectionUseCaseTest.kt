package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateSectionUseCaseTest {
    private lateinit var useCase: UpdateSectionUseCase
    private lateinit var repository: SectionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UpdateSectionUseCase(repository)
    }

    @Test
    fun `update should return success when succeeds`() = runTest {
        val path = "test/path"
        val section = mockk<Section>(relaxed = true)
        coEvery { repository.update(path, section) } returns Result.success(Unit)

        val result = useCase(path, section)

        coVerify(exactly = 1) { repository.update(path, section) }
        assert(result.isSuccess)
    }

    @Test
    fun `update should return failure when fails`() = runTest {
        val path = "test/path"
        val section = mockk<Section>(relaxed = true)
        val exception = RuntimeException("Update failed")
        coEvery { repository.update(path, section) } returns Result.failure(exception)

        val result = useCase(path, section)

        coVerify(exactly = 1) { repository.update(path, section) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}