package tr.edu.bilimankara20307006.taskflow.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.model.User
import tr.edu.bilimankara20307006.taskflow.data.model.ProjectRole
import tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager

/**
 * AddTeamMemberScreen - iOS AddTeamMemberView.swift'in Android versiyonu
 * Projeye ekip üyesi ekleme ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeamMemberScreen(
    projectId: String,
    onBackClick: () -> Unit = {},
    onMemberAdded: () -> Unit = {},
    viewModel: ProjectViewModel = viewModel()
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    
    // Projeyi yükle
    var project by remember { mutableStateOf<Project?>(null) }
    
    LaunchedEffect(projectId) {
        val result = viewModel.getProjectById(projectId)
        result.onSuccess { loadedProject ->
            project = loadedProject
        }
    }
    
    if (project == null) {
        // Loading state
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
    val currentProject = project!!
    val currentUserId = FirebaseManager.getCurrentUserId() ?: ""
    val userRole = currentProject.getUserRole(currentUserId)
    val canManageMembers = currentProject.canUserManageMembers(currentUserId)
    
    // Yetkisi yoksa sadece okuma modunda göster
    if (!canManageMembers) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(localizationManager.localizedString("TeamMembers")) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, localizationManager.localizedString("Back"))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                )
            },
            containerColor = Color(0xFF121212)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Bilgilendirme
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Üye ekleme/çıkarma yetkiniz yok",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rolünüz: ${userRole.displayName}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Current Team Section (Sadece görüntüleme)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = localizationManager.localizedString("CurrentTeam") + " (${currentProject.teamMembers.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        currentProject.teamMembers.forEach { member ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (member.uid == currentProject.ownerId) 
                                                Color(0xFFFF9500).copy(alpha = 0.2f)
                                            else 
                                                Color(0xFF66D68C).copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.initials,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (member.uid == currentProject.ownerId) 
                                            Color(0xFFFF9500)
                                        else 
                                            Color(0xFF66D68C)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        text = member.displayName ?: localizationManager.localizedString("User"),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    
                                    Text(
                                        text = currentProject.getUserRole(member.uid).displayName,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFF9500)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        return
    }
    
    var searchEmail by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<User?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val greenAccent = Color(0xFF66D69A) // iOS'taki green accent
    
    // Success dialog
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {
                showSuccess = false
                searchEmail = ""
                searchResult = null
            },
            title = { Text(localizationManager.localizedString("Success")) },
            text = { Text(localizationManager.localizedString("MemberAddedSuccessfully")) },
            confirmButton = {
                TextButton(onClick = {
                    showSuccess = false
                    searchEmail = ""
                    searchResult = null
                    onMemberAdded()
                }) {
                    Text(localizationManager.localizedString("OK"))
                }
            }
        )
    }
    
    // Error dialog
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text(localizationManager.localizedString("Error")) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text(localizationManager.localizedString("OK"))
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizationManager.localizedString("AddTeamMember")) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, localizationManager.localizedString("Back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = greenAccent
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = localizationManager.localizedString("AddTeamMemberToProject"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = localizationManager.localizedString("SearchByEmail"),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // Search Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Search Field
                    OutlinedTextField(
                        value = searchEmail,
                        onValueChange = { searchEmail = it },
                        label = { Text(localizationManager.localizedString("EmailAddress")) },
                        placeholder = { Text("ornek@email.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, localizationManager.localizedString("Email"))
                        },
                        trailingIcon = {
                            if (searchEmail.isNotEmpty()) {
                                IconButton(onClick = { searchEmail = "" }) {
                                    Icon(Icons.Default.Clear, localizationManager.localizedString("Clear"))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = greenAccent,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = greenAccent
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Search Button
                    Button(
                        onClick = {
                            scope.launch {
                                isSearching = true
                                searchResult = null
                                errorMessage = null
                                
                                val cleanEmail = searchEmail.trim().lowercase()
                                if (cleanEmail.isEmpty()) {
                                    isSearching = false
                                    return@launch
                                }
                                
                                println("🔍 Kullanıcı aranıyor: $cleanEmail")
                                
                                val result = viewModel.searchUserByEmail(cleanEmail)
                                isSearching = false
                                
                                result.onSuccess { user ->
                                    if (user != null) {
                                        println("✅ Kullanıcı bulundu: ${user.displayName ?: "İsimsiz"} - ${user.email}")
                                        searchResult = user
                                    } else {
                                        println("⚠️ Kullanıcı bulunamadı: $cleanEmail")
                                        errorMessage = localizationManager.localizedString("UserNotFound")
                                    }
                                }.onFailure { error ->
                                    println("❌ Arama hatası: ${error.message}")
                                    errorMessage = localizationManager.localizedString("SearchError") + ": ${error.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = searchEmail.isNotEmpty() && !isSearching,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greenAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(localizationManager.localizedString("SearchUser"))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Search Result Card
            searchResult?.let { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        greenAccent.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.initials,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = greenAccent
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = user.displayName ?: localizationManager.localizedString("User"),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                
                                Text(
                                    text = user.email ?: "",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Add Button
                        Button(
                            onClick = {
                                scope.launch {
                                    println("🎯 Eklenecek kullanıcı: ${user.displayName ?: "N/A"} - UID: ${user.uid}")
                                    
                                    val result = viewModel.addTeamMember(user.uid, currentProject.id)
                                    
                                    result.onSuccess {
                                        showSuccess = true
                                    }.onFailure { error ->
                                        println("❌ Ekleme hatası: ${error.message}")
                                        errorMessage = error.message
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = greenAccent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Send, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(localizationManager.localizedString("AddToProject"))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Current Team Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = localizationManager.localizedString("CurrentTeam") + " (${currentProject.teamMembers.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (currentProject.teamMembers.isEmpty()) {
                        Text(
                            text = localizationManager.localizedString("NoTeamMembersYet"),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        currentProject.teamMembers.forEach { member ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (member.uid == currentProject.ownerId) 
                                                Color(0xFFFF9500).copy(alpha = 0.2f)
                                            else 
                                                Color(0xFF66D68C).copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.initials,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (member.uid == currentProject.ownerId) 
                                            Color(0xFFFF9500)
                                        else 
                                            Color(0xFF66D68C)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        text = member.displayName ?: localizationManager.localizedString("User"),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    
                                    if (member.uid == currentProject.ownerId) {
                                        Text(
                                            text = localizationManager.localizedString("ProjectLeader"),
                                            fontSize = 12.sp,
                                            color = Color(0xFFFF9500)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
