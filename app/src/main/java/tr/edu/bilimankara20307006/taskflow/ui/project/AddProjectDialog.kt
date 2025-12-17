package tr.edu.bilimankara20307006.taskflow.ui.project

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Yeni Proje Ekleme Dialog'u - iOS ile aynı, takım üyeleri kısmı yok
 * Sadece proje başlığı, tanımı ve teslim tarihi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onProjectCreated: (Project, List<String>) -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    
    // Form states
    var projectTitle by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
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
                .background(darkBackground.copy(alpha = dialogAlpha * 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .graphicsLayer {
                        scaleX = dialogScale
                        scaleY = dialogScale
                        alpha = dialogAlpha
                    },
                shape = RoundedCornerShape(20.dp),
                color = cardBackground
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
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
                            text = "Yeni Proje",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        IconButton(
                            onClick = {
                                dialogVisible = false
                                onDismiss()
                            }
                        ) {
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
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Proje Başlığı
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Proje Başlığı *",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                OutlinedTextField(
                                    value = projectTitle,
                                    onValueChange = { projectTitle = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = "Proje adını girin...",
                                            color = textSecondaryColor
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = inputBackground,
                                        unfocusedContainerColor = inputBackground
                                    )
                                )
                            }
                        }
                        
                        // Proje Açıklaması
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Açıklama",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                OutlinedTextField(
                                    value = projectDescription,
                                    onValueChange = { projectDescription = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = "Projenin açıklamasını girin...",
                                            color = textSecondaryColor
                                        )
                                    },
                                    minLines = 3,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = inputBackground,
                                        unfocusedContainerColor = inputBackground
                                    )
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
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedDate?.let { 
                                                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(it)
                                            } ?: "Tarih seçin",
                                            color = if (selectedDate != null) textColor else textSecondaryColor,
                                            fontSize = 16.sp
                                        )
                                        
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = "Takvim",
                                            tint = Color(0xFF4CAF50)
                                        )
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
                                    onProjectCreated(newProject, emptyList())
                                }
                            },
                            enabled = projectTitle.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Oluştur", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
        
        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = Date(it)
                        }
                        showDatePicker = false
                    }) {
                        Text("Tamam")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("İptal")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}