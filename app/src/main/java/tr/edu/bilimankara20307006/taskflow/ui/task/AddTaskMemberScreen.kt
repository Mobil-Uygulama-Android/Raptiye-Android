package tr.edu.bilimankara20307006.taskflow.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tr.edu.bilimankara20307006.taskflow.data.manager.InvitationManager
import tr.edu.bilimankara20307006.taskflow.data.model.User
import tr.edu.bilimankara20307006.taskflow.data.model.Invitation
import tr.edu.bilimankara20307006.taskflow.data.model.InvitationStatus
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager

/**
 * Göreve Üye Ekle Ekranı - iOS AddMemberView ile aynı
 * 
 * Email ile kullanıcı arama ve davet gönderme
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskMemberScreen(
    projectId: String,
    projectName: String,
    existingMembers: List<User>,
    onMemberSelected: (User) -> Unit,
    onDismiss: () -> Unit,
    viewModel: AddTaskMemberViewModel = viewModel()
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    
    val searchEmail by viewModel.searchEmail.collectAsState()
    val searchResult by viewModel.searchResult.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val pendingInvitations by viewModel.pendingInvitations.collectAsState()
    val invitedUsers by viewModel.invitedUsers.collectAsState()
    
    LaunchedEffect(projectId) {
        viewModel.loadPendingInvitations(projectId)
    }
    
    val greenColor = Color(0xFF66D68C)
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBackground = Color(0xFF1E2228)
    val inputBackground = Color(0xFF262C35)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Projeye Ekip Üyesi Ekle",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Kapat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Icon
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(greenColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = greenColor
                        )
                    }
                }
            }
            
            // Title
            item {
                Text(
                    text = "Projeye Ekip Üyesi Ekle",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Subtitle
            item {
                Text(
                    text = "Email adresi ile kullanıcı arayın",
                    fontSize = 14.sp,
                    color = textSecondaryColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Email input
            item {
                OutlinedTextField(
                    value = searchEmail,
                    onValueChange = { viewModel.updateSearchEmail(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("utkusevikkk@gmail.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchEmail.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchEmail("") }) {
                                Icon(Icons.Default.Close, "Temizle")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = textSecondaryColor.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )
            }
            
            // Search button
            item {
                Button(
                    onClick = { viewModel.searchUser() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = greenColor
                    ),
                    enabled = searchEmail.isNotEmpty() && !isSearching
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kullanıcı Ara",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // Search result
            searchResult?.let { result ->
                when (result) {
                    is SearchResult.Found -> {
                        item {
                            UserCard(
                                user = result.user,
                                isInvited = pendingInvitations.any { it.receiverId == result.user.uid },
                                isMember = existingMembers.any { it.uid == result.user.uid },
                                onInvite = { 
                                    viewModel.sendInvitation(projectId, projectName, result.user)
                                },
                                onSelect = {
                                    onMemberSelected(result.user)
                                    onDismiss()
                                },
                                greenColor = greenColor,
                                textColor = textColor,
                                textSecondaryColor = textSecondaryColor,
                                cardBackground = cardBackground
                            )
                        }
                    }
                    is SearchResult.NotFound -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFF9500).copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9500)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Kullanıcı bulunamadı",
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                    is SearchResult.Error -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = result.message,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Invited users waiting for response
            if (invitedUsers.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    Text(
                        text = "Davet Gönderilen Kullanıcılar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                
                items(invitedUsers) { user ->
                    InvitedUserCard(
                        user = user,
                        textColor = textColor,
                        textSecondaryColor = textSecondaryColor,
                        cardBackground = cardBackground,
                        onDelete = {
                            // TODO: Delete invitation if needed
                        }
                    )
                }
            }
            
            // Current team members
            if (existingMembers.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    Text(
                        text = "Mevcut Ekip Üyeleri",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                
                items(existingMembers) { member ->
                    ExistingMemberCard(
                        user = member,
                        greenColor = greenColor,
                        textColor = textColor,
                        textSecondaryColor = textSecondaryColor,
                        cardBackground = cardBackground
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    isInvited: Boolean,
    isMember: Boolean,
    onInvite: () -> Unit,
    onSelect: () -> Unit,
    greenColor: Color,
    textColor: Color,
    textSecondaryColor: Color,
    cardBackground: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        shape = RoundedCornerShape(12.dp)
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
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(greenColor.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.initials,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = greenColor
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = user.displayName ?: "Unknown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = user.email ?: "",
                        fontSize = 14.sp,
                        color = textSecondaryColor
                    )
                }
            }
            
            // Action button
            when {
                isMember -> {
                    // Already a member - can select
                    Button(
                        onClick = onSelect,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greenColor
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seç", fontWeight = FontWeight.SemiBold)
                    }
                }
                isInvited -> {
                    // Invitation sent
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF9500)
                        )
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Davet Gönderildi", fontWeight = FontWeight.SemiBold)
                    }
                }
                else -> {
                    // Not a member - invite
                    OutlinedButton(
                        onClick = onInvite,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = greenColor
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Davet Gönder", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvitedUserCard(
    user: User,
    textColor: Color,
    textSecondaryColor: Color,
    cardBackground: Color,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        shape = RoundedCornerShape(12.dp)
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
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B7355).copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.initials,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B7355)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = user.displayName ?: "Unknown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = user.email ?: "",
                        fontSize = 14.sp,
                        color = textSecondaryColor
                    )
                }
                
                // Delete button (like iOS)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC3545).copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = Color(0xFFDC3545)
                    )
                }
            }
            
            // Waiting status
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B7355),
                    disabledContainerColor = Color(0xFF8B7355)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Davet Gönderildi",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ExistingMemberCard(
    user: User,
    greenColor: Color,
    textColor: Color,
    textSecondaryColor: Color,
    cardBackground: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
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
                    fontWeight = FontWeight.Bold,
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
                    fontSize = 14.sp,
                    color = textSecondaryColor
                )
            }
        }
    }
}

// ViewModel
class AddTaskMemberViewModel : ViewModel() {
    
    private val db = FirebaseFirestore.getInstance()
    
    private val _searchEmail = MutableStateFlow("")
    val searchEmail = _searchEmail.asStateFlow()
    
    private val _searchResult = MutableStateFlow<SearchResult?>(null)
    val searchResult = _searchResult.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()
    
    private val _pendingInvitations = MutableStateFlow<List<Invitation>>(emptyList())
    val pendingInvitations = _pendingInvitations.asStateFlow()
    
    // Computed property for invited users
    val invitedUsers: kotlinx.coroutines.flow.StateFlow<List<User>> = _pendingInvitations.map { invitations ->
        invitations.mapNotNull { invitation ->
            // Convert invitation to User for display
            User(
                uid = invitation.receiverId,
                email = invitation.receiverEmail,
                displayName = invitation.receiverEmail?.substringBefore("@")
            )
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())
    
    fun loadPendingInvitations(projectId: String) {
        viewModelScope.launch {
            try {
                // Listen to pending invitations for this project
                db.collection("invitations")
                    .whereEqualTo("projectId", projectId)
                    .whereEqualTo("status", "pending")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            println("❌ Error loading invitations: ${error.message}")
                            return@addSnapshotListener
                        }
                        
                        val invitations = snapshot?.documents?.mapNotNull { doc ->
                            try {
                                Invitation.fromMap(doc.data ?: return@mapNotNull null)
                            } catch (e: Exception) {
                                println("⚠️ Parse error: ${e.message}")
                                null
                            }
                        } ?: emptyList()
                        
                        _pendingInvitations.value = invitations
                        println("📬 Loaded ${invitations.size} pending invitations for project: $projectId")
                    }
            } catch (e: Exception) {
                println("❌ Error setting up invitation listener: ${e.message}")
            }
        }
    }
    
    fun updateSearchEmail(email: String) {
        _searchEmail.value = email
        _searchResult.value = null
    }
    
    fun searchUser() {
        viewModelScope.launch {
            _isSearching.value = true
            _searchResult.value = null
            
            try {
                val email = _searchEmail.value.trim()
                
                val querySnapshot = db.collection("users")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .await()
                
                if (querySnapshot.isEmpty) {
                    _searchResult.value = SearchResult.NotFound
                } else {
                    val doc = querySnapshot.documents[0]
                    val user = User(
                        uid = doc.id,
                        email = doc.getString("email") ?: "",
                        displayName = doc.getString("fullName") ?: doc.getString("displayName"),
                        photoUrl = doc.getString("photoUrl")
                    )
                    _searchResult.value = SearchResult.Found(user)
                }
            } catch (e: Exception) {
                _searchResult.value = SearchResult.Error(e.message ?: "Hata oluştu")
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    fun sendInvitation(projectId: String, projectName: String, user: User) {
        viewModelScope.launch {
            try {
                println("📨 Sending invitation to: ${user.email}")
                
                InvitationManager.createInvitation(
                    receiverEmail = user.email ?: "",
                    projectId = projectId,
                    projectName = projectName
                ).onSuccess {
                    println("✅ Invitation sent successfully")
                    // Refresh will happen automatically via listener
                }.onFailure { e ->
                    println("❌ Failed to send invitation: ${e.message}")
                }
            } catch (e: Exception) {
                println("❌ Error sending invitation: ${e.message}")
            }
        }
    }
}

sealed class SearchResult {
    data class Found(val user: User) : SearchResult()
    data object NotFound : SearchResult()
    data class Error(val message: String) : SearchResult()
}
