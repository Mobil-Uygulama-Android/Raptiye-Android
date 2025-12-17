package tr.edu.bilimankara20307006.taskflow.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.ui.auth.AuthViewModel
import tr.edu.bilimankara20307006.taskflow.ui.notifications.NotificationViewModel
import tr.edu.bilimankara20307006.taskflow.ui.project.ProjectListScreen
import tr.edu.bilimankara20307006.taskflow.ui.project.ProjectBoardScreen
import tr.edu.bilimankara20307006.taskflow.ui.project.ProjectDetailScreen
import tr.edu.bilimankara20307006.taskflow.ui.task.TaskDetailScreen
import tr.edu.bilimankara20307006.taskflow.ui.analytics.ProjectAnalyticsScreen
import tr.edu.bilimankara20307006.taskflow.ui.profile.ProfileEditScreen
import tr.edu.bilimankara20307006.taskflow.ui.settings.NotificationSettingsScreen
import tr.edu.bilimankara20307006.taskflow.data.model.Task
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus
import tr.edu.bilimankara20307006.taskflow.data.model.User
import tr.edu.bilimankara20307006.taskflow.ui.theme.ThemeManager
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager
import androidx.compose.ui.platform.LocalContext

/**
 * Ana Tab Ekranı - iOS CustomTabView ile birebir uyumlu
 * Projeler, Görevler, Bildirimler ve Ayarlar sekmelerini içerir
 */
@Composable
fun MainTabScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    val projectViewModel: tr.edu.bilimankara20307006.taskflow.ui.project.ProjectViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()
    val repository = remember { tr.edu.bilimankara20307006.taskflow.data.repository.TaskRepository.getInstance() }
    
    // Notification state for badge
    val notificationState by notificationViewModel.state.collectAsState()
    val unreadNotificationsCount = notificationState.notifications.count { !it.isRead }
    
    // Load notifications for badge
    LaunchedEffect(Unit) {
        notificationViewModel.loadNotifications()
    }
    
    var selectedTab by remember { mutableStateOf(0) }
    var showProjectBoard by remember { mutableStateOf(false) }
    var showProjectDashboard by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    var showProfileEdit by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var showAccountSettings by remember { mutableStateOf(false) }
    var showPrivacySettings by remember { mutableStateOf(false) }
    var showHelpSupport by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    
    // Dil değişikliklerini takip et
    val currentLanguage = localizationManager.currentLocale
    
    // Tema renklerini MaterialTheme'den al
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    
    // Back button handling kontrolü - Ana ekrandayken sistem back tuşu çalışsın
    val shouldHandleBackButton = selectedProject != null || 
                                  selectedTask != null || 
                                  showAnalytics || 
                                  showProjectBoard || 
                                  showProjectDashboard ||
                                  showProfileEdit ||
                                  showNotificationSettings ||
                                  showAccountSettings ||
                                  showPrivacySettings ||
                                  showHelpSupport ||
                                  showAbout ||
                                  selectedTab != 0
    
    // Back button handling - Geri tuşuna basıldığında ekranlar arası gezinme
    BackHandler(enabled = shouldHandleBackButton) {
        when {
            showAbout -> showAbout = false
            showHelpSupport -> showHelpSupport = false
            showPrivacySettings -> showPrivacySettings = false
            showAccountSettings -> showAccountSettings = false
            showNotificationSettings -> showNotificationSettings = false
            showProfileEdit -> showProfileEdit = false
            selectedProject != null -> selectedProject = null
            selectedTask != null -> selectedTask = null
            showAnalytics -> showAnalytics = false
            showProjectBoard -> showProjectBoard = false
            showProjectDashboard -> showProjectDashboard = false
            selectedTab != 0 -> selectedTab = 0
        }
    }
    
    // Bildirim ayarları ekranı gösteriliyorsa (TAB BAR GİZLENİR)
    if (showNotificationSettings) {
        NotificationSettingsScreen(
            onBackClick = { showNotificationSettings = false }
        )
        return
    }
    
    // Ayarlar alt ekranları gösteriliyorsa (TAB BAR GİZLENİR)
    if (showAbout) {
        tr.edu.bilimankara20307006.taskflow.ui.settings.AboutScreen(
            onBackClick = { showAbout = false }
        )
        return
    }
    
    if (showHelpSupport) {
        tr.edu.bilimankara20307006.taskflow.ui.settings.HelpSupportScreen(
            onBackClick = { showHelpSupport = false }
        )
        return
    }
    
    if (showPrivacySettings) {
        tr.edu.bilimankara20307006.taskflow.ui.settings.PrivacySettingsScreen(
            onBackClick = { showPrivacySettings = false }
        )
        return
    }
    
    if (showAccountSettings) {
        tr.edu.bilimankara20307006.taskflow.ui.settings.AccountSettingsScreen(
            onBackClick = { showAccountSettings = false }
        )
        return
    }
    
    // Profil düzenleme ekranı gösteriliyorsa (TAB BAR GİZLENİR)
    if (showProfileEdit) {
        ProfileEditScreen(
            onBackClick = { showProfileEdit = false }
        )
        return
    }
    
    // AddTeamMember ekranı için state
    var showAddMember by remember { mutableStateOf(false) }
    var addMemberProjectId by remember { mutableStateOf<String?>(null) }
    
    // AddTask ekranı için state
    var showAddTask by remember { mutableStateOf(false) }
    var addTaskProjectId by remember { mutableStateOf<String?>(null) }
    
    // AddTask ekranı gösteriliyorsa
    if (showAddTask && addTaskProjectId != null) {
        // Projenin üyelerini al
        val projectMembers = remember(selectedProject) {
            val members = mutableListOf<User>()
            selectedProject?.teamLeader?.let { members.add(it) }
            members.addAll(selectedProject?.teamMembers ?: emptyList())
            members.distinctBy { it.uid }
        }
        
        tr.edu.bilimankara20307006.taskflow.ui.task.AddTaskScreen(
            projectId = addTaskProjectId!!,
            projectName = selectedProject?.title ?: "",
            availableAssignees = projectMembers,
            onBackClick = {
                showAddTask = false
                addTaskProjectId = null
            },
            onTaskCreated = { task ->
                scope.launch {
                    println("🔄 Görev Firebase'e kaydediliyor: ${task.title}")
                    val result = repository.createTask(
                        title = task.title,
                        description = task.description,
                        projectId = task.projectId,
                        assignedToId = task.assigneeId,
                        priority = task.priority,
                        dueDate = task.dueDate
                    )
                    result.onSuccess { createdTask ->
                        println("✅ Görev başarıyla kaydedildi: ${createdTask.id}")
                    }.onFailure { error ->
                        println("❌ Görev kaydetme hatası: ${error.message}")
                    }
                }
                showAddTask = false
                addTaskProjectId = null
                // Proje detay ekranına dön ve yenile
                selectedProject = null
            }
        )
        return
    }
    
    // AddTeamMember ekranı gösteriliyorsa
    if (showAddMember && addMemberProjectId != null && selectedProject != null) {
        tr.edu.bilimankara20307006.taskflow.ui.project.AddTeamMemberScreen(
            projectId = addMemberProjectId!!,
            onBackClick = {
                showAddMember = false
                addMemberProjectId = null
            },
            onMemberAdded = {
                showAddMember = false
                addMemberProjectId = null
                // Ekranı kapat, ana ekrana dönünce otomatik yenilenecek
                selectedProject = null
            }
        )
        return
    }
    
    // Proje detay ekranı gösteriliyorsa (TAB BAR GİZLENİR)
    if (selectedProject != null) {
        println("✅ ProjectDetailScreen açılıyor: ${selectedProject!!.title}")
        ProjectDetailScreen(
            projectId = selectedProject!!.id,
            onBackClick = { 
                println("⬅️ Geri butonu basıldı")
                selectedProject = null 
            },
            onAddMemberClick = { projectId ->
                addMemberProjectId = projectId
                showAddMember = true
            },
            onAddTaskClick = { projectId ->
                addTaskProjectId = projectId
                showAddTask = true
            }
        )
        return
    }
    
    // Görev detay ekranı gösteriliyorsa
    if (selectedTask != null) {
        TaskDetailScreen(
            task = selectedTask!!,
            onBackClick = { selectedTask = null }
        )
        return
    }
    
    // Analytics ekranı gösteriliyorsa
    if (showAnalytics) {
        ProjectAnalyticsScreen(
            onBackClick = { showAnalytics = false }
        )
        return
    }
    
    // Proje Dashboard ekranı gösteriliyorsa (Yapılacaklar/Devam Ediyor/Tamamlandı)
    if (showProjectDashboard) {
        tr.edu.bilimankara20307006.taskflow.ui.project.ProjectDashboardScreen(
            onBackClick = { showProjectDashboard = false },
            onTaskClick = { task -> selectedTask = task },
            onProjectClick = { project -> selectedProject = project }
        )
        return
    }
    
    // Proje Panosu ekranı gösteriliyorsa (Kanban Board)
    if (showProjectBoard) {
        ProjectBoardScreen(
            projectId = selectedProject?.id,
            onBackClick = { showProjectBoard = false },
            onTaskClick = { task -> selectedTask = task }
        )
        return
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Main content based on selected tab with smooth transitions
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + slideInHorizontally(
                    initialOffsetX = { if (targetState > initialState) 300 else -300 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) togetherWith fadeOut(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + slideOutHorizontally(
                    targetOffsetX = { if (targetState > initialState) -300 else 300 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            },
            label = "tabContent"
        ) { tab ->
            when (tab) {
                0 -> ProjectListScreen(
                    onNavigateToBoard = { showProjectDashboard = true },
                    onNavigateToAnalytics = { showAnalytics = true },
                    selectedProject = selectedProject,
                    onProjectSelected = { 
                        println("🚀 Proje seçildi: ${it?.title}")
                        selectedProject = it 
                    }
                )
                1 -> tr.edu.bilimankara20307006.taskflow.ui.notifications.NotificationsScreen(
                    viewModel = notificationViewModel
                )
                2 -> SettingsScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = onNavigateToLogin,
                    onProfileClick = { showProfileEdit = true },
                    onNotificationSettingsClick = { showNotificationSettings = true },
                    onAccountSettingsClick = { showAccountSettings = true },
                    onPrivacySettingsClick = { showPrivacySettings = true },
                    onHelpSupportClick = { showHelpSupport = true },
                    onAboutClick = { showAbout = true }
                )
            }
        }
        
        // Custom Bottom Navigation Bar - iOS style
        CustomBottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            localizationManager = localizationManager,
            modifier = Modifier.align(Alignment.BottomCenter),
            unreadNotificationsCount = unreadNotificationsCount
        )
    }
}

/**
 * Custom Bottom Navigation Bar - iOS tab bar stili
 */
@Composable
fun CustomBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    localizationManager: LocalizationManager,
    modifier: Modifier = Modifier,
    unreadNotificationsCount: Int = 0
) {
    val tabBackground = MaterialTheme.colorScheme.surface
    val selectedColor = Color(0xFF66D68C) // Green #66D68C
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = tabBackground,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Projeler tab
            TabBarItem(
                icon = Icons.Default.AssignmentTurnedIn,
                title = localizationManager.localizedString("Projects"),
                isSelected = selectedTab == 0,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = { onTabSelected(0) }
            )
            
            // Bildirimler tab
            TabBarItem(
                icon = Icons.Default.Notifications,
                title = localizationManager.localizedString("Notifications"),
                isSelected = selectedTab == 1,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = { onTabSelected(1) },
                badgeCount = unreadNotificationsCount
            )
            
            // Ayarlar tab
            TabBarItem(
                icon = Icons.Default.Settings,
                title = localizationManager.localizedString("Settings"),
                isSelected = selectedTab == 2,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

/**
 * Tab Bar Item - iOS tarzı tab butonu
 */
@Composable
fun TabBarItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(8.dp)
            .clickableWithoutRipple(onClick = onClick)
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) selectedColor else unselectedColor,
                modifier = Modifier.size(24.dp)
            )
            
            // Badge for unread notifications
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30))
                )
            }
        }
        
        Text(
            text = title,
            fontSize = 10.sp,
            color = if (isSelected) selectedColor else unselectedColor
        )
    }
}

/**
 * Ripple effect olmadan clickable modifier
 */
@Composable
fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        indication = null,
        interactionSource = interactionSource,
        onClick = onClick
    )
}

/**
 * Bildirimler Ekranı - iOS NotificationsView ile aynı
 */
@Composable
fun NotificationsScreen(localizationManager: LocalizationManager) {
    val darkBackground = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    
    // Animation states
    var contentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        contentVisible = true
    }
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )
    
    val contentScale by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0.9f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "contentScale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .alpha(contentAlpha)
                .graphicsLayer {
                    scaleX = contentScale
                    scaleY = contentScale
                }
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(60.dp)
            )
            
            Text(
                text = localizationManager.localizedString("Notifications"),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            
            Text(
                text = localizationManager.localizedString("NoNotificationsMessage"),
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Settings Row Item
 */
@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val iconSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithoutRipple { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = title,
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = iconSecondaryColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Dark Mode Toggle Row - iOS stili switch
 */
@Composable
fun DarkModeToggleRow(
    onThemeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    val textColor = MaterialTheme.colorScheme.onSurface
    val isDarkMode = themeManager.isDarkMode
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DarkMode,
            contentDescription = "Dark Mode",
            tint = Color(0xFFAF52DE),
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = if (localizationManager.currentLocale == "tr") "Koyu Tema" else "Dark Mode",
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        
        // iOS stili yeşil switch
        Switch(
            checked = isDarkMode,
            onCheckedChange = { isChecked ->
                onThemeChange(if (isChecked) ThemeManager.THEME_DARK else ThemeManager.THEME_LIGHT)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF34C759), // iOS yeşili
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF8E8E93).copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * Language Toggle Row - iOS stili kaydırmalı seçim
 */
@Composable
fun LanguageToggleRow(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val textColor = MaterialTheme.colorScheme.onSurface
    val backgroundColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val selectedColor = if (isDark) Color(0xFF48484A) else Color.White
    val selectedTextColor = if (isDark) Color.White else Color.Black
    val unselectedTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
    
    // Animasyonlu offset için state
    val offsetAnimation by animateFloatAsState(
        targetValue = if (currentLanguage == "tr") 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "languageOffset"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = "Language",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = if (currentLanguage == "tr") "Dil" else "Language",
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        
        // iOS stili toggle butonlar
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = backgroundColor
        ) {
            Box(
                modifier = Modifier.padding(2.dp)
            ) {
                // Animasyonlu seçili arka plan
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            translationX = this.size.width * offsetAnimation / 2f
                        }
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.5f),
                        shape = RoundedCornerShape(6.dp),
                        color = selectedColor,
                        shadowElevation = 2.dp
                    ) {}
                }
                
                // Butonlar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Türkçe butonu
                    Box(
                        modifier = Modifier
                            .clickableWithoutRipple { onLanguageChange("tr") }
                            .padding(0.dp)
                            .widthIn(min = 70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Türkçe",
                            fontSize = 13.sp,
                            fontWeight = if (currentLanguage == "tr") FontWeight.SemiBold else FontWeight.Normal,
                            color = animateColorAsState(
                                targetValue = if (currentLanguage == "tr") selectedTextColor else unselectedTextColor,
                                animationSpec = tween(300),
                                label = "trTextColor"
                            ).value,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            maxLines = 1
                        )
                    }
                    
                    // İngilizce butonu
                    Box(
                        modifier = Modifier
                            .clickableWithoutRipple { onLanguageChange("en") }
                            .padding(0.dp)
                            .widthIn(min = 70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "English",
                            fontSize = 13.sp,
                            fontWeight = if (currentLanguage == "en") FontWeight.SemiBold else FontWeight.Normal,
                            color = animateColorAsState(
                                targetValue = if (currentLanguage == "en") selectedTextColor else unselectedTextColor,
                                animationSpec = tween(300),
                                label = "enTextColor"
                            ).value,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dil Seçimi Dialogu
 */
@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (currentLanguage == "tr") "Dil Seçin" else "Select Language",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                LanguageOption(
                    language = "Türkçe",
                    code = "tr",
                    isSelected = currentLanguage == "tr",
                    onClick = { onLanguageSelected("tr") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LanguageOption(
                    language = "English",
                    code = "en",
                    isSelected = currentLanguage == "en",
                    onClick = { onLanguageSelected("en") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (currentLanguage == "tr") "İptal" else "Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun LanguageOption(
    language: String,
    code: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Tema Seçimi Dialogu
 */
@Composable
fun ThemeSelectionDialog(
    currentThemeMode: String,
    onThemeModeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = localizationManager.localizedString("ThemeMode"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ThemeOption(
                    title = localizationManager.localizedString("SystemDefault"),
                    mode = "system",
                    isSelected = currentThemeMode == "system",
                    onClick = { onThemeModeSelected("system") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ThemeOption(
                    title = localizationManager.localizedString("LightMode"),
                    mode = "light",
                    isSelected = currentThemeMode == "light",
                    onClick = { onThemeModeSelected("light") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ThemeOption(
                    title = localizationManager.localizedString("DarkMode"),
                    mode = "dark",
                    isSelected = currentThemeMode == "dark",
                    onClick = { onThemeModeSelected("dark") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizationManager.localizedString("Cancel"))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun ThemeOption(
    title: String,
    mode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Ayarlar Ekranı - iOS SettingsView ile aynı
 */
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onProfileClick: () -> Unit = {},
    onNotificationSettingsClick: () -> Unit = {},
    onAccountSettingsClick: () -> Unit = {},
    onPrivacySettingsClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    
    // Tema renkleri
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var profileVisible by remember { mutableStateOf(false) }
    var settingsVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
        delay(150)
        profileVisible = true
        delay(150)
        settingsVisible = true
    }
    
    val headerAlpha by animateFloatAsState(
        targetValue = if (headerVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )
    
    val profileAlpha by animateFloatAsState(
        targetValue = if (profileVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "profileAlpha"
    )
    
    val profileScale by animateFloatAsState(
        targetValue = if (profileVisible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "profileScale"
    )
    
    val settingsAlpha by animateFloatAsState(
        targetValue = if (settingsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "settingsAlpha"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Header (Fixed)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .alpha(headerAlpha),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizationManager.localizedString("Settings"),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )
        }
        
        // Scrollable Content
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Profile Section
            authState.user?.let { user ->
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .alpha(profileAlpha)
                        .graphicsLayer {
                            scaleX = profileScale
                            scaleY = profileScale
                        }
                ) {
                Text(
                    text = localizationManager.localizedString("ProfileInformation"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTextColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAccountSettingsClick() },
                    color = surfaceColor,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User avatar - iOS style
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    Color(0xFF66D68C),
                                    androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user.displayName?.firstOrNull()?.uppercase() ?: "U"),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = user.displayName ?: localizationManager.localizedString("User"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryTextColor
                            )
                            
                            Text(
                                text = user.email ?: "",
                                fontSize = 14.sp,
                                color = secondaryTextColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        
        // Settings Options
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .alpha(settingsAlpha)
        ) {
            Text(
                text = localizationManager.localizedString("AppSettings"),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryTextColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        title = localizationManager.localizedString("Notifications"),
                        color = Color(0xFFFF9500),
                        onClick = { onNotificationSettingsClick() }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), 
                        thickness = 0.5.dp
                    )
                    // Koyu Tema toggle switch
                    DarkModeToggleRow(
                        onThemeChange = { theme ->
                            themeManager.setThemeMode(theme)
                        }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), 
                        thickness = 0.5.dp
                    )
                    // Dil seçimi - iOS stili slider
                    LanguageToggleRow(
                        currentLanguage = localizationManager.currentLocale,
                        onLanguageChange = { locale ->
                            localizationManager.setLocale(locale)
                        }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), 
                        thickness = 0.5.dp
                    )
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = if (localizationManager.currentLocale == "tr") 
                            "Yardım ve Destek" else "Help & Support",
                        color = Color(0xFF34C759),
                        onClick = { onHelpSupportClick() }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), 
                        thickness = 0.5.dp
                    )
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = localizationManager.localizedString("About"),
                        color = Color(0xFF607D8B),
                        onClick = { onAboutClick() }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Sign out section
        Button(
            onClick = {
                authViewModel.signOut()
                onNavigateToLogin()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = surfaceColor
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = localizationManager.localizedString("SignOut"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Red
                )
            }
        }
        
        Spacer(modifier = Modifier.height(90.dp)) // Space for bottom nav
    }
    
    // Dil Seçimi Dialogu
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = localizationManager.currentLocale,
            onLanguageSelected = { locale ->
                localizationManager.setLocale(locale)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    // Tema Seçimi Dialogu
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentThemeMode = themeManager.themeMode,
            onThemeModeSelected = { mode ->
                themeManager.setThemeMode(mode)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

/**
 * Dil Seçimi Dialogu
 */
@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    
    val dialogBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBackground,
        title = {
            Text(
                text = localizationManager.localizedString("LanguageSelection"),
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LanguageOption(
                    language = localizationManager.localizedString("Turkish"),
                    code = "tr",
                    isSelected = currentLanguage == "tr",
                    onClick = { onLanguageSelected("tr") }
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), 
                    thickness = 0.5.dp
                )
                
                LanguageOption(
                    language = localizationManager.localizedString("English"),
                    code = "en",
                    isSelected = currentLanguage == "en",
                    onClick = { onLanguageSelected("en") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = localizationManager.localizedString("Cancel"),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    )
}

/**
 * Dil Seçeneği
 */
@Composable
fun LanguageOption(
    language: String,
    languageCode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = language,
            color = textColor,
            fontSize = 16.sp
        )
        
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Tema Seçimi Dialogu
 */
@Composable
fun ThemeSelectionDialog(
    currentThemeMode: String,
    onThemeModeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    
    val dialogBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBackground,
        title = {
            Text(
                text = localizationManager.localizedString("ThemeSelection"),
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sistem Ayarı
                ThemeOption(
                    title = localizationManager.localizedString("SystemTheme"),
                    mode = ThemeManager.THEME_SYSTEM,
                    isSelected = currentThemeMode == ThemeManager.THEME_SYSTEM,
                    onClick = { onThemeModeSelected(ThemeManager.THEME_SYSTEM) }
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), 
                    thickness = 0.5.dp
                )
                
                // Açık Tema
                ThemeOption(
                    title = localizationManager.localizedString("LightTheme"),
                    mode = ThemeManager.THEME_LIGHT,
                    isSelected = currentThemeMode == ThemeManager.THEME_LIGHT,
                    onClick = { onThemeModeSelected(ThemeManager.THEME_LIGHT) }
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), 
                    thickness = 0.5.dp
                )
                
                // Koyu Tema
                ThemeOption(
                    title = localizationManager.localizedString("DarkTheme"),
                    mode = ThemeManager.THEME_DARK,
                    isSelected = currentThemeMode == ThemeManager.THEME_DARK,
                    onClick = { onThemeModeSelected(ThemeManager.THEME_DARK) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = localizationManager.localizedString("Cancel"),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    )
}

/**
 * Tema Seçeneği
 */
@Composable
fun ThemeOption(
    themeName: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val iconSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = themeName,
            tint = if (isSelected) Color(0xFF4CAF50) else iconSecondaryColor,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = themeName,
            color = textColor,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Filtre Chip - iOS gibi
 */
@Composable
fun CustomFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = backgroundColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Görev Kartı - iOS gibi
 */
@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBackground = MaterialTheme.colorScheme.surface
    
    val statusColor = if (task.isCompleted) Color(0xFF34C759) else Color(0xFFFF9500)
    val statusText = if (task.isCompleted) "Tamamlandı" else "Devam Ediyor"
    
    val priorityColor = when (task.priority.lowercase()) {
        "yüksek", "high" -> Color(0xFFFF3B30)
        "orta", "medium" -> Color(0xFFFF9500)
        else -> Color(0xFF34C759)
    }
    val priorityText = task.priority
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = cardBackground,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
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
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            
            // Açıklama
            if (task.description.isNotEmpty()) {
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    color = textSecondaryColor,
                    maxLines = 2
                )
            }
            
            // Alt bilgiler
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Durum badge - iOS gibi
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                
                // Öncelik badge - iOS gibi
                Surface(
                    color = priorityColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = priorityText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Tarih - iOS gibi
                task.dueDate?.let { date ->
                    val dateFormat = SimpleDateFormat("d MMM", Locale("tr"))
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
                            text = dateFormat.format(date),
                            fontSize = 13.sp,
                            color = textSecondaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Görevler Listesi Ekranı - iOS TasksView ile aynı
 */
@Composable
fun TasksListScreen(
    onTaskClick: (Task) -> Unit,
    localizationManager: LocalizationManager
) {
    val context = LocalContext.current
    val projectViewModel: tr.edu.bilimankara20307006.taskflow.ui.project.ProjectViewModel = viewModel()
    
    var allTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("Tümü") }
    
    val darkBackground = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBackground = MaterialTheme.colorScheme.surface
    
    // Tüm görevleri yükle ve otomatik yenile
    LaunchedEffect(Unit) {
        while (true) {
            isLoading = true
            println("🔄 Tüm görevler yükleniyor...")
            
            val result = projectViewModel.repository.getAllTasks()
            result.onSuccess { tasks ->
                allTasks = tasks
                isLoading = false
                println("✅ ${tasks.size} görev yüklendi")
            }.onFailure {
                isLoading = false
                println("❌ Görevler yüklenemedi: ${it.message}")
            }
            
            delay(5000) // 5 saniyede bir yenile
        }
    }
    
    // Filtrelenmiş görevler
    val filteredTasks = remember(allTasks, selectedFilter) {
        when (selectedFilter) {
            "Beklemede" -> allTasks.filter { !it.isCompleted }
            "Tamamlandı" -> allTasks.filter { it.isCompleted }
            "Yüksek Öncelik" -> allTasks.filter { it.priority.lowercase() in listOf("yüksek", "high") }
            else -> allTasks
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Üst Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = cardBackground,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localizationManager.localizedString("Tasks"),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    // Görev sayısı badge'i
                    Surface(
                        color = Color(0xFF34C759).copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Text(
                            text = "${filteredTasks.size}",
                            color = Color(0xFF34C759),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                
                // Filtre Butonları - iOS gibi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Tümü", "Beklemede", "Tamamlandı", "Yüksek Öncelik").forEach { filter ->
                        CustomFilterChip(
                            text = filter,
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }
            }
        }
        
        // Görev Listesi
        if (isLoading && allTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = Color(0xFF66D68C)
                )
            }
        } else if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = textSecondaryColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Görev bulunamadı",
                        fontSize = 18.sp,
                        color = textSecondaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTasks.size) { index ->
                    TaskCard(
                        task = filteredTasks[index],
                        onClick = { onTaskClick(filteredTasks[index]) }
                    )
                }
            }
        }
    }
}
}
