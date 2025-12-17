package tr.edu.bilimankara20307006.taskflow.ui.notifications

import androidx.compose.animation.*
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
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tr.edu.bilimankara20307006.taskflow.data.model.Notification
import tr.edu.bilimankara20307006.taskflow.data.model.NotificationActionType
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * iOS Benzeri Tab'lı Bildirimler Ekranı
 * - Davetler sekmesi (PROJECT_INVITATION)
 * - Bildirimler sekmesi (diğer tüm bildirimler)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Tab state
    var selectedTab by remember { mutableStateOf(0) }
    
    val darkBackground = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val greenColor = Color(0xFF4CAF50)
    
    // Bildirimleri kategorilere ayır
    val invitations = state.notifications.filter { it.type == NotificationActionType.PROJECT_INVITATION }
    val otherNotifications = state.notifications.filter { it.type != NotificationActionType.PROJECT_INVITATION }
    
    // Okunmamış davet sayısı
    val unreadInvitationsCount = invitations.count { !it.isRead }
    
    // Menu state
    var showMenu by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedNotifications = remember { mutableStateListOf<String>() }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectionMode) {
                            if (localizationManager.currentLocale == "tr")
                                "${selectedNotifications.size} seçildi"
                            else "${selectedNotifications.size} selected"
                        } else {
                            if (localizationManager.currentLocale == "tr") "Bildirimler" else "Notifications"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectionMode) {
                            selectionMode = false
                            selectedNotifications.clear()
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(
                            imageVector = if (selectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (selectionMode) "İptal" else "Geri",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    if (selectionMode) {
                        // Selection mode actions
                        IconButton(
                            onClick = {
                                selectedNotifications.forEach { notificationId ->
                                    viewModel.markAsUnread(notificationId)
                                }
                                selectedNotifications.clear()
                                selectionMode = false
                            },
                            enabled = selectedNotifications.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailUnread,
                                contentDescription = "Okunmadı olarak işaretle",
                                tint = if (selectedNotifications.isNotEmpty()) greenColor else textColor.copy(alpha = 0.3f)
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                if (selectedNotifications.isNotEmpty()) {
                                    showDeleteConfirmDialog = true
                                }
                            },
                            enabled = selectedNotifications.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = if (selectedNotifications.isNotEmpty()) Color(0xFFFF3B30) else textColor.copy(alpha = 0.3f)
                            )
                        }
                    } else {
                        // Normal mode menu
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menü",
                                    tint = textColor
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (localizationManager.currentLocale == "tr") 
                                                "Seç" else "Select",
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = {
                                        selectionMode = true
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = greenColor
                                        )
                                    }
                                )
                                
                                if (state.notifications.any { !it.isRead }) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (localizationManager.currentLocale == "tr") 
                                                    "Tümünü Okundu İşaretle" else "Mark All Read",
                                                fontSize = 14.sp
                                            )
                                        },
                                        onClick = {
                                            viewModel.markAllAsRead()
                                            showMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = null,
                                                tint = greenColor
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground
                )
            )
        },
        bottomBar = {
            // Bottom bar removed - actions now in TopAppBar
        },
        containerColor = darkBackground
    ) { paddingValues ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Custom Tab Row - iOS Style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Davetler Tab
                TabItem(
                    title = if (localizationManager.currentLocale == "tr") "Davetler" else "Invites",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f),
                    badgeCount = unreadInvitationsCount,
                    greenColor = greenColor
                )
                
                // Bildirimler Tab
                TabItem(
                    title = if (localizationManager.currentLocale == "tr") "Bildirimler" else "Notifications",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f),
                    greenColor = greenColor
                )
            }
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // Davetler Sekmesi
                    when {
                        state.isLoading -> {
                            LoadingView()
                        }
                        invitations.isEmpty() -> {
                            EmptyView(
                                icon = Icons.Default.Mail,
                                title = if (localizationManager.currentLocale == "tr") 
                                    "Henüz davet yok" else "No invitations yet",
                                subtitle = if (localizationManager.currentLocale == "tr") 
                                    "Proje davetleri burada görünecek" else "Project invitations will appear here"
                            )
                        }
                        else -> {
                            NotificationsList(
                                notifications = invitations,
                                viewModel = viewModel,
                                localizationManager = localizationManager,
                                selectionMode = selectionMode,
                                selectedNotifications = selectedNotifications
                            )
                        }
                    }
                }
                1 -> {
                    // Bildirimler Sekmesi
                    when {
                        state.isLoading -> {
                            LoadingView()
                        }
                        otherNotifications.isEmpty() -> {
                            EmptyView(
                                icon = Icons.Default.Notifications,
                                title = if (localizationManager.currentLocale == "tr") 
                                    "Henüz bildirim yok" else "No notifications yet",
                                subtitle = if (localizationManager.currentLocale == "tr") 
                                    "Bildirimler burada görünecek" else "Notifications will appear here"
                            )
                        }
                        else -> {
                            NotificationsList(
                                notifications = otherNotifications,
                                viewModel = viewModel,
                                localizationManager = localizationManager,
                                selectionMode = selectionMode,
                                selectedNotifications = selectedNotifications
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            count = selectedNotifications.size,
            onConfirm = {
                selectedNotifications.forEach { notificationId ->
                    viewModel.deleteNotification(notificationId)
                }
                selectedNotifications.clear()
                selectionMode = false
                showDeleteConfirmDialog = false
            },
            onDismiss = {
                showDeleteConfirmDialog = false
            }
        )
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    greenColor: Color
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) textColor else textColor.copy(alpha = 0.6f)
            )
            
            // Badge
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    if (isSelected) greenColor else Color.Transparent,
                    shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                )
        )
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF4CAF50),
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun EmptyView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = title,
                fontSize = 18.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun NotificationsList(
    notifications: List<Notification>,
    viewModel: NotificationViewModel,
    localizationManager: LocalizationManager,
    selectionMode: Boolean = false,
    selectedNotifications: MutableList<String> = mutableListOf()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = 80.dp // Bottom nav bar + padding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notifications) { notification ->
            val isSelected = selectedNotifications.contains(notification.id)
            
            NotificationCard(
                notification = notification,
                onMarkAsRead = { viewModel.markAsRead(notification.id) },
                onDelete = { viewModel.deleteNotification(notification.id) },
                onAcceptInvitation = { viewModel.acceptProjectInvitation(notification.id) },
                onDeclineInvitation = { viewModel.declineProjectInvitation(notification.id) },
                localizationManager = localizationManager,
                selectionMode = selectionMode,
                isSelected = isSelected,
                onSelectionChange = { selected ->
                    if (selected) {
                        selectedNotifications.add(notification.id)
                    } else {
                        selectedNotifications.remove(notification.id)
                    }
                }
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notification: Notification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onAcceptInvitation: () -> Unit = {},
    onDeclineInvitation: () -> Unit = {},
    localizationManager: LocalizationManager,
    modifier: Modifier = Modifier,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectionChange: (Boolean) -> Unit = {}
) {
    val cardBackground = if (isSelected) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else 
        MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = cardBackground,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection checkbox
                if (selectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = onSelectionChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50),
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(getNotificationColor(notification.type).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getNotificationIcon(notification.type),
                        contentDescription = null,
                        tint = getNotificationColor(notification.type),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = selectionMode || !notification.isRead,
                            onClick = {
                                if (selectionMode) {
                                    onSelectionChange(!isSelected)
                                } else if (!notification.isRead) {
                                    onMarkAsRead()
                                }
                            }
                        ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getLocalizedNotificationTitle(notification, localizationManager),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                    }
                    
                    Text(
                        text = getLocalizedNotificationMessage(notification, localizationManager),
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                    
                    Text(
                        text = formatTimeAgo(notification.createdAt, localizationManager),
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.5f)
                    )
                }
            }
            
            // Action buttons for invitations
            if (notification.type == NotificationActionType.PROJECT_INVITATION) {
                val invitationStatus = notification.invitationStatus
                
                when (invitationStatus) {
                    "accepted" -> {
                        Text(
                            text = if (localizationManager.currentLocale == "tr") 
                                "✓ Kabul Edildi" else "✓ Accepted",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    "declined" -> {
                        Text(
                            text = if (localizationManager.currentLocale == "tr") 
                                "✗ Reddedildi" else "✗ Declined",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF3B30)
                        )
                    }
                    else -> {
                        // Pending - show action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDeclineInvitation,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFFF3B30)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF3B30))
                            ) {
                                Text(
                                    text = if (localizationManager.currentLocale == "tr") "Reddet" else "Decline",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Button(
                                onClick = onAcceptInvitation,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = if (localizationManager.currentLocale == "tr") "Kabul Et" else "Accept",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper functions
private fun getNotificationIcon(type: NotificationActionType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        NotificationActionType.PROJECT_INVITATION -> Icons.Default.PersonAdd
        NotificationActionType.TASK_ASSIGNED -> Icons.Default.Assignment
        NotificationActionType.TASK_COMPLETED -> Icons.Default.CheckCircle
        NotificationActionType.TASK_COMMENT -> Icons.Default.Comment
        NotificationActionType.PROJECT_MEMBER_LEFT -> Icons.Default.ExitToApp
        else -> Icons.Default.Notifications
    }
}

private fun getNotificationColor(type: NotificationActionType): Color {
    return when (type) {
        NotificationActionType.PROJECT_INVITATION -> Color(0xFF4CAF50)
        NotificationActionType.TASK_ASSIGNED -> Color(0xFF2196F3)
        NotificationActionType.TASK_COMPLETED -> Color(0xFF4CAF50)
        NotificationActionType.TASK_COMMENT -> Color(0xFF9C27B0)
        NotificationActionType.PROJECT_MEMBER_LEFT -> Color(0xFFFF9800)
        else -> Color(0xFF607D8B)
    }
}

private fun formatTimeAgo(timestamp: Long, localizationManager: LocalizationManager): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val isTurkish = localizationManager.currentLocale == "tr"
    
    return when {
        diff < 60_000 -> if (isTurkish) "Az önce" else "Just now"
        diff < 3600_000 -> {
            val minutes = (diff / 60_000).toInt()
            if (isTurkish) "$minutes dk önce" else "$minutes min ago"
        }
        diff < 86400_000 -> {
            val hours = (diff / 3600_000).toInt()
            if (isTurkish) "$hours saat önce" else "$hours hour${if (hours > 1) "s" else ""} ago"
        }
        diff < 604800_000 -> {
            val days = (diff / 86400_000).toInt()
            if (isTurkish) "$days gün önce" else "$days day${if (days > 1) "s" else ""} ago"
        }
        else -> {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}

/**
 * Bildirimin yerelleştirilmiş başlığını döndür
 */
private fun getLocalizedNotificationTitle(
    notification: Notification,
    localizationManager: LocalizationManager
): String {
    val isTurkish = localizationManager.currentLocale == "tr"
    
    return when (notification.type) {
        NotificationActionType.PROJECT_INVITATION -> 
            if (isTurkish) "Proje Daveti" else "Project Invitation"
        NotificationActionType.TASK_ASSIGNED -> 
            if (isTurkish) "Görev Atandı" else "Task Assigned"
        NotificationActionType.TASK_COMPLETED -> 
            if (isTurkish) "Görev Tamamlandı" else "Task Completed"
        NotificationActionType.PROJECT_MEMBER_LEFT -> 
            if (isTurkish) "Projeden Ayrılma" else "Member Left Project"
        NotificationActionType.PROJECT_MEMBER_ADDED -> 
            if (isTurkish) "Yeni Takım Üyesi" else "New Team Member"
        NotificationActionType.PROJECT_DELETED -> 
            if (isTurkish) "Proje Silindi" else "Project Deleted"
        NotificationActionType.TASK_DUE_SOON -> 
            if (isTurkish) "Son Tarih Yaklaşıyor" else "Deadline Approaching"
        NotificationActionType.TASK_OVERDUE -> 
            if (isTurkish) "Görev Gecikti" else "Task Overdue"
        NotificationActionType.TASK_COMMENT -> 
            if (isTurkish) "Yeni Yorum" else "New Comment"
        NotificationActionType.SYSTEM_ANNOUNCEMENT -> 
            if (isTurkish) "Sistem Duyurusu" else "System Announcement"
        NotificationActionType.REMINDER -> 
            if (isTurkish) "Hatırlatma" else "Reminder"
        NotificationActionType.GENERAL -> 
            if (isTurkish) "Bildirim" else "Notification"
    }
}

/**
 * Bildirimin yerelleştirilmiş mesajını döndür
 */
private fun getLocalizedNotificationMessage(
    notification: Notification,
    localizationManager: LocalizationManager
): String {
    val isTurkish = localizationManager.currentLocale == "tr"
    val fromUserName = notification.fromUserName ?: (if (isTurkish) "Bilinmeyen Kullanıcı" else "Unknown User")
    val projectName = notification.projectName ?: ""
    val taskTitle = notification.taskTitle ?: ""
    
    return when (notification.type) {
        NotificationActionType.PROJECT_INVITATION -> 
            if (isTurkish) "$fromUserName sizi \"$projectName\" projesine davet etti"
            else "$fromUserName invited you to \"$projectName\" project"
            
        NotificationActionType.TASK_ASSIGNED -> 
            if (isTurkish) "$fromUserName size \"$taskTitle\" görevini atadı"
            else "$fromUserName assigned \"$taskTitle\" task to you"
            
        NotificationActionType.TASK_COMPLETED -> 
            if (isTurkish) "$fromUserName \"$taskTitle\" görevini tamamladı"
            else "$fromUserName completed \"$taskTitle\" task"
            
        NotificationActionType.PROJECT_MEMBER_LEFT -> 
            if (isTurkish) "$fromUserName \"$projectName\" projesinden ayrıldı"
            else "$fromUserName left \"$projectName\" project"
            
        NotificationActionType.PROJECT_MEMBER_ADDED -> 
            if (isTurkish) "$fromUserName \"$projectName\" projesine eklendi"
            else "$fromUserName was added to \"$projectName\" project"
            
        NotificationActionType.PROJECT_DELETED -> 
            if (isTurkish) "\"$projectName\" projesi silindi"
            else "\"$projectName\" project has been deleted"
            
        NotificationActionType.TASK_DUE_SOON -> 
            if (isTurkish) "\"$taskTitle\" görevinin son tarihi yaklaşıyor"
            else "\"$taskTitle\" task deadline is approaching soon"
            
        NotificationActionType.TASK_OVERDUE -> 
            if (isTurkish) "\"$taskTitle\" görevinin son tarihi geçti"
            else "\"$taskTitle\" task is overdue"
            
        NotificationActionType.TASK_COMMENT -> 
            if (isTurkish) "$fromUserName \"$taskTitle\" görevine yorum ekledi"
            else "$fromUserName added a comment to \"$taskTitle\" task"
            
        NotificationActionType.SYSTEM_ANNOUNCEMENT -> 
            if (isTurkish) notification.message
            else notification.message
            
        NotificationActionType.REMINDER -> 
            if (isTurkish) notification.message
            else notification.message
            
        NotificationActionType.GENERAL -> 
            notification.message
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    count: Int
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Bildirimleri Sil",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = "$count bildirim silinecek. Emin misiniz?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Evet", color = Color(0xFFFF3B30))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hayır")
            }
        }
    )
}
