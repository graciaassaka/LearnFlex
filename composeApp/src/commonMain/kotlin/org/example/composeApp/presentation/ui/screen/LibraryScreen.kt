package org.example.composeApp.presentation.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.*
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.presentation.action.LibraryAction
import org.example.composeApp.presentation.navigation.AppDestination
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.LibraryUIState
import org.example.composeApp.presentation.ui.component.*
import org.example.composeApp.presentation.ui.constant.TestTags
import org.example.composeApp.presentation.ui.dimension.Dimension
import org.example.composeApp.presentation.ui.dimension.Padding
import org.example.composeApp.presentation.ui.dimension.Spacing
import org.example.composeApp.presentation.ui.util.HandleUIEvents
import org.example.composeApp.presentation.ui.util.ScreenConfig
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.composeApp.presentation.ui.util.toDateString
import org.example.composeApp.presentation.viewModel.LibraryViewModel
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Composable function for the LibraryScreen, which provides the UI and handles actions
 * related to the library feature of the application. This screen supports adaptive layouts
 * and multiple panes for better user interaction.
 *
 * @param windowSizeClass The current window size class used to adapt the UI for different screen sizes.
 * @param navController The navigation controller used to handle navigation events.
 * @param viewModel The ViewModel responsible for the state and business logic of the Library screen.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LibraryScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: LibraryViewModel
) {
    val screenConfig = ScreenConfig(
        windowSizeClass = windowSizeClass,
        snackbarHostState = remember { SnackbarHostState() },
        snackbarType = remember { mutableStateOf(SnackbarType.Info) },
        uiState = viewModel.state.collectAsStateWithLifecycle(),
        isScreenVisible = viewModel.isScreenVisible.collectAsStateWithLifecycle(),
    )

    HandleUIEvents(Route.Library, navController, viewModel, screenConfig.snackbarHostState) {
        screenConfig.snackbarType.value = it
    }

    val navigator = rememberSupportingPaneScaffoldNavigator()
    ThreePaneBackHandler(navigator = navigator)

    CustomScaffold(
        snackbarHostState = screenConfig.snackbarHostState,
        snackbarType = screenConfig.snackbarType.value,
        currentDestination = AppDestination.Library,
        onDestinationSelected = { viewModel.handleAction(LibraryAction.Navigate(it.route)) },
        onRefresh = { viewModel.handleAction(LibraryAction.Refresh) },
        enabled = !screenConfig.uiState.value.isDownloading &&
                !screenConfig.uiState.value.isUploading &&
                !screenConfig.uiState.value.isGenerating,
    ) { paddingValues ->
        SupportingPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            mainPane = {
                MainPane(
                    screenConfig = screenConfig,
                    handleAction = viewModel::handleAction,
                    showSupportingPaneButton = navigator.scaffoldValue[SupportingPaneScaffoldRole.Supporting] == PaneAdaptedValue.Hidden,
                    onNavigateToSupportingPane = { navigator.navigateTo(SupportingPaneScaffoldRole.Supporting) },
                    modifier = Modifier.padding(paddingValues).testTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL.tag)
                )
            },
            supportingPane = {
                SupportingPane(
                    screenConfig = screenConfig,
                    handleAction = viewModel::handleAction,
                    onNavigateToMainPane = { navigator.navigateTo(SupportingPaneScaffoldRole.Main) },
                    modifier = Modifier.padding(paddingValues).testTag(TestTags.LIBRARY_SCREEN_SUPPORTING_PANEL.tag)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalLayoutApi::class)
@Composable
private fun ThreePaneScaffoldScope.MainPane(
    screenConfig: ScreenConfig<LibraryUIState>,
    handleAction: (LibraryAction) -> Unit,
    showSupportingPaneButton: Boolean,
    onNavigateToSupportingPane: () -> Unit,
    modifier: Modifier = Modifier,
) = with(screenConfig) {
    val lazyListState = rememberLazyListState()
    val isLoading = uiState.value.isDownloading || uiState.value.isUploading || uiState.value.isGenerating
    AnimatedPane(modifier = modifier.safeDrawingPadding()) {
        Box(modifier = Modifier.fillMaxSize()) {
            EditorSection(
                windowSizeClass = windowSizeClass,
                uiState = uiState.value,
                handleAction = handleAction,
                lazyListState = lazyListState,
                modifier = Modifier.fillMaxSize()
                    .testTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag)
            )
            if (isLoading) LoadingWithCancelButton(
                isCancellable = uiState.value.isGenerating,
                cancel = { handleAction(LibraryAction.CancelGeneration) },
                modifier = Modifier.align(Alignment.Center)
            )
            if (showSupportingPaneButton) SupportingPaneButton(
                onNavigateToSupportingPane = onNavigateToSupportingPane,
                modifier = Modifier.align(Alignment.TopEnd)
                    .testTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_SUPPORTING_PANE_BUTTON.tag)
            )
            if (uiState.value.showDiscardWarningDialog) DiscardWarningDialog(
                handleAction = handleAction,
                modifier = Modifier.align(Alignment.Center)
            )
            CustomVerticalScrollbar(lazyListState)
        }
    }
}

@Composable
private fun EditorSection(
    windowSizeClass: WindowSizeClass,
    uiState: LibraryUIState,
    handleAction: (LibraryAction) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) = LazyColumn(
    state = lazyListState,
    modifier = modifier.fillMaxSize().padding(Padding.MEDIUM.dp),
    verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    item {
        SyllabusDescriptionSection(
            windowSizeClass = windowSizeClass,
            uiState = uiState,
            handleAction = handleAction,
            modifier = Modifier.fillMaxWidth()
        )
    }
    item {
        ActionButtons(
            uiState = uiState,
            handleAction = handleAction,
            modifier = Modifier.fillMaxWidth()
        )
    }
    item {
        uiState.curriculum?.let { curriculum ->
            CurriculumDetailsSection(
                displayMode = uiState.displayMode,
                curriculum = curriculum,
                modules = uiState.modules,
                enabled = !uiState.isDownloading && !uiState.isUploading,
                handleAction = handleAction,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalResourceApi::class)
@Composable
private fun SyllabusDescriptionSection(
    windowSizeClass: WindowSizeClass,
    uiState: LibraryUIState,
    handleAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemModifier = if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
        Modifier.height(200.dp).width(300.dp)
    } else {
        Modifier.height(200.dp).fillMaxWidth()
    }
    val composition = rememberLottieComposition {
        LottieCompositionSpec.JsonString(Res.readBytes("files/orange_coder.json").decodeToString())
    }
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
        maxItemsInEachRow = 3,
        maxLines = 3
    ) {
        Image(
            painter = rememberLottiePainter(
                composition = composition.value,
                isPlaying = true,
                iterations = Compottie.IterateForever
            ),
            contentDescription = null,
            modifier = itemModifier,
            contentScale = ContentScale.Fit
        )
        DocumentUpload(
            onDocumentSelected = { handleAction(LibraryAction.SummarizeSyllabus(it)) },
            onDocumentDeleted = { handleAction(LibraryAction.DeleteSyllabusFile) },
            enabled = !uiState.isDownloading && !uiState.isUploading,
            handleError = { handleAction(LibraryAction.HandleError(it)) },
            isUploaded = uiState.syllabusFile != null,
            isUploading = uiState.isUploading,
            modifier = itemModifier
        )
        OutlinedTextField(
            value = uiState.syllabusDescription,
            onValueChange = { handleAction(LibraryAction.EditSyllabusDescription(it)) },
            enabled = !uiState.isDownloading && !uiState.isUploading,
            label = { Text(stringResource(Res.string.syllabus_description_label)) },
            shape = RoundedCornerShape(Dimension.CORNER_RADIUS_MEDIUM.dp),
            modifier = itemModifier
        )
    }
}

@Composable
private fun ActionButtons(
    uiState: LibraryUIState,
    handleAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier
) = Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    val buttonModifier = Modifier.weight(1f).padding(Padding.SMALL.dp)
    Button(
        onClick = { handleAction(LibraryAction.GenerateCurriculum) },
        enabled = !uiState.isDownloading &&
                !uiState.isUploading &&
                uiState.syllabusDescription.isNotBlank() &&
                uiState.curriculum == null,
        modifier = buttonModifier,
        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp),
        content = { Text(stringResource(Res.string.generate_content_button_label)) }
    )
    Button(
        onClick = { handleAction(LibraryAction.SaveContent) },
        enabled = !uiState.isDownloading &&
                !uiState.isUploading &&
                uiState.displayMode == LibraryUIState.DisplayMode.Edit,
        modifier = buttonModifier,
        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp),
        content = { Text(stringResource(Res.string.save_content_button_label)) }
    )
}

@Composable
private fun CurriculumDetailsSection(
    displayMode: LibraryUIState.DisplayMode,
    curriculum: Curriculum,
    modules: List<Module>,
    enabled: Boolean,
    handleAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
    horizontalAlignment = Alignment.Start
) {
    CurriculumListItem(
        displayMode = displayMode,
        curriculum = curriculum,
        enabled = enabled,
        onNavigate = { handleAction(LibraryAction.Navigate(Route.Study(curriculum.id))) },
        onDiscard = { handleAction(LibraryAction.DiscardContent) }
    )
    HorizontalDivider()
    Row(modifier = Modifier.fillMaxWidth().padding(start = Padding.MEDIUM.dp)) {
        Text(
            text = stringResource(Res.string.modules),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleMedium
        )
    }
    HorizontalDivider()
    curriculum.content.forEachIndexed { index, module ->
        var isExpanded by remember(index) { mutableStateOf(false) }
        if (index > 0) HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = Padding.LARGE.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
                horizontalAlignment = Alignment.Start
            ) {
                ModuleListItem(
                    displayMode = displayMode,
                    module = module,
                    enabled = enabled,
                    expanded = isExpanded,
                    onExpand = { isExpanded = !isExpanded },
                    onRemove = { handleAction(LibraryAction.RemoveModule(module)) },
                    onNavigate = {
                        handleAction(
                            LibraryAction.Navigate(Route.Study(curriculum.id, modules.firstOrNull { it.title == module }?.id))
                        )
                    },
                )
                if (isExpanded) ModuleContent(
                    displayMode = displayMode,
                    modules = modules,
                    title = module,
                    enabled = enabled,
                    onGenerateModule = { handleAction(LibraryAction.GenerateModule(module)) },
                    onRemoveLesson = { lesson, moduleId -> handleAction(LibraryAction.RemoveLesson(lesson, moduleId)) }
                )
            }
        }
    }
}

@Composable
private fun CurriculumListItem(
    displayMode: LibraryUIState.DisplayMode,
    curriculum: Curriculum,
    enabled: Boolean,
    onNavigate: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) = ListItem(
    headlineContent = {
        ToolTip(text = curriculum.description) {
            Text(text = curriculum.title, style = MaterialTheme.typography.titleLarge)
        }
    },
    modifier = modifier.fillMaxWidth(),
    leadingContent = {
        when (displayMode) {
            LibraryUIState.DisplayMode.View -> IconButton(onClick = onNavigate, enabled = enabled) {
                Icon(
                    painter = painterResource(Res.drawable.ic_curriculum),
                    contentDescription = stringResource(Res.string.open_in_study_button_label)
                )
            }

            LibraryUIState.DisplayMode.Edit -> IconButton(onClick = onDiscard, enabled = enabled) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.discard_content_button_label)
                )
            }
        }
    }
)

@Composable
private fun ModuleListItem(
    displayMode: LibraryUIState.DisplayMode,
    module: String,
    enabled: Boolean,
    expanded: Boolean,
    onExpand: () -> Unit,
    onNavigate: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) = ListItem(
    headlineContent = { Text(text = module, style = MaterialTheme.typography.titleSmall) },
    trailingContent = { ExpandCollapseIconButton(expanded = expanded, onClick = onExpand, enabled = enabled) },
    modifier = modifier,
    leadingContent = {
        when (displayMode) {
            LibraryUIState.DisplayMode.View -> IconButton(onClick = onNavigate, enabled = enabled) {
                Icon(
                    painter = painterResource(Res.drawable.ic_module),
                    contentDescription = stringResource(Res.string.open_in_study_button_label)
                )
            }

            LibraryUIState.DisplayMode.Edit -> IconButton(onClick = onRemove, enabled = enabled) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.remove_module_button_label)
                )
            }
        }
    }
)

@Composable
private fun ModuleContent(
    displayMode: LibraryUIState.DisplayMode,
    modules: List<Module>,
    title: String,
    enabled: Boolean,
    onGenerateModule: () -> Unit,
    onRemoveLesson: (String, String) -> Unit,
    modifier: Modifier = Modifier
) = Column(
    modifier = modifier.fillMaxWidth().padding(start = Padding.MEDIUM.dp),
    verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
    horizontalAlignment = Alignment.Start
) {
    val module = modules.firstOrNull { it.title == title }
    module?.content?.forEachIndexed { i, lesson ->
        if (i > 0) HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = DividerDefaults.Thickness.times(0.5f)
        )
        LessonListItem(
            displayMode = displayMode,
            lesson = lesson,
            enabled = enabled,
            onRemove = { onRemoveLesson(lesson, module.id) }
        )
    } ?: Button(
        onClick = onGenerateModule,
        enabled = enabled,
        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp),
        content = { Text(stringResource(Res.string.generate_content_button_label)) }
    )
}

@Composable
private fun LessonListItem(
    displayMode: LibraryUIState.DisplayMode,
    lesson: String,
    enabled: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) = ListItem(
    headlineContent = {
        Text(
            text = lesson,
            style = MaterialTheme.typography.bodySmall
        )
    },
    modifier = modifier.fillMaxWidth(),
    leadingContent = {
        if (displayMode == LibraryUIState.DisplayMode.Edit) IconButton(onClick = onRemove, enabled = enabled) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = stringResource(Res.string.remove_lesson_button_label)
            )
        }
    }
)

@Composable
private fun SupportingPaneButton(
    onNavigateToSupportingPane: () -> Unit,
    modifier: Modifier = Modifier
) = IconButton(
    onClick = onNavigateToSupportingPane,
    modifier = modifier
) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = stringResource(Res.string.display_supporting_panel_button_label)
    )
}

@Composable
private fun DiscardWarningDialog(
    handleAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier
) = AlertDialog(
    icon = {
        Icon(imageVector = Icons.Default.Warning, contentDescription = null)
    },
    title = {
        Text(stringResource(Res.string.discard_warning_dialog_title))
    },
    text = {
        Text(stringResource(Res.string.discard_changes_warning))
    },
    onDismissRequest = {
        handleAction(LibraryAction.HideDiscardWarningDialog)
    },
    confirmButton = {
        TextButton(onClick = { handleAction(LibraryAction.DiscardContent) }) {
            Text(stringResource(Res.string.discard_button_label))
        }
    },
    dismissButton = {
        TextButton(onClick = { handleAction(LibraryAction.HideDiscardWarningDialog) }) {
            Text(stringResource(Res.string.cancel_button_label))
        }
    },
    modifier = modifier
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ThreePaneScaffoldScope.SupportingPane(
    screenConfig: ScreenConfig<LibraryUIState>,
    handleAction: (LibraryAction) -> Unit,
    onNavigateToMainPane: () -> Unit,
    modifier: Modifier = Modifier,
) = with(screenConfig) {
    val listState = rememberLazyListState()
    AnimatedPane(modifier = modifier.safeDrawingPadding()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
            horizontalAlignment = Alignment.Start
        ) {
            FilterBar(
                query = uiState.value.filterQuery,
                onQueryChange = { handleAction(LibraryAction.EditFilterQuery(it)) },
                modifier = Modifier.fillMaxWidth().testTag(TestTags.LIBRARY_SCREEN_SUPPORTING_PANEL_FILTER_BAR.tag)
            )
            RefreshBox(
                modifier = Modifier.fillMaxWidth(),
                isRefreshing = uiState.value.isDownloading,
                onRefresh = { handleAction(LibraryAction.Refresh) }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag(TestTags.LIBRARY_SCREEN_SUPPORTING_PANEL_CURRICULA_LIST.tag),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    uiState.value.filteredCurricula.forEach { curriculum ->
                        item {
                            CurriculumTooltipListItem(
                                curriculum = curriculum,
                                onOpenCurriculum = { handleAction(LibraryAction.OpenCurriculum(curriculum.id)); onNavigateToMainPane() },
                                onDeleteCurriculum = { handleAction(LibraryAction.DeleteCurriculum(curriculum.id)) },
                                onNavigate = { handleAction(LibraryAction.Navigate(Route.Study(curriculum.id))) },
                            )
                        }
                    }
                }
                CustomVerticalScrollbar(
                    lazyListState = listState,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun FilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) = OutlinedTextField(
    value = query,
    onValueChange = onQueryChange,
    label = { Text(stringResource(Res.string.search_button_label)) },
    leadingIcon = {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = stringResource(Res.string.search_button_label)
        )
    },
    trailingIcon = {
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(Res.string.clear_search_query_button_label)
                )
            }
        }
    },
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CurriculumTooltipListItem(
    curriculum: Curriculum,
    onOpenCurriculum: () -> Unit,
    onDeleteCurriculum: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) = ListItem(
    headlineContent = {
        Text(
            text = curriculum.title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall,
        )
    },
    modifier = modifier.fillMaxWidth().clickable { onOpenCurriculum() },
    overlineContent = {
        Text(
            text = Status.valueOf(curriculum.status.uppercase()).value,
            style = MaterialTheme.typography.bodySmall
        )
    },
    supportingContent = {
        Text(
            text = "${stringResource(Res.string.created_on)}:\n${curriculum.createdAt.toDateString()}",
            style = MaterialTheme.typography.bodySmall
        )
    },
    leadingContent = {
        IconButton(onClick = onNavigate) {
            Icon(
                painter = painterResource(Res.drawable.ic_curriculum),
                contentDescription = null
            )
        }
    },
    trailingContent = {
        IconButton(onClick = onDeleteCurriculum) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(Res.string.delete_curriculum_button_label)
            )
        }
    }
)

