package org.example.composeApp.presentation.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.alexzhirkevich.compottie.*
import kotlinx.coroutines.delay
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.presentation.action.DashboardAction
import org.example.composeApp.presentation.navigation.AppDestination
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.DashboardUIState
import org.example.composeApp.presentation.ui.component.*
import org.example.composeApp.presentation.ui.constant.TestTags
import org.example.composeApp.presentation.ui.dimension.Dimension
import org.example.composeApp.presentation.ui.dimension.Elevation
import org.example.composeApp.presentation.ui.dimension.Padding
import org.example.composeApp.presentation.ui.dimension.Spacing
import org.example.composeApp.presentation.ui.layout.VerticalBarGraphLayout
import org.example.composeApp.presentation.ui.util.HandleUIEvents
import org.example.composeApp.presentation.ui.util.ScreenConfig
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.composeApp.presentation.viewModel.DashboardViewModel
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.util.*
import kotlin.random.Random

/**
 * Enum class representing various dimensions used in the UI measured in Dp.
 * Each enum value corresponds to a specific UI component's dimensional requirement.
 *
 * @property dp The dimension value in Dp for the associated UI component.
 */
private enum class Dimensions(val dp: Dp) {
    MIN_GRID_CELL_SIZE(200.dp),
    WEEKLY_ACTIVITY_SECTION_HEIGHT(200.dp),
    MODULE_CARD_HEIGHT(150.dp),
    PROGRESS_INDICATOR_STROKE_WIDTH(12.dp),
}

/**
 * A private list of file paths referencing specific Lottie animation files.
 * These files are associated with certain activities such as meditation,
 * reading, teaching, studying, and online tests.
 */
private val lottieFiles = listOf(
    "files/meditation.json",
    "files/reading.json",
    "files/teaching.json",
    "files/studying.json"
)

/**
 * A list of drawable resources representing various images of owls in a library setting.
 * These images can be used as background pictures in the dashboard.
 */
private val backgroundPictures = listOf(
    Res.drawable.owl_in_library,
    Res.drawable.barn_owl_on_books,
    Res.drawable.owl_flying_in_library,
    Res.drawable.owl_on_books
)

/**
 * A composable function representing the Dashboard screen of the application.
 *
 * @param windowSizeClass The size class of the window, used to adapt UI components to different screen sizes.
 * @param navController The navigation controller used to navigate between screens in the app.
 * @param viewModel The ViewModel that manages the state and logic for the Dashboard screen.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DashboardScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val screenConfig = ScreenConfig(
        windowSizeClass = windowSizeClass,
        snackbarHostState = remember { SnackbarHostState() },
        snackbarType = remember { mutableStateOf(SnackbarType.Info) },
        uiState = viewModel.state.collectAsStateWithLifecycle(),
        isScreenVisible = viewModel.isScreenVisible.collectAsStateWithLifecycle()
    )
    val navigator = rememberSupportingPaneScaffoldNavigator()
    ThreePaneBackHandler(navigator = navigator)

    HandleUIEvents(Route.Dashboard, navController, viewModel, screenConfig.snackbarHostState) {
        screenConfig.snackbarType.value = it
    }

    CustomScaffold(
        snackbarHostState = screenConfig.snackbarHostState,
        snackbarType = screenConfig.snackbarType.value,
        currentDestination = AppDestination.Dashboard,
        onDestinationSelected = { viewModel.handleAction(DashboardAction.Navigate(it.route)) },
        onRefresh = { viewModel.handleAction(DashboardAction.Refresh) },
        enabled = !screenConfig.uiState.value.isLoading
    ) { paddingValues ->
        SupportingPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            mainPane = {
                MainPane(
                    screenConfig = screenConfig,
                    handleAction = viewModel::handleAction,
                    modifier = Modifier.padding(paddingValues)
                )
            },
            supportingPane = {
                SupportingPane(
                    screenConfig = screenConfig,
                    paddingValues = paddingValues
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ThreePaneScaffoldScope.MainPane(
    screenConfig: ScreenConfig<DashboardUIState>,
    handleAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier,
) = with(screenConfig) {
    val lazyGridState = rememberLazyGridState()

    AnimatedPane(modifier = modifier.safeDrawingPadding()) {
        RefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = uiState.value.isLoading,
            onRefresh = { handleAction(DashboardAction.Refresh) }
        ) {
            DashboardContent(
                screenConfig = screenConfig,
                handleAction = handleAction,
                lazyGridState = lazyGridState
            )
            CustomVerticalScrollbar(
                lazyGridState = lazyGridState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun DashboardContent(
    screenConfig: ScreenConfig<DashboardUIState>,
    handleAction: (DashboardAction) -> Unit,
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier
) = LazyVerticalGrid(
    columns = GridCells.Adaptive(Dimensions.MIN_GRID_CELL_SIZE.dp),
    contentPadding = PaddingValues(Padding.MEDIUM.dp),
    state = lazyGridState,
    modifier = modifier
) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        WelcomeSection(
            screenConfig = screenConfig,
            handleAction = handleAction,
            modifier = Modifier.testTag(TestTags.DASHBOARD_WELCOME_SECTION.tag)
        )
    }
    item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.Companion.height(Spacing.LARGE.dp)) }
    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = stringResource(Res.string.weekly_activity),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
    item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.Companion.height(Spacing.MEDIUM.dp)) }
    item(span = { GridItemSpan(maxLineSpan) }) {
        WeeklyActivitiesSection(
            screenConfig = screenConfig,
            modifier = Modifier.testTag(TestTags.DASHBOARD_WEEKLY_ACTIVITY_SECTION.tag)
        )
    }
    item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.Companion.height(Spacing.LARGE.dp)) }
    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = stringResource(Res.string.modules),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
    item(span = { GridItemSpan(maxLineSpan) }) {
        Spacer(modifier = Modifier.Companion.height(Spacing.MEDIUM.dp))
    }
    items(screenConfig.uiState.value.modules) { module ->
        ModuleCard(
            isLoading = screenConfig.uiState.value.isLoading,
            module = module,
            modifier = Modifier.Companion
                .padding(Padding.SMALL.dp)
                .testTag("module_card_${module.id}"),
            onModuleClicked = { handleAction(DashboardAction.OpenModule(it)) }
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun WelcomeSection(
    screenConfig: ScreenConfig<DashboardUIState>,
    handleAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier
) = with(screenConfig) {
    val visible by remember { mutableStateOf(isScreenVisible.value) }
    val lottieComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(Res.readBytes(lottieFiles[Random.nextInt(0, lottieFiles.size)]).decodeToString())
    }
    val lottiAnimProgress by animateLottieCompositionAsState(
        composition = lottieComposition,
        iterations = Compottie.IterateForever,
    )
    val boxModifier = if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Expanded) {
        modifier.fillMaxWidth()
    } else {
        modifier.fillMaxWidth().requiredHeightIn(min = 200.dp, max = 250.dp)
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1000))
    ) {
        BoxWithConstraints(modifier = boxModifier) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
                horizontalAlignment = Alignment.Start
            ) {
                TitleSection(
                    isLoading = uiState.value.isLoading,
                    modifier = Modifier
                        .height(this@BoxWithConstraints.maxHeight / 3)
                        .width(this@BoxWithConstraints.maxWidth * 2 / 3)
                        .testTag(TestTags.DASHBOARD_TITLE_SECTION.tag)
                )
                WelcomeCard(
                    isLoading = uiState.value.isLoading,
                    curriculum = uiState.value.curriculum,
                    maxHeight = this@BoxWithConstraints.maxHeight,
                    maxWidth = this@BoxWithConstraints.maxWidth,
                    onCurriculumSelected = { handleAction(DashboardAction.OpenCurriculum) },
                    modifier = Modifier.testTag(TestTags.DASHBOARD_WELCOME_CARD.tag)
                )
            }
            Image(
                painter = rememberLottiePainter(composition = lottieComposition, progress = { lottiAnimProgress }),
                contentDescription = null,
                modifier = Modifier.size(minOf(maxWidth / 3, maxHeight)).align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun TitleSection(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) = if (isLoading) {
    Box(modifier = modifier.shimmerEffect())
} else {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(Res.string.welcome_to),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(Res.string.app_name),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = MaterialTheme.typography.displayMedium.fontSize.times(0.5f),
            style = MaterialTheme.typography.displayMedium,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun WelcomeCard(
    isLoading: Boolean,
    curriculum: Curriculum?,
    maxHeight: Dp,
    maxWidth: Dp,
    onCurriculumSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) = if (isLoading) Box(
    modifier = modifier
        .fillMaxWidth()
        .height(maxHeight * 2 / 3)
        .shimmerEffect()
) else Card(
    onClick = { curriculum?.let { onCurriculumSelected(it.id) } },
    modifier = modifier.fillMaxWidth(),
    shape = RectangleShape,
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    elevation = CardDefaults.cardElevation(Elevation.MEDIUM.dp)
) {
    Column(
        modifier = Modifier.Companion
            .padding(Padding.MEDIUM.dp)
            .height(maxHeight * 2 / 3)
            .width(maxWidth * 2 / 3),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = curriculum?.title ?: (stringResource(Res.string.hi_there)),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = curriculum?.description ?: stringResource(Res.string.welcome_message),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun WeeklyActivitiesSection(
    screenConfig: ScreenConfig<DashboardUIState>,
    modifier: Modifier = Modifier
) = if (screenConfig.uiState.value.isLoading) {
    Box(
        modifier = modifier
            .height(Dimensions.WEEKLY_ACTIVITY_SECTION_HEIGHT.dp)
            .shadow(elevation = Elevation.MEDIUM.dp)
            .fillMaxWidth()
            .shimmerEffect()
    )
} else {
    var selectedDay by remember { mutableStateOf("") }
    var showPopup by remember { mutableStateOf(false) }
    var backgroundPictureIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30000L)
            backgroundPictureIndex = Random.nextInt(0, backgroundPictures.size)
        }
    }

    BackgroundContainer(
        painter = painterResource(backgroundPictures[backgroundPictureIndex]),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Padding.MEDIUM.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WeeklyActivitiesContent(
                windowSizeClass = screenConfig.windowSizeClass,
                weeklyActivities = screenConfig.uiState.value.weeklyActivity,
                totalMinutes = screenConfig.uiState.value.totalMinutes,
                averageMinutes = screenConfig.uiState.value.averageMinutes,
                selectedDay = selectedDay,
                showPopup = showPopup,
                onDaySelected = { selectedDay = it; showPopup = true },
                onDismissPopup = { showPopup = false }
            )
        }
    }
}


@Composable
private fun BackgroundContainer(
    painter: Painter,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val backgroundColors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.75f)
    )
    Box(
        modifier = modifier
            .height(Dimensions.WEEKLY_ACTIVITY_SECTION_HEIGHT.dp)
            .shadow(elevation = Elevation.MEDIUM.dp)
            .fillMaxWidth()
    ) {
        AnimatedContent(
            targetState = painter,
            transitionSpec = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down) + fadeIn() togetherWith
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeOut()
            }
        ) { targetPainter ->
            Image(
                painter = targetPainter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.horizontalGradient(colors = backgroundColors))
        )
        content()
    }
}

@Composable
private fun WeeklyActivitiesContent(
    windowSizeClass: WindowSizeClass,
    weeklyActivities: Map<DayOfWeek, Pair<Long, Int>>,
    totalMinutes: Int,
    averageMinutes: Int,
    selectedDay: String,
    showPopup: Boolean,
    onDaySelected: (String) -> Unit,
    onDismissPopup: () -> Unit
) = Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    if (
        windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded && totalMinutes != 0 && averageMinutes != 0
    ) {
        ActivitySummary(
            totalMinutes = totalMinutes,
            averageMinutes = averageMinutes,
            modifier = Modifier.weight(0.25f)
        )
    }
    VerticalBarGraphLayout(
        data = weeklyActivities.map { (day, timeSpent) -> day.name.take(3) to timeSpent.first.toFloat() },
        onBarClicked = onDaySelected,
    )
    Box(modifier = Modifier.weight(0.25f)) {
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded && showPopup)
            weeklyActivities.entries.find { it.key.name.take(3) == selectedDay }?.let { (day, timeSpent) ->
                DayDetailsPopup(
                    day = day.name,
                    timeSpent = timeSpent.first.toFloat(),
                    sessions = timeSpent.second,
                    onDismiss = onDismissPopup
                )
            }
    }
}


@Composable
private fun DayDetailsPopup(
    day: String,
    timeSpent: Float,
    sessions: Int,
    onDismiss: () -> Unit
) = Popup(
    onDismissRequest = onDismiss,
    properties = PopupProperties(clippingEnabled = false),
    alignment = Alignment.Center
) {
    Column(
        modifier = Modifier.Companion.padding(Padding.SMALL.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = day,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "${stringResource(Res.string.total_minutes)}: $timeSpent",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "${stringResource(Res.string.sessions)}: $sessions",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ActivitySummary(
    totalMinutes: Int,
    averageMinutes: Int,
    modifier: Modifier = Modifier
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
    horizontalAlignment = Alignment.Start
) {
    Text(
        text = stringResource(Res.string.activity_summary),
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = "${stringResource(Res.string.total_minutes)}: $totalMinutes",
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        text = "${stringResource(Res.string.average_daily_minutes)}: $averageMinutes",
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun ModuleCard(
    isLoading: Boolean,
    module: Module,
    onModuleClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) = if (isLoading) {
    Box(modifier = modifier.height(Dimensions.MODULE_CARD_HEIGHT.dp).shimmerEffect())
} else {
    Box(modifier = modifier.height(Dimensions.MODULE_CARD_HEIGHT.dp)) {
        Card(
            onClick = { onModuleClicked(module.id) },
            shape = RectangleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            elevation = CardDefaults.cardElevation(Elevation.SMALL.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1 / 4f)
                    .padding(Padding.SMALL.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = module.title,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                Text(
                    text = "${stringResource(Res.string.best_quiz_score)} ${module.quizScore}",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Last Active On:  ${SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(Date(module.lastUpdated))}",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ThreePaneScaffoldScope.SupportingPane(
    screenConfig: ScreenConfig<DashboardUIState>,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) = AnimatedPane(modifier = modifier.safeDrawingPadding()) {
    Row(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.fillMaxHeight()) {
            CurriculumSection(
                screenConfig = screenConfig,
                modifier = Modifier.testTag(TestTags.DASHBOARD_CURRICULUM_SECTION.tag)
            )
        }
    }
}

@Composable
private fun CurriculumSection(
    screenConfig: ScreenConfig<DashboardUIState>,
    modifier: Modifier = Modifier
) = if (screenConfig.uiState.value.isLoading) {
    Box(modifier = modifier.fillMaxSize().shimmerEffect())
} else {
    val brushColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )
    val infiniteTransition = rememberInfiniteTransition(label = "colorTransition")
    val targetOffset = with(LocalDensity.current) { 1000.dp.toPx() }
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = targetOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 50000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(brushColors, offset)
        SyllabusSection(screenConfig.uiState.value.itemsCompletion)
    }
}

@Composable
private fun AnimatedBackground(
    brushColors: List<Color>,
    offset: Float,
    modifier: Modifier = Modifier
) = Box(
    modifier = modifier
        .fillMaxSize()
        .blur(40.dp)
        .drawWithCache {
            val brushSize = 400f
            val brush = Brush.linearGradient(
                colors = brushColors,
                start = Offset(offset, offset),
                end = Offset(offset + brushSize, offset + brushSize),
                tileMode = TileMode.Mirror
            )
            onDrawBehind {
                drawRect(brush = brush)
            }
        }
)

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun SyllabusSection(
    itemCompletions: List<Triple<String, Int, Int>>,
    modifier: Modifier = Modifier
) {
    val composition = rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/library.json").decodeToString()
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Padding.MEDIUM.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.curriculum_progress),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        CompletionCard(itemCompletions)
        Image(
            painter = rememberLottiePainter(
                composition = composition.value,
                isPlaying = true,
                iterations = Compottie.IterateForever
            ),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun CompletionCard(
    itemCompletions: List<Triple<String, Int, Int>>,
    modifier: Modifier = Modifier
) {
    val barColors = MutableList(itemCompletions.size) { i -> remember(i) { mutableStateOf(Color.Gray) } }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_MEDIUM.dp),
        elevation = CardDefaults.cardElevation(Elevation.MEDIUM.dp)
    ) {
        Column(
            modifier = Modifier.Companion
                .padding(Padding.MEDIUM.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemCompletions.forEachIndexed { i, (label, completed, total) ->
                    Box(modifier = Modifier.weight(1f)) {
                        CompletionStatus(
                            label = label,
                            completed = completed,
                            total = total,
                            textColor = MaterialTheme.colorScheme.onBackground,
                            barColor = barColors[i].value
                        )
                    }
                }
            }
            Box {
                itemCompletions.forEachIndexed { i, (label, completed, total) ->
                    val progress = completed.toFloat() / total
                    CustomCircularProgressIndicator(
                        label = label,
                        progress = if (progress.isNaN()) 0f else progress,
                        modifier = Modifier.align(Alignment.Center),
                        size = (200 - i * 40).dp,
                        strokeWidth = Dimensions.PROGRESS_INDICATOR_STROKE_WIDTH.dp,
                        progressValueTextStyle = MaterialTheme.typography.displayMedium,
                        progressValuesTextColor = MaterialTheme.colorScheme.primary,
                        onProgressColorChange = { color -> barColors[i].value = color }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionStatus(
    label: String,
    completed: Int,
    total: Int,
    textColor: Color,
    barColor: Color,
    modifier: Modifier = Modifier
) = Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Layout(
        content = {
            CompletionStatusBar(
                completed = completed,
                total = total,
                label = label,
                textColor = textColor,
                barColor = barColor
            )
        }
    ) { measurables, constraints ->
        val barMeasurable = measurables.find { it.layoutId == "bar" }!!
        val textMeasurable = measurables.find { it.layoutId == "text" }!!

        val totalWeight = 1f
        val barWeight = 0.2f
        val textWeight = 0.8f

        val textPlaceable = textMeasurable.measure(
            constraints.copy(
                maxWidth = (constraints.maxWidth * (textWeight / totalWeight)).toInt()
            )
        )
        val barPlaceable = barMeasurable.measure(
            constraints.copy(
                maxWidth = (constraints.maxWidth * (barWeight / totalWeight)).toInt(),
                minHeight = textPlaceable.height,
                maxHeight = textPlaceable.height
            )
        )
        val totalWidth = barPlaceable.width + textPlaceable.width
        val totalHeight = textPlaceable.height

        layout(totalWidth, totalHeight) {
            barPlaceable.placeRelative(
                x = 0,
                y = 0
            )

            textPlaceable.placeRelative(
                x = barPlaceable.width + Spacing.SMALL.dp.roundToPx(),
                y = 0
            )
        }
    }
}

@Composable
fun CompletionStatusBar(
    completed: Int,
    total: Int,
    label: String,
    textColor: Color,
    barColor: Color
) {
    Box(
        modifier = Modifier.Companion
            .width(Padding.SMALL.dp)
            .background(color = barColor, shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp))
            .layoutId("bar")
    )
    Column(
        modifier = Modifier.layoutId("text"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$completed/$total",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
