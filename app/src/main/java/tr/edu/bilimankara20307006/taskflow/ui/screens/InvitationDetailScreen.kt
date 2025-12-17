package tr.edu.bilimankara20307006.taskflow.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.manager.InvitationManager
import tr.edu.bilimankara20307006.taskflow.data.model.Invitation
import tr.edu.bilimankara20307006.taskflow.data.model.InvitationStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * Invitation Detail Screen - In-app Accept/Reject UI
 * 
 * This is where users actually accept or reject invitations.
 * Push notifications just deeplink here.
 * 
 * Design: iOS and Android both use this screen for invitation actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationDetailScreen(
    invitationId: String,
    navController: NavController,
    viewModel: InvitationDetailViewModel = viewModel()
) {
    val invitation by viewModel.invitation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val actionResult by viewModel.actionResult.collectAsState()
    
    LaunchedEffect(invitationId) {
        viewModel.loadInvitation(invitationId)
    }
    
    // Navigate back after successful action
    LaunchedEffect(actionResult) {
        if (actionResult is ActionResult.Success) {
            kotlinx.coroutines.delay(1500) // Show success message
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Invitation") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                error != null -> {
                    ErrorView(
                        message = error!!,
                        onRetry = { viewModel.loadInvitation(invitationId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                invitation != null -> {
                    InvitationContent(
                        invitation = invitation!!,
                        actionResult = actionResult,
                        onAccept = { viewModel.acceptInvitation() },
                        onReject = { viewModel.rejectInvitation() }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvitationContent(
    invitation: Invitation,
    actionResult: ActionResult?,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Project icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = invitation.projectName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Invitation message
        Text(
            text = "${invitation.senderName} has invited you to join this project",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        Text(
            text = dateFormat.format(Date(invitation.createdAt)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Status-based UI
        when (invitation.status) {
            InvitationStatus.PENDING -> {
                if (actionResult is ActionResult.Loading) {
                    LoadingActions()
                } else if (actionResult is ActionResult.Success) {
                    SuccessMessage(actionResult.message)
                } else if (actionResult is ActionResult.Error) {
                    ErrorMessage(actionResult.message)
                    Spacer(modifier = Modifier.height(16.dp))
                    ActionButtons(onAccept, onReject)
                } else {
                    ActionButtons(onAccept, onReject)
                }
            }
            
            InvitationStatus.ACCEPTED -> {
                StatusCard(
                    text = "You accepted this invitation",
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }
            
            InvitationStatus.REJECTED -> {
                StatusCard(
                    text = "You declined this invitation",
                    color = MaterialTheme.colorScheme.errorContainer
                )
            }
            
            InvitationStatus.EXPIRED -> {
                StatusCard(
                    text = "This invitation has expired",
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "About Project Invitations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "By accepting this invitation, you will become a member of the project and will be able to view tasks, contribute to discussions, and collaborate with team members.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Accept button
        Button(
            onClick = onAccept,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Accept Invitation", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Reject button
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Decline", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun LoadingActions() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Processing...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SuccessMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
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
                Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun StatusCard(text: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

// ViewModel
class InvitationDetailViewModel : ViewModel() {
    
    private val _invitation = MutableStateFlow<Invitation?>(null)
    val invitation = _invitation.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult = _actionResult.asStateFlow()
    
    fun loadInvitation(invitationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            InvitationManager.getInvitation(invitationId)
                .onSuccess { invitation ->
                    _invitation.value = invitation
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to load invitation"
                    _isLoading.value = false
                }
        }
    }
    
    fun acceptInvitation() {
        val inv = _invitation.value ?: return
        
        viewModelScope.launch {
            _actionResult.value = ActionResult.Loading
            
            InvitationManager.acceptInvitation(inv.id)
                .onSuccess {
                    _actionResult.value = ActionResult.Success("Invitation accepted! Redirecting...")
                    // Update local state
                    _invitation.value = inv.copy(status = InvitationStatus.ACCEPTED)
                }
                .onFailure { e ->
                    _actionResult.value = ActionResult.Error(e.message ?: "Failed to accept invitation")
                }
        }
    }
    
    fun rejectInvitation() {
        val inv = _invitation.value ?: return
        
        viewModelScope.launch {
            _actionResult.value = ActionResult.Loading
            
            InvitationManager.rejectInvitation(inv.id)
                .onSuccess {
                    _actionResult.value = ActionResult.Success("Invitation declined")
                    // Update local state
                    _invitation.value = inv.copy(status = InvitationStatus.REJECTED)
                }
                .onFailure { e ->
                    _actionResult.value = ActionResult.Error(e.message ?: "Failed to decline invitation")
                }
        }
    }
}

sealed class ActionResult {
    data object Loading : ActionResult()
    data class Success(val message: String) : ActionResult()
    data class Error(val message: String) : ActionResult()
}
