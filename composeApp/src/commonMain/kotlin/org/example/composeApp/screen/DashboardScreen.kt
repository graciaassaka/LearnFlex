package org.example.composeApp.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.delay
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.component.CustomCircularProgressIndicator
import org.example.composeApp.component.CustomLazyListVerticalScrollbar
import org.example.composeApp.component.ShimmerBox
import org.example.composeApp.component.shimmerEffect
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.dimension.Elevation
import org.example.composeApp.dimension.Padding
import org.example.composeApp.dimension.Spacing
import org.example.composeApp.layout.VerticalBarGraphLayout
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardScreen(
    windowSizeClass: WindowSizeClass,
) {
    val username = "John Doe"
    val weeklyActivities = listOf(
        Triple("Monday", 0.5f, 2),
        Triple("Tuesday", 0.7f, 3),
        Triple("Wednesday", 0.3f, 1),
        Triple("Thursday", 0.9f, 2),
        Triple("Friday", 0.6f, 2),
        Triple("Saturday", 0.8f, 1),
        Triple("Sunday", 0.4f, 3)
    )

    val itemsCompletion = listOf(
        Triple("Modules", 4, 6),
        Triple("Lessons", 3, 5),
        Triple("Sections", 2, 3)
    )

    val curricula = listOf(
        Curriculum(
            id = "curriculum1",
            imageUrl = "https://example.com/image1.jpg",
            syllabus = "Syllabus 1",
            description = "Description 1",
            status = "active",
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        ),
        Curriculum(
            id = "curriculum2",
            imageUrl = "https://example.com/image2.jpg",
            syllabus = "Syllabus 2",
            description = "Description 2",
            status = "inactive",
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        ),
        Curriculum(
            id = "curriculum3",
            imageUrl = "https://example.com/image3.jpg",
            syllabus = "Syllabus 3",
            description = "Description 3",
            status = "active",
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        ),
        Curriculum(
            id = "curriculum4",
            imageUrl = "https://example.com/image4.jpg",
            syllabus = "Syllabus 4",
            description = "Description 4",
            status = "inactive",
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    )

    val modules = listOf(
        Module(
            id = "1",
            title = "Module 1",
            imageUrl = "https://universemagazine.com/wp-content/uploads/2022/08/zm4nfgq29yi91-1536x1536-1.jpg",
            description = "This is the first module.",
            index = 1,
            quizScore = 80,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        ),
        Module(
            id = "2",
            title = "Module 2",
            imageUrl = "https://universemagazine.com/wp-content/uploads/2022/08/zm4nfgq29yi91-1536x1536-1.jpg",
            description = "This is the second module.",
            index = 2,
            quizScore = 90,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        ),
        Module(
            id = "3",
            title = "Module 3",
            imageUrl = "https://universemagazine.com/wp-content/uploads/2022/08/zm4nfgq29yi91-1536x1536-1.jpg",
            description = "This is the third module.",
            index = 3,
            quizScore = 70,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        ),
        Module(
            id = "4",
            title = "Module 4",
            imageUrl = "https://universemagazine.com/wp-content/uploads/2022/08/zm4nfgq29yi91-1536x1536-1.jpg",
            description = "This is the fourth module.",
            index = 4,
            quizScore = 60,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        ),
        Module(
            id = "5",
            title = "Module 5",
            imageUrl = "https://universemagazine.com/wp-content/uploads/2022/08/zm4nfgq29yi91-1536x1536-1.jpg",
            description = "This is the fifth module.",
            index = 5,
            quizScore = 50,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        ),
        Module(
            id = "6",
            title = "Module 6",
            imageUrl = "https://universemagazine.com/wp-content/uploads/2022/08/zm4nfgq29yi91-1536x1536-1.jpg",
            description = "This is the sixth module.",
            index = 6,
            quizScore = 40,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    )

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(5000)
        isLoading = false
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(200.dp),
                contentPadding = PaddingValues(Padding.MEDIUM.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    WelcomeSection(
                        username = username,
                        isLoading = isLoading,
                        isVisible = true
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(Spacing.LARGE.dp))
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ShimmerBox(
                        isLoading = isLoading,
                        height = 32.dp,
                        width = 100.dp
                    ) {
                        Text(
                            text = stringResource(Res.string.weekly_activity),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    WeeklyActivitiesSection(
                        isLoading = isLoading,
                        windowSizeClass = windowSizeClass,
                        weeklyActivities = weeklyActivities,
                        totalMinutes = 300,
                        averageMinutes = 45
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(Spacing.LARGE.dp))
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    ShimmerBox(
                        isLoading = isLoading,
                        height = 32.dp,
                        width = 100.dp
                    ) {
                        Text(
                            text = stringResource(Res.string.modules),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                items(modules) { module ->
                    ModuleCard(
                        isLoading = isLoading,
                        module = module,
                        modifier = Modifier.padding(Padding.SMALL.dp),
                        onModuleClicked = { moduleId -> }
                    )
                }
            }
        }
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            CurriculumSection(
                isLoading = isLoading,
                curricula = curricula,
                itemCompletions = itemsCompletion,
                onCurriculumClicked = { curriculumId -> }
            )
        }
    }
}

@Composable
private fun WelcomeSection(
    username: String,
    isVisible: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    curriculum: Curriculum? = null,
    onCurriculumSelected: () -> Unit = {}
) {
    val visible by remember { mutableStateOf(isVisible) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1000))
    ) {
        BoxWithConstraints(
            modifier = modifier
                .height(200.dp)
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
                )
                WelcomeCard(
                    isLoading = isLoading,
                    username = username,
                    curriculum = curriculum,
                    maxHeight = this@BoxWithConstraints.maxHeight,
                    maxWidth = this@BoxWithConstraints.maxWidth,
                    onCurriculumSelected = onCurriculumSelected
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
        verticalArrangement = Arrangement.spacedBy(Spacing.X_SMALL.dp),
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
            fontSize = MaterialTheme.typography.displayMedium.fontSize.times(0.7f),
            style = MaterialTheme.typography.displayMedium,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun WelcomeCard(
    isLoading: Boolean,
    username: String,
    curriculum: Curriculum?,
    maxHeight: Dp,
    maxWidth: Dp,
    onCurriculumSelected: () -> Unit,
    modifier: Modifier = Modifier
) = if (isLoading) Box(
    modifier = modifier
        .fillMaxWidth()
        .height(maxHeight * 2 / 3)
        .shimmerEffect()
) else Card(
    onClick = onCurriculumSelected,
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
            text = curriculum?.syllabus ?: (stringResource(Res.string.welcome_back) + "$username!"),
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
    weeklyActivities: List<Triple<String, Float, Int>>,
    totalMinutes: Int,
    averageMinutes: Int,
    modifier: Modifier = Modifier,
) = if (isLoading) {
    Box(
        modifier = modifier
            .height(200.dp)
            .shadow(elevation = Elevation.MEDIUM.dp)
            .fillMaxWidth()
            .shimmerEffect()
    )
} else {
    var selectedDay by remember { mutableStateOf("") }
    var showPopup by remember { mutableStateOf(false) }

    BackgroundContainer(modifier) {
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
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val backgroundColors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.75f)
    )
    Box(
        modifier = modifier
            .height(200.dp)
            .shadow(elevation = Elevation.MEDIUM.dp)
            .fillMaxWidth()
    ) {
        Image(
            painter = painterResource(Res.drawable.owl_in_library),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
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
    weeklyActivities: List<Triple<String, Float, Int>>,
    totalMinutes: Int,
    averageMinutes: Int,
    selectedDay: String,
    showPopup: Boolean,
    onDaySelected: (String) -> Unit,
    onDismissPopup: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            ActivitySummary(
                totalMinutes = totalMinutes,
                averageMinutes = averageMinutes,
                modifier = Modifier.weight(0.25f)
            )
        }
        VerticalBarGraphLayout(
            data = weeklyActivities.map { (day, progress, _) -> day.take(3) to progress },
            onBarClicked = onDaySelected,
        )
        Box(modifier = Modifier.weight(0.25f)) {
            if (showPopup) DayDetailsPopup(
                weeklyActivities = weeklyActivities,
                selectedDay = selectedDay,
                onDismiss = onDismissPopup
            )
        }
    }
}

@Composable
private fun DayDetailsPopup(
    weeklyActivities: List<Triple<String, Float, Int>>,
    selectedDay: String,
    onDismiss: () -> Unit
) = Popup(
    onDismissRequest = onDismiss,
    alignment = Alignment.Center
) {
    Column(
        modifier = Modifier.padding(Padding.SMALL.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
        horizontalAlignment = Alignment.Start
    ) {
        weeklyActivities.find { it.first.take(3) == selectedDay }?.let { (day, timeSpent, sessions) ->
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
    Box(modifier = modifier.height(250.dp).shimmerEffect())
} else {
    Box(modifier = modifier.height(250.dp)) {
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
        CustomLazyListVerticalScrollbar(
            lazyListState = lazyState,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
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
        .padding(Padding.MEDIUM.dp),
    state = lazyState,
    verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    item {
        Text(
            text = stringResource(Res.string.curriculum_progress),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
    item { CompletionCard(itemCompletions) }
    item {
        Text(
            text = stringResource(Res.string.completed_curricula),
            color = MaterialTheme.colorScheme.onPrimary,
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
) = Card(
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
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemCompletions.forEach { (label, completed, total) ->
                Box(modifier = Modifier.weight(1f)) {
                    CompletionStatus(
                        label = label,
                        completed = completed,
                        total = total,
                        textColor = Color.Black
                    )
                }
            }
        }
        Box {
            itemCompletions.forEachIndexed { i, (label, completed, total) ->
                CustomCircularProgressIndicator(
                    label = label,
                    progress = completed.toFloat() / total,
                    modifier = Modifier.align(Alignment.Center),
                    size = (200 - i * 40).dp,
                    strokeWidth = 12.dp,
                    progressValueTextStyle = MaterialTheme.typography.displayMedium,
                    progressValuesTextColor = MaterialTheme.colorScheme.primary
                )
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
    modifier: Modifier = Modifier
) = Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Column(
        modifier = Modifier.weight(0.8f),
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
                    text = curriculum.syllabus,
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