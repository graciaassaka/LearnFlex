package org.example.composeApp.screen

import android.content.Context
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.ui.screen.DashboardScreen
import org.example.composeApp.ui.theme.LearnFlexTheme
import org.example.composeApp.ui.util.TestTags
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.presentation.action.DashboardAction
import org.example.shared.presentation.state.DashboardUIState
import org.example.shared.presentation.util.UIEvent
import org.example.shared.presentation.viewModel.DashboardViewModel
import org.jetbrains.compose.resources.stringResource
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.DayOfWeek

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var viewModel: DashboardViewModel
    private lateinit var uiState: MutableStateFlow<DashboardUIState>
    private lateinit var uiEventFlow: MutableSharedFlow<UIEvent>
    private lateinit var isScreenVisible: MutableStateFlow<Boolean>
    private lateinit var windowSizeClass: WindowSizeClass
    private lateinit var context: Context
    private lateinit var stringMap: Map<String, String>

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        navController = TestNavHostController(context)
        viewModel = mockk(relaxed = true)
        uiState = MutableStateFlow(DashboardUIState())
        uiEventFlow = MutableSharedFlow()
        isScreenVisible = MutableStateFlow(true)
        windowSizeClass = WindowSizeClass.calculateFromSize(
            DpSize(1344.dp, 2992.dp),
            setOf(WindowWidthSizeClass.Compact),
            setOf(WindowHeightSizeClass.Compact)
        )

        every { viewModel.state } returns uiState
        every { viewModel.uiEvent } returns uiEventFlow
        every { viewModel.isScreenVisible } returns isScreenVisible

        composeTestRule.setContent {
            stringMap = mapOf(
                "welcome_to" to stringResource(Res.string.welcome_to),
                "app_name" to stringResource(Res.string.app_name),
                "hi_there" to stringResource(Res.string.hi_there),
                "welcome_message" to stringResource(Res.string.welcome_message),
                "best_quiz_score" to stringResource(Res.string.best_quiz_score),
            )
            LearnFlexTheme {
                DashboardScreen(
                    navController = navController,
                    viewModel = viewModel,
                    windowSizeClass = windowSizeClass
                )
            }
        }
    }

    @Test
    fun dashboardScreen_shouldDisplayCorrectly() {
        composeTestRule.onNodeWithTag(TestTags.DASHBOARD_WELCOME_SECTION.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.DASHBOARD_WEEKLY_ACTIVITY_SECTION.tag).assertIsDisplayed()
    }

    @Test
    fun welcomeSection_withoutActiveCurriculum_shouldDisplayCorrectly() {
        uiState.value = DashboardUIState(activeCurriculum = null)

        composeTestRule.onNodeWithTag(TestTags.DASHBOARD_WELCOME_SECTION.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.DASHBOARD_TITLE_SECTION.tag).apply {
            assertIsDisplayed()
            assert(hasAnyChild(hasTextExactly(stringMap["welcome_to"]!!)))
            assert(hasAnyChild(hasTextExactly(stringMap["app_name"]!!)))
        }
        composeTestRule.onNodeWithTag(TestTags.DASHBOARD_WELCOME_CARD.tag, useUnmergedTree = true).apply {
            assertIsDisplayed()
            assertHasClickAction()
            onChildAt(0).assert(hasText(stringMap["hi_there"]!!))
            onChildAt(1).assert(hasText(stringMap["welcome_message"]!!))
        }
    }

    @Test
    fun welcomeSection_withActiveCurriculum_shouldDisplayCorrectly() {
        val activeCurriculum = mockk<Curriculum>()

        every { activeCurriculum.title } returns "Some title"
        every { activeCurriculum.description } returns "Some description"

        uiState.value = DashboardUIState(activeCurriculum = activeCurriculum)

        composeTestRule.onNodeWithTag(TestTags.DASHBOARD_WELCOME_SECTION.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.DASHBOARD_TITLE_SECTION.tag).apply {
            assertIsDisplayed()
            assert(hasAnyChild(hasTextExactly(stringMap["welcome_to"]!!)))
            assert(hasAnyChild(hasTextExactly(stringMap["app_name"]!!)))
        }
        composeTestRule.onNodeWithTag(TestTags.DASHBOARD_WELCOME_CARD.tag, useUnmergedTree = true).apply {
            assertIsDisplayed()
            assertHasClickAction()
            onChildAt(0).assert(hasText(activeCurriculum.title))
            onChildAt(1).assert(hasText(activeCurriculum.description))
        }
    }

    @Test
    fun welcomeCard_shouldCall_viewModel_onCurriculumClicked_whenClicked() {
        val curriculumId = "curriculum_id"
        val activeCurriculum = mockk<Curriculum>()
        uiState.value = DashboardUIState(activeCurriculum = activeCurriculum)

        every { activeCurriculum.id } returns curriculumId
        every { activeCurriculum.title } returns "Some title"
        every { activeCurriculum.description } returns "Some description"

        composeTestRule.onNodeWithTag(TestTags.DASHBOARD_WELCOME_CARD.tag).performClick()

        verify(exactly = 1) {
            viewModel.handleAction(
                match {
                    it is DashboardAction.OpenCurriculum
                }
            )
        }
    }

    @Test
    fun weeklyActivitySection_shouldDisplayCorrectly() {
        val weeklyActivity = mapOf(
            DayOfWeek.MONDAY to Pair(1L, 1),
            DayOfWeek.TUESDAY to Pair(2L, 2),
            DayOfWeek.WEDNESDAY to Pair(3L, 3),
            DayOfWeek.THURSDAY to Pair(4L, 4),
            DayOfWeek.FRIDAY to Pair(5L, 5),
            DayOfWeek.SATURDAY to Pair(6L, 6),
            DayOfWeek.SUNDAY to Pair(7L, 7)
        )

        uiState.value = DashboardUIState(weeklyActivity = weeklyActivity)

        composeTestRule.onNodeWithTag(TestTags.DASHBOARD_WEEKLY_ACTIVITY_SECTION.tag).apply {
            assertIsDisplayed()
            weeklyActivity.forEach { (day, _) ->
                assert(hasAnyDescendant(hasText(day.name.take(3))))
            }
        }
    }

    @Test
    fun moduleCard_shouldDisplayCorrectly() {
        val moduleId = "module_id"
        val moduleTitle = "Module Title"
        val moduleScore = 8

        val module = mockk<Module>()

        every { module.id } returns moduleId
        every { module.title } returns moduleTitle
        every { module.quizScore } returns moduleScore

        uiState.value = DashboardUIState(modules = listOf(module))

        composeTestRule.onNodeWithTag("module_card_${moduleId}", true).apply {
            assertIsDisplayed()
            onChild().apply {
                onChildAt(0).assert(hasText(moduleTitle))
                onChildAt(1).assert(hasText("${stringMap["best_quiz_score"]!!} $moduleScore"))
            }
        }
    }

    @Test
    fun moduleCard_shouldCall_viewModel_onModuleClicked_whenClicked() {
        val moduleId = "module_id"
        val moduleTitle = "Module Title"
        val moduleScore = 8

        val module = mockk<Module>()

        every { module.id } returns moduleId
        every { module.title } returns moduleTitle
        every { module.quizScore } returns moduleScore

        uiState.value = DashboardUIState(modules = listOf(module))

        composeTestRule.onNodeWithTag("module_card_${moduleId}", true).performClick()

        verify(exactly = 1) { viewModel.handleAction(DashboardAction.OpenModule(moduleId)) }
    }
}
