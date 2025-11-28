package tr.edu.bilimankara20307006.taskflow.data.repository

import tr.edu.bilimankara20307006.taskflow.data.network.RetrofitClient
import tr.edu.bilimankara20307006.taskflow.data.network.model.LoginRequest
import tr.edu.bilimankara20307006.taskflow.data.network.model.LoginResponse
import tr.edu.bilimankara20307006.taskflow.data.network.model.RegisterRequest
import tr.edu.bilimankara20307006.taskflow.data.network.model.RegisterResponse

/**
 * Auth Repository
 * Authentication işlemlerini yönetir
 */
class AuthRepository {
    
    private val authApi = RetrofitClient.authApi
    
    /**
     * Kullanıcı kaydı
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        displayName: String? = null
    ): NetworkResult<RegisterResponse> {
        val request = RegisterRequest(
            username = username,
            email = email,
            password = password,
            displayName = displayName
        )
        
        return safeApiCall {
            authApi.register(request)
        }
    }
    
    /**
     * Kullanıcı girişi
     */
    suspend fun login(
        email: String,
        password: String
    ): NetworkResult<LoginResponse> {
        val request = LoginRequest(
            email = email,
            password = password
        )
        
        return safeApiCall {
            authApi.login(request)
        }
    }
    
    /**
     * Kullanıcı çıkışı
     */
    suspend fun logout(token: String): NetworkResult<Unit> {
        return safeApiCall {
            authApi.logout("Bearer $token")
        }
    }
    
    /**
     * Mevcut kullanıcıyı getir
     */
    suspend fun getCurrentUser(token: String): NetworkResult<tr.edu.bilimankara20307006.taskflow.data.network.model.UserResponse> {
        return safeApiCall {
            authApi.getCurrentUser("Bearer $token")
        }
    }
    
    companion object {
        @Volatile
        private var instance: AuthRepository? = null
        
        fun getInstance(): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository().also { instance = it }
            }
        }
    }
}
