package tr.edu.bilimankara20307006.taskflow.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Görev Durumu
 */
enum class TaskStatus {
    TODO,        // Yapılacak
    IN_PROGRESS, // Devam Ediyor
    COMPLETED    // Tamamlandı
}

/**
 * Görev Data Modeli - Firebase uyumlu
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: String = "medium",
    val assigneeId: String = "",
    val creatorId: String = "",
    val dueDate: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Eski alanlar (geriye dönük uyumluluk için)
    val assignee: User? = null,
    val isCompleted: Boolean = (status == TaskStatus.COMPLETED),
    val createdDate: Date = Date(createdAt),
    val comments: List<Comment> = emptyList()
) {
    /**
     * Son teslim tarihi formatlanmış
     */
    val formattedDueDate: String
        get() = dueDate ?: ""
    
    companion object {
        /**
         * Örnek görevler - Demo amaçlı
         */
        fun sampleTasks(projectId: String): List<Task> {
            val sampleUsers = User.sampleUsers
            return listOf(
                Task(
                    projectId = projectId,
                    title = "UI/UX Design for Mobile App",
                    description = "Create a modern and user-friendly interface for the new student project tracking application.",
                    status = TaskStatus.TODO,
                    priority = "high",
                    assignee = sampleUsers[0], // Emily Carter
                    dueDate = "2024-01-30"
                ),
                Task(
                    projectId = projectId,
                    title = "Backend API Development",
                    description = "Develop RESTful API endpoints for project and task management.",
                    status = TaskStatus.IN_PROGRESS,
                    priority = "high",
                    assignee = sampleUsers[1], // David Lee
                    dueDate = "2024-01-25"
                ),
                Task(
                    projectId = projectId,
                    title = "Database Schema Design",
                    description = "Design and implement database schema for storing projects, tasks, and user data.",
                    status = TaskStatus.COMPLETED,
                    priority = "medium",
                    assignee = sampleUsers[2], // Ahmet Yılmaz
                    dueDate = "2024-01-20"
                ),
                Task(
                    projectId = projectId,
                    title = "Write Unit Tests",
                    description = "Write comprehensive unit tests for all API endpoints and business logic.",
                    status = TaskStatus.TODO,
                    priority = "medium",
                    assignee = sampleUsers[3], // Ayşe Demir
                    dueDate = "2024-02-05"
                ),
                Task(
                    projectId = projectId,
                    title = "Code Review & Refactoring",
                    description = "Review existing codebase and refactor where necessary to improve code quality.",
                    status = TaskStatus.IN_PROGRESS,
                    priority = "low",
                    assignee = null, // Atanmamış
                    dueDate = null
                ),
                Task(
                    projectId = projectId,
                    title = "Deploy to Production",
                    description = "Deploy the application to production environment after final testing.",
                    status = TaskStatus.COMPLETED,
                    priority = "high",
                    assignee = sampleUsers[1], // David Lee
                    dueDate = "2024-01-15"
                )
            )
        }
    }
}
