package tr.edu.bilimankara20307006.taskflow.ui.project

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager

/**
 * Proje Listeleme Ekranı - iOS ProjectListView ile birebir uyumlu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    onNavigateToBoard: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    selectedProject: Project? = null,
    onProjectSelected: (Project?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    val currentLocale = localizationManager.currentLocale // Force recomposition on locale change
    val coroutineScope = rememberCoroutineScope()
    
    // ViewModel
    val viewModel: ProjectListViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    
    // Show add project dialog
    var showAddProjectDialog by remember { mutableStateOf(false) }
    
    // İlk yükleme kontrolü - sadece ilk açılışta animasyon olsun
    var isInitialLoad by remember { mutableStateOf(true) }
    
    // Loading overlay state - 300ms delay ile yumuşak geçiş
    var showLoadingOverlay by remember { mutableStateOf(false) }
    
    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            delay(300) // 300ms bekle, hızlı yüklemelerde loading gösterme
            showLoadingOverlay = true
        } else {
            showLoadingOverlay = false
        }
    }
    
    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var searchVisible by remember { mutableStateOf(false) }
    var filtersVisible by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    
    // Trigger animations on first composition
    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
        delay(150)
        searchVisible = true
        delay(150)
        filtersVisible = true
        delay(150)
        titleVisible = true
        delay(550) // Header animasyonları bitince
        isInitialLoad = false // Artık scroll sırasında animasyon olmasın
    }
    
    // Animated values
    val headerAlpha by animateFloatAsState(
        targetValue = if (headerVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )
    
    val searchAlpha by animateFloatAsState(
        targetValue = if (searchVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "searchAlpha"
    )
    
    val searchScale by animateFloatAsState(
        targetValue = if (searchVisible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "searchScale"
    )
    
    val filtersAlpha by animateFloatAsState(
        targetValue = if (filtersVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "filtersAlpha"
    )
    
    val titleAlpha by animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "titleAlpha"
    )
    
    var searchText by remember { mutableStateOf("") }
    var selectedSortOption by remember { mutableStateOf(localizationManager.localizedString("SortOptionDate")) }
    var selectedFilterOption by remember { mutableStateOf(localizationManager.localizedString("FilterOptionAll")) }
    
    // Backend'den gelen projeler - state'ten al
    val projects = state.projects
    
    // Hata mesajı göster
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            // TODO: Snackbar veya Toast ile hata göster
            println("Error: $error")
        }
    }
    
    // Button press animations
    var analyticsButtonPressed by remember { mutableStateOf(false) }
    var boardButtonPressed by remember { mutableStateOf(false) }
    var addButtonPressed by remember { mutableStateOf(false) }
    
    // Track newly added project for animation
    var newlyAddedProjectId by remember { mutableStateOf<String?>(null) }
    
    val analyticsButtonScale by animateFloatAsState(
        targetValue = if (analyticsButtonPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "analyticsButtonScale"
    )
    
    val boardButtonScale by animateFloatAsState(
        targetValue = if (boardButtonPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "boardButtonScale"
    )
    
    val addButtonScale by animateFloatAsState(
        targetValue = if (addButtonPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "addButtonScale"
    )
    
    // Pull to refresh state
    val pullToRefreshState = rememberPullToRefreshState()
    
    // Handle pull to refresh
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            println("🔄 Pull to refresh tetiklendi, projeler yenileniyor...")
            viewModel.refreshProjects()
        }
    }
    
    // Pull to refresh'i refreshing durumu ile senkronize et
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing && pullToRefreshState.isRefreshing) {
            delay(300) // Kısa bir animasyon için bekleme
            pullToRefreshState.endRefresh()
        }
    }
    
    val sortOptions = listOf(
        localizationManager.localizedString("SortOptionDate"),
        localizationManager.localizedString("SortOptionName"),
        localizationManager.localizedString("SortOptionProgress")
    )
    val filterOptions = listOf(
        localizationManager.localizedString("FilterOptionAll"),
        localizationManager.localizedString("FilterOptionActive"),
        localizationManager.localizedString("FilterOptionCompleted")
    )
    
    // Filtreleme ve sıralama
    val filteredProjects = remember(searchText, selectedSortOption, selectedFilterOption, projects, localizationManager.currentLocale) {
        var filtered = projects
        
        // Arama filtresi
        if (searchText.isNotEmpty()) {
            filtered = filtered.filter { project ->
                project.title.contains(searchText, ignoreCase = true) ||
                project.description.contains(searchText, ignoreCase = true)
            }
        }
        
        // Durum filtresi
        filtered = when (selectedFilterOption) {
            localizationManager.localizedString("FilterOptionActive") -> filtered.filter { !it.isCompleted }
            localizationManager.localizedString("FilterOptionCompleted") -> filtered.filter { it.isCompleted }
            else -> filtered
        }
        
        // Sıralama
        when (selectedSortOption) {
            localizationManager.localizedString("SortOptionName") -> filtered.sortedBy { it.title }
            localizationManager.localizedString("SortOptionProgress") -> filtered.sortedByDescending { it.progressPercentage }
            else -> filtered.sortedByDescending { it.createdDate }
        }
    }
    
    // Tema renklerini MaterialTheme'den al
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val searchBackground = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onBackground
    
    // Snackbar host for error messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Hata mesajını Snackbar ile göster
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    // AddTeamMember ekranı için state
    var showAddMember by remember { mutableStateOf(false) }
    var addMemberProjectId by remember { mutableStateOf<String?>(null) }
    
    // AddTeamMember ekranı gösteriliyorsa
    if (showAddMember && addMemberProjectId != null && selectedProject != null) {
        AddTeamMemberScreen(
            projectId = addMemberProjectId!!,
            onBackClick = {
                showAddMember = false
                addMemberProjectId = null
            },
            onMemberAdded = {
                showAddMember = false
                addMemberProjectId = null
                // Ekranı kapat, ana ekrana dönünce otomatik yenilenecek
                onProjectSelected(null)
            }
        )
        return
    }
    
    // Show project detail screen if a project is selected
    if (selectedProject != null) {
        ProjectDetailScreen(
            projectId = selectedProject!!.id,
            onBackClick = {
                onProjectSelected(null)
            },
            onAddMemberClick = { projectId ->
                addMemberProjectId = projectId
                showAddMember = true
            }
        )
        return
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Tüm içeriği tek bir LazyColumn içinde göster
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header with title and buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .alpha(headerAlpha),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Text(
                    text = localizationManager.localizedString("Projects"),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Analytics butonu - Sadece proje varsa aktif
                    val hasProjects = projects.isNotEmpty()
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .scale(if (hasProjects) analyticsButtonScale else 1f)
                            .background(
                                if (hasProjects) Color(0xFFFF9F0A) else Color(0xFFFF9F0A).copy(alpha = 0.3f),
                                CircleShape
                            )
                            .then(
                                if (hasProjects) {
                                    Modifier
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onPress = {
                                                    analyticsButtonPressed = true
                                                    tryAwaitRelease()
                                                    analyticsButtonPressed = false
                                                }
                                            )
                                        }
                                        .clickable {
                                            coroutineScope.launch {
                                                delay(100)
                                                // İlk projeyi seç ve analytics'e git
                                                if (projects.isNotEmpty()) {
                                                    onProjectSelected(projects.first())
                                                    delay(100)
                                                    onNavigateToAnalytics()
                                                }
                                            }
                                        }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = localizationManager.localizedString("Analytics"),
                            tint = if (hasProjects) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Kanban Panosu butonu - Sadece proje varsa aktif
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .scale(if (hasProjects) boardButtonScale else 1f)
                            .background(
                                if (hasProjects) Color(0xFF32D74B) else Color(0xFF32D74B).copy(alpha = 0.3f),
                                CircleShape
                            )
                            .then(
                                if (hasProjects) {
                                    Modifier
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onPress = {
                                                    boardButtonPressed = true
                                                    tryAwaitRelease()
                                                    boardButtonPressed = false
                                                }
                                            )
                                        }
                                        .clickable {
                                            coroutineScope.launch {
                                                delay(100)
                                                // Proje Panosu ekranına git (Dashboard)
                                                onNavigateToBoard()
                                            }
                                        }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ViewKanban,
                            contentDescription = localizationManager.localizedString("KanbanBoard"),
                            tint = if (hasProjects) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Yeni Proje Ekleme butonu
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .scale(addButtonScale)
                            .background(Color(0xFF007AFF), CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        addButtonPressed = true
                                        tryAwaitRelease()
                                        addButtonPressed = false
                                    }
                                )
                            }
                            .clickable {
                                coroutineScope.launch {
                                    delay(100)
                                    showAddProjectDialog = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = localizationManager.localizedString("NewProject"),
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                }
            }
            
            // Search bar
            item {
                OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp)
                    .scale(searchScale)
                    .alpha(searchAlpha),
                placeholder = {
                    Text(
                        text = localizationManager.localizedString("SearchProjects"),
                        color = textColor.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = localizationManager.localizedString("Search"),
                        tint = textColor.copy(alpha = 0.5f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = searchBackground,
                    unfocusedContainerColor = searchBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = textColor
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            }
            
            // Sort and Filter buttons
            item {
                Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .alpha(filtersAlpha),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sort dropdown
                var sortExpanded by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { sortExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = searchBackground,
                            contentColor = textColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(localizationManager.localizedString("Sort"), fontSize = 14.sp)
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        sortOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = option,
                                            color = if (selectedSortOption == option) Color(0xFF4CAF50) else textColor
                                        )
                                        if (selectedSortOption == option) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color(0xFF4CAF50)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedSortOption = option
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Filter dropdown
                var filterExpanded by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { filterExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = searchBackground,
                            contentColor = textColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(localizationManager.localizedString("Filter"), fontSize = 14.sp)
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false }
                    ) {
                        filterOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = option,
                                            color = if (selectedFilterOption == option) Color(0xFF4CAF50) else textColor
                                        )
                                        if (selectedFilterOption == option) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color(0xFF4CAF50)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedFilterOption = option
                                    filterExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            }
            
            // Projects section header
            item {
                Text(
                text = localizationManager.localizedString("MyProjects"),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .alpha(titleAlpha)
            )
            }
            
            // Loading indicator
            if (state.isLoading && projects.isEmpty()) {
                item {
                    Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                }
            } else if (filteredProjects.isEmpty() && !state.isLoading) {
                // iOS-style Empty State
                item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .padding(top = 50.dp, bottom = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Folder icon with plus
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(80.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 10.dp, y = 10.dp)
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = if (localizationManager.currentLocale == "tr") 
                            "Henüz Proje Yok" 
                        else 
                            "No Projects Yet",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = if (localizationManager.currentLocale == "tr")
                            "Yeni bir proje oluşturmak için +\nbutonuna tıklayın"
                        else
                            "Tap the + button to create\na new project",
                        fontSize = 16.sp,
                        color = textColor.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                    
                    // Create Project Button
                    Button(
                        onClick = { showAddProjectDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007AFF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (localizationManager.currentLocale == "tr")
                                "Yeni Proje Oluştur"
                            else
                                "Create New Project",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                }
            }
            
            // Projects list
            if (filteredProjects.isNotEmpty()) {
                itemsIndexed(filteredProjects) { index, project ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        AnimatedProjectCard(
                            project = project,
                            index = index,
                            isNewlyAdded = project.id == newlyAddedProjectId,
                            isInitialLoad = isInitialLoad,
                            onClick = { 
                                println("🔥 Proje tıklandı: ${project.title}")
                                onProjectSelected(project) 
                            }
                        )
                    }
                }
            }
        }
        
        // Add Project Dialog
        if (showAddProjectDialog) {
            AddProjectDialog(
                onDismiss = { showAddProjectDialog = false },
                onProjectCreated = { newProject, memberIds ->
                    coroutineScope.launch {
                        // Backend'e yeni proje ekle
                        viewModel.createProject(
                            title = newProject.title,
                            description = newProject.description,
                            iconName = newProject.iconName,
                            iconColor = newProject.iconColor,
                            dueDate = newProject.dueDate?.toString(),
                            teamMemberIds = memberIds
                        )
                        
                        newlyAddedProjectId = newProject.id
                        showAddProjectDialog = false
                        
                        // Reset animation flag after animation completes
                        delay(600)
                        newlyAddedProjectId = null
                    }
                }
            )
        }
        
        // Snackbar for error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
        
        // Pull to refresh indicator - sadece yenileme sırasında görünsün
        if (pullToRefreshState.isRefreshing) {
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        
        // Loading overlay - dark mode uyumlu, 300ms delay ile
        AnimatedVisibility(
            visible = showLoadingOverlay,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color(0xFF66D68C),
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = localizationManager.localizedString("Loading"),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Proje Kartı - iOS ProjectCardView ile aynı
 */
@Composable
fun ProjectCardView(
    project: Project,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardBackground = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // Icon color mapping
    val iconColor = when (project.iconColor) {
        "mint" -> Color(0xFF00C7BE)
        "orange" -> Color(0xFFFF9500)
        "blue" -> Color(0xFF4CAF50) // Converted to green
        "green" -> Color(0xFF4CAF50)
        "purple" -> Color(0xFFAF52DE)
        "red" -> Color(0xFFFF3B30)
        else -> Color(0xFF4CAF50)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Project icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (project.iconName) {
                        "phone_android" -> Icons.Default.PhoneAndroid
                        "shopping_cart" -> Icons.Default.ShoppingCart
                        "forum" -> Icons.Default.Forum
                        else -> Icons.Default.Folder
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Project info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = project.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = project.description,
                    fontSize = 14.sp,
                    color = textSecondaryColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Progress bar ve görev bilgisi
                if (project.tasksCount > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Progress bar - yeşil #66D68C
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF2E2E2E))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(project.progressPercentage.toFloat())
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF66D68C))
                            )
                        }
                        
                        // "X / Y görev tamamlandı" bilgisi
                        Text(
                            text = "${project.completedTasksCount} / ${project.tasksCount} görev tamamlandı",
                            fontSize = 12.sp,
                            color = Color(0xFF66D68C),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Görev yoksa bilgi göster
                    Text(
                        text = "Henüz görev eklenmedi",
                        fontSize = 12.sp,
                        color = textSecondaryColor.copy(alpha = 0.6f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Chevron
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = textSecondaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Animated wrapper for project cards with staggered entrance
 */
@Composable
fun AnimatedProjectCard(
    project: Project,
    index: Int,
    isNewlyAdded: Boolean = false,
    isInitialLoad: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // İlk yüklemede false başla, diğer durumlarda true (animasyon olmasın)
    var isVisible by remember(project.id) { mutableStateOf(!isInitialLoad) }
    
    // Trigger animation with staggered delay based on index or immediate for new projects
    LaunchedEffect(project.id, isInitialLoad) {
        if (isNewlyAdded) {
            // New project: immediate animation with special effect
            delay(50)
            isVisible = true
        } else if (isInitialLoad) {
            // İlk yüklemede staggered animation
            delay(550L + (index * 80L))
            isVisible = true
        } else {
            // Scroll sırasında animasyon yok, direkt görünür
            isVisible = true
        }
    }
    
    // Animated values with enhanced animation for newly added projects
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (isNewlyAdded) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        } else {
            tween(durationMillis = 400, easing = FastOutSlowInEasing)
        },
        label = "cardAlpha"
    )
    
    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else if (isNewlyAdded) 30.dp else 20.dp,
        animationSpec = if (isNewlyAdded) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        } else {
            tween(durationMillis = 400, easing = FastOutSlowInEasing)
        },
        label = "cardOffsetY"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else if (isNewlyAdded) 0.8f else 0.95f,
        animationSpec = if (isNewlyAdded) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        } else {
            tween(durationMillis = 400, easing = FastOutSlowInEasing)
        },
        label = "cardScale"
    )
    
    Box(
        modifier = modifier
            .offset(y = offsetY)
            .scale(scale)
            .alpha(alpha)
    ) {
        ProjectCardView(
            project = project,
            onClick = onClick
        )
    }
}
