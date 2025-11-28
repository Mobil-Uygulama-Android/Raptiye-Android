package tr.edu.bilimankara20307006.taskflow.data.network.model

import com.google.gson.annotations.SerializedName

/**
 * Network User Model
 * MongoDB'den gelen User verisi
 */
data class UserResponse(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("displayName")
    val displayName: String? = null,
    
    @SerializedName("photoUrl")
    val photoUrl: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

/**
 * Login Request
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

/**
 * Login Response
 */
data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("token")
    val token: String,
    
    @SerializedName("user")
    val user: UserResponse,
    
    @SerializedName("message")
    val message: String? = null
)

/**
 * Register Request
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("displayName")
    val displayName: String? = null
)

/**
 * Register Response
 */
data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("token")
    val token: String,
    
    @SerializedName("user")
    val user: UserResponse,
    
    @SerializedName("message")
    val message: String? = null
)

/**
 * API Error Response
 */
data class ErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("error")
    val error: String? = null
)
