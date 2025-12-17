package tr.edu.bilimankara20307006.taskflow.ui.task

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tr.edu.bilimankara20307006.taskflow.data.model.Task
import tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus
import tr.edu.bilimankara20307006.taskflow.data.model.User
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Görev Ekleme Ekranı - iOS AddTaskView ile birebir uyumlu
 * 
 * Özellikler:
 * - Görev başlığı ve açıklaması
 * - Görevli seçimi (avatar circle ile)
 * - Öncelik seçimi (Low, Medium, High)
 * - Son teslim tarihi seçimi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    projectId: String,
    projectName: String,
    availableAssignees: List<User>,
    onBackClick: () -> Unit,
    onTaskCreated: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale
    val focusManager = LocalFocusManager.current
    
    // Form states
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedAssignee by remember { mutableStateOf<User?>(null) }
    var selectedPriority by remember { mutableStateOf("medium") }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var showAddMemberScreen by remember { mutableStateOf(false) }
    
    // Animation states
    var screenVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        screenVisible = true
    }
    
    val topBarAlpha by animateFloatAsState(
        targetValue = if (screenVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "topBarAlpha"
    )
    
    // Theme colors
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = Color(0xFF1E2228) // iOS dark card background
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val inputBackground = Color(0xFF262C35) // iOS input background
    val greenColor = Color(0xFF66D68C) // Green accent color
    
    // Date formatter
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizationManager.localizedString("AddTask"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizationManager.localizedString("Back"),
                            tint = textColor
                        )
                    }
                },
                actions = {
                    // Save button
                    TextButton(
                        onClick = {
                            if (taskTitle.isBlank()) {
                                alertMessage = localizationManager.localizedString("PleaseEnterTaskTitle")
                                showAlert = true
                            } else {
                                val newTask = Task(
                                    projectId = projectId,
                                    title = taskTitle,
                                    description = taskDescription,
                                    status = TaskStatus.TODO,
                                    priority = selectedPriority,
                                    assigneeId = selectedAssignee?.uid ?: "",
                                    assignee = selectedAssignee,
                                    dueDate = dueDate?.let { dateFormatter.format(it) }
                                )
                                onTaskCreated(newTask)
                                onBackClick()
                            }
                        },
                        enabled = taskTitle.isNotBlank()
                    ) {
                        Text(
                            text = localizationManager.localizedString("Save"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (taskTitle.isNotBlank()) greenColor else textSecondaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground
                ),
                modifier = Modifier.alpha(topBarAlpha)
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Görev Başlığı
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = localizationManager.localizedString("TaskTitle"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSecondaryColor
                        )
                        
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = localizationManager.localizedString("EnterTaskTitle"),
                                    color = textSecondaryColor.copy(alpha = 0.5f)
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 16.sp,
                                color = textColor
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = inputBackground,
                                focusedContainerColor = inputBackground,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = greenColor,
                                cursorColor = greenColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
                
                // Görev Açıklaması
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = localizationManager.localizedString("TaskDescription"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSecondaryColor
                        )
                        
                        OutlinedTextField(
                            value = taskDescription,
                            onValueChange = { taskDescription = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = {
                                Text(
                                    text = localizationManager.localizedString("EnterTaskDescription"),
                                    color = textSecondaryColor.copy(alpha = 0.5f)
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 16.sp,
                                color = textColor
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = inputBackground,
                                focusedContainerColor = inputBackground,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = greenColor,
                                cursorColor = greenColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5
                        )
                    }
                }
                
                // Görevli Seçimi
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizationManager.localizedString("Assignee"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = textSecondaryColor
                            )
                            
                            // Add Member button
                            TextButton(
                                onClick = { showAddMemberScreen = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = greenColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Üye Ekle",
                                    fontSize = 14.sp,
                                    color = greenColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        // Üye yoksa bilgilendirme kartı
                        if (availableAssignees.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = inputBackground
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = textSecondaryColor.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = localizationManager.localizedString("NoTeamMembers"),
                                        fontSize = 14.sp,
                                        color = textSecondaryColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = localizationManager.localizedString("AddMembersToAssignTasks"),
                                        fontSize = 12.sp,
                                        color = textSecondaryColor.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        
                        // Seçili görevli (varsa)
                        if (selectedAssignee != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = inputBackground
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar circle
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(greenColor.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = selectedAssignee!!.initials,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = greenColor
                                        )
                                    }
                                    
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = selectedAssignee!!.displayName ?: "Unknown",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textColor
                                        )
                                        Text(
                                            text = selectedAssignee!!.email ?: "",
                                            fontSize = 13.sp,
                                            color = textSecondaryColor
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = { selectedAssignee = null }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = textSecondaryColor
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Kullanıcı listesi
                        availableAssignees.filter { it.uid != selectedAssignee?.uid }.forEach { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedAssignee = user },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2A3038) // Slightly lighter
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar circle
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(greenColor.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.initials,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = greenColor
                                        )
                                    }
                                    
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = user.displayName ?: "Unknown",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textColor
                                        )
                                        Text(
                                            text = user.email ?: "",
                                            fontSize = 13.sp,
                                            color = textSecondaryColor
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = "Select",
                                        tint = greenColor
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Öncelik Seçimi
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = localizationManager.localizedString("Priority"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSecondaryColor
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf("low", "medium", "high").forEach { priority ->
                                val isSelected = selectedPriority == priority
                                val priorityText = when (priority) {
                                    "low" -> localizationManager.localizedString("LowPriority")
                                    "medium" -> localizationManager.localizedString("MediumPriority")
                                    "high" -> localizationManager.localizedString("HighPriority")
                                    else -> priority
                                }
                                
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPriority = priority },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) 
                                            greenColor.copy(alpha = 0.2f) 
                                        else 
                                            inputBackground
                                    ),
                                    border = if (isSelected) 
                                        androidx.compose.foundation.BorderStroke(1.dp, greenColor) 
                                    else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) 
                                                Icons.Default.CheckCircle 
                                            else 
                                                Icons.Default.Circle,
                                            contentDescription = null,
                                            tint = if (isSelected) greenColor else textSecondaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = priorityText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) textColor else textSecondaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Son Teslim Tarihi
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = localizationManager.localizedString("DueDate"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSecondaryColor
                        )
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = inputBackground
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
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = greenColor
                                    )
                                    Text(
                                        text = dueDate?.let { dateFormatter.format(it) }
                                            ?: localizationManager.localizedString("SelectDate"),
                                        fontSize = 16.sp,
                                        color = if (dueDate != null) textColor else textSecondaryColor
                                    )
                                }
                                
                                if (dueDate != null) {
                                    IconButton(
                                        onClick = { dueDate = null }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear date",
                                            tint = textSecondaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Bottom spacing for save button
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate?.time ?: System.currentTimeMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Sadece bugün ve sonraki günleri seçilebilir yap
                    return utcTimeMillis >= System.currentTimeMillis() - 86400000 // 24 saat tolerans
                }
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dueDate = Date(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(
                        text = localizationManager.localizedString("OK"),
                        color = greenColor
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        text = localizationManager.localizedString("Cancel"),
                        color = textSecondaryColor
                    )
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = cardBackground
                )
            )
        }
    }
    
    // Alert Dialog
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = {
                Text(
                    text = localizationManager.localizedString("Warning"),
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(text = alertMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = { showAlert = false }
                ) {
                    Text(
                        text = localizationManager.localizedString("OK"),
                        color = greenColor
                    )
                }
            },
            containerColor = cardBackground
        )
    }
    
    // Add Member Screen Modal
    if (showAddMemberScreen) {
        AddTaskMemberScreen(
            projectId = projectId,
            projectName = projectName,
            existingMembers = availableAssignees,
            onMemberSelected = { user ->
                selectedAssignee = user
                showAddMemberScreen = false
            },
            onDismiss = { showAddMemberScreen = false }
        )
    }
}
