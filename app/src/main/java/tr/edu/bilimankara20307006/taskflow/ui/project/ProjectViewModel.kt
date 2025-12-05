package tr.edu.bilimankara20307006.taskflow.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.model.User
import tr.edu.bilimankara20307006.taskflow.data.repository.ProjectRepository

/**
 * ProjectViewModel - Proje işlemlerini yöneten ViewModel
 */
class ProjectViewModel : ViewModel() {
    
    val repository = ProjectRepository.getInstance()
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    /**
     * Projeleri yükle
     */
    fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.getProjects()
                .onSuccess { projectList ->
                    _projects.value = projectList
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * ID'ye göre proje getir
     */
    suspend fun getProjectById(projectId: String): Result<Project> {
        return repository.getProjectById(projectId)
    }
    
    /**
     * Email ile kullanıcı ara - iOS ProjectManager searchUserByEmail
     */
    suspend fun searchUserByEmail(email: String): Result<User?> {
        return repository.searchUserByEmail(email)
    }
    
    /**
     * Projeye ekip üyesi ekle - iOS ProjectManager addTeamMember
     */
    suspend fun addTeamMember(userId: String, projectId: String): Result<Unit> {
        return repository.addTeamMember(userId, projectId)
    }
    
    /**
     * Projeden ekip üyesi çıkar - iOS ProjectManager removeTeamMember
     */
    suspend fun removeTeamMember(userId: String, projectId: String): Result<Unit> {
        return repository.removeTeamMember(userId, projectId)
    }
}
