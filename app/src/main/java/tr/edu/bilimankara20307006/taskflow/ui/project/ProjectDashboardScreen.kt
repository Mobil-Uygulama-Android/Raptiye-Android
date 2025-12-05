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
import tr.edu.bilimankara20307006.taskflow.data.model.Task
import tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Proje Panosu Ekranı - iOS ProjectDashboard ile birebir uyumlu
 * Tüm projelerdeki görevleri durumlarına göre (Yapılacaklar, Devam Ediyor, Tamamlandı) gösterir
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProjectDashboardScreen(
    onBackClick: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onProjectClick: (Project) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale
    
    // Renk tanımları
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val selectedTabColor = Color(0xFF32D74B) // iOS yeşili
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // ViewModel
    val viewModel: ProjectListViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    
    // Tab'lar
    val tabs = listOf(
        localizationManager.localizedString("Todo"),
        localizationManager.localizedString("InProgress"),
        localizationManager.localizedString("Completed")
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    // Tüm projelerdeki görevleri al
    var allTasks by remember { mutableStateOf<List<Pair<Task, Project>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(state.projects) {
        isLoading = true
        val taskList = mutableListOf<Pair<Task, Project>>()
        val taskRepository = tr.edu.bilimankara20307006.taskflow.data.repository.TaskRepository.getInstance()
        
        state.projects.forEach { project ->
            try {
                val result = taskRepository.getTasksByProject(project.id)
                result.onSuccess { tasks ->
                    tasks.forEach { task ->
                        taskList.add(task to project)
                    }
                }
            } catch (e: Exception) {
                println("❌ Proje ${project.title} görevleri alınamadı: ${e.message}")
            }
        }
        allTasks = taskList
        isLoading = false
    }
    
    // Görevleri durumlarına göre filtrele
    val todoTasks = remember(allTasks) {
        allTasks.filter { it.first.status == TaskStatus.TODO }
    }
    val inProgressTasks = remember(allTasks) {
        allTasks.filter { it.first.status == TaskStatus.IN_PROGRESS }
    }
    val completedTasks = remember(allTasks) {
        allTasks.filter { it.first.status == TaskStatus.COMPLETED }
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
    
    Scaffold(
        containerColor = darkBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(topBarAlpha)
            ) {
                // Top Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(cardBackground)
                        .padding(horizontal = 8.dp)
                ) {
                    // Geri butonu
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizationManager.localizedString("Back"),
                            tint = textColor
                        )
                    }
                    
                    // Başlık
                    Text(
                        text = localizationManager.localizedString("ProjectDashboard"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                // Tab Bar
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = cardBackground,
                    contentColor = textColor,
                    indicator = { },
                    divider = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(tabsAlpha)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = if (pagerState.currentPage == index) 
                                        FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (pagerState.currentPage == index) 
                                        selectedTabColor else textSecondaryColor
                                )
                                if (pagerState.currentPage == index) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .width(40.dp)
                                            .height(3.dp)
                                            .background(
                                                selectedTabColor,
                                                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            val tasks = when (page) {
                0 -> todoTasks
                1 -> inProgressTasks
                2 -> completedTasks
                else -> emptyList()
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = selectedTabColor)
                }
            } else if (tasks.isEmpty()) {
                // Boş durum
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (page) {
                                0 -> Icons.Default.TaskAlt
                                1 -> Icons.Default.Sync
                                2 -> Icons.Default.CheckCircle
                                else -> Icons.Default.TaskAlt
                            },
                            contentDescription = null,
                            tint = textSecondaryColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (page) {
                                0 -> localizationManager.localizedString("NoTodoTasks")
                                1 -> localizationManager.localizedString("NoInProgressTasks")
                                2 -> localizationManager.localizedString("NoCompletedTasks")
                                else -> ""
                            },
                            fontSize = 16.sp,
                            color = textSecondaryColor.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks, key = { it.first.id }) { (task, project) ->
                        TaskCardWithProject(
                            task = task,
                            project = project,
                            onClick = { onTaskClick(task) },
                            onProjectClick = { onProjectClick(project) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCardWithProject(
    task: Task,
    project: Project,
    onClick: () -> Unit,
    onProjectClick: () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // Proje ikonu rengi
    val projectColor = when (project.iconColor) {
        "blue" -> Color(0xFF007AFF)
        "purple" -> Color(0xFFAF52DE)
        "pink" -> Color(0xFFFF2D55)
        "red" -> Color(0xFFFF3B30)
        "orange" -> Color(0xFFFF9500)
        "yellow" -> Color(0xFFFFCC00)
        "green" -> Color(0xFF34C759)
        "teal" -> Color(0xFF5AC8FA)
        "indigo" -> Color(0xFF5856D6)
        else -> Color(0xFF007AFF)
    }
    
    // Öncelik rengi
    val priorityColor = when (task.priority) {
        "high" -> Color(0xFFFF3B30)
        "medium" -> Color(0xFFFF9500)
        "low" -> Color(0xFF34C759)
        else -> textSecondaryColor
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Proje bilgisi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onProjectClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(projectColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (project.iconName) {
                            "folder" -> Icons.Default.Folder
                            "briefcase" -> Icons.Default.Work
                            "chart" -> Icons.Default.BarChart
                            else -> Icons.Default.Folder
                        },
                        contentDescription = null,
                        tint = projectColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = project.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = projectColor,
                    modifier = Modifier.weight(1f)
                )
                
                // Öncelik göstergesi
                if (task.priority.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(priorityColor, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Görev başlığı
            Text(
                text = task.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Görev açıklaması
            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    color = textSecondaryColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Alt bilgiler
            if (!task.dueDate.isNullOrEmpty() || task.assigneeId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bitiş tarihi
                    if (!task.dueDate.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = textSecondaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = task.dueDate,
                                fontSize = 12.sp,
                                color = textSecondaryColor
                            )
                        }
                    }
                    
                    // Atanan kişi
                    if (task.assigneeId.isNotEmpty()) {
                        val assigneeName = remember(task.assigneeId, project.teamMembers) {
                            // Önce teamMembers'tan bul
                            val member = project.teamMembers.find { it.uid == task.assigneeId }
                            member?.displayName ?: member?.email ?: 
                            // Bulamazsan teamLeader'dan bak
                            if (project.teamLeader?.uid == task.assigneeId) {
                                project.teamLeader?.displayName ?: project.teamLeader?.email
                            } else {
                                task.assigneeId
                            }
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = textSecondaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = assigneeName ?: task.assigneeId,
                                fontSize = 12.sp,
                                color = textSecondaryColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
