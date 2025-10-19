package tr.edu.bilimankara20307006.taskflow.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    // Form states
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    
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
                        text = if (localizationManager.currentLocale == "tr") "Profili Düzenle" else "Edit Profile",
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
                actions = {
                    TextButton(
                        onClick = {
                            // TODO: Kaydetme işlemi
                            onBackClick()
                        },
                        modifier = Modifier.alpha(headerAlpha)
                    ) {
                        Text(
                            text = localizationManager.localizedString("Save"),
                            color = Color(0xFF4CAF50),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // Profile Photo Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(photoAlpha),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.1f))
                            .border(2.dp, Color(0xFF4CAF50), CircleShape)
                            .clickable {
                                // TODO: Resim seçme işlemi
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUrl == null) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = if (localizationManager.currentLocale == "tr") 
                                    "Fotoğraf Ekle" else "Add Photo",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = if (localizationManager.currentLocale == "tr") 
                            "Profil Fotoğrafı Ekle" else "Add Profile Photo",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Personal Information Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(contentAlpha),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (localizationManager.currentLocale == "tr") 
                            "Kişisel Bilgiler" else "Personal Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    // First Name
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = {
                            Text(
                                if (localizationManager.currentLocale == "tr") "Ad" else "First Name"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = textSecondaryColor.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF4CAF50),
                            unfocusedLabelColor = textSecondaryColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = Color(0xFF4CAF50)
                        )
                    )
                    
                    // Last Name
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = {
                            Text(
                                if (localizationManager.currentLocale == "tr") "Soyad" else "Last Name"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = textSecondaryColor.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF4CAF50),
                            unfocusedLabelColor = textSecondaryColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = Color(0xFF4CAF50)
                        )
                    )
                }
            }
            
            // Professional Information Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(contentAlpha),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (localizationManager.currentLocale == "tr") 
                            "Profesyonel Bilgiler" else "Professional Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    // Title/Position
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = {
                            Text(
                                if (localizationManager.currentLocale == "tr") "Unvan" else "Title"
                            )
                        },
                        placeholder = {
                            Text(
                                if (localizationManager.currentLocale == "tr") 
                                    "ör: Yazılım Geliştirici" else "e.g: Software Developer"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = textSecondaryColor.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF4CAF50),
                            unfocusedLabelColor = textSecondaryColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = Color(0xFF4CAF50)
                        )
                    )
                    
                    // Skills
                    OutlinedTextField(
                        value = skills,
                        onValueChange = { skills = it },
                        label = {
                            Text(
                                if (localizationManager.currentLocale == "tr") "Yetenekler" else "Skills"
                            )
                        },
                        placeholder = {
                            Text(
                                if (localizationManager.currentLocale == "tr") 
                                    "ör: Kotlin, Android, UI/UX" else "e.g: Kotlin, Android, UI/UX"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = textSecondaryColor.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF4CAF50),
                            unfocusedLabelColor = textSecondaryColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = Color(0xFF4CAF50)
                        )
                    )
                }
            }
            
            // Bio Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(contentAlpha),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (localizationManager.currentLocale == "tr") 
                            "Biyografi" else "Biography",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = {
                            Text(
                                if (localizationManager.currentLocale == "tr") 
                                    "Kısa Biyografi" else "Short Bio"
                            )
                        },
                        placeholder = {
                            Text(
                                if (localizationManager.currentLocale == "tr") 
                                    "Kendiniz hakkında kısa bir açıklama yazın..." 
                                else "Write a short description about yourself..."
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 5,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = textSecondaryColor.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF4CAF50),
                            unfocusedLabelColor = textSecondaryColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = Color(0xFF4CAF50)
                        )
                    )
                }
            }
            
            // Save Button (Additional, optional since we have one in top bar)
            item {
                Button(
                    onClick = {
                        // TODO: Kaydetme işlemi
                        onBackClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .alpha(contentAlpha),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        text = localizationManager.localizedString("Save"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
