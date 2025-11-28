package tr.edu.bilimankara20307006.taskflow.data.network.api

import retrofit2.Response
import retrofit2.http.*
import tr.edu.bilimankara20307006.taskflow.data.network.ApiConstants
import tr.edu.bilimankara20307006.taskflow.data.network.model.*

/**
 * Task API Service
 * Görev işlemleri için API
 */
interface TaskApiService {
    
    /**
     * Tüm görevleri getir
     */
    @GET(ApiConstants.Endpoints.TASKS)
    suspend fun getTasks(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String
    ): Response<TasksResponse>
    
    /**
     * Projeye göre görevleri getir
     */
    @GET(ApiConstants.Endpoints.TASKS_BY_PROJECT)
    suspend fun getTasksByProject(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String,
        @Path("projectId") projectId: String
    ): Response<TasksResponse>
    
    /**
     * ID'ye göre görev getir
     */
    @GET(ApiConstants.Endpoints.TASK_BY_ID)
    suspend fun getTaskById(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String,
        @Path("id") taskId: String
    ): Response<TaskResponse>
    
    /**
     * Yeni görev oluştur
     */
    @POST(ApiConstants.Endpoints.CREATE_TASK)
    suspend fun createTask(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String,
        @Body request: CreateTaskRequest
    ): Response<TaskResponse>
    
    /**
     * Görevi güncelle
     */
    @PUT(ApiConstants.Endpoints.UPDATE_TASK)
    suspend fun updateTask(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String,
        @Path("id") taskId: String,
        @Body request: UpdateTaskRequest
    ): Response<TaskResponse>
    
    /**
     * Görevi sil
     */
    @DELETE(ApiConstants.Endpoints.DELETE_TASK)
    suspend fun deleteTask(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String,
        @Path("id") taskId: String
    ): Response<Unit>
    
    /**
     * Göreve yorum ekle
     */
    @POST(ApiConstants.Endpoints.COMMENTS)
    suspend fun addComment(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String,
        @Body request: CreateCommentRequest
    ): Response<CommentResponse>
    
    /**
     * Görevin yorumlarını getir
     */
    @GET(ApiConstants.Endpoints.COMMENTS_BY_TASK)
    suspend fun getCommentsByTask(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String,
        @Path("taskId") taskId: String
    ): Response<List<CommentResponse>>
}
