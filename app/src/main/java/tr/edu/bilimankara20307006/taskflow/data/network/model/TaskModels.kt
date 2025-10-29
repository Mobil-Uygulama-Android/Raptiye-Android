package tr.edu.bilimankara20307006.taskflow.data.network.model

import com.google.gson.annotations.SerializedName

/**
 * Network Task Model
 */
data class TaskResponse(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("projectId")
    val projectId: String,
    
    @SerializedName("status")
    val status: String, // "TODO", "IN_PROGRESS", "COMPLETED"
    
    @SerializedName("priority")
    val priority: String? = "MEDIUM", // "LOW", "MEDIUM", "HIGH"
    
    @SerializedName("isCompleted")
    val isCompleted: Boolean = false,
    
    @SerializedName("dueDate")
    val dueDate: String? = null,
    
    @SerializedName("assignedTo")
    val assignedTo: UserResponse? = null,
    
    @SerializedName("createdBy")
    val createdBy: String,
    
    @SerializedName("tags")
    val tags: List<String>? = null,
    
    @SerializedName("attachments")
    val attachments: List<String>? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

/**
 * Create Task Request
 */
data class CreateTaskRequest(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("projectId")
    val projectId: String,
    
    @SerializedName("assignedToId")
    val assignedToId: String? = null,
    
    @SerializedName("priority")
    val priority: String? = "MEDIUM",
    
    @SerializedName("dueDate")
    val dueDate: String? = null,
    
    @SerializedName("tags")
    val tags: List<String>? = null
)

/**
 * Update Task Request
 */
data class UpdateTaskRequest(
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("priority")
    val priority: String? = null,
    
    @SerializedName("isCompleted")
    val isCompleted: Boolean? = null,
    
    @SerializedName("dueDate")
    val dueDate: String? = null,
    
    @SerializedName("assignedToId")
    val assignedToId: String? = null
)

/**
 * Tasks List Response
 */
data class TasksResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: List<TaskResponse>,
    
    @SerializedName("count")
    val count: Int
)

/**
 * Comment Model
 */
data class CommentResponse(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("taskId")
    val taskId: String,
    
    @SerializedName("author")
    val author: UserResponse,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

/**
 * Create Comment Request
 */
data class CreateCommentRequest(
    @SerializedName("text")
    val text: String,
    
    @SerializedName("taskId")
    val taskId: String
)
