package tr.edu.bilimankara20307006.taskflow.data.repository

import tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager
import tr.edu.bilimankara20307006.taskflow.data.model.Project

/**
 * Project Repository
 * Firebase Firestore üzerinden proje işlemlerini yönetir
 */
class ProjectRepository {
    
    /**
     * Tüm projeleri getir
     */
    suspend fun getProjects(): Result<List<Project>> {
        return FirebaseManager.getProjects()
    }
    
    /**
     * ID'ye göre proje getir
     */
    suspend fun getProjectById(projectId: String): Result<Project> {
        return FirebaseManager.getProjectById(projectId)
    }
    
    /**
     * Yeni proje oluştur
     */
    suspend fun createProject(
        title: String,
        description: String,
        iconName: String = "folder",
        iconColor: String = "blue",
        dueDate: String? = null,
        teamMemberIds: List<String>? = null
    ): Result<Project> {
        return FirebaseManager.createProject(
            title = title,
            description = description,
            iconName = iconName,
            iconColor = iconColor,
            dueDate = dueDate,
            teamMemberIds = teamMemberIds
        )
    }
    
    /**
     * Projeyi güncelle
     */
    suspend fun updateProject(
        projectId: String,
        title: String? = null,
        description: String? = null,
        status: String? = null,
        iconName: String? = null,
        iconColor: String? = null,
        dueDate: String? = null
    ): Result<Unit> {
        return FirebaseManager.updateProject(
            projectId = projectId,
            title = title,
            description = description,
            status = status,
            iconName = iconName,
            iconColor = iconColor,
            dueDate = dueDate
        )
    }
    
    /**
     * Projeyi sil
     */
    suspend fun deleteProject(projectId: String): Result<Unit> {
        return FirebaseManager.deleteProject(projectId)
    }
    
    /**
     * Real-time proje değişikliklerini dinle - iOS gibi
     */
    fun observeProjects(onUpdate: (List<Project>) -> Unit, onError: (Exception) -> Unit) {
        FirebaseManager.observeProjects(onUpdate, onError)
    }
    
    /**
     * Projeye ait görevleri getir
     */
    suspend fun getTasks(projectId: String): Result<List<tr.edu.bilimankara20307006.taskflow.data.model.Task>> {
        return FirebaseManager.getTasks(projectId)
    }
    
    /**
     * Real-time görev değişikliklerini dinle - cross-platform sync için kritik
     */
    fun observeTasks(
        projectId: String, 
        onUpdate: (List<tr.edu.bilimankara20307006.taskflow.data.model.Task>) -> Unit, 
        onError: (Exception) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        return FirebaseManager.observeTasks(projectId, onUpdate, onError)
    }
    
    /**
     * Kullanıcının tüm görevlerini getir - iOS gibi
     */
    suspend fun getAllTasks(): Result<List<tr.edu.bilimankara20307006.taskflow.data.model.Task>> {
        return FirebaseManager.getAllTasks()
    }
    
    /**
     * Email ile kullanıcı ara - iOS ProjectManager searchUserByEmail
     */
    suspend fun searchUserByEmail(email: String): Result<tr.edu.bilimankara20307006.taskflow.data.model.User?> {
        return FirebaseManager.searchUserByEmail(email)
    }
    
    /**
     * Projeye ekip üyesi ekle - iOS ProjectManager addTeamMember
     */
    suspend fun addTeamMember(userId: String, projectId: String): Result<Unit> {
        return FirebaseManager.addTeamMember(userId, projectId)
    }
    
    /**
     * Projeden ekip üyesi çıkar - iOS ProjectManager removeTeamMember
     */
    suspend fun removeTeamMember(userId: String, projectId: String): Result<Unit> {
        return FirebaseManager.removeTeamMember(userId, projectId)
    }
    
    /**
     * Tüm projelerin görev istatistiklerini güncelle (Migration için)
     */
    suspend fun updateAllProjectStats(): Result<Unit> {
        return FirebaseManager.updateAllProjectStats()
    }
    
    companion object {
        @Volatile
        private var instance: ProjectRepository? = null
        
        fun getInstance(): ProjectRepository {
            return instance ?: synchronized(this) {
                instance ?: ProjectRepository().also { instance = it }
            }
        }
    }
}
