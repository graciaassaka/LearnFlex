package org.example.composeApp.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.component.CustomCircularProgressIndicator
import org.example.composeApp.component.CustomLazyGridScrollbar
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.dimension.Elevation
import org.example.composeApp.dimension.Padding
import org.example.composeApp.dimension.Spacing
import org.example.composeApp.layout.VerticalBarGraph
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
        Pair("Mon", 0.5f),
        Pair("Tue", 0.7f),
        Pair("Wed", 0.3f),
        Pair("Thu", 0.9f),
        Pair("Fri", 0.6f),
        Pair("Sat", 0.8f),
        Pair("Sun", 0.4f)
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

    val curriculumProgress = 0.75f

    val gridState = rememberLazyGridState()

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(2f)
                .padding(Padding.MEDIUM.dp)
                .fillMaxHeight()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(200.dp),
                state = gridState,
                contentPadding = PaddingValues(Padding.MEDIUM.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    WelcomeSection(
                        username = username,
                        isVisible = true
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(Spacing.LARGE.dp))
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    WeeklyActivitiesSection(
                        weeklyActivities = weeklyActivities
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(Spacing.LARGE.dp))
                }

                items(modules) { module ->
                    ModuleCard(
                        module = module,
                        onModuleClicked = { moduleId -> }
                    )
                }
            }
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) CustomLazyGridScrollbar(
                gridState = gridState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) Box(
            modifier = Modifier
                .weight(1f)
                .padding(Padding.MEDIUM.dp)
                .fillMaxHeight()
        ) {
            CurriculumSection(
                curriculumProgress = curriculumProgress,
                curricula = curricula,
                onCurriculumClicked = { curriculumId -> }
            )
        }
    }
}

@Composable
private fun WelcomeSection(
    username: String,
    isVisible: Boolean,
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
                Column(
                    modifier = Modifier
                        .height(this@BoxWithConstraints.maxHeight / 3)
                        .width(this@BoxWithConstraints.maxWidth * 2 / 3),
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
                Card(
                    onClick = { onCurriculumSelected() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimension.CORNER_RADIUS_MEDIUM.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(Elevation.MEDIUM.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(Padding.MEDIUM.dp)
                            .height(this@BoxWithConstraints.maxHeight * 2 / 3)
                            .width(this@BoxWithConstraints.maxWidth * 2 / 3),
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
private fun WeeklyActivitiesSection(
    weeklyActivities: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
) = Card(
    modifier = modifier
        .height(200.dp)
        .fillMaxWidth(),
    shape = RoundedCornerShape(Dimension.CORNER_RADIUS_MEDIUM.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    elevation = CardDefaults.cardElevation(Elevation.MEDIUM.dp)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Padding.MEDIUM.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.weekly_activity),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
        )
        VerticalBarGraph(
            data = weeklyActivities,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ModuleCard(
    module: Module,
    onModuleClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) = Box(modifier = modifier.height(250.dp)) {
    Card(
        onClick = { onModuleClicked(module.id) },
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.padding(Padding.SMALL.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.SMALL.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(module.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.height((250.dp - Padding.SMALL.dp) * 2 / 3),
                contentScale = ContentScale.Crop
            )
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

@Composable
private fun CurriculumSection(
    curriculumProgress: Float,
    curricula: List<Curriculum>,
    onCurriculumClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(Padding.MEDIUM.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.LARGE.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimension.CORNER_RADIUS_MEDIUM.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(Elevation.MEDIUM.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Padding.MEDIUM.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.curriculum_progress),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                CustomCircularProgressIndicator(
                    progress = curriculumProgress,
                    size = 150.dp,
                    strokeWidth = 16.dp,
                    textStyle = MaterialTheme.typography.titleMedium,
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.completed_curricula),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp)
            ) {
                items(curricula) { curriculum ->
                    CurriculumCard(
                        curriculum = curriculum,
                        onCurriculumClicked = onCurriculumClicked
                    )
                }
            }
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

    Card(
        onClick = { onCurriculumClicked(curriculum.id) },
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Padding.SMALL.dp),
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