package tr.edu.bilimankara20307006.taskflow.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager

/**
 * Profil Düzenleme Ekranı
 * Kullanıcı profil bilgilerini düzenleyebilir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    
    // Back button handling
    BackHandler(enabled = true) {
        onBackClick()
    }
    
    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var photoVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
        delay(150)
        photoVisible = true
        delay(150)
        contentVisible = true
    }
    
    val headerAlpha by animateFloatAsState(
        targetValue = if (headerVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )
    
    val photoAlpha by animateFloatAsState(
        targetValue = if (photoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "photoAlpha"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )
    
    // Get current user from Firebase
    val currentUser = FirebaseAuth.getInstance().currentUser
    val displayName = currentUser?.displayName ?: "User"
    val email = currentUser?.email ?: ""
    val userInitial = displayName.firstOrNull()?.uppercase() ?: "U"
    
    // Form states
    var fullName by remember { mutableStateOf(displayName) }
    var userEmail by remember { mutableStateOf(email) }
    var bio by remember { mutableStateOf("") }
    
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
                        text = localizationManager.localizedString("ProfileInformation"),
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
                            imageVector = Icons.Default.Close,
                            contentDescription = localizationManager.localizedString("Back"),
                            tint = textColor
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // TODO: Kaydetme işlemi
                            onBackClick()
                        },
                        modifier = Modifier.alpha(headerAlpha)
                    ) {
                        Text(
                            text = localizationManager.localizedString("Edit"),
                            color = Color(0xFF007AFF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // Profile Photo Section with Name and Email - iOS style
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(photoAlpha),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF007AFF))
                            .clickable {
                                // TODO: Resim seçme işlemi
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitial,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // User Name
                    Text(
                        text = displayName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    
                    // Email
                    Text(
                        text = email,
                        fontSize = 14.sp,
                        color = textSecondaryColor
                    )
                }
            }
            
            // Full Name Field - iOS style
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(contentAlpha),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (localizationManager.currentLocale == "tr") "Ad Soyad" else "Full Name",
                        fontSize = 13.sp,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardBackground
                        )
                    ) {
                        TextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = {
                                Text(
                                    displayName,
                                    color = textSecondaryColor
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                cursorColor = Color(0xFF007AFF)
                            )
                        )
                    }
                }
            }
            
            // Email Field - iOS style
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(contentAlpha),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (localizationManager.currentLocale == "tr") "E-posta" else "Email",
                        fontSize = 13.sp,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardBackground
                        )
                    ) {
                        TextField(
                            value = userEmail,
                            onValueChange = { userEmail = it },
                            placeholder = {
                                Text(
                                    email,
                                    color = textSecondaryColor
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                cursorColor = Color(0xFF007AFF)
                            )
                        )
                    }
                }
            }
            
            // About Section - iOS style
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(contentAlpha),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (localizationManager.currentLocale == "tr") 
                            "Hakkımda" else "About",
                        fontSize = 13.sp,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardBackground
                        )
                    ) {
                        TextField(
                            value = bio,
                            onValueChange = { bio = it },
                            placeholder = {
                                Text(
                                    if (localizationManager.currentLocale == "tr") 
                                        "" 
                                    else "",
                                    color = textSecondaryColor
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp),
                            minLines = 6,
                            maxLines = 10,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                cursorColor = Color(0xFF007AFF)
                            )
                        )
                    }
                }
            }
            
            // Password Change Button - iOS style
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(contentAlpha)
                        .clickable {
                            // TODO: Şifre değiştirme ekranı
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardBackground
                    )
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
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = if (localizationManager.currentLocale == "tr") 
                                    "Şifre Değiştir" else "Change Password",
                                fontSize = 16.sp,
                                color = textColor
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = textSecondaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Delete Account Button - iOS style
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(contentAlpha)
                        .clickable {
                            // TODO: Hesap silme uyarısı
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardBackground
                    )
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
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = if (localizationManager.currentLocale == "tr") 
                                    "Hesabı Sil" else "Delete Account",
                                fontSize = 16.sp,
                                color = Color(0xFFFF3B30)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = textSecondaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
