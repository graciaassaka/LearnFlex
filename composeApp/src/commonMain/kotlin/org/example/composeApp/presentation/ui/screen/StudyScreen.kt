package org.example.composeApp.presentation.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.*
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.presentation.action.StudyAction
import org.example.composeApp.presentation.navigation.AppDestination
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.StudyUIState
import org.example.composeApp.presentation.ui.component.*
import org.example.composeApp.presentation.ui.dimension.Dimension
import org.example.composeApp.presentation.ui.dimension.Elevation
import org.example.composeApp.presentation.ui.dimension.Padding
import org.example.composeApp.presentation.ui.dimension.Spacing
import org.example.composeApp.presentation.ui.util.HandleUIEvents
import org.example.composeApp.presentation.ui.util.ScreenConfig
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.composeApp.presentation.ui.util.toDateString
import org.example.composeApp.presentation.viewModel.StudyViewModel
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.model.Question
import org.example.shared.domain.model.Quiz
import org.example.shared.domain.model.Section
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.model.interfaces.DescribableRecord
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Composable function for the Study screen.
 *
 * @param windowSizeClass The window size class for adaptive design.
 * @param navController The NavController for navigation.
 * @param viewModel The StudyViewModel instance to access the app state.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun StudyScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: StudyViewModel = koinViewModel()
) {
    val screenConfig = ScreenConfig(
        windowSizeClass = windowSizeClass,
        snackbarHostState = remember { SnackbarHostState() },
        snackbarType = remember { mutableStateOf(SnackbarType.Info) },
        uiState = viewModel.state.collectAsStateWithLifecycle(),
        isScreenVisible = viewModel.isScreenVisible.collectAsStateWithLifecycle()
    )
    val navigator = rememberListDetailPaneScaffoldNavigator<Lesson>()
    ThreePaneBackHandler(navigator) {
        viewModel.handleAction(StudyAction.GoBack)
    }
    HandleUIEvents(Route.Study(), navController, viewModel, screenConfig.snackbarHostState) {
        screenConfig.snackbarType.value = it
    }
    CustomScaffold(
        snackbarHostState = screenConfig.snackbarHostState,
        snackbarType = screenConfig.snackbarType.value,
        currentDestination = AppDestination.Study,
        onDestinationSelected = { viewModel.handleAction(StudyAction.Navigate(it.route)) },
        onRefresh = { viewModel.handleAction(StudyAction.Refresh) },
        enabled = !screenConfig.uiState.value.isRefreshing &&
                !screenConfig.uiState.value.isUploading &&
                !screenConfig.uiState.value.isGenerating
    ) { paddingValues ->
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                ListPane(
                    uiState = screenConfig.uiState.value,
                    handleAction = viewModel::handleAction,
                    navigator = navigator,
                    modifier = Modifier.padding(paddingValues)
                )
            },
            detailPane = {
                DetailPane(
                    uiState = screenConfig.uiState.value,
                    handleAction = viewModel::handleAction,
                    navigator = navigator,
                    modifier = Modifier.padding(paddingValues)
                )
            },
            extraPane = {
                ExtraPane(
                    uiState = screenConfig.uiState.value,
                    handleAction = viewModel::handleAction,
                    navigator = navigator,
                    modifier = Modifier.padding(paddingValues)
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ThreePaneScaffoldScope.ListPane(
    uiState: StudyUIState,
    handleAction: (StudyAction) -> Unit,
    navigator: ThreePaneScaffoldNavigator<Lesson>,
    modifier: Modifier = Modifier
) = AnimatedPane(modifier = modifier.safeDrawingPadding()) {
    val lazyListState = rememberLazyListState()
    val isLoading = uiState.isUploading || uiState.isGenerating
    RefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { handleAction(StudyAction.Refresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        StudyLazyColumn(
            uiState = uiState,
            handleAction = handleAction,
            lazyListState = lazyListState,
            navigator = navigator,
            modifier = Modifier.fillMaxSize()
        )
        if (isLoading && navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Hidden) {
            LoadingWithCancelButton(
                isCancellable = uiState.isGenerating,
                cancel = { handleAction(StudyAction.CancelGeneration) },
                modifier = Modifier.align(Alignment.Center)
            )
        }
        CustomVerticalScrollbar(lazyListState)
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun StudyLazyColumn(
    uiState: StudyUIState,
    handleAction: (StudyAction) -> Unit,
    navigator: ThreePaneScaffoldNavigator<Lesson>,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) = LazyColumn(
    verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
    horizontalAlignment = Alignment.Start,
    state = lazyListState,
    contentPadding = PaddingValues(Padding.MEDIUM.dp),
    modifier = modifier
) {
    item {
        ItemMenu(
            selectedItem = uiState.curriculum,
            items = uiState.curricula,
            noSelectedItemText = stringResource(Res.string.no_curriculum_selected),
            onItemSelected = { handleAction(StudyAction.SelectCurriculum(it)) },
            enabled = !uiState.isRefreshing && !uiState.isUploading,
            icon = painterResource(Res.drawable.ic_curriculum)
        )
    }
    item {
        ItemMenu(
            selectedItem = uiState.module,
            items = uiState.modules,
            noSelectedItemText = stringResource(Res.string.no_module_selected),
            onItemSelected = { handleAction(StudyAction.SelectModule(it)) },
            enabled = !uiState.isRefreshing && !uiState.isUploading,
            icon = painterResource(Res.drawable.ic_module)
        )
    }
    itemsIndexed(uiState.module?.content.orEmpty()) { i, lessonTitle ->
        if (i > 0) HorizontalDivider(modifier = Modifier.fillMaxWidth())
        uiState.lessons.find { it.title == lessonTitle }?.let { lesson ->
            LessonItem(
                lesson = lesson,
                enabled = !uiState.isRefreshing && !uiState.isUploading,
                onSelect = {
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, lesson)
                    handleAction(StudyAction.SelectLesson(lesson.id))
                },
                onRegenerate = {
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, lesson)
                    handleAction(StudyAction.SelectLesson(lesson.id))
                    handleAction(StudyAction.RegenerateLesson(lesson.id))
                }
            )
        } ?: LessonItem(
            lessonTitle = lessonTitle,
            enabled = !uiState.isRefreshing && !uiState.isUploading,
            onDownload = { handleAction(StudyAction.GenerateLesson(lessonTitle)) }
        )
    }
    item { Spacer(modifier = Modifier.height(Padding.LARGE.dp)) }
    if (uiState.module != null) item {
        Button(
            onClick = {
                handleAction(StudyAction.GenerateModuleQuiz)
                navigator.navigateTo(ListDetailPaneScaffoldRole.Extra)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isRefreshing && !uiState.isUploading && uiState.module.canQuiz(uiState.lessons),
            contentPadding = PaddingValues(Padding.SMALL.dp),
            shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp)
        ) {
            Text(
                text = stringResource(Res.string.start_quiz_button_label),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun <T> ItemMenu(
    selectedItem: T?,
    items: List<T>,
    noSelectedItemText: String,
    onItemSelected: (String) -> Unit,
    icon: Painter,
    enabled: Boolean,
    modifier: Modifier = Modifier
) where T : DescribableRecord, T : DatabaseRecord {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth().padding(Padding.MEDIUM.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = selectedItem?.title ?: noSelectedItemText,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(0.9f)
        )
        Box(modifier = Modifier.weight(0.1f)) {
            ExpandCollapseIconButton(
                expanded = expanded,
                onClick = { expanded = !expanded },
                enabled = enabled,
                modifier = Modifier.align(Alignment.Center)
            )
            DropdownMenu(
                expanded = expanded,
                enabled = enabled,
                onDismissRequest = { expanded = false },
                items = items,
                onItemClicked = onItemSelected,
                icon = icon
            )
        }
    }
}

@Composable
private fun <T> DropdownMenu(
    expanded: Boolean,
    enabled: Boolean,
    onDismissRequest: () -> Unit,
    items: List<T>,
    onItemClicked: (String) -> Unit,
    icon: Painter,
    modifier: Modifier = Modifier
) where T : DescribableRecord, T : DatabaseRecord {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = Elevation.MEDIUM.dp
    ) {
        for (i in items.indices.reversed()) {
            val item = items[i]
            if (i < items.size - 1) HorizontalDivider(modifier = Modifier.fillMaxWidth())
            DropdownMenuItem(
                text = { Text(text = item.title, style = MaterialTheme.typography.bodySmall) },
                onClick = { onItemClicked(item.id); onDismissRequest() },
                modifier = Modifier.fillMaxWidth().padding(Padding.SMALL.dp),
                leadingIcon = { Icon(painter = icon, contentDescription = null) },
                enabled = enabled,
                contentPadding = PaddingValues(Padding.SMALL.dp)
            )
        }
    }
}

@Composable
private fun LessonItem(
    lessonTitle: String,
    enabled: Boolean,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) = ListItem(
    headlineContent = {
        Text(
            text = lessonTitle,
            style = MaterialTheme.typography.bodyMedium
        )
    },
    modifier = modifier,
    trailingContent = {
        IconButton(onClick = onDownload, enabled = enabled) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = stringResource(Res.string.download_button_label)
            )
        }
    }
)

@Composable
private fun LessonItem(
    lesson: Lesson,
    enabled: Boolean,
    onSelect: () -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier,
) = ListItem(
    headlineContent = {
        Text(
            text = lesson.title,
            style = MaterialTheme.typography.bodyMedium
        )
    },
    modifier = modifier.clickable(enabled = enabled, onClick = onSelect),
    overlineContent = {
        InfoRow(
            label = stringResource(Res.string.last_recorded_activity),
            value = lesson.lastUpdated.toDateString()
        )
    },
    supportingContent = {
        InfoRow(
            label = stringResource(Res.string.best_quiz_score),
            value = "${lesson.quizScore}/${lesson.quizScoreMax}"
        )
    },
    colors = ListItemDefaults.colors(
        headlineColor = if (lesson.isCompleted()) {
            Color(0xFF4CAF50)
        } else {
            ListItemDefaults.contentColor
        }
    ),
    leadingContent = {
        Icon(
            painter = painterResource(Res.drawable.ic_lesson),
            contentDescription = null
        )
    },
    trailingContent = {
        IconButton(onClick = onRegenerate, enabled = enabled) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(Res.string.download_button_label)
            )
        }
    }
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ThreePaneScaffoldScope.DetailPane(
    uiState: StudyUIState,
    handleAction: (StudyAction) -> Unit,
    navigator: ThreePaneScaffoldNavigator<Lesson>,
    modifier: Modifier = Modifier
) = AnimatedPane(modifier = modifier.safeDrawingPadding()) {
    val isLoading = uiState.isUploading || uiState.isGenerating
    var lazyListState = rememberLazyListState()
    RefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { handleAction(StudyAction.Refresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.lesson != null) {
            LessonDetail(
                lesson = uiState.lesson,
                sections = uiState.sections,
                handleAction = handleAction,
                enabled = !isLoading,
                lazyListState = lazyListState,
                navigator = navigator,
                modifier = Modifier.padding(Padding.MEDIUM.dp).offset(y = Padding.MEDIUM.dp)
            )
            CustomVerticalScrollbar(lazyListState)
        } else {
            Text(
                text = stringResource(Res.string.no_lesson_selected),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        if (isLoading && navigator.scaffoldValue[ListDetailPaneScaffoldRole.Extra] == PaneAdaptedValue.Hidden) {
            LoadingWithCancelButton(
                isCancellable = uiState.isGenerating,
                cancel = { handleAction(StudyAction.CancelGeneration) },
                modifier = Modifier.align(Alignment.Center)
            )
        }
        if (
            navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded &&
            navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Hidden
        ) {
            IconButton(
                onClick = { navigator.navigateTo(ListDetailPaneScaffoldRole.List) },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.download_button_label)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun LessonDetail(
    lesson: Lesson,
    sections: List<Section>,
    handleAction: (StudyAction) -> Unit,
    enabled: Boolean,
    lazyListState: LazyListState,
    navigator: ThreePaneScaffoldNavigator<Lesson>,
    modifier: Modifier = Modifier
) = LazyColumn(
    state = lazyListState,
    verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
    horizontalAlignment = Alignment.Start,
    modifier = modifier
) {
    item { Text(text = lesson.title, style = MaterialTheme.typography.displaySmall) }
    item { Spacer(modifier = Modifier.height(Padding.MEDIUM.dp)) }
    item {
        Text(
            text = stringResource(Res.string.description),
            textDecoration = TextDecoration.Underline,
            style = MaterialTheme.typography.titleMedium
        )
    }
    item { Text(text = lesson.description, style = MaterialTheme.typography.bodyMedium) }
    itemsIndexed(lesson.content) { i, sectionTitle ->
        val expanded = rememberSaveable(i) { mutableStateOf(false) }
        sections.find { it.title == sectionTitle }?.let { section ->
            SectionCard(
                section = section,
                onStartQuiz = {
                    handleAction(StudyAction.GenerateSectionQuiz(it))
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Extra)
                },
                onRegenerate = { handleAction(StudyAction.RegenerateSection(section.id)) },
                onExpand = { expanded.value = !expanded.value },
                expanded = expanded.value,
                enabled = enabled
            )
        } ?: SectionCard(
            sectionTitle = sectionTitle,
            onGenerate = { handleAction(StudyAction.GenerateSection(sectionTitle)) },
            onExpand = { expanded.value = !expanded.value },
            expanded = expanded.value,
            enabled = enabled
        )
    }
    item { Spacer(modifier = Modifier.height(Padding.LARGE.dp)) }
    item {
        StartQuizButton(
            onStartQuiz = { handleAction(StudyAction.GenerateLessonQuiz) },
            enabled = enabled && lesson.canQuiz(sections),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SectionCard(
    sectionTitle: String,
    onGenerate: () -> Unit,
    onExpand: () -> Unit,
    expanded: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) = Card(
    onClick = onExpand,
    modifier = modifier.fillMaxWidth().animateContentSize(),
    shape = RectangleShape
) {
    Column(modifier = Modifier.padding(Padding.MEDIUM.dp)) {
        SectionHeading(
            title = sectionTitle,
            enabled = enabled,
            onExpand = onExpand,
            expanded = expanded,
            modifier = Modifier.fillMaxWidth()
        )
        if (expanded) Button(
            onClick = onGenerate,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            contentPadding = PaddingValues(Padding.SMALL.dp),
            shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp)
        ) {
            Text(
                text = stringResource(Res.string.generate_content_button_label),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun SectionCard(
    section: Section,
    onStartQuiz: (String) -> Unit,
    onRegenerate: () -> Unit,
    onExpand: () -> Unit,
    expanded: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = if (expanded) {
            MaterialTheme.colorScheme.surface
        } else {
            CardDefaults.cardColors().containerColor
        },
        animationSpec = tween(300),
        label = "Color"
    )
    Card(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RectangleShape
    ) {
        Column(
            modifier = Modifier.padding(Padding.MEDIUM.dp),
            verticalArrangement = Arrangement.spacedBy(Padding.SMALL.dp),
            horizontalAlignment = Alignment.Start
        ) {
            SectionHeading(
                title = section.title,
                quizScore = section.quizScore,
                quizScoreMax = section.quizScoreMax,
                enabled = enabled,
                onExpand = onExpand,
                expanded = expanded,
                modifier = Modifier.fillMaxWidth()
            )
            if (expanded) {
                section.content.forEach { content ->
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = TextUnit(24f, TextUnitType.Sp),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StartQuizButton(
                        onStartQuiz = { onStartQuiz(section.id) },
                        enabled = enabled,
                        modifier = Modifier.padding(Padding.SMALL.dp)
                    )
                    OutlinedButton(
                        onClick = onRegenerate,
                        modifier = modifier.padding(Padding.SMALL.dp),
                        enabled = enabled,
                        contentPadding = PaddingValues(Padding.SMALL.dp),
                        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.regenerate_content_button_label),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    enabled: Boolean,
    expanded: Boolean,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    quizScore: Int? = null,
    quizScoreMax: Int? = null
) = Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.Start
) {
    if (quizScore != null && quizScoreMax != null) {
        InfoRow(
            label = stringResource(Res.string.best_quiz_score),
            value = "$quizScore/$quizScoreMax",
            modifier = Modifier.padding(Padding.SMALL.dp)
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(0.9f)
        )
        Box(modifier = Modifier.weight(0.1f)) {
            ExpandCollapseIconButton(
                expanded = expanded,
                onClick = onExpand,
                enabled = enabled,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String, value: String,
    modifier: Modifier = Modifier
) = Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall
    )
    Text(
        text = value,
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun StartQuizButton(
    onStartQuiz: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onStartQuiz,
        enabled = enabled,
        contentPadding = PaddingValues(Padding.SMALL.dp),
        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(Res.string.start_quiz_button_label),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalResourceApi::class)
@Composable
private fun ThreePaneScaffoldScope.ExtraPane(
    uiState: StudyUIState,
    handleAction: (StudyAction) -> Unit,
    navigator: ThreePaneScaffoldNavigator<Lesson>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var currentQuestionIndex by rememberSaveable { mutableStateOf(0) }
    val currentQuestion = uiState.quiz.questions.getOrNull(currentQuestionIndex)

    val isLoading = uiState.isGenerating
    val enabled = !isLoading && currentQuestionIndex < uiState.quiz.questions.size

    var selectedOption by rememberSaveable { mutableStateOf<String?>(null) }
    val onAnswer = fun(option: String) {
        handleAction(StudyAction.AnswerQuizQuestion(option))
        if (currentQuestionIndex < uiState.quiz.questions.size) currentQuestionIndex++
        if (currentQuestionIndex == uiState.quiz.questionNumber) {
            handleAction(StudyAction.SubmitQuiz)
            currentQuestionIndex = 0
        }
        selectedOption = null
    }
    LaunchedEffect(uiState.showQuizPane) {
        if (!uiState.showQuizPane) {
            currentQuestionIndex = 0
            navigator.navigateBack()
        }
    }
    AnimatedPane(modifier = modifier.safeDrawingPadding()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(Padding.SMALL.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuizLottieImage()
                if (!enabled && currentQuestion == null) TypingIndicator(modifier = Modifier.padding(Padding.MEDIUM.dp))
                currentQuestion?.let { question ->
                    Question(
                        question = currentQuestion,
                        enabled = enabled,
                        onAnswer = { selectedOption?.let(onAnswer) },
                        onSelect = { selectedOption = it },
                        modifier = Modifier.padding(Padding.MEDIUM.dp),
                        selectedOption = selectedOption
                    )
                }
                CancelQuizButton(onCancel = { handleAction(StudyAction.CancelGeneration) })
            }
            CustomVerticalScrollbar(scrollState)
        }
    }
    if (uiState.showQuizResultDialog) {
        navigator.navigateBack()
        QuizResultDialog(
            quiz = uiState.quiz,
            onDismissRequest = { handleAction(StudyAction.SaveQuizResult) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun QuizLottieImage(
    modifier: Modifier = Modifier
) {
    val composition = rememberLottieComposition {
        LottieCompositionSpec.JsonString(Res.readBytes("files/online_test.json").decodeToString())
    }
    Image(
        painter = rememberLottiePainter(
            composition = composition.value,
            isPlaying = true,
            iterations = Compottie.IterateForever
        ),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
private fun <Q : Question> Question(
    question: Q,
    enabled: Boolean,
    onAnswer: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedOption: String? = null
) = Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
    horizontalAlignment = Alignment.Start
) {
    Text(text = question.text)
    when (question) {
        is Question.MultipleChoice -> question.options.forEach { option ->
            MultipleChoiceOptionCard(
                letter = option.letter,
                value = option.value,
                onSelect = { onSelect(option.letter) },
                enabled = enabled,
                modifier = Modifier.padding(Padding.SMALL.dp),
                selectedLetter = selectedOption
            )
        }

        is Question.TrueFalse      -> setOf(true, false).forEach { choice ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .selectable(
                        selected = choice.toString() == selectedOption,
                        onClick = { onSelect(choice.toString()) },
                        enabled = enabled,
                        role = Role.Checkbox
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = choice.toString() == selectedOption,
                    onClick = { onSelect(choice.toString()) },
                    enabled = enabled
                )
                Text(
                    text = choice.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Padding.SMALL.dp)
                )
            }
        }
    }
    QuestionSubmitButton(
        onSubmit = onAnswer,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MultipleChoiceOptionCard(
    letter: String,
    value: String,
    onSelect: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    selectedLetter: String? = null
) {
    val color by animateColorAsState(
        targetValue = if (selectedLetter == letter) {
            MaterialTheme.colorScheme.primary
        } else {
            CardDefaults.cardColors().containerColor
        },
        animationSpec = tween(300),
        label = "Color"
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp),
        modifier = modifier.fillMaxWidth().selectable(
            selected = selectedLetter == letter,
            onClick = onSelect,
            enabled = enabled,
            role = Role.Checkbox
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "$letter.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(Padding.SMALL.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (selectedLetter?.equals(letter) == false) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                color = if (selectedLetter == letter) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(0.9f)
            )
        }
    }
}

@Composable
private fun QuestionSubmitButton(
    onSubmit: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) = Button(
    onClick = onSubmit,
    enabled = enabled,
    contentPadding = PaddingValues(Padding.SMALL.dp),
    shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp),
    modifier = modifier
) {
    Text(
        text = stringResource(Res.string.submit_question_button_label),
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
private fun CancelQuizButton(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) = OutlinedButton(
    onClick = onCancel,
    modifier = modifier.fillMaxWidth().padding(Padding.MEDIUM.dp),
    shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp),
    contentPadding = PaddingValues(Padding.SMALL.dp),
    enabled = enabled,
) {
    Text(
        text = stringResource(Res.string.cancel_button_label),
        style = MaterialTheme.typography.labelSmall
    )
}


@Composable
private fun QuizResultDialog(
    quiz: Quiz,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        icon = { Icon(painter = painterResource(Res.drawable.ic_quiz), contentDescription = null) },
        title = {
            Text(
                text = stringResource(Res.string.quiz_result_dialog_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = { QuizResultContent(quiz = quiz, modifier = Modifier.fillMaxWidth()) },
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                contentPadding = PaddingValues(Padding.SMALL.dp),
                shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp)
            ) {
                Text(
                    text = stringResource(Res.string.dismiss_button_label),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    )
}

@Composable
private fun QuizResultContent(
    quiz: Quiz,
    modifier: Modifier = Modifier
) {
    val getAnswer = fun(question: Question, answer: Any): String {
        return when (question) {
            is Question.MultipleChoice -> question.options.find { it.letter == answer }?.value ?: ""
            is Question.TrueFalse      -> answer.toString()
        }
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Padding.SMALL.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${stringResource(Res.string.quiz_score)}: ${quiz.score}/${quiz.maxScore}",
            style = MaterialTheme.typography.bodyMedium
        )
        quiz.questions.zip(quiz.answers).forEachIndexed { i, (question, answer) ->
            if (i > 0) HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Text(
                text = "${i.inc()}. ${question.text}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (answer != question.correctAnswer) Text(
                text = "${stringResource(Res.string.correct_answer)} -> ${getAnswer(question, question.correctAnswer)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${stringResource(Res.string.your_answer)} -> ${getAnswer(question, answer)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (answer == question.correctAnswer) Color(0xFF4CAF50) else Color(0xFFD32F2F)
            )
        }
    }
}



