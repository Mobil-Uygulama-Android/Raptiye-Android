package tr.edu.bilimankara20307006.taskflow.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.repository.ProjectRepository

/**
 * ProjectListViewModel - Proje listesi için state yönetimi
 * 
 * Firebase Firestore'dan projeleri yükler, oluşturur, günceller ve siler.
 */
data class ProjectListState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

class ProjectListViewModel : ViewModel() {
    
    private val projectRepository = ProjectRepository.getInstance()
    
    private val _state = MutableStateFlow(ProjectListState())
    val state: StateFlow<ProjectListState> = _state.asStateFlow()
    
    init {
        // ViewModel oluşturulduğunda real-time dinleyici başlat
        startRealtimeListener()
    }
    
    /**
     * Real-time Firebase listener başlat - iOS gibi otomatik güncelleme
     */
    private fun startRealtimeListener() {
        println("🎧 Real-time listener başlatılıyor...")
        projectRepository.observeProjects(
            onUpdate = { projects ->
                println("✅ Real-time güncelleme alındı: ${projects.size} proje")
                _state.value = _state.value.copy(
                    projects = projects,
                    isLoading = false,
                    errorMessage = null
                )
            },
            onError = { error ->
                println("❌ Real-time listener hatası: ${error.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = error.message
                )
            }
        )
    }
    
    /**
     * Firebase'den tüm projeleri yükler.
     */
    fun loadProjects() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            projectRepository.getProjects()
                .onSuccess { projects ->
                    _state.value = _state.value.copy(
                        projects = projects,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        projects = emptyList(),
                        isLoading = false,
                        errorMessage = error.message ?: "Projeler yüklenirken hata oluştu"
                    )
                }
        }
    }
    
    /**
     * Projeleri yenile (pull-to-refresh).
     */
    fun refreshProjects() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, errorMessage = null)
            
            projectRepository.getProjects()
                .onSuccess { projects ->
                    _state.value = _state.value.copy(
                        projects = projects,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        errorMessage = error.message
                    )
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
        dueDate: String? = null,
        teamMemberIds: List<String>? = null
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            projectRepository.createProject(
                title = title,
                description = description,
                iconName = iconName,
                iconColor = iconColor,
                dueDate = dueDate,
                teamMemberIds = teamMemberIds
            )
                .onSuccess {
                    // Yeni proje eklendi, listeyi yenile
                    loadProjects()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Proje oluşturulamadı"
                    )
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
            
            projectRepository.updateProject(
                projectId = projectId,
                title = title,
                description = description,
                iconName = iconName,
                iconColor = iconColor,
                status = status,
                dueDate = dueDate
            )
                .onSuccess {
                    // Proje güncellendi, listeyi yenile
                    loadProjects()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Proje güncellenemedi"
                    )
                }
        }
    }
    
    /**
     * Projeyi siler - hem Firebase'den hem de local state'ten.
     */
    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            println("🔄 ViewModel: Proje silme başladı - $projectId")
            
            // Önce local listeden kaldır (UI anında güncellensin)
            val updatedProjects = _state.value.projects.filter { it.id != projectId }
            _state.value = _state.value.copy(
                projects = updatedProjects,
                errorMessage = null
            )
            println("✅ ViewModel: Local listeden kaldırıldı, kalan proje sayısı: ${updatedProjects.size}")
            
            // Sonra Firebase'den sil
            projectRepository.deleteProject(projectId)
                .onSuccess {
                    println("✅ ViewModel: Firebase'den silme başarılı")
                }
                .onFailure { error ->
                    println("❌ ViewModel: Firebase'den silme hatası: ${error.message}")
                    // Hata durumunda projeyi geri ekle
                    _state.value = _state.value.copy(
                        projects = _state.value.projects,
                        errorMessage = error.message ?: "Proje silinemedi"
                    )
                    // Listeyi yeniden yükle
                    loadProjects()
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
     * Belirli bir projenin görevlerini getirir.
     */
    fun getTasksForProject(projectId: String): List<tr.edu.bilimankara20307006.taskflow.data.model.Task> {
        // TODO: Firebase'den gerçek görevleri çek
        // Şimdilik boş liste dönüyoruz
        return emptyList()
    }
}
