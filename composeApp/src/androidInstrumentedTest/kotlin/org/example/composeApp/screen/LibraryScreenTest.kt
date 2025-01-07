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
import io.mockk.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.presentation.ui.screen.LibraryScreen
import org.example.composeApp.presentation.ui.theme.LearnFlexTheme
import org.example.composeApp.presentation.ui.constant.TestTags
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.composeApp.presentation.action.LibraryAction
import org.example.composeApp.presentation.state.LibraryUIState
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.composeApp.presentation.ui.util.UIEvent
import org.example.composeApp.presentation.viewModel.LibraryViewModel
import org.jetbrains.compose.resources.stringResource
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var viewModel: LibraryViewModel
    private lateinit var uiState: MutableStateFlow<LibraryUIState>
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
        uiState = MutableStateFlow(LibraryUIState())
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
                "upload_document" to stringResource(Res.string.upload_document),
                "syllabus_description_label" to stringResource(Res.string.syllabus_description_label),
                "generate_content_button_label" to stringResource(Res.string.generate_content_button_label),
                "save_content_button_label" to stringResource(Res.string.save_content_button_label),
                "discard_content_button_label" to stringResource(Res.string.discard_content_button_label),
                "remove_module_button_label" to stringResource(Res.string.remove_module_button_label),
                "remove_lesson_button_label" to stringResource(Res.string.remove_lesson_button_label),
                "content_saved_success" to stringResource(Res.string.save_content_success),
                "cancel_button_label" to stringResource(Res.string.cancel_button_label),
            )
            LearnFlexTheme(darkTheme = true) {
                LibraryScreen(
                    navController = navController,
                    viewModel = viewModel,
                    windowSizeClass = windowSizeClass
                )
            }
        }
    }

    @Test
    fun mainPane_shouldDisplayCorrectly() {
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL.tag).apply {
            assertIsDisplayed()
            hasAnyChild(hasTestTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag))
            hasAnyChild(hasTestTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_SUPPORTING_PANE_BUTTON.tag))
        }
    }

    @Test
    fun generatedContentSection_shouldDisplayCorrectly() {
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag).apply {
            assertIsDisplayed()
            printToLog("Generated content section displayed")
            onChildAt(0).apply {
                assertTextEquals(stringMap["upload_document"]!!)
                assertHasClickAction()
            }
            onChildAt(1).apply {
                assertTextContains(stringMap["syllabus_description_label"]!!)
                assert(hasSetTextAction())
            }
            onChildAt(2).apply {
                assertTextEquals(stringMap["generate_content_button_label"]!!)
                assertHasClickAction()
            }
            onChildAt(3).apply {
                assertTextEquals(stringMap["save_content_button_label"]!!)
                assertHasClickAction()
            }
        }
    }

    @Test
    fun generatedContentSection_syllabusDescriptionTextField_shouldUpdateCorrectly() {
        val description = "Some description"
        every { viewModel.handleAction(LibraryAction.EditSyllabusDescription(description)) } answers {
            uiState.update { it.copy(syllabusDescription = description) }
        }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag).onChildAt(1).apply {
            performTextInput(description)
            assertTextContains(description)
        }
        verify {
            viewModel.handleAction(LibraryAction.EditSyllabusDescription(description))
        }
    }

    @Test
    fun generatedContentSection_disablesAllButtons_when_isDownloading_and_isUploading() {
        val curriculum = Curriculum(
            title = "Default Title",
            description = "This is a generated curriculum description.",
            content = listOf("module 1")
        )
        val modules = listOf(
            Module(
                title = "module 1",
                description = "Module description",
                content = listOf("lesson 1")
            )
        )
        uiState.update {
            it.copy(
                curriculum = curriculum,
                modules = modules,
                isDownloading = true,
                isUploading = true
            )
        }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag).apply {
            onChildren().filter(hasClickAction()).assertAll(isNotEnabled())
        }
    }

    @Test
    fun generateCurriculumButton_shouldCall_viewModel_generateCurriculum_whenClicked() {
        val description = "Some description"
        every { viewModel.handleAction(LibraryAction.EditSyllabusDescription(description)) } answers {
            uiState.update { it.copy(syllabusDescription = description) }
        }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag).apply {
            onChildAt(1).performTextInput(description)
            onChildAt(2).performClick()
        }
        verifyOrder {
            viewModel.handleAction(LibraryAction.EditSyllabusDescription(description))
            viewModel.handleAction(LibraryAction.GenerateCurriculum)
        }
    }

    @Test
    fun removeCurriculumButton_shouldCall_viewModel_DiscardContent_whenClicked() {
        val curriculum = Curriculum(
            title = "Default Title",
            description = "This is a generated curriculum description.",
            content = listOf("module 1")
        )
        uiState.update { it.copy(curriculum = curriculum) }
        every { viewModel.handleAction(LibraryAction.DiscardContent) } answers {
            uiState.update { it.copy(curriculum = null) }
        }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag).apply {
            onChildren().apply {
                filterToOne(hasText(curriculum.title)).apply {
                    assertExists()
                    onChild().performClick()
                    assertDoesNotExist()
                }
            }
        }
        verify {
            viewModel.handleAction(LibraryAction.DiscardContent)
        }
    }

    @Test
    fun generateModuleButton_shouldCall_viewModel_generateModule_whenClicked() {
        val curriculum = Curriculum(
            title = "Default Title",
            description = "This is a generated curriculum description.",
            content = listOf("module 1")
        )
        uiState.update { it.copy(curriculum = curriculum) }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag).apply {
            performScrollToNode(hasText(stringMap["generate_content_button_label"]!!).and(isEnabled()))
            onChildren().filterToOne(hasText(stringMap["generate_content_button_label"]!!).and(isEnabled())).performClick()
        }
        verify {
            viewModel.handleAction(LibraryAction.GenerateModule("module 1"))
        }
    }

    @Test
    fun removeModuleButton_shouldCall_viewModel_removeModule_whenClicked() {
        val curriculum = Curriculum(
            title = "Default Title",
            description = "This is a generated curriculum description.",
            content = listOf("module 1")
        )
        val modules = listOf(
            Module(
                title = "module 1",
                description = "Module description",
                content = listOf("lesson 1")
            )
        )
        uiState.update {
            it.copy(
                curriculum = curriculum,
                modules = modules
            )
        }
        every { viewModel.handleAction(LibraryAction.RemoveModule("module 1")) } answers {
            uiState.update {
                it.copy(
                    curriculum = it.curriculum?.copy(content = it.curriculum!!.content.toMutableList().apply { removeAt(0) }),
                    modules = modules.filter { it.title != "module 1" }
                )
            }
        }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag).apply {
            performScrollToNode(hasText(modules[0].title))
            onChildren().filterToOne(hasText(modules[0].title)).apply {
                assertExists()
                onChild().performClick()
                assertDoesNotExist()
            }
        }
        verify {
            viewModel.handleAction(LibraryAction.RemoveModule("module 1"))
        }
    }

    @Test
    fun removeLessonButton_shouldCall_viewModel_removeLesson_whenClicked() {
        val curriculum = Curriculum(
            title = "Default Title",
            description = "This is a generated curriculum description.",
            content = listOf("module 1")
        )
        val modules = listOf(
            Module(
                id = "module_1",
                title = "module 1",
                description = "Module description",
                content = listOf("lesson 1")
            )
        )
        uiState.update {
            it.copy(
                curriculum = curriculum,
                modules = modules
            )
        }
        every { viewModel.handleAction(LibraryAction.RemoveLesson("lesson 1", "module_1")) } answers {
            uiState.update { currentState ->
                currentState.copy(
                    modules = currentState.modules.map { module ->
                        module.copy(content = module.content.toMutableList().apply { removeAt(0) })
                    }
                )
            }
        }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag).apply {
            performScrollToNode(hasText(modules[0].content[0]))
            onChildren().filterToOne(hasText(modules[0].content[0])).apply {
                assertExists()
                onChild().performClick()
                assertDoesNotExist()
            }
        }
        verify {
            viewModel.handleAction(LibraryAction.RemoveLesson("lesson 1", "module_1"))
        }
    }

    @Test
    fun saveButton_shouldCall_viewModel_SaveContent_whenClicked() = runTest {
        val curriculum = Curriculum(
            title = "Default Title",
            description = "This is a generated curriculum description.",
            content = listOf("module 1")
        )
        val modules = listOf(
            Module(
                title = "module 1",
                description = "Module description",
                content = listOf("lesson 1")
            )
        )
        uiState.update {
            it.copy(
                curriculum = curriculum,
                modules = modules
            )
        }
        coEvery { viewModel.handleAction(LibraryAction.SaveContent(stringMap["content_saved_success"]!!)) } coAnswers {
            uiEventFlow.emit(UIEvent.ShowSnackbar(stringMap["content_saved_success"]!!, SnackbarType.Success))
        }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag).apply {
            performScrollToNode(hasText(stringMap["save_content_button_label"]!!))
            onChildren().filterToOne(hasText(stringMap["save_content_button_label"]!!)).performClick()
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(stringMap["content_saved_success"]!!).assertIsDisplayed()
        verify {
            viewModel.handleAction(LibraryAction.SaveContent(stringMap["content_saved_success"]!!))
        }
    }

    @Test
    fun generatedCurriculumSection_shouldDisplayCorrectly() {
        val curriculum = Curriculum(
            title = "Default Title",
            description = "This is a generated curriculum description.",
            content = listOf("module 1", "module 2")
        )
        val modules = listOf(
            Module(
                title = "module 1",
                description = "Module description",
                content = listOf("lesson 1")
            )
        )
        uiState.update {
            it.copy(
                curriculum = curriculum,
                modules = modules
            )
        }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag).apply {
            onChildren().apply {
                performScrollToNode(hasText(curriculum.title))
                filterToOne(hasText(curriculum.title)).apply {
                    assertTextEquals(curriculum.title)
                    onChild().apply {
                        assertHasClickAction()
                        assert(hasContentDescription(stringMap["discard_content_button_label"]!!))
                    }
                }
                performScrollToNode(hasText(curriculum.content[0]))
                filterToOne(hasText(curriculum.content[0])).apply {
                    assertTextEquals(curriculum.content[0])
                    onChild().apply {
                        assertHasClickAction()
                        assert(hasContentDescription(stringMap["remove_module_button_label"]!!))
                    }
                }
                performScrollToNode(hasText(modules[0].content[0]))
                filterToOne(hasText(modules[0].content[0])).apply {
                    assertTextEquals(modules[0].content[0])
                    onChild().apply {
                        assertHasClickAction()
                        assert(hasContentDescription(stringMap["remove_lesson_button_label"]!!))
                    }
                }
                performScrollToNode(hasText(stringMap["generate_content_button_label"]!!).and(isEnabled()))
                filterToOne(hasText(stringMap["generate_content_button_label"]!!).and(isEnabled())).assertHasClickAction()
            }
        }
    }

    @Test
    fun supportingPane_shouldDisplayCorrectly() {
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_SUPPORTING_PANE_BUTTON.tag).performClick()
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_SUPPORTING_PANEL.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_SUPPORTING_PANEL_FILTER_BAR.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_SUPPORTING_PANEL_CURRICULA_LIST.tag).assertIsDisplayed()
    }

    @Test
    fun filterBar_should_call_viewModel_filterCurricula_whenTextChanged() {
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_SUPPORTING_PANE_BUTTON.tag).performClick()
        val query = "Some title"
        val curricula = listOf(
            Curriculum(
                title = "Some title",
                description = "Some description",
                content = listOf("module 1")
            ),
            Curriculum(
                title = "Another title",
                description = "Another description",
                content = listOf("module 1")
            )
        )
        uiState.update {
            it.copy(
                curricula = curricula,
                filteredCurricula = curricula
            )
        }
        every { viewModel.handleAction(LibraryAction.EditFilterQuery(query)) } answers {
            uiState.update { current ->
                current.copy(
                    filterQuery = query,
                    filteredCurricula = current.curricula.filter { it.title.contains(query, ignoreCase = true) }
                )
            }
        }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_SUPPORTING_PANEL_FILTER_BAR.tag).apply {
            performTextInput(query)
        }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_SUPPORTING_PANEL_CURRICULA_LIST.tag).apply {
            onChildren().assertCountEquals(1)
            onChildAt(0).assertTextContains(query)
        }
        verify {
            viewModel.handleAction(LibraryAction.EditFilterQuery(query))
        }
    }

    @Test
    fun curriculaList_should_call_viewModel_openCurriculum_whenClicked() {
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_SUPPORTING_PANE_BUTTON.tag).performClick()
        val curricula = listOf(
            Curriculum(
                title = "Some title",
                description = "Some description",
                content = listOf("module 1")
            ),
            Curriculum(
                title = "Another title",
                description = "Another description",
                content = listOf("module 1")
            )
        )
        uiState.update {
            it.copy(
                curricula = curricula,
                filteredCurricula = curricula
            )
        }
        composeTestRule.onNodeWithTag(TestTags.LIBRARY_SCREEN_SUPPORTING_PANEL_CURRICULA_LIST.tag).apply {
            onChildAt(0).performClick()
        }
        verify {
            viewModel.handleAction(LibraryAction.OpenCurriculum(curricula[0].id))
        }
    }
}