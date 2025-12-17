package tr.edu.bilimankara20307006.taskflow.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    
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
                            "Yardım" else "Help",
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
            // Başlarken Section
            FAQSection(
                title = if (localizationManager.currentLocale == "tr") "Başlarken" else "Getting Started",
                items = if (localizationManager.currentLocale == "tr") listOf(
                    FAQItem(
                        question = "Nasıl proje oluştururum?",
                        answer = "Projeler ekranındaki '+' butonuna tıklayarak yeni bir proje oluşturun. Proje detaylarını doldurun ve görevler ekleyin."
                    ),
                    FAQItem(
                        question = "Nasıl ekip üyesi eklerim?",
                        answer = "Bir projeyi açın, 'Üye Ekle' butonuna tıklayın ve kullanıcıları e-posta adreslerine göre arayın."
                    ),
                    FAQItem(
                        question = "Nasıl görev eklerim?",
                        answer = "Proje detaylarında, Görevler bölümündeki '+' simgesine tıklayarak yeni bir görev ekleyin."
                    )
                ) else listOf(
                    FAQItem(
                        question = "How do I create a project?",
                        answer = "Tap the '+' button on the Projects screen to create a new project. Fill in the project details and add tasks."
                    ),
                    FAQItem(
                        question = "How do I add team members?",
                        answer = "Open a project, tap 'Add Member' and search for users by their email addresses."
                    ),
                    FAQItem(
                        question = "How do I add tasks?",
                        answer = "In project details, tap the '+' icon in the Tasks section to add a new task."
                    )
                ),
                textColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                surfaceColor = surfaceColor
            )
            
            // Projeleri Yönetme Section
            FAQSection(
                title = if (localizationManager.currentLocale == "tr") "Projeleri Yönetme" else "Managing Projects",
                items = if (localizationManager.currentLocale == "tr") listOf(
                    FAQItem(
                        question = "Bir projeyi nasıl düzenlerim?",
                        answer = "Projeyi açın, sağ üstteki menü simgesine (•••) tıklayın ve 'Projeyi Düzenle' seçeneğini seçin."
                    ),
                    FAQItem(
                        question = "Bir projeyi nasıl silerim?",
                        answer = "Projeyi açın, menü simgesine (•••) tıklayın, 'Projeyi Sil' seçeneğini seçin ve onaylayın."
                    ),
                    FAQItem(
                        question = "İlerlemeyi nasıl takip ederim?",
                        answer = "Proje ilerlemesi tamamlanan görevlere göre otomatik olarak hesaplanır. Grafik simgesine tıklayarak detaylı analitiği görüntüleyin."
                    )
                ) else listOf(
                    FAQItem(
                        question = "How do I edit a project?",
                        answer = "Open the project, tap the menu icon (•••) in the top right, and select 'Edit Project'."
                    ),
                    FAQItem(
                        question = "How do I delete a project?",
                        answer = "Open the project, tap the menu icon (•••), select 'Delete Project', and confirm."
                    ),
                    FAQItem(
                        question = "How do I track progress?",
                        answer = "Project progress is automatically calculated based on completed tasks. Tap the graph icon to view detailed analytics."
                    )
                ),
                textColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                surfaceColor = surfaceColor
            )
            
            // Hesap ve Ayarlar Section
            FAQSection(
                title = if (localizationManager.currentLocale == "tr") "Hesap ve Ayarlar" else "Account & Settings",
                items = if (localizationManager.currentLocale == "tr") listOf(
                    FAQItem(
                        question = "Profilimi nasıl değiştiririm?",
                        answer = "Ayarlar sekmesine gidin, bilgilerinizi düzenlemek için en üstteki profil kartınıza tıklayın."
                    ),
                    FAQItem(
                        question = "Dili nasıl değiştiririm?",
                        answer = "Ayarlar > Dil'e gidin ve Türkçe veya İngilizce'yi seçin."
                    ),
                    FAQItem(
                        question = "Karanlık modu nasıl etkinleştiririm?",
                        answer = "Ayarlar'a gidin ve 'Koyu Tema' anahtarını açın."
                    )
                ) else listOf(
                    FAQItem(
                        question = "How do I change my profile?",
                        answer = "Go to Settings tab, tap your profile card at the top to edit your information."
                    ),
                    FAQItem(
                        question = "How do I change the language?",
                        answer = "Go to Settings > Language and select Turkish or English."
                    ),
                    FAQItem(
                        question = "How do I enable dark mode?",
                        answer = "Go to Settings and turn on the 'Dark Mode' switch."
                    )
                ),
                textColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor,
                surfaceColor = surfaceColor
            )
            
            // Daha Fazla Yardıma İhtiyacınız Var mı? Section
            Text(
                text = if (localizationManager.currentLocale == "tr") 
                    "Daha Fazla Yardıma İhtiyacınız Var mı?" else "Need More Help?",
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
                    ContactItem(
                        icon = Icons.Default.Email,
                        title = if (localizationManager.currentLocale == "tr") 
                            "E-posta Desteği" else "Email Support",
                        color = Color(0xFF66D68C),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@raptiye.app")
                                putExtra(Intent.EXTRA_SUBJECT, "Raptiye Support Request")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FAQSection(
    title: String,
    items: List<FAQItem>,
    textColor: Color,
    secondaryTextColor: Color,
    surfaceColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
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
                items.forEachIndexed { index, item ->
                    ExpandableFAQItem(
                        item = item,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor
                    )
                    
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            color = secondaryTextColor.copy(alpha = 0.1f),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableFAQItem(
    item: FAQItem,
    textColor: Color,
    secondaryTextColor: Color
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "chevronRotation"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.question,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = secondaryTextColor,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotationAngle)
            )
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(300),
                expandFrom = Alignment.Top
            ),
            exit = shrinkVertically(
                animationSpec = tween(300),
                shrinkTowards = Alignment.Top
            )
        ) {
            Text(
                text = item.answer,
                fontSize = 14.sp,
                color = secondaryTextColor,
                lineHeight = 20.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun ContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    
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
        
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = textColor.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

data class FAQItem(
    val question: String,
    val answer: String
)
