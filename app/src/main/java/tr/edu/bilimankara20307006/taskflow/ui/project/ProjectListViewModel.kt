package tr.edu.bilimankara20307006.taskflow.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.network.model.ProjectResponse
import tr.edu.bilimankara20307006.taskflow.data.repository.NetworkResult
import tr.edu.bilimankara20307006.taskflow.data.repository.ProjectRepository
import tr.edu.bilimankara20307006.taskflow.data.storage.TokenManager

/**
 * ProjectListViewModel - Proje listesi için state yönetimi
 * 
 * Backend'den projeleri yükler, oluşturur, günceller ve siler.
 */
data class ProjectListState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

class ProjectListViewModel : ViewModel() {
    
    private val projectRepository = ProjectRepository.getInstance { TokenManager.getToken() }
    
    private val _state = MutableStateFlow(ProjectListState())
    val state: StateFlow<ProjectListState> = _state.asStateFlow()
    
    init {
        // ViewModel oluşturulduğunda projeleri yükle
        loadProjects()
    }
    
    /**
     * Backend'den tüm projeleri yükler.
     */
    fun loadProjects() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = projectRepository.getProjects()) {
                is NetworkResult.Success -> {
                    val projects = result.data.projects.map { it.toProject() }
                    _state.value = _state.value.copy(
                        projects = projects,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is NetworkResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled
                }
            }
        }
    }
    
    /**
     * Projeleri yenile (pull-to-refresh).
     */
    fun refreshProjects() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, errorMessage = null)
            
            when (val result = projectRepository.getProjects()) {
                is NetworkResult.Success -> {
                    val projects = result.data.projects.map { it.toProject() }
                    _state.value = _state.value.copy(
                        projects = projects,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
                is NetworkResult.Error -> {
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled
                }
            }
        }
    }
    
    /**
     * Yeni proje oluşturur.
     */
    fun createProject(
        title: String,
        description: String,
        iconName: String = "folder",
        iconColor: String = "blue",
        status: String = "Yapılacaklar",
        dueDate: String? = null
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = projectRepository.createProject(
                title = title,
                description = description,
                iconName = iconName,
                iconColor = iconColor,
                status = status,
                dueDate = dueDate
            )) {
                is NetworkResult.Success -> {
                    // Yeni proje eklendi, listeyi yenile
                    loadProjects()
                }
                is NetworkResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled
                }
            }
        }
    }
    
    /**
     * Mevcut projeyi günceller.
     */
    fun updateProject(
        projectId: String,
        title: String? = null,
        description: String? = null,
        iconName: String? = null,
        iconColor: String? = null,
        status: String? = null,
        dueDate: String? = null
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = projectRepository.updateProject(
                projectId = projectId,
                title = title,
                description = description,
                iconName = iconName,
                iconColor = iconColor,
                status = status,
                dueDate = dueDate
            )) {
                is NetworkResult.Success -> {
                    // Proje güncellendi, listeyi yenile
                    loadProjects()
                }
                is NetworkResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled
                }
            }
        }
    }
    
    /**
     * Projeyi siler.
     */
    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = projectRepository.deleteProject(projectId)) {
                is NetworkResult.Success -> {
                    // Proje silindi, local listeden de kaldır
                    _state.value = _state.value.copy(
                        projects = _state.value.projects.filter { it.id != projectId },
                        isLoading = false
                    )
                }
                is NetworkResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled
                }
            }
        }
    }
    
    /**
     * Hata mesajını temizler.
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
    
    /**
     * ProjectResponse'u Project modeline dönüştürür.
     */
    private fun ProjectResponse.toProject(): Project {
        return Project(
            id = this.id,
            title = this.title,
            description = this.description ?: "",
            iconName = this.iconName ?: "folder",
            iconColor = this.iconColor ?: "blue",
            status = this.status ?: "Yapılacaklar",
            dueDate = this.dueDate,
            teamLeader = this.teamLeader,
            teamMembers = this.teamMembers ?: emptyList(),
            createdBy = this.createdBy,
            createdAt = this.createdAt ?: "",
            updatedAt = this.updatedAt ?: ""
        )
    }
}
