package tr.edu.bilimankara20307006.taskflow.data.repository

import tr.edu.bilimankara20307006.taskflow.data.network.RetrofitClient
import tr.edu.bilimankara20307006.taskflow.data.network.model.*

/**
 * Project Repository
 * Proje işlemlerini yönetir
 */
class ProjectRepository(private val tokenProvider: () -> String?) {
    
    private val projectApi = RetrofitClient.getProjectApi(tokenProvider)
    
    /**
     * Tüm projeleri getir
     */
    suspend fun getProjects(): NetworkResult<ProjectsResponse> {
        return safeApiCall {
            projectApi.getProjects("Bearer ${tokenProvider()}")
        }
    }
    
    /**
     * ID'ye göre proje getir
     */
    suspend fun getProjectById(projectId: String): NetworkResult<ProjectResponse> {
        return safeApiCall {
            projectApi.getProjectById("Bearer ${tokenProvider()}", projectId)
        }
    }
    
    /**
     * Yeni proje oluştur
     */
    suspend fun createProject(
        title: String,
        description: String,
        iconName: String? = "folder",
        iconColor: String? = "blue",
        dueDate: String? = null,
        teamMemberIds: List<String>? = null
    ): NetworkResult<ProjectResponse> {
        val request = CreateProjectRequest(
            title = title,
            description = description,
            iconName = iconName,
            iconColor = iconColor,
            dueDate = dueDate,
            teamMemberIds = teamMemberIds
        )
        
        return safeApiCall {
            projectApi.createProject("Bearer ${tokenProvider()}", request)
        }
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
        isCompleted: Boolean? = null,
        dueDate: String? = null
    ): NetworkResult<ProjectResponse> {
        val request = UpdateProjectRequest(
            title = title,
            description = description,
            status = status,
            iconName = iconName,
            iconColor = iconColor,
            isCompleted = isCompleted,
            dueDate = dueDate
        )
        
        return safeApiCall {
            projectApi.updateProject("Bearer ${tokenProvider()}", projectId, request)
        }
    }
    
    /**
     * Projeyi sil
     */
    suspend fun deleteProject(projectId: String): NetworkResult<Unit> {
        return safeApiCall {
            projectApi.deleteProject("Bearer ${tokenProvider()}", projectId)
        }
    }
    
    companion object {
        @Volatile
        private var instance: ProjectRepository? = null
        
        fun getInstance(tokenProvider: () -> String?): ProjectRepository {
            return instance ?: synchronized(this) {
                instance ?: ProjectRepository(tokenProvider).also { instance = it }
            }
        }
    }
}
