package tr.edu.bilimankara20307006.taskflow.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
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

/**
 * Bildirim Ayarları Ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit = {},
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
    var contentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
        delay(150)
        contentVisible = true
    }
    
    val headerAlpha by animateFloatAsState(
        targetValue = if (headerVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )
    
    // Notification preferences
    var notificationPreference by remember { mutableStateOf("all") } // all, mentions, none
    var notificationSound by remember { mutableStateOf("ring_vibrate") } // ring_vibrate, vibrate_only
    var showDisableDialog by remember { mutableStateOf(false) }
    
    // Theme colors
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (localizationManager.currentLocale == "tr") 
                            "Bildirim Ayarları" else "Notification Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        modifier = Modifier.alpha(headerAlpha)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.alpha(headerAlpha)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizationManager.localizedString("Back"),
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground
                )
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .alpha(contentAlpha),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Notification Preference Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (localizationManager.currentLocale == "tr") 
                        "Bildirim Tercihi" else "Notification Preference",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cardBackground,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        // All notifications
                        NotificationPreferenceItem(
                            title = if (localizationManager.currentLocale == "tr") "Tümü" else "All",
                            description = if (localizationManager.currentLocale == "tr") 
                                "Tüm bildirimleri al" else "Receive all notifications",
                            isSelected = notificationPreference == "all",
                            onClick = { notificationPreference = "all" }
                        )
                        
                        HorizontalDivider(color = textSecondaryColor.copy(alpha = 0.1f))
                        
                        // Mentions only
                        NotificationPreferenceItem(
                            title = if (localizationManager.currentLocale == "tr") 
                                "Sadece Hakkımdakiler" else "Mentions Only",
                            description = if (localizationManager.currentLocale == "tr") 
                                "Sadece seni etiketleyen bildirimleri al" 
                            else "Only receive notifications when mentioned",
                            isSelected = notificationPreference == "mentions",
                            onClick = { notificationPreference = "mentions" }
                        )
                        
                        HorizontalDivider(color = textSecondaryColor.copy(alpha = 0.1f))
                        
                        // None
                        NotificationPreferenceItem(
                            title = if (localizationManager.currentLocale == "tr") "Hiçbiri" else "None",
                            description = if (localizationManager.currentLocale == "tr") 
                                "Bildirimleri kapat" else "Turn off notifications",
                            isSelected = notificationPreference == "none",
                            onClick = { 
                                if (notificationPreference != "none") {
                                    showDisableDialog = true
                                }
                            }
                        )
                    }
                }
            }
            
            // Notification Sound Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (localizationManager.currentLocale == "tr") 
                        "Bildirim Şekli" else "Notification Type",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cardBackground,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        // Ring and Vibrate
                        NotificationSoundItem(
                            title = if (localizationManager.currentLocale == "tr") 
                                "Çal ve Titret" else "Ring and Vibrate",
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            isSelected = notificationSound == "ring_vibrate",
                            onClick = { notificationSound = "ring_vibrate" }
                        )
                        
                        HorizontalDivider(color = textSecondaryColor.copy(alpha = 0.1f))
                        
                        // Vibrate Only
                        NotificationSoundItem(
                            title = if (localizationManager.currentLocale == "tr") 
                                "Sadece Titret" else "Vibrate Only",
                            icon = Icons.Default.Vibration,
                            isSelected = notificationSound == "vibrate_only",
                            onClick = { notificationSound = "vibrate_only" }
                        )
                    }
                }
            }
        }
    }
    
    // Disable Notifications Confirmation Dialog
    if (showDisableDialog) {
        AlertDialog(
            onDismissRequest = { showDisableDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9500),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (localizationManager.currentLocale == "tr") 
                        "Bildirimleri Kapat?" else "Turn Off Notifications?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (localizationManager.currentLocale == "tr") 
                        "Bildirimleri tamamen kapatmak projelerde iletişim sıkıntısı çıkartacaktır. Kapatmak istediğinizden emin misiniz?"
                    else "Turning off notifications completely will cause communication issues in projects. Are you sure you want to turn them off?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        notificationPreference = "none"
                        showDisableDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFFF3B30)
                    )
                ) {
                    Text(
                        text = if (localizationManager.currentLocale == "tr") "Kapat" else "Turn Off",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDisableDialog = false }
                ) {
                    Text(
                        text = localizationManager.localizedString("Cancel"),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = cardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun NotificationPreferenceItem(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            Text(
                text = description,
                fontSize = 14.sp,
                color = textSecondaryColor
            )
        }
        
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF4CAF50),
                unselectedColor = textSecondaryColor
            )
        )
    }
}

@Composable
private fun NotificationSoundItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF4CAF50) else textColor,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF4CAF50),
                unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )
    }
}
