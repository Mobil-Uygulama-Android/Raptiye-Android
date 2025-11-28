package tr.edu.bilimankara20307006.taskflow.data.network.model

import com.google.gson.annotations.SerializedName

/**
 * Network Project Model
 */
data class ProjectResponse(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("iconName")
    val iconName: String? = "folder",
    
    @SerializedName("iconColor")
    val iconColor: String? = "blue",
    
    @SerializedName("status")
    val status: String, // "TODO", "IN_PROGRESS", "COMPLETED"
    
    @SerializedName("dueDate")
    val dueDate: String? = null,
    
    @SerializedName("isCompleted")
    val isCompleted: Boolean = false,
    
    @SerializedName("tasksCount")
    val tasksCount: Int = 0,
    
    @SerializedName("completedTasksCount")
    val completedTasksCount: Int = 0,
    
    @SerializedName("teamLeader")
    val teamLeader: UserResponse? = null,
    
    @SerializedName("teamMembers")
    val teamMembers: List<UserResponse>? = null,
    
    @SerializedName("createdBy")
    val createdBy: String,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

/**
 * Create Project Request
 */
data class CreateProjectRequest(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("iconName")
    val iconName: String? = "folder",
    
    @SerializedName("iconColor")
    val iconColor: String? = "blue",
    
    @SerializedName("dueDate")
    val dueDate: String? = null,
    
    @SerializedName("teamMemberIds")
    val teamMemberIds: List<String>? = null
)

/**
 * Update Project Request
 */
data class UpdateProjectRequest(
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("iconName")
    val iconName: String? = null,
    
    @SerializedName("iconColor")
    val iconColor: String? = null,
    
    @SerializedName("isCompleted")
    val isCompleted: Boolean? = null,
    
    @SerializedName("dueDate")
    val dueDate: String? = null
)

/**
 * Projects List Response
 */
data class ProjectsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: List<ProjectResponse>,
    
    @SerializedName("count")
    val count: Int
)
