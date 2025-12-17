package tr.edu.bilimankara20307006.taskflow.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
 * Gizlilik ve Güvenlik Ekranı - iOS Privacy & Security
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    
    // States
    var headerVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var profilePrivate by remember { mutableStateOf(false) }
    var shareAnalytics by remember { mutableStateOf(true) }
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var showLocationAlways by remember { mutableStateOf(false) }
    
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
    
    BackHandler { onBackClick() }
    
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
                            "Gizlilik ve Güvenlik" else "Privacy & Security",
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
                            contentDescription = "Geri",
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
            // Profile Privacy
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (localizationManager.currentLocale == "tr") "Profil Gizliliği" else "Profile Privacy",
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
                        PrivacySwitchItem(
                            icon = Icons.Default.Visibility,
                            title = if (localizationManager.currentLocale == "tr") 
                                "Gizli Profil" else "Private Profile",
                            subtitle = if (localizationManager.currentLocale == "tr") 
                                "Profilinizi sadece takipçileriniz görebilir" 
                            else "Only your followers can see your profile",
                            color = Color(0xFF9C27B0),
                            checked = profilePrivate,
                            onCheckedChange = { profilePrivate = it }
                        )
                    }
                }
            }
            
            // Data & Analytics
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (localizationManager.currentLocale == "tr") "Veri ve Analitik" else "Data & Analytics",
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
                        PrivacySwitchItem(
                            icon = Icons.Default.Analytics,
                            title = if (localizationManager.currentLocale == "tr") 
                                "Analitik Verilerini Paylaş" else "Share Analytics",
                            subtitle = if (localizationManager.currentLocale == "tr") 
                                "Uygulamayı geliştirmemize yardımcı olun" 
                            else "Help us improve the app",
                            color = Color(0xFF66D68C),
                            checked = shareAnalytics,
                            onCheckedChange = { shareAnalytics = it }
                        )
                    }
                }
            }
            
            // Security
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (localizationManager.currentLocale == "tr") "Güvenlik" else "Security",
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
                        PrivacySwitchItem(
                            icon = Icons.Default.Security,
                            title = if (localizationManager.currentLocale == "tr") 
                                "İki Faktörlü Kimlik Doğrulama" else "Two-Factor Authentication",
                            subtitle = if (localizationManager.currentLocale == "tr") 
                                "Hesabınızı ekstra koruma katmanı ile güvenli tutun" 
                            else "Keep your account secure with extra protection",
                            color = Color(0xFF4CAF50),
                            checked = twoFactorEnabled,
                            onCheckedChange = { twoFactorEnabled = it }
                        )
                        
                        HorizontalDivider(color = textSecondaryColor.copy(alpha = 0.1f))
                        
                        PrivacySwitchItem(
                            icon = Icons.Default.LocationOn,
                            title = if (localizationManager.currentLocale == "tr") 
                                "Konum Bilgisi" else "Location Services",
                            subtitle = if (localizationManager.currentLocale == "tr") 
                                "Konumunuzu her zaman paylaş" 
                            else "Always share your location",
                            color = Color(0xFFFF9500),
                            checked = showLocationAlways,
                            onCheckedChange = { showLocationAlways = it }
                        )
                    }
                }
            }
            
            // Data Management
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (localizationManager.currentLocale == "tr") "Veri Yönetimi" else "Data Management",
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
                        PrivacyMenuItem(
                            icon = Icons.Default.Download,
                            title = if (localizationManager.currentLocale == "tr") 
                                "Verilerimi İndir" else "Download My Data",
                            subtitle = if (localizationManager.currentLocale == "tr") 
                                "Tüm verilerinizin bir kopyasını indirin" 
                            else "Download a copy of all your data",
                            color = Color(0xFF66D68C),
                            onClick = { /* Download data logic */ }
                        )
                        
                        HorizontalDivider(color = textSecondaryColor.copy(alpha = 0.1f))
                        
                        PrivacyMenuItem(
                            icon = Icons.Default.CleaningServices,
                            title = if (localizationManager.currentLocale == "tr") 
                                "Önbelleği Temizle" else "Clear Cache",
                            subtitle = if (localizationManager.currentLocale == "tr") 
                                "Geçici dosyaları temizle" 
                            else "Clear temporary files",
                            color = Color(0xFFFF9500),
                            onClick = { /* Clear cache logic */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacySwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
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
                text = subtitle,
                fontSize = 14.sp,
                color = textSecondaryColor
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF66D68C),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray
            )
        )
    }
}

@Composable
private fun PrivacyMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
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
                text = subtitle,
                fontSize = 14.sp,
                color = textSecondaryColor
            )
        }
        
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
