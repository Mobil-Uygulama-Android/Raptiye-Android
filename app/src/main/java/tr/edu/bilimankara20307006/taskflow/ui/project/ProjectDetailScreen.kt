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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.model.ProjectStatus
import tr.edu.bilimankara20307006.taskflow.data.model.Task
import tr.edu.bilimankara20307006.taskflow.data.model.User
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

/**
 * Proje Detay Ekranı
 * Projeye tıklandığında açılır - Resimde gösterilen tasarım
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBackClick: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onAddMemberClick: (String) -> Unit = {},
    onAddTaskClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Firebase'den projeyi ve görevleri yükle
    var project by remember { mutableStateOf<Project?>(null) }
    var isLoadingProject by remember { mutableStateOf(true) }
    var showAnalytics by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var isArchived by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val repository = remember { tr.edu.bilimankara20307006.taskflow.data.repository.ProjectRepository.getInstance() }
    val coroutineScope = rememberCoroutineScope()
    val viewModel: ProjectListViewModel = viewModel()
    
    // Current user ve proje sahipliği kontrolü
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isProjectOwner = currentUser?.uid == project?.ownerId
    
    // Refresh fonksiyonu
    val refreshData = suspend {
        println("🔄 Proje verisi yenileniyor...")
        val result = repository.getProjectById(projectId)
        result.onSuccess { loadedProject ->
            println("✅ Proje yüklendi: ${loadedProject.title}")
            println("👥 TeamLeader: ${loadedProject.teamLeader?.email}")
            println("👥 TeamMembers: ${loadedProject.teamMembers.size}")
            project = loadedProject
            isLoadingProject = false
        }.onFailure { error ->
            println("❌ Proje yükleme hatası: ${error.message}")
            isLoadingProject = false
        }
    }
    
    // Projeyi yükle - ilk açılışta ve her 5 saniyede bir (iOS gibi real-time)
    LaunchedEffect(projectId) {
        isLoadingProject = true
        refreshData()
        
        // Her 5 saniyede bir otomatik yenile (iOS'taki gibi)
        while (true) {
            delay(5000)
            refreshData()
        }
    }
    
    // Loading state
    if (isLoadingProject || project == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    val currentProject = project!!
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    
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
    
    // iOS gibi gerçek project data'sını kullan
    // TeamLeader yoksa, owner'dan oluştur veya teamMembers'tan bul
    val teamLeader = remember(currentProject) {
        currentProject.teamLeader ?: currentProject.teamMembers.find { it.uid == currentProject.ownerId }
    }
    
    val teamMembers = remember(currentProject.teamMembers, currentProject.ownerId) {
        // Proje liderini listeden çıkar (iOS'taki gibi)
        currentProject.teamMembers.filter { it.uid != currentProject.ownerId }
    }
    
    // Snackbar host state for error messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error message as snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            errorMessage = null
        }
    }
    
    // Firebase'den gerçek görevleri yükle
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoadingTasks by remember { mutableStateOf(true) }
    
    // Görevleri yenile
    val taskRepository = remember { tr.edu.bilimankara20307006.taskflow.data.repository.TaskRepository.getInstance() }
    val refreshTasks = suspend {
        val result = taskRepository.getTasksByProject(projectId)
        result.onSuccess { taskList ->
            println("✅ ${taskList.size} görev yüklendi")
            tasks = taskList
            isLoadingTasks = false
        }.onFailure { error ->
            println("❌ Görev yükleme hatası: ${error.message}")
            isLoadingTasks = false
        }
    }
    
    LaunchedEffect(projectId) {
        refreshTasks()
        
        // Her 5 saniyede bir görevleri yenile
        while (true) {
            delay(5000)
            refreshTasks()
        }
    }
    
    // Analytics ekranını göster (tasks yüklendikten sonra)
    if (showAnalytics) {
        ProjectAnalyticsDetailScreen(
            project = currentProject,
            tasks = tasks,
            onBackClick = { showAnalytics = false }
        )
        return
    }
    
    // Görev sayılarını hesapla (iOS'taki gibi)
    val tasksCount = tasks.size
    val completedTasksCount = tasks.count { it.status == tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.COMPLETED }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    // iOS gibi grafik butonu
                    IconButton(onClick = { showAnalytics = true }) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = localizationManager.localizedString("Statistics"),
                            tint = Color(0xFF4CAF50)
                        )
                    }
                    
                    // iOS gibi üç nokta menüsü
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menü",
                                tint = textColor
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Projeyi Düzenle
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            localizationManager.localizedString("EditProject"),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    showEditDialog = true
                                }
                            )
                            
                            HorizontalDivider()
                            
                            // Proje sahibiyse "Projeyi Sil", değilse "Projeden Ayrıl"
                            if (isProjectOwner) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color(0xFFFF3B30),
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Text(
                                                localizationManager.localizedString("DeleteProject"),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFFFF3B30)
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.ExitToApp,
                                                contentDescription = null,
                                                tint = Color(0xFFFF9500),
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Text(
                                                if (localizationManager.currentLocale == "tr") "Projeden Ayrıl" else "Leave Project",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFFFF9500)
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        showLeaveDialog = true
                                    }
                                )
                            }
                        }
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
                            text = currentProject.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        // Project Description
                        Text(
                            text = currentProject.description,
                            fontSize = 16.sp,
                            color = textSecondaryColor,
                            lineHeight = 24.sp
                        )
                        
                        // Due Date
                        currentProject.dueDate?.let { dateString ->
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
                                    text = "${localizationManager.localizedString("DueDate")}: $dateString",
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
                                text = "$completedTasksCount/$tasksCount",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondaryColor
                            )
                        }
                        
                        // Progress Bar
                        val progressPercentage = if (tasksCount > 0) completedTasksCount.toFloat() / tasksCount.toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { progressPercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF66D68C),
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
                    // Team Header with Add Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizationManager.localizedString("Team"),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        // Add Member Button - iOS gibi
                        TextButton(
                            onClick = {
                                onAddMemberClick(currentProject.id)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = localizationManager.localizedString("AddMember"),
                                tint = Color(0xFF66D69A),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = localizationManager.localizedString("AddMember"),
                                color = Color(0xFF66D69A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Team Leader
                    teamLeader?.let { leader ->
                        Text(
                            text = localizationManager.localizedString("TeamLeader"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondaryColor
                        )
                        
                        TeamMemberItem(
                            user = leader,
                            isLeader = true,
                            taskCount = 1
                        )
                    }
                    
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
                            onClick = { onAddTaskClick(projectId) },
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
        
        // Silme Onay Dialogu - iOS gibi
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "Projeyi Sil",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Bu projeyi silmek istediğinizden emin misiniz?",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Proje: ${currentProject.title}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondaryColor
                        )
                        Text(
                            text = "Bu işlem geri alınamaz. Tüm görevler ve veriler kalıcı olarak silinecektir.",
                            fontSize = 14.sp,
                            color = Color(0xFFFF3B30)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            println("🗑️ Proje silme işlemi başlatılıyor: ${currentProject.id}")
                            // ViewModel üzerinden sil - hem Firebase'den hem de local listeden kaldırır
                            viewModel.deleteProject(currentProject.id)
                            // UI'dan hemen geri dön (state otomatik güncellenecek)
                            println("⬅️ Geri dönülüyor...")
                            onBackClick()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFFF3B30)
                        )
                    ) {
                        Text(
                            "Sil",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text(
                            "İptal",
                            fontSize = 16.sp
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
        }
        
        // Projeden Ayrıl Dialogu
        if (showLeaveDialog) {
            val currentProject = project
            if (currentProject != null && currentUser != null) {
                AlertDialog(
                    onDismissRequest = { showLeaveDialog = false },
                    icon = {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color(0xFFFF9500),
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    title = {
                        Text(
                            text = if (localizationManager.currentLocale == "tr") 
                                "Projeden Ayrıl?" else "Leave Project?",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9500)
                        )
                    },
                    text = {
                        Text(
                            text = if (localizationManager.currentLocale == "tr")
                                "\"${currentProject.title}\" projesinden ayrılmak istediğinize emin misiniz? Bu projeye tekrar erişmek için yeniden davet edilmeniz gerekecek."
                            else "Are you sure you want to leave \"${currentProject.title}\"? You'll need to be invited again to access this project.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLeaveDialog = false
                                println("🚪 Projeden ayrılma işlemi başlatılıyor: ${currentProject.id}")
                                // ViewModel üzerinden ayrıl - hem Firebase'den hem de local listeden kaldırır
                                viewModel.leaveProject(currentProject.id, currentUser.uid)
                                // UI'dan hemen geri dön (state otomatik güncellenecek)
                                println("⬅️ Geri dönülüyor...")
                                onBackClick()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFFF9500)
                            )
                        ) {
                            Text(
                                if (localizationManager.currentLocale == "tr") "Ayrıl" else "Leave",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showLeaveDialog = false }
                        ) {
                            Text(
                                if (localizationManager.currentLocale == "tr") "İptal" else "Cancel",
                                fontSize = 16.sp
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
        
        // Düzenleme Dialogu - Tüm alanlar düzenlenebilir
        if (showEditDialog) {
            var editTitle by remember { mutableStateOf(currentProject.title) }
            var editDescription by remember { mutableStateOf(currentProject.description) }
            var editIconName by remember { mutableStateOf(currentProject.iconName) }
            var editIconColor by remember { mutableStateOf(currentProject.iconColor) }
            var editStatus by remember { mutableStateOf(currentProject.status) }
            var editDueDate by remember { mutableStateOf(currentProject.dueDate ?: "") }
            var showIconPicker by remember { mutableStateOf(false) }
            var showColorPicker by remember { mutableStateOf(false) }
            var showStatusPicker by remember { mutableStateOf(false) }
            var showDatePicker by remember { mutableStateOf(false) }
            
            // İkon seçenekleri
            val iconOptions = listOf(
                "work" to Icons.Default.Work,
                "folder" to Icons.Default.Folder,
                "home" to Icons.Default.Home,
                "code" to Icons.Default.Code,
                "school" to Icons.Default.School,
                "favorite" to Icons.Default.Favorite,
                "star" to Icons.Default.Star,
                "settings" to Icons.Default.Settings
            )
            
            // Renk seçenekleri - iOS renkleri
            val colorOptions = listOf(
                "#4CAF50" to "Yeşil",
                "#2196F3" to "Mavi",
                "#FF9500" to "Turuncu",
                "#FF3B30" to "Kırmızı",
                "#9C27B0" to "Mor",
                "#607D8B" to "Gri",
                "#00BCD4" to "Turkuaz",
                "#FF9800" to "Amber"
            )
            
            // Durum seçenekleri
            val statusOptions = listOf(ProjectStatus.ACTIVE, ProjectStatus.COMPLETED, ProjectStatus.ARCHIVED)
            val statusLabels = mapOf(
                ProjectStatus.ACTIVE to "Aktif",
                ProjectStatus.COMPLETED to "Tamamlandı",
                ProjectStatus.ARCHIVED to "Arşivlendi"
            )
            
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Projeyi Düzenle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Proje Adı
                        item {
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("Proje Adı") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Açıklama
                        item {
                            OutlinedTextField(
                                value = editDescription,
                                onValueChange = { editDescription = it },
                                label = { Text("Açıklama") },
                                minLines = 3,
                                maxLines = 5,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // İkon Seçimi
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "İkon",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    iconOptions.forEach { (name, icon) ->
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (editIconName == name) Color(0xFF4CAF50).copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable { editIconName = name },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                icon,
                                                contentDescription = name,
                                                tint = if (editIconName == name) Color(0xFF4CAF50)
                                                      else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Renk Seçimi
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Renk",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    colorOptions.forEach { (hex, label) ->
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(Color(android.graphics.Color.parseColor(hex)))
                                                .clickable { editIconColor = hex },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (editIconColor == hex) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Seçili",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Durum Seçimi
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Durum",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    statusOptions.forEach { status ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (editStatus == status) Color(0xFF4CAF50).copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable { editStatus = status }
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                        ) {
                                            Text(
                                                text = statusLabels[status] ?: status.name,
                                                fontSize = 14.sp,
                                                fontWeight = if (editStatus == status) FontWeight.Bold else FontWeight.Normal,
                                                color = if (editStatus == status) Color(0xFF4CAF50)
                                                       else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Bitiş Tarihi
                        item {
                            OutlinedTextField(
                                value = editDueDate,
                                onValueChange = { editDueDate = it },
                                label = { Text("Bitiş Tarihi (yyyy-MM-dd)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        // Tarih seçici açılabilir (basitlik için manuel giriş)
                                    }) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = "Tarih Seç")
                                    }
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showEditDialog = false
                            coroutineScope.launch {
                                repository.updateProject(
                                    projectId = currentProject.id,
                                    title = editTitle,
                                    description = editDescription,
                                    iconName = editIconName,
                                    iconColor = editIconColor,
                                    status = editStatus.name.lowercase(),
                                    dueDate = editDueDate.ifEmpty { null }
                                )
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            "Kaydet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEditDialog = false }
                    ) {
                        Text(
                            "İptal",
                            fontSize = 16.sp
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
        }
        
        // Paylaşım Dialogu
        if (showShareDialog) {
            AlertDialog(
                onDismissRequest = { showShareDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFF66D68C),
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Projeyi Paylaş",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Proje: ${currentProject.title}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Proje ID: ${currentProject.id}",
                            fontSize = 14.sp,
                            color = textSecondaryColor
                        )
                        Text(
                            text = "Bu projeyi paylaşmak için proje ID'sini kopyalayın ve gönderin.",
                            fontSize = 14.sp,
                            color = textSecondaryColor
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showShareDialog = false
                            // Clipboard'a kopyala
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Project ID", currentProject.id)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Proje ID kopyalandı", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF66D68C)
                        )
                    ) {
                        Text(
                            "Kopyala",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showShareDialog = false }
                    ) {
                        Text(
                            "Kapat",
                            fontSize = 16.sp
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
        }
        
        // Bildirim Dialogu
        if (showNotificationDialog) {
            var notificationsEnabled by remember { mutableStateOf(true) }
            
            AlertDialog(
                onDismissRequest = { showNotificationDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFFFF9500),
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Proje Bildirimleri",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Proje: ${currentProject.title}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Bildirimleri Aç",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Yeni görevler ve güncellemeler",
                                    fontSize = 13.sp,
                                    color = textSecondaryColor
                                )
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF66D68C)
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showNotificationDialog = false
                            // TODO: Bildirim tercihini kaydet
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFFF9500)
                        )
                    ) {
                        Text(
                            "Kaydet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showNotificationDialog = false }
                    ) {
                        Text(
                            "İptal",
                            fontSize = 16.sp
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
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
                        text = user.initials,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = avatarColor
                    )
                }
                
                Column {
                    Text(
                        text = user.displayName ?: "Kullanıcı",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = user.email ?: "",
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
    
    // iOS gibi öncelik renkleri
    val priorityColor = when (task.priority.lowercase()) {
        "yüksek", "high" -> Color(0xFFFF3B30)
        "orta", "medium" -> Color(0xFFFF9500)
        "düşük", "low" -> Color(0xFF34C759)
        else -> Color(0xFF8E8E93)
    }
    
    val priorityText = when (task.priority.lowercase()) {
        "yüksek", "high" -> "Yüksek"
        "orta", "medium" -> "Orta"
        "düşük", "low" -> "Düşük"
        else -> task.priority
    }
    
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
            // iOS gibi soldaki renkli çizgi
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .background(priorityColor, RoundedCornerShape(2.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
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
                        color = textColor,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Priority badge - iOS gibi
                        Surface(
                            color = priorityColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = priorityText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = priorityColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        // Due date - iOS gibi format
                        task.dueDate?.let { dateString ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = textSecondaryColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = dateString,
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

/**
 * Proje Analytics Detay Ekranı - iOS gibi
 * Gerçek proje verilerini gösterir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectAnalyticsDetailScreen(
    project: Project,
    tasks: List<Task>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // İstatistikleri hesapla
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.status == tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.COMPLETED }
    val inProgressTasks = tasks.count { it.status == tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.IN_PROGRESS }
    val todoTasks = tasks.count { it.status == tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.TODO }
    val completionRate = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks * 100).toInt() else 0
    
    // Öncelik dağılımı
    val highPriorityTasks = tasks.count { it.priority.lowercase() in listOf("yüksek", "high") }
    val mediumPriorityTasks = tasks.count { it.priority.lowercase() in listOf("orta", "medium") }
    val lowPriorityTasks = tasks.count { it.priority.lowercase() in listOf("düşük", "low") }
    
    // Takım performansı
    val teamMembers = project.teamMembers
    val teamPerformance = teamMembers.map { member ->
        val memberTasks = tasks.filter { it.assignee?.uid == member.uid }
        val memberCompleted = memberTasks.count { it.status == tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.COMPLETED }
        Triple(member, memberTasks.size, memberCompleted)
    }
    
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = cardBackground,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = textColor
                            )
                        }
                        Column {
                            Text(
                                text = localizationManager.localizedString("Statistics"),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = project.title,
                                fontSize = 14.sp,
                                color = textSecondaryColor
                            )
                        }
                    }
                    
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        containerColor = darkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // Genel Bakış
            item {
                Text(
                    text = localizationManager.localizedString("Overview"),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            // Tamamlanma Oranı Kartı - iOS gradient stili
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50),
                                    Color(0xFF66D69A)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = localizationManager.localizedString("Completion"),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "%$completionRate",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "$completedTasks",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = localizationManager.localizedString("Completed"),
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(40.dp)
                                    .background(Color.White.copy(alpha = 0.3f))
                            )
                            
                            Column {
                                Text(
                                    text = "$totalTasks",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = localizationManager.localizedString("Total"),
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Görev Durumu
            item {
                Text(
                    text = localizationManager.localizedString("TaskStatus"),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Yapılacak - iOS glassmorphism
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF8E8E93).copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Circle,
                                contentDescription = null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "$todoTasks",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = localizationManager.localizedString("Todo"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = textSecondaryColor
                            )
                        }
                    }
                    
                    // Devam Eden
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFF9500).copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color(0xFFFF9500),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "$inProgressTasks",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = localizationManager.localizedString("InProgress"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = textSecondaryColor
                            )
                        }
                    }
                    
                    // Tamamlanan
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF34C759).copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF34C759),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "$completedTasks",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = localizationManager.localizedString("Completed"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = textSecondaryColor
                            )
                        }
                    }
                }
            }
            
            // Öncelik Dağılımı
            item {
                Text(
                    text = localizationManager.localizedString("PriorityDistribution"),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PriorityCard(
                        label = localizationManager.localizedString("HighPriority"),
                        count = highPriorityTasks,
                        total = totalTasks,
                        color = Color(0xFFFF3B30),
                        icon = Icons.Default.Error
                    )
                    PriorityCard(
                        label = localizationManager.localizedString("MediumPriority"),
                        count = mediumPriorityTasks,
                        total = totalTasks,
                        color = Color(0xFFFF9500),
                        icon = Icons.Default.Warning
                    )
                    PriorityCard(
                        label = localizationManager.localizedString("LowPriority"),
                        count = lowPriorityTasks,
                        total = totalTasks,
                        color = Color(0xFF34C759),
                        icon = Icons.Default.Info
                    )
                }
            }
            
            // Takım Performansı
            if (teamPerformance.isNotEmpty()) {
                item {
                    Text(
                        text = localizationManager.localizedString("TeamPerformance"),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                items(teamPerformance.size) { index ->
                    val (member, totalMemberTasks, completedMemberTasks) = teamPerformance[index]
                    val memberCompletionRate = if (totalMemberTasks > 0) 
                        (completedMemberTasks.toFloat() / totalMemberTasks * 100).toInt() else 0
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = cardBackground,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar - iOS gradient
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(
                                                androidx.compose.ui.graphics.Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFF4CAF50),
                                                        Color(0xFF66D69A)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.initials,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    
                                    Column {
                                        Text(
                                            text = member.displayName ?: member.email ?: "Kullanıcı",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Text(
                                            text = "$completedMemberTasks / $totalMemberTasks görev",
                                            fontSize = 14.sp,
                                            color = textSecondaryColor
                                        )
                                    }
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "%$memberCompletionRate",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Text(
                                        text = "tamamlandı",
                                        fontSize = 12.sp,
                                        color = textSecondaryColor
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(textSecondaryColor.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(memberCompletionRate / 100f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF4CAF50),
                                                    Color(0xFF66D69A)
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Öncelik kartı bileşeni - iOS stili
 */
@Composable
private fun PriorityCard(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBackground = MaterialTheme.colorScheme.surface
    val percentage = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardBackground,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(26.dp)
                    )
                }
                
                Column {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = "$count görev",
                        fontSize = 14.sp,
                        color = textSecondaryColor
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "%$percentage",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                
                // Mini progress indicator
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(textSecondaryColor.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percentage / 100f)
                            .background(color, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}
