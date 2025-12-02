package tr.edu.bilimankara20307006.taskflow.ui.project

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.model.ProjectStatus
import tr.edu.bilimankara20307006.taskflow.data.model.Task

/**
 * Kanban Panosu Ekranı
 * Ekran görüntüsündeki tasarıma birebir uygun
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProjectBoardScreen(
    onBackClick: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Renk tanımları
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val selectedTabColor = Color(0xFF4CAF50)
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // Tab'lar
    val tabs = listOf("Yapılacaklar", "Devam Ediyor", "Tamamlandı")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    // Projeler
    val projects = remember { Project.sampleProjects }
    
    // Görevler - ilk proje için
    val tasks = remember { Task.sampleTasks(projects.firstOrNull()?.id ?: "") }
    
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
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Proje Panosu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = textColor
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = darkBackground
            ),
            modifier = Modifier.alpha(topBarAlpha)
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = darkBackground,
            contentColor = textColor,
            modifier = Modifier.alpha(tabsAlpha),
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    val currentTab = tabPositions[pagerState.currentPage]
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.BottomStart)
                            .offset(x = currentTab.left)
                            .width(currentTab.width)
                            .height(3.dp)
                            .background(selectedTabColor)
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = if (pagerState.currentPage == index) 
                                FontWeight.SemiBold else FontWeight.Normal,
                            color = if (pagerState.currentPage == index) 
                                selectedTabColor else Color.Gray
                        )
                    }
                )
            }
        }
        
        // Horizontal Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> TaskListTab(
                    tasks = tasks.filter { !it.isCompleted },
                    cardBackground = cardBackground,
                    onTaskClick = onTaskClick
                )
                1 -> TaskListTab(
                    tasks = tasks.filter { !it.isCompleted },
                    cardBackground = cardBackground,
                    onTaskClick = onTaskClick
                )
                2 -> TaskListTab(
                    tasks = tasks.filter { it.isCompleted },
                    cardBackground = cardBackground,
                    onTaskClick = onTaskClick
                )
            }
        }
    }
}

/**
 * Görev listesi tab'ı
 */
@Composable
private fun TaskListTab(
    tasks: List<Task>,
    cardBackground: Color,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            TaskCard(
                task = task,
                cardBackground = cardBackground,
                onClick = { onTaskClick(task) }
            )
        }
    }
}

/**
 * Görev kartı - Ekran görüntüsündeki tasarım
 */
@Composable
private fun TaskCard(
    task: Task,
    cardBackground: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol taraf - Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Orta - Başlık ve tarih
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (task.dueDate != null) {
                    Text(
                        text = "Son teslim: ${task.formattedDueDate}",
                        fontSize = 14.sp,
                        color = textSecondaryColor
                    )
                }
            }
        }
    }
}

