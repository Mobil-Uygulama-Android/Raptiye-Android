package tr.edu.bilimankara20307006.taskflow.data.repository

import tr.edu.bilimankara20307006.taskflow.data.network.RetrofitClient
import tr.edu.bilimankara20307006.taskflow.data.network.model.*

/**
 * Task Repository
 * Görev işlemlerini yönetir
 */
class TaskRepository(private val tokenProvider: () -> String?) {
    
    private val taskApi = RetrofitClient.getTaskApi(tokenProvider)
    
    /**
     * Tüm görevleri getir
     */
    suspend fun getTasks(): NetworkResult<TasksResponse> {
        return safeApiCall {
            taskApi.getTasks("Bearer ${tokenProvider()}")
        }
    }
    
    /**
     * Projeye göre görevleri getir
     */
    suspend fun getTasksByProject(projectId: String): NetworkResult<TasksResponse> {
        return safeApiCall {
            taskApi.getTasksByProject("Bearer ${tokenProvider()}", projectId)
        }
    }
    
    /**
     * ID'ye göre görev getir
     */
    suspend fun getTaskById(taskId: String): NetworkResult<TaskResponse> {
        return safeApiCall {
            taskApi.getTaskById("Bearer ${tokenProvider()}", taskId)
        }
    }
    
    /**
     * Yeni görev oluştur
     */
    suspend fun createTask(
        title: String,
        description: String,
        projectId: String,
        assignedToId: String? = null,
        priority: String? = "MEDIUM",
        dueDate: String? = null,
        tags: List<String>? = null
    ): NetworkResult<TaskResponse> {
        val request = CreateTaskRequest(
            title = title,
            description = description,
            projectId = projectId,
            assignedToId = assignedToId,
            priority = priority,
            dueDate = dueDate,
            tags = tags
        )
        
        return safeApiCall {
            taskApi.createTask("Bearer ${tokenProvider()}", request)
        }
    }
    
    /**
     * Görevi güncelle
     */
    suspend fun updateTask(
        taskId: String,
        title: String? = null,
        description: String? = null,
        status: String? = null,
        priority: String? = null,
        isCompleted: Boolean? = null,
        dueDate: String? = null,
        assignedToId: String? = null
    ): NetworkResult<TaskResponse> {
        val request = UpdateTaskRequest(
            title = title,
            description = description,
            status = status,
            priority = priority,
            isCompleted = isCompleted,
            dueDate = dueDate,
            assignedToId = assignedToId
        )
        
        return safeApiCall {
            taskApi.updateTask("Bearer ${tokenProvider()}", taskId, request)
        }
    }
    
    /**
     * Görevi sil
     */
    suspend fun deleteTask(taskId: String): NetworkResult<Unit> {
        return safeApiCall {
            taskApi.deleteTask("Bearer ${tokenProvider()}", taskId)
        }
    }
    
    /**
     * Göreve yorum ekle
     */
    suspend fun addComment(
        taskId: String,
        text: String
    ): NetworkResult<CommentResponse> {
        val request = CreateCommentRequest(
            text = text,
            taskId = taskId
        )
        
        return safeApiCall {
            taskApi.addComment("Bearer ${tokenProvider()}", request)
        }
    }
    
    /**
     * Görevin yorumlarını getir
     */
    suspend fun getCommentsByTask(taskId: String): NetworkResult<List<CommentResponse>> {
        return safeApiCall {
            taskApi.getCommentsByTask("Bearer ${tokenProvider()}", taskId)
        }
    }
    
    companion object {
        @Volatile
        private var instance: TaskRepository? = null
        
        fun getInstance(tokenProvider: () -> String?): TaskRepository {
            return instance ?: synchronized(this) {
                instance ?: TaskRepository(tokenProvider).also { instance = it }
            }
        }
    }
}
