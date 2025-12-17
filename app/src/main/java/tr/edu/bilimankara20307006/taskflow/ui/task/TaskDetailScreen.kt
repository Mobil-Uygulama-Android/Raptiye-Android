package tr.edu.bilimankara20307006.taskflow.ui.task

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager
import tr.edu.bilimankara20307006.taskflow.data.model.Comment
import tr.edu.bilimankara20307006.taskflow.data.model.Task
import tr.edu.bilimankara20307006.taskflow.data.model.User
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Görev Detay Ekranı
 * Ekran görüntüsündeki "Task Details" tasarımına birebir uygun
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: Task,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale
    val scope = rememberCoroutineScope()
    
    // Renk tanımları
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val inputBackground = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val greenColor = Color(0xFF66D68C)
    
    // State
    var taskTitle by remember { mutableStateOf(task.title) }
    var taskDescription by remember { mutableStateOf(task.description) }
    var taskDueDate by remember { mutableStateOf(task.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isAddingComment by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Date formatter
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateParser = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    
    // Real-time yorum dinleyicisi
    DisposableEffect(task.id) {
        val listener = FirebaseManager.listenToComments(task.id) { newComments ->
            comments = newComments
        }
        
        onDispose {
            listener.remove()
        }
    }
    
    // Yorum ekleme fonksiyonu
    fun addComment() {
        if (commentText.isBlank()) return
        
        val tempComment = Comment(
            taskId = task.id,
            userId = FirebaseManager.getCurrentUserId() ?: "",
            userName = "Gönderiliyor...",
            message = commentText,
            timestamp = System.currentTimeMillis()
        )
        
        // Optimistic UI update
        val previousComments = comments
        comments = comments + tempComment
        val messageToSend = commentText
        commentText = ""
        
        scope.launch {
            isAddingComment = true
            val result = FirebaseManager.addComment(task.id, messageToSend)
            isAddingComment = false
            
            result.onFailure { error ->
                // Hata durumunda geri al
                comments = previousComments
                commentText = messageToSend
                errorMessage = error.message ?: "Yorum eklenemedi"
                showError = true
            }
            // Başarılı durumda real-time listener otomatik güncelleyecek
        }
    }
    
    // Animation states
    var topBarVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true
        delay(150)
        contentVisible = true
    }
    
    val topBarAlpha by animateFloatAsState(
        targetValue = if (topBarVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "topBarAlpha"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizationManager.localizedString("TaskDetails"),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground
                ),
                modifier = Modifier.alpha(topBarAlpha)
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .alpha(contentAlpha),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // Title Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = localizationManager.localizedString("TaskTitle"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBackground,
                            unfocusedContainerColor = inputBackground,
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
            
            // Description Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = localizationManager.localizedString("Description"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    
                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBackground,
                            unfocusedContainerColor = inputBackground,
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5
                    )
                }
            }
            
            // Assignee Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = localizationManager.localizedString("Assignee"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    
                    task.assignee?.let { assignee ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(inputBackground, RoundedCornerShape(12.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            UserAvatar(
                                user = assignee,
                                size = 40.dp
                            )
                            
                            // Name
                            Text(
                                text = assignee.displayName ?: "Unknown",
                                fontSize = 16.sp,
                                color = textColor
                            )
                        }
                    }
                }
            }
            
            // Due Date Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = localizationManager.localizedString("DueDate"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(inputBackground, RoundedCornerShape(12.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Calendar",
                                tint = Color(0xFF66D68C),
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Text(
                                text = if (!taskDueDate.isNullOrEmpty()) {
                                    try {
                                        val date = dateParser.parse(taskDueDate!!)
                                        date?.let { dateFormatter.format(it) } ?: taskDueDate!!
                                    } catch (e: Exception) {
                                        taskDueDate!!
                                    }
                                } else {
                                    "Tarih seç"
                                },
                                fontSize = 16.sp,
                                color = if (taskDueDate != null) textColor else textSecondaryColor
                            )
                        }
                        
                        if (taskDueDate != null) {
                            IconButton(
                                onClick = { taskDueDate = null }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear date",
                                    tint = textSecondaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Comments Section
            item {
                Text(
                    text = localizationManager.localizedString("Comments"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            // Comment List
            items(comments) { comment ->
                CommentItem(
                    comment = comment,
                    textColor = textColor,
                    textSecondaryColor = textSecondaryColor,
                    cardBackground = cardBackground
                )
            }
            
            // Add Comment
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Avatar circle with initials
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(greenColor.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = FirebaseManager.getCurrentUserId()?.take(2)?.uppercase() ?: "ME",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = greenColor
                        )
                    }
                    
                    // Comment Input
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "Yorum ekle...",
                                color = textSecondaryColor
                            )
                        },
                        enabled = !isAddingComment,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBackground,
                            unfocusedContainerColor = inputBackground,
                            focusedBorderColor = greenColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = greenColor,
                            disabledContainerColor = inputBackground,
                            disabledTextColor = textSecondaryColor
                        ),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 4,
                        trailingIcon = {
                            if (commentText.isNotEmpty() && !isAddingComment) {
                                IconButton(onClick = { addComment() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = greenColor
                                    )
                                }
                            } else if (isAddingComment) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = greenColor,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    )
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = taskDueDate?.let {
                try {
                    dateParser.parse(it)?.time
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            } ?: System.currentTimeMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Sadece bugün ve sonraki günleri seçilebilir yap
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    return utcTimeMillis >= today
                }
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val date = Date(it)
                            taskDueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(
                        text = localizationManager.localizedString("OK"),
                        color = Color(0xFF66D68C)
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
    
    // Error Snackbar
    if (showError) {
        LaunchedEffect(Unit) {
            delay(3000)
            showError = false
        }
        
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = Color(0xFFFF5252),
            contentColor = Color.White
        ) {
            Text(errorMessage)
        }
    }
}

/**
 * Yorum item'ı - Yeni tasarım
 */
@Composable
private fun CommentItem(
    comment: Comment,
    textColor: Color,
    textSecondaryColor: Color,
    cardBackground: Color,
    modifier: Modifier = Modifier
) {
    val greenColor = Color(0xFF66D68C)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar circle with initials
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(greenColor.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.userName.take(2).uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = greenColor
            )
        }
        
        // Comment content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                
                Text(
                    text = comment.formattedDate,
                    fontSize = 12.sp,
                    color = textSecondaryColor
                )
            }
            
            Text(
                text = comment.message,
                fontSize = 14.sp,
                color = textColor,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * Kullanıcı avatarı
 */
@Composable
private fun UserAvatar(
    user: User,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val avatarTextColor = Color.White
    val greenColor = Color(0xFF66D68C)
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(greenColor.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = user.displayName?.take(2)?.uppercase() ?: "??",
            fontSize = (size.value / 2.5).sp,
            fontWeight = FontWeight.Bold,
            color = greenColor
        )
    }
}
