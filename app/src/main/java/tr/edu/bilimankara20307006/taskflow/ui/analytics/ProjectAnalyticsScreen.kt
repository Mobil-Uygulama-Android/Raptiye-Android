package tr.edu.bilimankara20307006.taskflow.ui.analytics

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.model.ProjectAnalytics
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi

/**
 * Proje Analizi Ekranı
 * Ekran görüntüsündeki "Project Analytics" tasarımına birebir uygun
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProjectAnalyticsScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    
    // Tema renklerini MaterialTheme'den al
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val selectedTabColor = Color(0xFF4CAF50)
    val unselectedTabColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // Tab'lar
    val tabs = listOf(
        localizationManager.localizedString("Overview"),
        localizationManager.localizedString("Progress"),
        localizationManager.localizedString("Team")
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    // Analytics verisi
    val analytics = remember { ProjectAnalytics.sampleAnalytics("project-1") }
    
    // Animation states
    var topBarVisible by remember { mutableStateOf(false) }
    var tabsVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true
        delay(150)
        tabsVisible = true
    }
    
    val topBarAlpha by animateFloatAsState(
        targetValue = if (topBarVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "topBarAlpha"
    )
    
    val tabsAlpha by animateFloatAsState(
        targetValue = if (tabsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "tabsAlpha"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizationManager.localizedString("ProjectAnalytics"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizationManager.localizedString("Back"),
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground
                ),
                modifier = Modifier.alpha(topBarAlpha)
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Custom Tab Row
            CustomTabRow(
                tabs = tabs,
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .alpha(tabsAlpha)
            )
            
            // Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> OverviewTab(analytics = analytics, localizationManager = localizationManager)
                    1 -> ProgressTab(localizationManager = localizationManager)
                    2 -> TeamTab(localizationManager = localizationManager)
                }
            }
        }
    }
}

/**
 * Custom Tab Row - Ekran görüntüsündeki stile uygun
 */
@Composable
private fun CustomTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = Color(0xFF4CAF50)
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tabBackground = MaterialTheme.colorScheme.surfaceVariant
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(tabBackground, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selectedTabIndex == index) selectedColor else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selectedTabIndex == index) Color.White else unselectedColor
                )
            }
        }
    }
}

/**
 * Overview Tab - Ana istatistikler
 */
@Composable
private fun OverviewTab(
    analytics: ProjectAnalytics,
    localizationManager: LocalizationManager,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Task Completion Rate Card
        item {
            TaskCompletionRateCard(analytics = analytics, localizationManager = localizationManager)
        }
        
        // Project Timeline Card
        item {
            ProjectTimelineCard(analytics = analytics, localizationManager = localizationManager)
        }
    }
}

/**
 * Progress Tab - Placeholder
 */
@Composable
private fun ProgressTab(localizationManager: LocalizationManager) {
    val textColor = MaterialTheme.colorScheme.onBackground
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = localizationManager.localizedString("ProgressContentHere"),
            color = textColor,
            fontSize = 16.sp
        )
    }
}

/**
 * Team Tab - Placeholder
 */
@Composable
private fun TeamTab(localizationManager: LocalizationManager) {
    val textColor = MaterialTheme.colorScheme.onBackground
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = localizationManager.localizedString("TeamContentHere"),
            color = textColor,
            fontSize = 16.sp
        )
    }
}

/**
 * Task Completion Rate Card
 */
@Composable
private fun TaskCompletionRateCard(
    analytics: ProjectAnalytics,
    localizationManager: LocalizationManager,
    modifier: Modifier = Modifier
) {
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val changeColor = if (analytics.completionRateChange >= 0) Color(0xFF32D74B) else Color(0xFFFF453A)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = localizationManager.localizedString("TaskCompletionRate"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondaryColor
                )
                
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${analytics.taskCompletionRate}%",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localizationManager.localizedString("Last30Days"),
                        fontSize = 14.sp,
                        color = textSecondaryColor
                    )
                    
                    Text(
                        text = "${if (analytics.completionRateChange >= 0) "+" else ""}${analytics.completionRateChange}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = changeColor
                    )
                }
            }
            
            // Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Completed
                BarChartItem(
                    label = localizationManager.localizedString("Completed"),
                    value = analytics.completedTasks,
                    maxValue = analytics.completedTasks + analytics.inProgressTasks + analytics.pendingTasks,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
                // In Progress
                BarChartItem(
                    label = localizationManager.localizedString("InProgress"),
                    value = analytics.inProgressTasks,
                    maxValue = analytics.completedTasks + analytics.inProgressTasks + analytics.pendingTasks,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
                // Pending
                BarChartItem(
                    label = localizationManager.localizedString("Pending"),
                    value = analytics.pendingTasks,
                    maxValue = analytics.completedTasks + analytics.inProgressTasks + analytics.pendingTasks,
                    color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Bar Chart Item
 */
@Composable
private fun BarChartItem(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val heightFraction = if (maxValue > 0) (value.toFloat() / maxValue.toFloat()) else 0f
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(heightFraction.coerceIn(0.2f, 1f))
                    .background(color, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            )
        }
        
        // Label
        Text(
            text = label,
            fontSize = 12.sp,
            color = textSecondaryColor,
            maxLines = 1
        )
    }
}

/**
 * Project Timeline Card
 */
@Composable
private fun ProjectTimelineCard(
    analytics: ProjectAnalytics,
    localizationManager: LocalizationManager,
    modifier: Modifier = Modifier
) {
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val changeColor = if (analytics.timelineChange >= 0) Color(0xFF32D74B) else Color(0xFFFF453A)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = localizationManager.localizedString("ProjectTimeline"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondaryColor
                )
                
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${analytics.projectTimelineDays} ${localizationManager.localizedString("Days")}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localizationManager.localizedString("CurrentProject"),
                        fontSize = 14.sp,
                        color = textSecondaryColor
                    )
                    
                    Text(
                        text = "${analytics.timelineChange}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = changeColor
                    )
                }
            }
            
            // Line Chart
            LineChart(
                data = analytics.weeklyData,
                localizationManager = localizationManager,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        }
    }
}

/**
 * Line Chart
 */
@Composable
private fun LineChart(
    data: List<tr.edu.bilimankara20307006.taskflow.data.model.WeekData>,
    localizationManager: LocalizationManager,
    modifier: Modifier = Modifier
) {
    val lineColor = Color(0xFF4CAF50)
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chart
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (data.isEmpty()) return@Canvas
            
            val maxValue = data.maxOf { it.value }
            val minValue = data.minOf { it.value }
            val range = maxValue - minValue
            
            val stepX = size.width / (data.size - 1).coerceAtLeast(1)
            
            // Draw line
            val path = Path()
            data.forEachIndexed { index, weekData ->
                val x = index * stepX
                val y = size.height - ((weekData.value - minValue) / range * size.height)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            drawPath(
                path = path,
                color = lineColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )
        }
        
        // Week labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { weekData ->
                Text(
                    text = "${localizationManager.localizedString("Week")} ${weekData.week}",
                    fontSize = 12.sp,
                    color = textSecondaryColor
                )
            }
        }
    }
}
