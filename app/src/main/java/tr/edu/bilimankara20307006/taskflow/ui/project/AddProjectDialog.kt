package tr.edu.bilimankara20307006.taskflow.ui.project

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.model.User
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Yeni Proje Ekleme Dialog'u
 * Proje başlığı, tanımı, teslim tarihi, takım üyeleri ve görevler eklenebilir
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onProjectCreated: (Project, List<String>) -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    
    // Form states
    var projectTitle by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTeamMembers by remember { mutableStateOf<List<User>>(emptyList()) }
    var teamLeader by remember { mutableStateOf<User?>(null) }
    var showTeamSelector by remember { mutableStateOf(false) }
    
    // Animation states
    var dialogVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(50)
        dialogVisible = true
    }
    
    val dialogAlpha by animateFloatAsState(
        targetValue = if (dialogVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "dialogAlpha"
    )
    
    val dialogScale by animateFloatAsState(
        targetValue = if (dialogVisible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dialogScale"
    )
    
    // Theme colors
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val inputBackground = MaterialTheme.colorScheme.surfaceVariant
    
    Dialog(
        onDismissRequest = {
            dialogVisible = false
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f * dialogAlpha)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f)
                    .scale(dialogScale)
                    .alpha(dialogAlpha),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizationManager.localizedString("NewProject"),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        IconButton(onClick = {
                            dialogVisible = false
                            onDismiss()
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = textSecondaryColor
                            )
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    // Content
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 20.dp)
                    ) {
                        // Proje Başlığı
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Proje Başlığı",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                OutlinedTextField(
                                    value = projectTitle,
                                    onValueChange = { projectTitle = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Proje adını girin") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = inputBackground,
                                        unfocusedContainerColor = inputBackground,
                                        focusedBorderColor = Color(0xFF4CAF50),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                        
                        // Proje Tanımı
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Proje Tanımı",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                OutlinedTextField(
                                    value = projectDescription,
                                    onValueChange = { projectDescription = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    placeholder = { Text("Proje hakkında detaylı açıklama yazın") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = inputBackground,
                                        unfocusedContainerColor = inputBackground,
                                        focusedBorderColor = Color(0xFF4CAF50),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    maxLines = 5
                                )
                            }
                        }
                        
                        // Teslim Tarihi
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Teslim Tarihi",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDatePicker = true },
                                    color = inputBackground,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedDate?.let {
                                                SimpleDateFormat("dd MMMM yyyy", Locale("tr")).format(it)
                                            } ?: "Tarih seçin",
                                            color = if (selectedDate != null) textColor else textSecondaryColor,
                                            fontSize = 16.sp
                                        )
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = "Takvim",
                                            tint = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Takım Üyeleri
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Takım Üyeleri",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                    
                                    Button(
                                        onClick = { showTeamSelector = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = localizationManager.localizedString("Add"),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(localizationManager.localizedString("AddMember"), fontSize = 14.sp)
                                    }
                                }
                                
                                if (selectedTeamMembers.isEmpty()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = inputBackground,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = localizationManager.localizedString("NoTeamMembers"),
                                            modifier = Modifier.padding(16.dp),
                                            color = textSecondaryColor,
                                            fontSize = 14.sp
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        selectedTeamMembers.forEach { member ->
                                            TeamMemberCard(
                                                member = member,
                                                isLeader = member == teamLeader,
                                                onSetLeader = { teamLeader = member },
                                                onRemove = { 
                                                    selectedTeamMembers = selectedTeamMembers - member
                                                    if (teamLeader == member) teamLeader = null
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    // Footer Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                dialogVisible = false
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textColor
                            )
                        ) {
                            Text("İptal", fontSize = 16.sp)
                        }
                        
                        Button(
                            onClick = {
                                if (projectTitle.isNotBlank()) {
                                    val newProject = Project(
                                        title = projectTitle,
                                        description = projectDescription,
                                        iconName = "folder",
                                        iconColor = "green",
                                        dueDate = selectedDate?.toString(),
                                        tasksCount = 0,
                                        completedTasksCount = 0
                                    )
                                    val memberIds = selectedTeamMembers.map { it.uid }
                                    onProjectCreated(newProject, memberIds)
                                    dialogVisible = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            enabled = projectTitle.isNotBlank()
                        ) {
                            Text("Projeyi Kaydet", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        
        // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismiss = { showDatePicker = false },
                onDateSelected = { date ->
                    selectedDate = date
                    showDatePicker = false
                }
            )
        }
        
        // Team Selector Dialog
        if (showTeamSelector) {
            TeamSelectorDialog(
                selectedMembers = selectedTeamMembers,
                onDismiss = { showTeamSelector = false },
                onMembersSelected = { members ->
                    selectedTeamMembers = members
                    showTeamSelector = false
                }
            )
        }
        

    }
}

@Composable
fun TeamMemberCard(
    member: User,
    isLeader: Boolean,
    onSetLeader: () -> Unit,
    onRemove: () -> Unit
) {
    val cardBackground = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardBackground,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.displayName?.firstOrNull()?.uppercase() ?: "U",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Column {
                    Text(
                        text = member.displayName ?: "Unknown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = member.email ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isLeader) {
                    Surface(
                        color = Color(0xFFFF9F0A).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Lider",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9F0A)
                        )
                    }
                } else {
                    IconButton(onClick = onSetLeader, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Lider Yap",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Kaldır",
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Date) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Sadece bugün ve sonraki günleri seçilebilir yap
                return utcTimeMillis >= System.currentTimeMillis() - 86400000 // 24 saat tolerans
            }
        }
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(Date(it))
                }
            }) {
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun TeamSelectorDialog(
    selectedMembers: List<User>,
    onDismiss: () -> Unit,
    onMembersSelected: (List<User>) -> Unit
) {
    var tempSelectedMembers by remember { mutableStateOf(selectedMembers) }
    var searchEmail by remember { mutableStateOf("") }
    var searchedUser by remember { mutableStateOf<User?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val repository = remember { tr.edu.bilimankara20307006.taskflow.data.repository.ProjectRepository.getInstance() }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column {
                Text(
                    text = "Takım Üyesi Ekle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(20.dp)
                )
                
                HorizontalDivider()
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Email arama
                    OutlinedTextField(
                        value = searchEmail,
                        onValueChange = { searchEmail = it },
                        label = { Text("E-posta Adresi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    
                    Button(
                        onClick = {
                            scope.launch {
                                isSearching = true
                                errorMessage = null
                                val result = repository.searchUserByEmail(searchEmail.trim())
                                result.onSuccess { user ->
                                    if (user != null) {
                                        searchedUser = user
                                    } else {
                                        errorMessage = "Kullanıcı bulunamadı"
                                    }
                                }.onFailure { error ->
                                    errorMessage = error.message
                                }
                                isSearching = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = searchEmail.isNotBlank() && !isSearching
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Kullanıcı Ara")
                        }
                    }
                    
                    // Hata mesajı
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                    
                    // Aranan kullanıcı
                    searchedUser?.let { user ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.displayName?.firstOrNull()?.uppercase() ?: "U",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                    
                                    Column {
                                        Text(
                                            text = user.displayName ?: "Unknown",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = user.email ?: "",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Button(
                                    onClick = {
                                        if (!tempSelectedMembers.any { it.uid == user.uid }) {
                                            tempSelectedMembers = tempSelectedMembers + user
                                        }
                                        searchedUser = null
                                        searchEmail = ""
                                    }
                                ) {
                                    Text("Ekle")
                                }
                            }
                        }
                    }
                    
                    // Seçilen üyeler
                    if (tempSelectedMembers.isNotEmpty()) {
                        Text(
                            text = "Seçilen Üyeler",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tempSelectedMembers) { member ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(Color(0xFF4CAF50).copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = member.displayName?.firstOrNull()?.uppercase() ?: "U",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4CAF50)
                                                )
                                            }
                                            
                                            Column {
                                                Text(
                                                    text = member.displayName ?: "Unknown",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = member.email ?: "",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        
                                        IconButton(
                                            onClick = {
                                                tempSelectedMembers = tempSelectedMembers - member
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Kaldır",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                HorizontalDivider()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("İptal")
                    }
                    
                    Button(
                        onClick = { onMembersSelected(tempSelectedMembers) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tamam")
                    }
                }
            }
        }
    }
}