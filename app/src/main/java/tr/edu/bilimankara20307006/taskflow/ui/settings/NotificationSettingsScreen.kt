package tr.edu.bilimankara20307006.taskflow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager
import tr.edu.bilimankara20307006.taskflow.data.model.NotificationSettings
import tr.edu.bilimankara20307006.taskflow.data.preferences.NotificationPreferences
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val notificationPrefs = remember { NotificationPreferences(context) }
    val scope = rememberCoroutineScope()
    
    var settings by remember { mutableStateOf(NotificationSettings()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Ayarları yükle
    LaunchedEffect(Unit) {
        scope.launch {
            // Önce local cache'den oku
            val cachedSettings = notificationPrefs.getSettings()
            settings = cachedSettings
            isLoading = false
            
            // Ardından Firestore'dan güncelle
            FirebaseManager.getNotificationSettings().onSuccess { firestoreSettings ->
                settings = firestoreSettings
                notificationPrefs.saveSettings(firestoreSettings)
            }
        }
    }
    
    // Ayarları kaydet
    fun saveSettings() {
        scope.launch {
            isSaving = true
            // Local cache'e kaydet
            notificationPrefs.saveSettings(settings)
            
            // Firestore'a kaydet
            FirebaseManager.saveNotificationSettings(settings).onSuccess {
                println("✅ Bildirim ayarları kaydedildi")
            }.onFailure {
                println("❌ Firestore'a kaydetme hatası: ${it.message}")
            }
            isSaving = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bildirim Ayarları") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF66D68C))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Push Bildirimleri
                NotificationToggleItem(
                    icon = Icons.Default.Notifications,
                    iconColor = Color(0xFF66D68C),
                    title = "Push Bildirimleri",
                    description = "Anlık bildirimler alın",
                    checked = settings.pushEnabled,
                    onCheckedChange = {
                        settings = settings.copy(pushEnabled = it)
                        saveSettings()
                    }
                )
                
                Divider(color = Color(0xFF2E2E2E))
                
                // Görev Hatırlatıcıları
                NotificationToggleItem(
                    icon = Icons.Default.AccessTime,
                    iconColor = Color(0xFFFF5252), // Kırmızı
                    title = "Görev Hatırlatıcıları",
                    description = "Görev son tarihleri yaklaştığında uyarı",
                    checked = settings.taskReminderEnabled,
                    onCheckedChange = {
                        settings = settings.copy(taskReminderEnabled = it)
                        saveSettings()
                    },
                    badge = Icons.Default.Warning
                )
                
                // Görev bitiş uyarısı slider
                if (settings.taskReminderEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E1E)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Görev bitiş uyarısı: ${settings.taskDeadlineDays} gün önce",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Slider(
                                value = settings.taskDeadlineDays.toFloat(),
                                onValueChange = {
                                    settings = settings.copy(taskDeadlineDays = it.toInt())
                                },
                                onValueChangeFinished = {
                                    saveSettings()
                                },
                                valueRange = 1f..30f,
                                steps = 28,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF66D68C),
                                    activeTrackColor = Color(0xFF66D68C),
                                    inactiveTrackColor = Color(0xFF2E2E2E)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("1 gün", fontSize = 12.sp, color = Color.Gray)
                                Text("30 gün", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                
                Divider(color = Color(0xFF2E2E2E))
                
                // Proje Güncellemeleri
                NotificationToggleItem(
                    icon = Icons.Default.Update,
                    iconColor = Color(0xFF2196F3), // Mavi
                    title = "Proje Güncellemeleri",
                    description = "Proje değişiklikleri hakkında bildirim",
                    checked = settings.projectUpdateEnabled,
                    onCheckedChange = {
                        settings = settings.copy(projectUpdateEnabled = it)
                        saveSettings()
                    }
                )
                
                // Proje bitiş uyarısı slider
                if (settings.projectUpdateEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E1E)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Proje bitiş uyarısı: ${settings.projectDeadlineDays} gün önce",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Slider(
                                value = settings.projectDeadlineDays.toFloat(),
                                onValueChange = {
                                    settings = settings.copy(projectDeadlineDays = it.toInt())
                                },
                                onValueChangeFinished = {
                                    saveSettings()
                                },
                                valueRange = 1f..30f,
                                steps = 28,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF66D68C),
                                    activeTrackColor = Color(0xFF66D68C),
                                    inactiveTrackColor = Color(0xFF2E2E2E)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("1 gün", fontSize = 12.sp, color = Color.Gray)
                                Text("30 gün", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                
                Divider(color = Color(0xFF2E2E2E))
                
                // Ekip Aktiviteleri
                NotificationToggleItem(
                    icon = Icons.Default.People,
                    iconColor = Color(0xFF9C27B0), // Mor
                    title = "Ekip Aktiviteleri",
                    description = "Ekip üyesi ekleme/çıkarma bildirimleri",
                    checked = settings.teamActivityEnabled,
                    onCheckedChange = {
                        settings = settings.copy(teamActivityEnabled = it)
                        saveSettings()
                    }
                )
                
                Divider(color = Color(0xFF2E2E2E))
                
                // E-posta Bildirimleri
                NotificationToggleItem(
                    icon = Icons.Default.Email,
                    iconColor = Color(0xFFFFC107), // Sarı
                    title = "E-posta Bildirimleri",
                    description = "Önemli güncellemeleri e-posta ile alın",
                    checked = settings.emailEnabled,
                    onCheckedChange = {
                        settings = settings.copy(emailEnabled = it)
                        saveSettings()
                    }
                )
                
                if (isSaving) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF66D68C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    badge: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
                if (badge != null) {
                    Icon(
                        imageVector = badge,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF66D68C),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF2E2E2E)
            )
        )
    }
}
