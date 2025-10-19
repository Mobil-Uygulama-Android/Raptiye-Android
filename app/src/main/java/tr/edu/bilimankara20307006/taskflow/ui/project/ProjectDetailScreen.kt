package tr.edu.bilimankara20307006.taskflow.ui.project

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.model.Task
import tr.edu.bilimankara20307006.taskflow.data.model.User
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Proje Detay Ekranı
 * Projeye tıklandığında açılır - Resimde gösterilen tasarım
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    project: Project,
    onBackClick: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    
    // Back button handling
    BackHandler(enabled = true) {
        onBackClick()
    }
    
    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var projectInfoVisible by remember { mutableStateOf(false) }
    var teamVisible by remember { mutableStateOf(false) }
    var tasksVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
        delay(150)
        projectInfoVisible = true
        delay(150)
        teamVisible = true
        delay(150)
        tasksVisible = true
    }
    
    val headerAlpha by animateFloatAsState(
        targetValue = if (headerVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )
    
    val projectInfoAlpha by animateFloatAsState(
        targetValue = if (projectInfoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "projectInfoAlpha"
    )
    
    val projectInfoScale by animateFloatAsState(
        targetValue = if (projectInfoVisible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "projectInfoScale"
    )
    
    val teamAlpha by animateFloatAsState(
        targetValue = if (teamVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "teamAlpha"
    )
    
    val tasksAlpha by animateFloatAsState(
        targetValue = if (tasksVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "tasksAlpha"
    )
    
    // Theme colors
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // Sample data (gerçek uygulamada Firebase'den gelecek)
    val teamLeader = remember {
        User("1", "Emily Carter", "emily@example.com")
    }
    
    val teamMembers = remember {
        listOf(
            User("2", "David Lee", "david@example.com"),
            User("3", "Ahmet Yılmaz", "ahmet@example.com")
        )
    }
    
    val tasks = remember {
        Task.sampleTasks(project.id)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizationManager.localizedString("ProjectDetails"),
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
                actions = {
                    IconButton(onClick = { /* Analytics */ }) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "İstatistikler",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground
                ),
                modifier = Modifier.alpha(headerAlpha)
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // Project Info Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(projectInfoScale)
                        .alpha(projectInfoAlpha),
                    color = cardBackground,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Project Title
                        Text(
                            text = project.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        // Project Description
                        Text(
                            text = project.description,
                            fontSize = 16.sp,
                            color = textSecondaryColor,
                            lineHeight = 24.sp
                        )
                        
                        // Due Date
                        project.dueDate?.let { date ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = localizationManager.localizedString("DueDate"),
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${localizationManager.localizedString("DueDate")}: ${SimpleDateFormat("dd MMMM yyyy", Locale(localizationManager.currentLocale)).format(date)}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Progress Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizationManager.localizedString("Progress"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            Text(
                                text = "${project.completedTasksCount}/${project.tasksCount}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondaryColor
                            )
                        }
                        
                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { project.progressPercentage.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF4CAF50),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }
            
            // Team Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(teamAlpha),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = localizationManager.localizedString("Team"),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    // Team Leader
                    Text(
                        text = localizationManager.localizedString("TeamLeader"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondaryColor
                    )
                    
                    TeamMemberItem(
                        user = teamLeader,
                        isLeader = true,
                        taskCount = 1
                    )
                    
                    // Team Members
                    if (teamMembers.isNotEmpty()) {
                        Text(
                            text = localizationManager.localizedString("TeamMembers"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondaryColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        teamMembers.forEach { member ->
                            TeamMemberItem(
                                user = member,
                                isLeader = false
                            )
                        }
                    }
                }
            }
            
            // Tasks Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(tasksAlpha),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizationManager.localizedString("Tasks"),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        IconButton(
                            onClick = { /* Add Task */ },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = localizationManager.localizedString("AddTask"),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Task Items
            items(tasks) { task ->
                TaskListItem(
                    task = task,
                    onClick = { onTaskClick(task) },
                    modifier = Modifier.alpha(tasksAlpha)
                )
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun TeamMemberItem(
    user: User,
    isLeader: Boolean,
    taskCount: Int? = null
) {
    val cardBackground = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardBackground,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with colored background
                val avatarColor = if (isLeader) Color(0xFFFF9F0A) else Color(0xFF4CAF50)
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(avatarColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName.firstOrNull()?.uppercase() ?: "U",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = avatarColor
                    )
                }
                
                Column {
                    Text(
                        text = user.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = textSecondaryColor
                    )
                }
            }
            
            // Task count badge (if provided)
            taskCount?.let { count ->
                Surface(
                    color = Color(0xFF34C759).copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = count.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34C759)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskListItem(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackground = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = cardBackground,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (task.isCompleted) Color(0xFF34C759) else Color.Transparent,
                            CircleShape
                        )
                        .clip(CircleShape)
                        .then(
                            if (!task.isCompleted) {
                                Modifier.background(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    CircleShape
                                )
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Tamamlandı",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Assigned user
                        task.assignee?.let { user ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = textSecondaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = user.displayName,
                                    fontSize = 12.sp,
                                    color = textSecondaryColor
                                )
                            }
                        }
                        
                        // Due date
                        task.dueDate?.let { date ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = textSecondaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date),
                                    fontSize = 12.sp,
                                    color = textSecondaryColor
                                )
                            }
                        }
                    }
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Detay",
                tint = textSecondaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
