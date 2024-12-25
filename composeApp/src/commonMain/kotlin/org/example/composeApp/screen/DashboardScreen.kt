package org.example.composeApp.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
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
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.delay
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.component.*
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.dimension.Elevation
import org.example.composeApp.dimension.Padding
import org.example.composeApp.dimension.Spacing
import org.example.composeApp.layout.VerticalBarGraphLayout
import org.example.composeApp.navigation.AppDestination
import org.example.composeApp.util.TestTags
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.viewModel.DashboardViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.time.DayOfWeek
import kotlin.random.Random

/**
 * Enum class representing various dimensions used in the UI measured in Dp.
 * Each enum value corresponds to a specific UI component's dimensional requirement.
 *
 * @property dp The dimension value in Dp for the associated UI component.
 */
private enum class Dimensions(val dp: Dp) {
    MIN_GRID_CELL_SIZE(200.dp),
    WELCOME_SECTION_HEIGHT(200.dp),
    WEEKLY_ACTIVITY_SECTION_HEIGHT(200.dp),
    MODULE_CARD_HEIGHT(250.dp),
    PROGRESS_INDICATOR_STROKE_WIDTH(12.dp),
}

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
@Composable
fun DashboardScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by remember { mutableStateOf<SnackbarType>(SnackbarType.Info) }

    val state by viewModel.state.collectAsState()
    val isScreenVisible by viewModel.isScreenVisible.collectAsState()

    HandleUIEvents(Route.CreateProfile, navController, viewModel, snackbarHostState) { currentSnackbarType = it }

    val itemsCompletion = listOf(
        Triple(
            Collection.MODULES.value,
            state.moduleCountByStatus[Status.FINISHED] ?: 0,
            state.moduleCountByStatus.values.sum()
        ),
        Triple(
            Collection.LESSONS.value,
            state.lessonCountByStatus[Status.FINISHED] ?: 0,
            state.lessonCountByStatus.values.sum()
        ),
        Triple(
            Collection.SECTIONS.value,
            state.sectionCountByStatus[Status.FINISHED] ?: 0,
            state.sectionCountByStatus.values.sum()
        )
    )

    val totalMinutes = state.weeklyActivity.values.sumOf { it.first.toInt() }
    val averageMinutes = if (state.weeklyActivity.isNotEmpty()) totalMinutes / state.weeklyActivity.size else 0

    val fabInteractionSource = remember { MutableInteractionSource() }
    val isFabHovered by fabInteractionSource.collectIsHoveredAsState()
    val lazyGridState = rememberLazyGridState()

    CustomScaffold(
        snackbarHostState = snackbarHostState,
        snackbarType = currentSnackbarType,
        currentDestination = AppDestination.Dashboard,
        onDestinationSelected = { viewModel.navigate(it.route, true) },
        enabled = !state.isLoading
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(Dimensions.MIN_GRID_CELL_SIZE.dp),
                    contentPadding = PaddingValues(Padding.MEDIUM.dp),
                    state = lazyGridState
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        WelcomeSection(
                            isVisible = isScreenVisible,
                            isLoading = state.isLoading,
                            modifier = Modifier.testTag(TestTags.DASHBOARD_WELCOME_SECTION.tag),
                            curriculum = state.activeCurriculum,
                            onCurriculumSelected = viewModel::onCurriculumClicked
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(Spacing.LARGE.dp))
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = stringResource(Res.string.weekly_activity),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(Spacing.MEDIUM.dp))
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        WeeklyActivitiesSection(
                            isLoading = state.isLoading,
                            windowSizeClass = windowSizeClass,
                            weeklyActivities = state.weeklyActivity,
                            totalMinutes = totalMinutes,
                            averageMinutes = averageMinutes,
                            modifier = Modifier.testTag(TestTags.DASHBOARD_WEEKLY_ACTIVITY_SECTION.tag)
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(Spacing.LARGE.dp))
                    }

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
                        Spacer(modifier = Modifier.height(Spacing.MEDIUM.dp))
                    }

                    items(state.modules) { module ->
                        ModuleCard(
                            isLoading = state.isLoading,
                            module = module,
                            modifier = Modifier
                                .padding(Padding.SMALL.dp)
                                .testTag("module_card_${module.id}"),
                            onModuleClicked = viewModel::onModuleClicked
                        )
                    }
                }
                CustomVerticalScrollbar(
                    lazyGridState = lazyGridState,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
                FloatingActionButton(
                    onClick = { viewModel.fetchAllUserData() },
                    interactionSource = fabInteractionSource,
                    modifier = Modifier
                        .padding(Padding.MEDIUM.dp)
                        .align(Alignment.BottomEnd)
                        .hoverable(interactionSource = fabInteractionSource)
                        .testTag(TestTags.DASHBOARD_REFRESH_FAB.tag)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(Padding.SMALL.dp)
                            .animateContentSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Refresh, stringResource(Res.string.refresh_button_label))
                        if (isFabHovered) Text(stringResource(Res.string.refresh_button_label))
                    }
                }
            }
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                CurriculumSection(
                    isLoading = state.isLoading,
                    curricula = state.curricula,
                    itemCompletions = itemsCompletion,
                    onCurriculumClicked = viewModel::onCurriculumClicked,
                    modifier = Modifier.testTag(TestTags.DASHBOARD_CURRICULUM_SECTION.tag)
                )
            }
        }
    }
}

@Composable
private fun WelcomeSection(
    isVisible: Boolean,
    isLoading: Boolean,
    onCurriculumSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    curriculum: Curriculum? = null
) {
    val visible by remember { mutableStateOf(isVisible) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1000))
    ) {
        BoxWithConstraints(
            modifier = modifier
                .height(Dimensions.WELCOME_SECTION_HEIGHT.dp)
                .fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
                horizontalAlignment = Alignment.Start
            ) {
                TitleSection(
                    isLoading = isLoading,
                    modifier = Modifier
                        .height(this@BoxWithConstraints.maxHeight / 3)
                        .width(this@BoxWithConstraints.maxWidth * 2 / 3)
                        .testTag(TestTags.DASHBOARD_TITLE_SECTION.tag)
                )
                WelcomeCard(
                    isLoading = isLoading,
                    curriculum = curriculum,
                    maxHeight = this@BoxWithConstraints.maxHeight,
                    maxWidth = this@BoxWithConstraints.maxWidth,
                    onCurriculumSelected = onCurriculumSelected,
                    modifier = Modifier.testTag(TestTags.DASHBOARD_WELCOME_CARD.tag)
                )
            }
            Image(
                painter = painterResource(Res.drawable.welcome),
                contentDescription = null,
                modifier = Modifier
                    .size(minOf(maxWidth / 3, maxHeight))
                    .align(Alignment.CenterEnd)
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
        modifier = Modifier
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
    isLoading: Boolean,
    windowSizeClass: WindowSizeClass,
    weeklyActivities: Map<DayOfWeek, Pair<Long, Int>>,
    totalMinutes: Int,
    averageMinutes: Int,
    modifier: Modifier = Modifier,
) = if (isLoading) {
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
                windowSizeClass = windowSizeClass,
                weeklyActivities = weeklyActivities,
                totalMinutes = totalMinutes,
                averageMinutes = averageMinutes,
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
        modifier = Modifier.padding(Padding.SMALL.dp),
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
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(module.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.weight(0.75f),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1 / 4f)
                    .padding(Padding.SMALL.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = module.title,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "${stringResource(Res.string.best_quiz_score)} ${module.quizScore}",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun CurriculumSection(
    isLoading: Boolean,
    curricula: List<Curriculum>,
    itemCompletions: List<Triple<String, Int, Int>>,
    onCurriculumClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) = if (isLoading) {
    Box(modifier = modifier.fillMaxSize().shimmerEffect())
} else {
    val lazyState = rememberLazyListState()
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
        CurriculumContent(curricula, itemCompletions, onCurriculumClicked, lazyState)
        CustomVerticalScrollbar(
            lazyListState = lazyState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
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

@Composable
private fun CurriculumContent(
    curricula: List<Curriculum>,
    itemCompletions: List<Triple<String, Int, Int>>,
    onCurriculumClicked: (String) -> Unit,
    lazyState: LazyListState,
    modifier: Modifier = Modifier
) = LazyColumn(
    modifier = modifier
        .fillMaxSize()
        .padding(horizontal = Padding.MEDIUM.dp),
    state = lazyState,
    verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    item {
        Text(
            text = stringResource(Res.string.curriculum_progress),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
    item { CompletionCard(itemCompletions) }
    item {
        Text(
            text = stringResource(Res.string.completed_curricula),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
    items(curricula) { curriculum ->
        CurriculumCard(
            curriculum = curriculum,
            onCurriculumClicked = onCurriculumClicked
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
            modifier = Modifier
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
            Box(
                modifier = Modifier
                    .width(Padding.SMALL.dp)
                    .background(
                        color = barColor,
                        shape = RoundedCornerShape(Dimension.CORNER_RADIUS_SMALL.dp)
                    )
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
private fun CurriculumCard(
    curriculum: Curriculum,
    onCurriculumClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val containerColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.background,
        animationSpec = tween(durationMillis = 300)
    )
    val contentColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(durationMillis = 300)
    )

    Card(
        onClick = { onCurriculumClicked(curriculum.id) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(Elevation.SMALL.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Padding.SMALL.dp, horizontal = Padding.MEDIUM.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = curriculum.title,
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) stringResource(Res.string.show_less_button_label)
                        else stringResource(Res.string.show_more_button_label)
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    text = curriculum.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}