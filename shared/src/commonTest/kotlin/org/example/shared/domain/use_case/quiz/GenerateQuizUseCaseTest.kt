package org.example.shared.domain.use_case.quiz

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.QuestionGeneratorClient
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Question
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GenerateQuizUseCaseTest {

    private lateinit var multipleChoiceClient: QuestionGeneratorClient<Question.MultipleChoice>
    private lateinit var trueFalseClient: QuestionGeneratorClient<Question.TrueFalse>
    private lateinit var orderingClient: QuestionGeneratorClient<Question.Ordering>
    private lateinit var generateQuizUseCase: GenerateQuizUseCase
    private lateinit var random: Random

    @BeforeTest
    fun setUp() {
        multipleChoiceClient = mockk()
        trueFalseClient = mockk()
        orderingClient = mockk()
        random = mockk()
        generateQuizUseCase = GenerateQuizUseCase(
            multipleChoiceClient,
            trueFalseClient,
            orderingClient,
            random
        )
    }

    @Test
    fun `invoke should distribute questions and merge flows`() = runTest {
        // Suppose for total=5:
        // multipleChoiceCount = 2, trueFalseCount = 2, orderingCount = 1
        every { random.nextInt(0, 6) } returns 2  // for multipleChoiceCount
        every { random.nextInt(0, 4) } returns 2  // for trueFalseCount

        // Mock flows
        val mcQuestionFlow = flowOf(
            Result.success(
                Question.MultipleChoice(
                    text = "MCQ 1",
                    correctAnswer = "Option A",
                    options = listOf(
                        Question.MultipleChoice.Option("A", "Option A"),
                        Question.MultipleChoice.Option("B", "Option B")
                    )
                )
            ),
            Result.success(
                Question.MultipleChoice(
                    text = "MCQ 2",
                    correctAnswer = "Option B",
                    options = listOf(
                        Question.MultipleChoice.Option("A", "Option A"),
                        Question.MultipleChoice.Option("B", "Option B")
                    )
                )
            )
        )

        val tfQuestionFlow = flowOf(
            Result.success(Question.TrueFalse("TFQ 1", true)),
            Result.success(Question.TrueFalse("TFQ 2", false))
        )

        val orderingQuestionFlow = flowOf(
            Result.success(
                Question.Ordering(
                    text = "OQ 1",
                    correctAnswer = listOf("first", "second")
                )
            )
        )

        // Stub generateQuestion
        every {
            multipleChoiceClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"),
                2
            )
        } returns mcQuestionFlow

        every {
            trueFalseClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"),
                2
            )
        } returns tfQuestionFlow

        every {
            orderingClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"),
                1
            )
        } returns orderingQuestionFlow

        // Execute
        val resultFlow: Flow<Result<Question>> = generateQuizUseCase.invoke(level = Level.BEGINNER, topic = "kotlin", number = 5)

        // Collect results
        val results = resultFlow.toList()

        // Validate
        assertEquals(5, results.size)
        // Check that distribution occurred as expected
        val mcResults = results.filter { it.getOrNull() is Question.MultipleChoice }
        val tfResults = results.filter { it.getOrNull() is Question.TrueFalse }
        val orderingResults = results.filter { it.getOrNull() is Question.Ordering }

        assertEquals(2, mcResults.size)
        assertEquals(2, tfResults.size)
        assertEquals(1, orderingResults.size)

        // Confirm mocks
        verify(exactly = 1) {
            multipleChoiceClient.generateQuestion(
                withArg {
                    assertEquals(Level.BEGINNER, it.level)
                    assertEquals("kotlin", it.topic)
                },
                2
            )
        }
        verify(exactly = 1) {
            trueFalseClient.generateQuestion(
                withArg {
                    assertEquals(Level.BEGINNER, it.level)
                    assertEquals("kotlin", it.topic)
                },
                2
            )
        }
        verify(exactly = 1) {
            orderingClient.generateQuestion(
                withArg {
                    assertEquals(Level.BEGINNER, it.level)
                    assertEquals("kotlin", it.topic)
                },
                1
            )
        }
    }

    @Test
    fun `invoke should emit failure if multipleChoice generator fails`() = runTest {
        // Suppose for total=5:
        // multipleChoiceCount = 2, trueFalseCount = 2, orderingCount = 1
        every { random.nextInt(0, 6) } returns 2  // for multipleChoiceCount
        every { random.nextInt(0, 4) } returns 2  // for trueFalseCount

        // Mock flows
        val mcFailureFlow = flowOf(
            // Just one item in the flow, a failure
            Result.failure<Question.MultipleChoice>(RuntimeException("MC Generator failed!"))
        )
        val tfFlow = flowOf(
            Result.success(Question.TrueFalse("TFQ 1", true)),
            Result.success(Question.TrueFalse("TFQ 2", false))
        )
        val orderingFlow = flowOf(
            Result.success(Question.Ordering("OQ 1", listOf("first", "second")))
        )

        // Stub generateQuestion
        every {
            multipleChoiceClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"),
                2
            )
        } returns mcFailureFlow

        every {
            trueFalseClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"),
                2
            )
        } returns tfFlow

        every {
            orderingClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"),
                1
            )
        } returns orderingFlow

        // Execute
        val resultFlow: Flow<Result<Question>> =
            generateQuizUseCase.invoke(level = Level.BEGINNER, topic = "kotlin", number = 5)

        // Collect results
        val results = resultFlow.toList()

        // We expect only 1 MC item (which is a failure), 2 TF, 1 Ordering = 4 total
        // Because MC flow is only emitting 1 item (a failure)
        assertEquals(4, results.size)

        val failures = results.filter { it.isFailure }
        val successes = results.filter { it.isSuccess }

        // We expect exactly 1 failure from MC
        assertEquals(1, failures.size)
        assertEquals(3, successes.size)

        // Optionally, confirm the message inside the failure
        val exception = failures.first().exceptionOrNull()
        assertEquals("MC Generator failed!", exception?.message)
    }

    @Test
    fun `invoke should emit failure if trueFalse generator fails`() = runTest {
        every { random.nextInt(0, 6) } returns 2  // multipleChoiceCount = 2
        every { random.nextInt(0, 4) } returns 2  // trueFalseCount = 2

        // Mock flows
        val mcFlow = flowOf(
            Result.success(
                Question.MultipleChoice(
                    text = "MCQ 1",
                    correctAnswer = "Option A",
                    options = listOf(Question.MultipleChoice.Option("A", "Option A"))
                ),
            ),
            Result.success(
                Question.MultipleChoice(
                    text = "MCQ 2",
                    correctAnswer = "Option A",
                    options = listOf(Question.MultipleChoice.Option("A", "Option A"))
                )
            )
        )
        val tfFailureFlow = flowOf(
            // One item in the flow, a failure
            Result.failure<Question.TrueFalse>(RuntimeException("TF Generator failed!"))
        )
        val orderingFlow = flowOf(
            Result.success(Question.Ordering("OQ 1", listOf("first", "second")))
        )

        every {
            multipleChoiceClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"), 2
            )
        } returns mcFlow

        every {
            trueFalseClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"), 2
            )
        } returns tfFailureFlow

        every {
            orderingClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"), 1
            )
        } returns orderingFlow

        val resultFlow = generateQuizUseCase(level = Level.BEGINNER, topic = "kotlin", number = 5)
        val results = resultFlow.toList()

        // This time MC emits 2 successes, TF emits 1 failure, Ordering 1 success
        // => total of 4 items
        assertEquals(4, results.size)

        val failures = results.filter { it.isFailure }
        val successes = results.filter { it.isSuccess }
        assertEquals(1, failures.size)
        assertEquals(3, successes.size)

        val exception = failures.first().exceptionOrNull()
        assertEquals("TF Generator failed!", exception?.message)
    }

    @Test
    fun `invoke should emit failure if ordering generator fails`() = runTest {
        every { random.nextInt(0, 6) } returns 2  // multipleChoiceCount = 2
        every { random.nextInt(0, 4) } returns 2  // trueFalseCount = 2

        // Mock flows
        val mcFlow = flowOf(
            Result.success(
                Question.MultipleChoice(
                    text = "MCQ 1",
                    correctAnswer = "Option A",
                    options = listOf(Question.MultipleChoice.Option("A", "Option A"))
                ),
            ),
            Result.success(
                Question.MultipleChoice(
                    text = "MCQ 2",
                    correctAnswer = "Option A",
                    options = listOf(Question.MultipleChoice.Option("A", "Option A"))
                )
            )
        )
        val tfFlow = flowOf(
            Result.success(Question.TrueFalse("TFQ 1", true)),
            Result.success(Question.TrueFalse("TFQ 2", false))
        )
        val orderingFailureFlow = flowOf(
            // Single item in the flow, a failure
            Result.failure<Question.Ordering>(RuntimeException("Ordering Generator failed!"))
        )

        every {
            multipleChoiceClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"), 2
            )
        } returns mcFlow

        every {
            trueFalseClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"), 2
            )
        } returns tfFlow

        every {
            orderingClient.generateQuestion(
                QuestionGeneratorClient.Context(level = Level.BEGINNER, topic = "kotlin"), 1
            )
        } returns orderingFailureFlow

        val resultFlow = generateQuizUseCase(level = Level.BEGINNER, topic = "kotlin", number = 5)
        val results = resultFlow.toList()

        // 2 MC successes, 2 TF successes, 1 Ordering failure => 5 items total
        // BUT notice we only have 1 ordering item, so total = 2 + 2 + 1 = 5
        assertEquals(5, results.size)

        val failures = results.filter { it.isFailure }
        val successes = results.filter { it.isSuccess }
        // We expect 1 failure, 4 successes
        assertEquals(1, failures.size)
        assertEquals(4, successes.size)

        val exception = failures.first().exceptionOrNull()
        assertEquals("Ordering Generator failed!", exception?.message)
    }

}
