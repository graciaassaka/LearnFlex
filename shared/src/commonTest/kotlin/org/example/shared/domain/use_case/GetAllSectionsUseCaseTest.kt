package org.example.shared.domain.use_case

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAllSectionsUseCaseTest {
    private lateinit var useCase: GetAllSectionsUseCase
    private lateinit var repository: SectionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetAllSectionsUseCase(repository)
    }

    @Test
    fun `getAll should return sections flow when succeeds`() {
        val path = "test/path"
        val sections = listOf(mockk<Section>(), mockk())
        val sectionsFlow = flowOf(Result.success(sections))
        every { repository.getAll(path) } returns sectionsFlow

        val result = useCase(path)

        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(sectionsFlow, result)
    }

    @Test
    fun `getAll should return error flow when fails`() {
        val path = "test/path"
        val exception = RuntimeException("Failed to get sections")
        val errorFlow = flowOf(Result.failure<List<Section>>(exception))
        every { repository.getAll(path) } returns errorFlow

        val result = useCase(path)

        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(errorFlow, result)
    }
}