package tr.edu.bilimankara20307006.taskflow.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tr.edu.bilimankara20307006.taskflow.ui.auth.AuthViewModel

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    
    // Sample tasks for demo
    var tasks by remember {
        mutableStateOf(
            listOf(
                Task("1", "Projeyi tamamla", "Android Raptiye uygulamasını bitir"),
                Task("2", "Sunumu hazırla", "Proje sunumunu PowerPoint'te hazırla"),
                Task("3", "Kahve al", "Favori kahve dükkanından latte al", true)
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Merhaba, ${authState.user?.displayName ?: "Kullanıcı"}!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Bugün ${tasks.count { !it.isCompleted }} göreviniz var",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.signOut()
                            onNavigateToLogin()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Çıkış Yap",
                            tint = Color(0xFF1E88E5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1E88E5)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Add new task
                    val newTask = Task(
                        id = (tasks.size + 1).toString(),
                        title = "Yeni Görev ${tasks.size + 1}",
                        description = "Bu yeni bir görevdir"
                    )
                    tasks = tasks + newTask
                },
                containerColor = Color(0xFF1E88E5),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Görev Ekle")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "📋 Görevleriniz",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items(tasks) { task ->
                TaskCard(
                    task = task,
                    onToggleComplete = { taskId ->
                        tasks = tasks.map { 
                            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) 
                            else it 
                        }
                    }
                )
            }
            
            if (tasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎉",
                                    fontSize = 48.sp
                                )
                                Text(
                                    text = "Hiç göreviniz yok!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Yeni görev eklemek için + butonuna tıklayın",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onToggleComplete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFFE8F5E8) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete(task.id) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF66D68C),
                    uncheckedColor = Color(0xFF1E88E5)
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) Color.Gray else Color.Black
                )
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Text(
                text = if (task.isCompleted) "✅" else "⏳",
                fontSize = 20.sp
            )
        }
    }
}