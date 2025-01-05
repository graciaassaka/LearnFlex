package org.example.composeApp.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.component.*
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.dimension.Padding
import org.example.composeApp.dimension.Spacing
import org.example.composeApp.navigation.AppDestination
import org.example.composeApp.util.ScreenConfig
import org.example.composeApp.util.TestTags
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.presentation.action.LibraryAction
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.state.LibraryUIState
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.viewModel.LibraryViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import java.text.SimpleDateFormat
import java.util.*

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

    HandleUIEvents(Route.CreateProfile, navController, viewModel, screenConfig.snackbarHostState) {
        screenConfig.snackbarType.value = it
    }

    val navigator = rememberSupportingPaneScaffoldNavigator()
    ThreePaneBackHandler(navigator = navigator)

    CustomScaffold(
        snackbarHostState = screenConfig.snackbarHostState,
        snackbarType = screenConfig.snackbarType.value,
        currentDestination = AppDestination.Library,
        onDestinationSelected = { viewModel.handleAction(LibraryAction.Navigate(it.route)) },
        enabled = !screenConfig.uiState.value.isRefreshing &&
                !screenConfig.uiState.value.isUploading &&
                !screenConfig.uiState.value.isDownloading,
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
    AnimatedPane(modifier = modifier.safeContentPadding()) {
        Box(modifier = Modifier.fillMaxSize()) {
            GeneratedContentSection(
                windowSizeClass = windowSizeClass,
                uiState = uiState.value,
                handleAction = handleAction,
                modifier = Modifier.fillMaxSize()
                    .testTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_GENERATED_CONTENT_SECTION.tag)
            )
            if (uiState.value.isDownloading || uiState.value.isUploading) LoadingIndicator(
                handleAction = handleAction,
                modifier = Modifier.align(Alignment.Center)
            )
            if (showSupportingPaneButton) SupportingPaneButton(
                onNavigateToSupportingPane = onNavigateToSupportingPane,
                modifier = Modifier.align(Alignment.BottomEnd)
                    .testTag(TestTags.LIBRARY_SCREEN_MAIN_PANEL_SUPPORTING_PANE_BUTTON.tag)
            )
            if (uiState.value.showDiscardWarningDialog) DiscardWarningDialog(
                handleAction = handleAction,
                modifier = Modifier.align(Alignment.Center)
            )
            CustomVerticalScrollbar(
                lazyListState = lazyListState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun GeneratedContentSection(
    windowSizeClass: WindowSizeClass,
    uiState: LibraryUIState,
    handleAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier
) = LazyColumn(
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
            GeneratedCurriculumSection(
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
    val itemModifier = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> Modifier.height(200.dp).width(300.dp)
        else -> Modifier.height(200.dp).fillMaxWidth()
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
    val saveContentSuccess = stringResource(Res.string.content_saved_success)
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
        onClick = { handleAction(LibraryAction.SaveContent(saveContentSuccess)) },
        enabled = !uiState.isDownloading &&
                !uiState.isUploading &&
                uiState.curriculum != null,
        modifier = buttonModifier,
        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp),
        content = { Text(stringResource(Res.string.save_content_button_label)) }
    )
}

@Composable
private fun GeneratedCurriculumSection(
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
        curriculum = curriculum,
        enabled = enabled,
        handleAction = handleAction
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
                    module = module,
                    index = index,
                    enabled = enabled,
                    handleAction = handleAction,
                    modifier = Modifier
                )
                ModuleContent(
                    modules = modules,
                    index = index,
                    enabled = enabled,
                    handleAction = handleAction,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun CurriculumListItem(
    curriculum: Curriculum,
    enabled: Boolean,
    handleAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier
) = ListItem(
    headlineContent = {
        Text(
            text = curriculum.title,
            style = MaterialTheme.typography.titleLarge
        )
    },
    modifier = modifier.fillMaxWidth(),
    leadingContent = {
        IconButton(
            onClick = { handleAction(LibraryAction.DiscardContent) },
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(Res.string.discard_content_button_label)
            )
        }
    }
)

@Composable
private fun ModuleListItem(
    module: String,
    index: Int,
    enabled: Boolean,
    handleAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier
) = ListItem(
    headlineContent = {
        Text(
            text = module,
            style = MaterialTheme.typography.titleSmall
        )
    },
    modifier = modifier,
    leadingContent = {
        IconButton(
            onClick = { handleAction(LibraryAction.RemoveModule(index)) },
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(Res.string.remove_module_button_label)
            )
        }
    }
)

@Composable
fun ModuleContent(
    modules: List<Module>,
    index: Int,
    enabled: Boolean,
    handleAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier
) = Column(
    modifier = modifier.fillMaxWidth().padding(start = Padding.MEDIUM.dp),
    verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
    horizontalAlignment = Alignment.Start
) {
    modules.firstOrNull { it.index == index }?.content?.forEachIndexed { i, lesson ->
        if (i > 0) HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = DividerDefaults.Thickness.times(0.5f)
        )
        LessonListItem(
            lesson = lesson,
            index = i,
            moduleId = modules[index].id,
            enabled = enabled,
            handleAction = handleAction
        )
    } ?: Button(
        onClick = { handleAction(LibraryAction.GenerateModule(index)) },
        enabled = enabled,
        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp),
        content = { Text(stringResource(Res.string.generate_content_button_label)) }
    )
}

@Composable
private fun LessonListItem(
    lesson: String,
    index: Int,
    moduleId: String,
    enabled: Boolean,
    handleAction: (LibraryAction) -> Unit,
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
        IconButton(
            onClick = { handleAction(LibraryAction.RemoveLesson(index, moduleId)) },
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = stringResource(Res.string.remove_lesson_button_label)
            )
        }
    }
)

@Composable
private fun LoadingIndicator(
    handleAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier
) = Box(modifier = modifier) {
    CircularProgressIndicator(
        modifier = Modifier.align(Alignment.Center).size(80.dp),
        color = MaterialTheme.colorScheme.primary,
    )
    IconButton(
        onClick = { handleAction(LibraryAction.CancelGeneration) },
        modifier = Modifier.align(Alignment.Center)
    ) {
        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = stringResource(Res.string.cancel_button_label)
        )
    }
}

@Composable
fun SupportingPaneButton(
    onNavigateToSupportingPane: () -> Unit,
    modifier: Modifier = Modifier
) = FloatingActionButton(
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
        Text(stringResource(Res.string.discard_warning_dialog_message))
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
    modifier: Modifier = Modifier,
) = with(screenConfig) {
    val listState = rememberLazyListState()
    AnimatedPane(modifier = Modifier.safeContentPadding()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilterBar(
                query = uiState.value.filterQuery,
                onQueryChange = { handleAction(LibraryAction.EditFilterQuery(it)) },
                modifier = Modifier.fillMaxWidth().testTag(TestTags.LIBRARY_SCREEN_SUPPORTING_PANEL_FILTER_BAR.tag)
            )
            RefreshBox(
                modifier = modifier.fillMaxWidth(),
                isRefreshing = uiState.value.isRefreshing,
                onRefresh = { handleAction(LibraryAction.Refresh) }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag(TestTags.LIBRARY_SCREEN_SUPPORTING_PANEL_CURRICULA_LIST.tag),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    uiState.value.filteredCurricula.forEach { curriculum ->
                        item { CurriculumTooltipListItem(curriculum = curriculum, handleAction = handleAction) }
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
    handleAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier
) = ToolTip(
    text = curriculum.description,
    modifier = modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = curriculum.title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = Modifier.fillMaxWidth()
            .clickable { handleAction(LibraryAction.OpenCurriculum(curriculum.id)) },
        overlineContent = {
            Text(
                text = Status.valueOf(curriculum.status.uppercase()).value,
                style = MaterialTheme.typography.bodySmall
            )
        },
        supportingContent = {
            Text(
                text = """
                        ${stringResource(Res.string.created_on)}:
                        ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(curriculum.createdAt))}
                    """.trimIndent(),
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null
            )
        }
    )
}
