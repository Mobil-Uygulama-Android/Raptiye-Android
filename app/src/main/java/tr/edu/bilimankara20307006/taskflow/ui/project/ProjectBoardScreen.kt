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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.model.ProjectStatus
import tr.edu.bilimankara20307006.taskflow.data.model.Task
import tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager

/**
 * Kanban Panosu Ekranı - iOS BoardView ile birebir uyumlu
 * Görevleri durumlarına göre (Yapılacaklar, Devam Ediyor, Tamamlandı) gösterir
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProjectBoardScreen(
    projectId: String? = null,
    onBackClick: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    // Renk tanımları
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val selectedTabColor = Color(0xFF32D74B) // iOS yeşili
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // ViewModel
    val viewModel: ProjectListViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    
    // Tab'lar - Localized
    val tabs = listOf(
        localizationManager.localizedString("Todo"),        // "Yapılacak" / "To Do"
        localizationManager.localizedString("InProgress"),  // "Devam Eden" / "In Progress"
        localizationManager.localizedString("Completed")    // "Tamamlandı" / "Completed"
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    // Seçili proje
    val selectedProject = remember(projectId, state.projects) {
        projectId?.let { id -> state.projects.find { it.id == id } }
    }
    
    // Görevler - seçili proje için
    val tasks = remember(selectedProject) {
        // TODO: Firebase'den gerçek görevleri çekecek
        // Şimdilik sample görevler kullanıyoruz
        selectedProject?.let { Task.sampleTasks(it.id) } ?: emptyList()
    }
    
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
                    text = selectedProject?.title ?: localizationManager.localizedString("ProjectBoard"),
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
                    tasks = tasks.filter { it.status == TaskStatus.TODO },
                    cardBackground = cardBackground,
                    textColor = textColor,
                    textSecondaryColor = textSecondaryColor,
                    localizationManager = localizationManager,
                    onTaskClick = onTaskClick
                )
                1 -> TaskListTab(
                    tasks = tasks.filter { it.status == TaskStatus.IN_PROGRESS },
                    cardBackground = cardBackground,
                    textColor = textColor,
                    textSecondaryColor = textSecondaryColor,
                    localizationManager = localizationManager,
                    onTaskClick = onTaskClick
                )
                2 -> TaskListTab(
                    tasks = tasks.filter { it.status == TaskStatus.COMPLETED },
                    cardBackground = cardBackground,
                    textColor = textColor,
                    textSecondaryColor = textSecondaryColor,
                    localizationManager = localizationManager,
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
    textColor: Color,
    textSecondaryColor: Color,
    localizationManager: LocalizationManager,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tasks.isEmpty()) {
        // Boş durum mesajı
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = textSecondaryColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = localizationManager.localizedString("NoTasksYet"),
                    fontSize = 16.sp,
                    color = textSecondaryColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
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
                    textColor = textColor,
                    textSecondaryColor = textSecondaryColor,
                    localizationManager = localizationManager,
                    onClick = { onTaskClick(task) }
                )
            }
        }
    }
}

/**
 * Görev kartı - iOS TaskCard ile birebir uyumlu
 * Görev başlığı, açıklaması, son teslim tarihi ve atanan kişiyi gösterir
 */
@Composable
private fun TaskCard(
    task: Task,
    cardBackground: Color,
    textColor: Color,
    textSecondaryColor: Color,
    localizationManager: LocalizationManager,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Başlık
            Text(
                text = task.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Açıklama (varsa)
            if (task.description.isNotEmpty()) {
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    color = textSecondaryColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Alt bilgiler: Atanan kişi ve tarih
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Atanan kişi
                if (task.assignee != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Avatar circle
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF32D74B).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = task.assignee.initials,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF32D74B)
                            )
                        }
                        Text(
                            text = task.assignee.displayName ?: task.assignee.email ?: "Unknown",
                            fontSize = 14.sp,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Atanmamış
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = textSecondaryColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = localizationManager.localizedString("Unassigned"),
                            fontSize = 14.sp,
                            color = textSecondaryColor,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                
                // Son teslim tarihi (varsa)
                if (task.dueDate != null && task.dueDate.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = textSecondaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = task.formattedDueDate,
                            fontSize = 12.sp,
                            color = textSecondaryColor
                        )
                    }
                }
            }
        }
    }
}

