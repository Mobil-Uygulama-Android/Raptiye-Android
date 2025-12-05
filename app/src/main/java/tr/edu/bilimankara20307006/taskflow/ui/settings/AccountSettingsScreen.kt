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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

/**
 * Hesap Ayarları Ekranı - iOS Account Settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    val auth = FirebaseAuth.getInstance()
    
    // States
    var headerVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
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
                        text = if (localizationManager.currentLocale == "tr") "Hesap Ayarları" else "Account Settings",
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
            // Personal Information Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (localizationManager.currentLocale == "tr") "Kişisel Bilgiler" else "Personal Information",
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
                        PersonalInfoItem(
                            label = if (localizationManager.currentLocale == "tr") "Ad Soyad" else "Full Name",
                            value = auth.currentUser?.displayName ?: "",
                            isClickable = true,
                            onClick = { showNameDialog = true }
                        )
                        
                        HorizontalDivider(color = textSecondaryColor.copy(alpha = 0.1f))
                        
                        PersonalInfoItem(
                            label = if (localizationManager.currentLocale == "tr") "E-posta" else "Email",
                            value = auth.currentUser?.email ?: "",
                            isClickable = false,
                            onClick = { }
                        )
                        
                        HorizontalDivider(color = textSecondaryColor.copy(alpha = 0.1f))
                        
                        PersonalInfoItem(
                            label = if (localizationManager.currentLocale == "tr") "Hakkımda" else "About Me",
                            value = if (localizationManager.currentLocale == "tr") 
                                "Kendinizi tanıtın..." else "Tell us about yourself...",
                            isClickable = true,
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }
            
            // Security Section
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
                        SettingsMenuItem(
                            icon = Icons.Default.Lock,
                            title = if (localizationManager.currentLocale == "tr") "Şifre Değiştir" else "Change Password",
                            subtitle = if (localizationManager.currentLocale == "tr") 
                                "Hesap şifrenizi güncelleyin" else "Update your account password",
                            color = Color(0xFFFF9500),
                            onClick = { showPasswordDialog = true }
                        )
                    }
                }
            }
            
            // Danger Zone
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (localizationManager.currentLocale == "tr") "Tehlikeli Bölge" else "Danger Zone",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF3B30)
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cardBackground,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        SettingsMenuItem(
                            icon = Icons.Default.Delete,
                            title = if (localizationManager.currentLocale == "tr") "Hesabı Sil" else "Delete Account",
                            subtitle = if (localizationManager.currentLocale == "tr") 
                                "Hesabınızı kalıcı olarak silin" else "Permanently delete your account",
                            color = Color(0xFFFF3B30),
                            showChevron = false,
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }
            }
        }
    }
    
    // Name Dialog
    if (showNameDialog) {
        var fullName by remember { mutableStateOf(auth.currentUser?.displayName ?: "") }
        
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = if (localizationManager.currentLocale == "tr") "Ad Soyad Değiştir" else "Change Name",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(if (localizationManager.currentLocale == "tr") "Ad Soyad" else "Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Name update logic
                        showNameDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4CAF50))
                ) {
                    Text(if (localizationManager.currentLocale == "tr") "Güncelle" else "Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text(if (localizationManager.currentLocale == "tr") "İptal" else "Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
    
    // Email Dialog
    if (showEmailDialog) {
        var newEmail by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            icon = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = if (localizationManager.currentLocale == "tr") "E-posta Değiştir" else "Change Email",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text(if (localizationManager.currentLocale == "tr") "Yeni E-posta" else "New Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(if (localizationManager.currentLocale == "tr") "Mevcut Şifre" else "Current Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Email update logic
                        showEmailDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2196F3))
                ) {
                    Text(if (localizationManager.currentLocale == "tr") "Güncelle" else "Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailDialog = false }) {
                    Text(if (localizationManager.currentLocale == "tr") "İptal" else "Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
    
    // About Dialog
    if (showAboutDialog) {
        var aboutText by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = if (localizationManager.currentLocale == "tr") "Hakkımda" else "About Me",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = aboutText,
                    onValueChange = { aboutText = it },
                    label = { Text(if (localizationManager.currentLocale == "tr") "Kendinizi tanıtın" else "Tell us about yourself") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // About update logic
                        showAboutDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9C27B0))
                ) {
                    Text(if (localizationManager.currentLocale == "tr") "Kaydet" else "Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(if (localizationManager.currentLocale == "tr") "İptal" else "Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
    
    // Password Dialog
    if (showPasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            icon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFFF9500),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = if (localizationManager.currentLocale == "tr") "Şifre Değiştir" else "Change Password",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text(if (localizationManager.currentLocale == "tr") "Mevcut Şifre" else "Current Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(if (localizationManager.currentLocale == "tr") "Yeni Şifre" else "New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(if (localizationManager.currentLocale == "tr") "Şifre Tekrar" else "Confirm Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Password update logic
                        showPasswordDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF9500))
                ) {
                    Text(if (localizationManager.currentLocale == "tr") "Güncelle" else "Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text(if (localizationManager.currentLocale == "tr") "İptal" else "Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
    
    // Delete Account Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF3B30),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = if (localizationManager.currentLocale == "tr") "Hesabı Sil?" else "Delete Account?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF3B30)
                )
            },
            text = {
                Text(
                    text = if (localizationManager.currentLocale == "tr") 
                        "Bu işlem geri alınamaz. Tüm verileriniz kalıcı olarak silinecektir."
                    else "This action cannot be undone. All your data will be permanently deleted.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Delete account logic
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF3B30))
                ) {
                    Text(if (localizationManager.currentLocale == "tr") "Sil" else "Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(if (localizationManager.currentLocale == "tr") "İptal" else "Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SettingsMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    showChevron: Boolean = true,
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
        
        if (showChevron) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PersonalInfoItem(
    label: String,
    value: String,
    isClickable: Boolean = true,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = textSecondaryColor
            )
            Text(
                text = value.ifEmpty { 
                    if (label.contains("Hakkımda") || label.contains("About")) 
                        if (label.contains("tr")) "Henüz eklenmedi" else "Not added yet"
                    else ""
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (value.isEmpty()) textSecondaryColor else textColor
            )
        }
        
        if (isClickable) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
