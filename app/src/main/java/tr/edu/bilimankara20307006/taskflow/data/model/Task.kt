package tr.edu.bilimankara20307006.taskflow.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Görev Data Modeli
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val assignee: User? = null,
    val dueDate: Date? = null,
    val isCompleted: Boolean = false,
    val projectId: String,
    val createdDate: Date = Date(),
    val comments: List<Comment> = emptyList()
) {
    /**
     * Son teslim tarihi formatlanmış
     */
    val formattedDueDate: String
        get() {
            if (dueDate == null) return ""
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(dueDate)
        }
    
    companion object {
        /**
         * Örnek görevler
         */
        fun sampleTasks(projectId: String): List<Task> {
            val user1 = User(
                uid = "1",
                displayName = "Emily Carter",
                email = "emily@example.com",
                photoUrl = null
            )
            
            val user2 = User(
                uid = "2",
                displayName = "David Lee",
                email = "david@example.com",
                photoUrl = null
            )
            
            val comment1 = Comment(
                id = "1",
                text = "Initial project scope and objectives defined.",
                author = user1,
                createdDate = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)
            )
            
            val comment2 = Comment(
                id = "2",
                text = "Resources and tools required for the project identified.",
                author = user2,
                createdDate = Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000)
            )
            
            return listOf(
                Task(
                    title = "UI/UX Design for Mobile App",
                    description = "Create a modern and user-friendly interface for the new student project tracking application.",
                    assignee = user1,
                    dueDate = Date(System.currentTimeMillis() + 20L * 24 * 60 * 60 * 1000), // 20 gün sonra
                    projectId = projectId,
                    comments = listOf(comment1, comment2)
                ),
                Task(
                    title = "Backend API Development",
                    description = "Develop RESTful API endpoints for project and task management.",
                    assignee = user2,
                    dueDate = Date(System.currentTimeMillis() + 45L * 24 * 60 * 60 * 1000), // 45 gün sonra
                    projectId = projectId,
                    comments = emptyList()
                ),
                Task(
                    title = "Database Schema Design",
                    description = "Design and implement database schema for storing projects, tasks, and user data.",
                    assignee = user1,
                    dueDate = Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000), // 30 gün sonra
                    projectId = projectId,
                    isCompleted = true,
                    comments = emptyList()
                )
            )
        }
    }
}
