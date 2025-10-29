package tr.edu.bilimankara20307006.taskflow.data.network.api

import retrofit2.Response
import retrofit2.http.*
import tr.edu.bilimankara20307006.taskflow.data.network.ApiConstants
import tr.edu.bilimankara20307006.taskflow.data.network.model.*

/**
 * Auth API Service
 * Kullanıcı kimlik doğrulama işlemleri için API
 */
interface AuthApiService {
    
    /**
     * Kullanıcı kaydı
     */
    @POST(ApiConstants.Endpoints.REGISTER)
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>
    
    /**
     * Kullanıcı girişi
     */
    @POST(ApiConstants.Endpoints.LOGIN)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
    
    /**
     * Kullanıcı çıkışı
     */
    @POST(ApiConstants.Endpoints.LOGOUT)
    suspend fun logout(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String
    ): Response<Unit>
    
    /**
     * Mevcut kullanıcı bilgilerini getir
     */
    @GET(ApiConstants.Endpoints.GET_CURRENT_USER)
    suspend fun getCurrentUser(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String
    ): Response<UserResponse>
}
