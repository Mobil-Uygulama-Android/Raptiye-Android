package tr.edu.bilimankara20307006.taskflow.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import tr.edu.bilimankara20307006.taskflow.ui.auth.AuthViewModel
import tr.edu.bilimankara20307006.taskflow.ui.project.ProjectListScreen
import tr.edu.bilimankara20307006.taskflow.ui.project.ProjectBoardScreen
import tr.edu.bilimankara20307006.taskflow.ui.project.ProjectDetailScreen
import tr.edu.bilimankara20307006.taskflow.ui.task.TaskDetailScreen
import tr.edu.bilimankara20307006.taskflow.ui.analytics.ProjectAnalyticsScreen
import tr.edu.bilimankara20307006.taskflow.ui.profile.ProfileEditScreen
import tr.edu.bilimankara20307006.taskflow.ui.settings.NotificationSettingsScreen
import tr.edu.bilimankara20307006.taskflow.data.model.Task
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.ui.theme.ThemeManager
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager
import androidx.compose.ui.platform.LocalContext

/**
 * Ana Tab Ekranı - iOS CustomTabView ile birebir uyumlu
 * Projeler, Bildirimler ve Ayarlar sekmelerini içerir
 */
@Composable
fun MainTabScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    
    var selectedTab by remember { mutableStateOf(0) }
    var showProjectBoard by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    var showProfileEdit by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    
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
                                  showProfileEdit ||
                                  showNotificationSettings ||
                                  selectedTab != 0
    
    // Back button handling - Geri tuşuna basıldığında ekranlar arası gezinme
    BackHandler(enabled = shouldHandleBackButton) {
        when {
            showNotificationSettings -> {
                // Bildirim ayarları ekranından geri dön
                showNotificationSettings = false
            }
            showProfileEdit -> {
                // Profil düzenleme ekranından geri dön
                showProfileEdit = false
            }
            selectedProject != null -> {
                // Proje detayından geri dön
                selectedProject = null
            }
            selectedTask != null -> {
                // Görev detayından geri dön
                selectedTask = null
            }
            showAnalytics -> {
                // Analytics ekranından geri dön
                showAnalytics = false
            }
            showProjectBoard -> {
                // Proje panosundan geri dön
                showProjectBoard = false
            }
            selectedTab != 0 -> {
                // Diğer tablardan Projeler tabına dön
                selectedTab = 0
            }
        }
    }
    
    // Bildirim ayarları ekranı gösteriliyorsa (TAB BAR GİZLENİR)
    if (showNotificationSettings) {
        NotificationSettingsScreen(
            onBackClick = { showNotificationSettings = false }
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
    
    // Proje detay ekranı gösteriliyorsa (TAB BAR GİZLENİR)
    if (selectedProject != null) {
        println("✅ ProjectDetailScreen açılıyor: ${selectedProject!!.title}")
        ProjectDetailScreen(
            project = selectedProject!!,
            onBackClick = { 
                println("⬅️ Geri butonu basıldı")
                selectedProject = null 
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
    
    // Proje Panosu ekranı gösteriliyorsa
    if (showProjectBoard) {
        ProjectBoardScreen(
            onBackClick = { showProjectBoard = false },
            onTaskClick = { task -> selectedTask = task }
        )
        return
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
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
                    onNavigateToBoard = { showProjectBoard = true },
                    onNavigateToAnalytics = { showAnalytics = true },
                    selectedProject = selectedProject,
                    onProjectSelected = { 
                        println("🚀 Proje seçildi: ${it?.title}")
                        selectedProject = it 
                    }
                )
                1 -> NotificationsScreen(localizationManager = localizationManager)
                2 -> SettingsScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = onNavigateToLogin,
                    onProfileClick = { showProfileEdit = true },
                    onNotificationSettingsClick = { showNotificationSettings = true }
                )
            }
        }
        
        // Custom Bottom Navigation Bar - iOS style
        CustomBottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            localizationManager = localizationManager,
            modifier = Modifier.align(Alignment.BottomCenter)
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
    modifier: Modifier = Modifier
) {
    val tabBackground = MaterialTheme.colorScheme.surface
    val selectedColor = Color(0xFF4CAF50) // Green from login logo
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = tabBackground,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 16.dp),
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
                onClick = { onTabSelected(1) }
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
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(8.dp)
            .clickableWithoutRipple(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) selectedColor else unselectedColor,
            modifier = Modifier.size(24.dp)
        )
        
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
 * Ayarlar Ekranı - iOS SettingsView ile aynı
 */
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onProfileClick: () -> Unit = {},
    onNotificationSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    
    // Tema renklerini MaterialTheme'den al
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
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
            .background(darkBackground)
    ) {
        // Header
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
                color = textColor
            )
        }
        
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
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProfileClick() },
                    color = cardBackground,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User avatar
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    Color(0xFF4CAF50).copy(alpha = 0.2f),
                                    androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user.displayName?.firstOrNull()?.uppercase() ?: "U"),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = user.displayName ?: localizationManager.localizedString("User"),
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
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = cardBackground,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        title = localizationManager.localizedString("Notifications"),
                        color = Color(0xFFFF9500),
                        onClick = { onNotificationSettingsClick() }
                    )
                    Divider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                    SettingsRow(
                        icon = Icons.Default.DarkMode,
                        title = localizationManager.localizedString("DarkMode"),
                        color = Color(0xFFAF52DE),
                        onClick = { showThemeDialog = true }
                    )
                    Divider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                    SettingsRow(
                        icon = Icons.Default.Language,
                        title = localizationManager.localizedString("Language"),
                        color = Color(0xFF4CAF50),
                        onClick = { showLanguageDialog = true }
                    )
                    Divider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                    SettingsRow(
                        icon = Icons.Default.Help,
                        title = localizationManager.localizedString("Help"),
                        color = Color(0xFF34C759),
                        onClick = { /* Yardım ekranı */ }
                    )
                    Divider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = localizationManager.localizedString("About"),
                        color = Color.Gray,
                        onClick = { /* Hakkında ekranı */ }
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
                containerColor = cardBackground
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
                    Icons.Default.ExitToApp,
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
                    languageCode = "tr",
                    isSelected = currentLanguage == "tr",
                    onClick = { onLanguageSelected("tr") }
                )
                
                Divider(color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                
                LanguageOption(
                    language = localizationManager.localizedString("English"),
                    languageCode = "en",
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
                    themeName = localizationManager.localizedString("SystemTheme"),
                    icon = Icons.Default.Smartphone,
                    isSelected = currentThemeMode == ThemeManager.THEME_SYSTEM,
                    onClick = { onThemeModeSelected(ThemeManager.THEME_SYSTEM) }
                )
                
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                
                // Açık Tema
                ThemeOption(
                    themeName = localizationManager.localizedString("LightTheme"),
                    icon = Icons.Default.WbSunny,
                    isSelected = currentThemeMode == ThemeManager.THEME_LIGHT,
                    onClick = { onThemeModeSelected(ThemeManager.THEME_LIGHT) }
                )
                
                Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                
                // Koyu Tema
                ThemeOption(
                    themeName = localizationManager.localizedString("DarkTheme"),
                    icon = Icons.Default.DarkMode,
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
                contentDescription = "Selected",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

