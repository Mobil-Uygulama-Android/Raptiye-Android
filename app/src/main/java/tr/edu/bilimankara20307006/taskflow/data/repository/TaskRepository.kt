package tr.edu.bilimankara20307006.taskflow.data.repository

import tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager
import tr.edu.bilimankara20307006.taskflow.data.model.Task

/**
 * Task Repository
 * Firebase Firestore üzerinden görev işlemlerini yönetir
 */
class TaskRepository {
    
    /**
     * Kullanıcının tüm görevlerini getir
     */
    suspend fun getAllTasks(): Result<List<Task>> {
        return FirebaseManager.getAllUserTasks()
    }
    
    /**
     * Projeye göre görevleri getir
     */
    suspend fun getTasksByProject(projectId: String): Result<List<Task>> {
        return FirebaseManager.getTasks(projectId)
    }
    
    /**
     * Yeni görev oluştur
     */
    suspend fun createTask(
        title: String,
        description: String,
        projectId: String,
        assignedToId: String? = null,
        priority: String = "medium",
        dueDate: String? = null
    ): Result<Task> {
        return FirebaseManager.createTask(
            projectId = projectId,
            title = title,
            description = description,
            priority = priority,
            assigneeId = assignedToId,
            dueDate = dueDate
        )
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
        dueDate: String? = null,
        assignedToId: String? = null
    ): Result<Unit> {
        return FirebaseManager.updateTask(
            taskId = taskId,
            title = title,
            description = description,
            status = status,
            priority = priority,
            assigneeId = assignedToId,
            dueDate = dueDate
        )
    }
    
    /**
     * Görev durumunu değiştir
     */
    suspend fun toggleTaskStatus(taskId: String): Result<Unit> {
        return FirebaseManager.toggleTaskStatus(taskId)
    }
    
    /**
     * Görevi sil
     */
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return FirebaseManager.deleteTask(taskId)
    }
    
    companion object {
        @Volatile
        private var instance: TaskRepository? = null
        
        fun getInstance(): TaskRepository {
            return instance ?: synchronized(this) {
                instance ?: TaskRepository().also { instance = it }
            }
        }
    }
}
