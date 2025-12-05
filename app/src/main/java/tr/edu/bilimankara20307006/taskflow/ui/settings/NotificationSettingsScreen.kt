package tr.edu.bilimankara20307006.taskflow.ui.settings

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    val prefs = remember { context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE) }
    
    // State - iOS-style notification settings
    var pushNotificationsEnabled by remember { mutableStateOf(prefs.getBoolean("push_enabled", true)) }
    var taskReminders by remember { mutableStateOf(prefs.getBoolean("task_reminders", true)) }
    var projectUpdates by remember { mutableStateOf(prefs.getBoolean("project_updates", true)) }
    var teamActivity by remember { mutableStateOf(prefs.getBoolean("team_activity", false)) }
    var emailNotifications by remember { mutableStateOf(prefs.getBoolean("email_notifications", false)) }
    
    // Animation states
    var headerAlpha by remember { mutableFloatStateOf(0f) }
    var contentAlpha by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        delay(50)
        headerAlpha = 1f
        delay(100)
        contentAlpha = 1f
    }
    
    val animatedHeaderAlpha by animateFloatAsState(
        targetValue = headerAlpha,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )
    
    val animatedContentAlpha by animateFloatAsState(
        targetValue = contentAlpha,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )
    
    // Theme colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    
    BackHandler { onBackClick() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (localizationManager.currentLocale == "tr") 
                            "Bildirimler" else "Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(animatedHeaderAlpha)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.alpha(animatedHeaderAlpha)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (localizationManager.currentLocale == "tr") 
                                "Geri" else "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = primaryTextColor,
                    navigationIconContentColor = primaryTextColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .alpha(animatedContentAlpha),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Bildirimler Section (iOS'taki gibi)
            Text(
                text = if (localizationManager.currentLocale == "tr") 
                    "Bildirimler" else "Notifications",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = secondaryTextColor
            )
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    NotificationToggleItem(
                        title = if (localizationManager.currentLocale == "tr") 
                            "Push Bildirimlerini Etkinleştir" else "Enable Push Notifications",
                        description = if (localizationManager.currentLocale == "tr")
                            "Cihazınızda bildirim alın"
                        else "Receive notifications on your device",
                        isChecked = pushNotificationsEnabled,
                        onCheckedChange = { 
                            pushNotificationsEnabled = it
                            prefs.edit().putBoolean("push_enabled", it).apply()
                        },
                        textColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )
                    
                    HorizontalDivider(
                        color = secondaryTextColor.copy(alpha = 0.1f),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    
                    NotificationToggleItem(
                        title = if (localizationManager.currentLocale == "tr") 
                            "Görev Hatırlatıcıları" else "Task Reminders",
                        description = if (localizationManager.currentLocale == "tr")
                            "Yaklaşan görevler için hatırlatma alın"
                        else "Get reminders for upcoming tasks",
                        isChecked = taskReminders,
                        onCheckedChange = { 
                            taskReminders = it
                            prefs.edit().putBoolean("task_reminders", it).apply()
                        },
                        textColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )
                    
                    HorizontalDivider(
                        color = secondaryTextColor.copy(alpha = 0.1f),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    
                    NotificationToggleItem(
                        title = if (localizationManager.currentLocale == "tr") 
                            "Proje Güncellemeleri" else "Project Updates",
                        description = if (localizationManager.currentLocale == "tr")
                            "Proje değişiklikleri hakkında güncel kalın"
                        else "Stay updated on project changes",
                        isChecked = projectUpdates,
                        onCheckedChange = { 
                            projectUpdates = it
                            prefs.edit().putBoolean("project_updates", it).apply()
                        },
                        textColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )
                    
                    HorizontalDivider(
                        color = secondaryTextColor.copy(alpha = 0.1f),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    
                    NotificationToggleItem(
                        title = if (localizationManager.currentLocale == "tr") 
                            "Ekip Aktivitesi" else "Team Activity",
                        description = if (localizationManager.currentLocale == "tr")
                            "Ekip üyesi eylemleri hakkında bildirim alın"
                        else "Get notified about team member actions",
                        isChecked = teamActivity,
                        onCheckedChange = { 
                            teamActivity = it
                            prefs.edit().putBoolean("team_activity", it).apply()
                        },
                        textColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
            }
            
            // E-posta Bildirimleri Section
            Text(
                text = if (localizationManager.currentLocale == "tr") 
                    "E-posta Bildirimleri" else "Email Notifications",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = secondaryTextColor
            )
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    NotificationToggleItem(
                        title = if (localizationManager.currentLocale == "tr") 
                            "E-posta Bildirimleri" else "Email Notifications",
                        description = if (localizationManager.currentLocale == "tr")
                            "Güncellemeleri e-posta ile alın"
                        else "Receive updates via email",
                        isChecked = emailNotifications,
                        onCheckedChange = { 
                            emailNotifications = it
                            prefs.edit().putBoolean("email_notifications", it).apply()
                        },
                        textColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    description: String?,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            if (description != null) {
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = secondaryTextColor,
                    lineHeight = 18.sp
                )
            }
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = secondaryTextColor.copy(alpha = 0.3f)
            )
        )
    }
}


