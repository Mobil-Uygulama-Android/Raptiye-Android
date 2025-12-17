package tr.edu.bilimankara20307006.taskflow.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager

/**
 * iOS-style About Screen
 * Exact replica of iOS About screen design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale
    
    var contentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        contentVisible = true
    }
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )
    
    BackHandler { onBackClick() }
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizationManager.localizedString("About"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 40.dp,
                bottom = 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo and App Info
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Green Circle with Push Pin Icon
                    Surface(
                        modifier = Modifier.size(140.dp),
                        color = Color(0xFF4D7C4E),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = Color(0xFF8FD99C),
                                modifier = Modifier
                                    .size(70.dp)
                                    .graphicsLayer {
                                        rotationZ = 45f
                                    }
                            )
                        }
                    }
                    
                    Text(
                        text = "Raptiye",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    Text(
                        text = "Version 1.0.0",
                        fontSize = 15.sp,
                        color = textSecondaryColor
                    )
                }
            }
            
            // About Raptiye Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = localizationManager.localizedString("AboutRaptiye"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    
                    Text(
                        text = localizationManager.localizedString("AboutRaptiyeDescription"),
                        fontSize = 15.sp,
                        color = textSecondaryColor,
                        lineHeight = 22.sp
                    )
                }
            }
            
            // Key Features Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = localizationManager.localizedString("KeyFeatures"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }
            
            // Feature Cards
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        icon = Icons.Default.Folder,
                        iconColor = Color(0xFF66D68C),
                        title = localizationManager.localizedString("ProjectManagement"),
                        description = localizationManager.localizedString("ProjectManagementDesc"),
                        backgroundColor = cardBackground,
                        textColor = textColor,
                        textSecondaryColor = textSecondaryColor
                    )
                    
                    FeatureCard(
                        icon = Icons.Default.CheckCircle,
                        iconColor = Color(0xFF66D68C),
                        title = localizationManager.localizedString("TaskTracking"),
                        description = localizationManager.localizedString("TaskTrackingDesc"),
                        backgroundColor = cardBackground,
                        textColor = textColor,
                        textSecondaryColor = textSecondaryColor
                    )
                    
                    FeatureCard(
                        icon = Icons.Default.Group,
                        iconColor = Color(0xFF66D68C),
                        title = localizationManager.localizedString("TeamCollaboration"),
                        description = localizationManager.localizedString("TeamCollaborationDesc"),
                        backgroundColor = cardBackground,
                        textColor = textColor,
                        textSecondaryColor = textSecondaryColor
                    )
                    
                    FeatureCard(
                        icon = Icons.Default.BarChart,
                        iconColor = Color(0xFF66D68C),
                        title = localizationManager.localizedString("AnalyticsFeature"),
                        description = localizationManager.localizedString("AnalyticsDesc"),
                        backgroundColor = cardBackground,
                        textColor = textColor,
                        textSecondaryColor = textSecondaryColor
                    )
                    
                    FeatureCard(
                        icon = Icons.Default.Language,
                        iconColor = Color(0xFF66D68C),
                        title = localizationManager.localizedString("MultiLanguage"),
                        description = localizationManager.localizedString("MultiLanguageDesc"),
                        backgroundColor = cardBackground,
                        textColor = textColor,
                        textSecondaryColor = textSecondaryColor
                    )
                }
            }
            
            // Privacy Policy Link
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.raptiye.com/privacy"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error
                            }
                        },
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
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = Color(0xFF66D68C),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = localizationManager.localizedString("PrivacyPolicy"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
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
            
            // Terms of Service Link
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.raptiye.com/terms"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error
                            }
                        },
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
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF66D68C),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = localizationManager.localizedString("TermsOfService"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
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
            
            // Copyright
            item {
                Text(
                    text = "© 2025 Raptiye. " + localizationManager.localizedString("AllRightsReserved"),
                    fontSize = 13.sp,
                    color = textSecondaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    backgroundColor: Color,
    textColor: Color,
    textSecondaryColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = textSecondaryColor,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
