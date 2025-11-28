package tr.edu.bilimankara20307006.taskflow.data.network.api

import retrofit2.Response
import retrofit2.http.*
import tr.edu.bilimankara20307006.taskflow.data.network.ApiConstants
import tr.edu.bilimankara20307006.taskflow.data.network.model.*

/**
 * Project API Service
 * Proje işlemleri için API
 */
interface ProjectApiService {
    
    /**
     * Tüm projeleri getir
     */
    @GET(ApiConstants.Endpoints.PROJECTS)
    suspend fun getProjects(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String
    ): Response<ProjectsResponse>
    
    /**
     * ID'ye göre proje getir
     */
    @GET(ApiConstants.Endpoints.PROJECT_BY_ID)
    suspend fun getProjectById(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String,
        @Path("id") projectId: String
    ): Response<ProjectResponse>
    
    /**
     * Yeni proje oluştur
     */
    @POST(ApiConstants.Endpoints.CREATE_PROJECT)
    suspend fun createProject(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String,
        @Body request: CreateProjectRequest
    ): Response<ProjectResponse>
    
    /**
     * Projeyi güncelle
     */
    @PUT(ApiConstants.Endpoints.UPDATE_PROJECT)
    suspend fun updateProject(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String,
        @Path("id") projectId: String,
        @Body request: UpdateProjectRequest
    ): Response<ProjectResponse>
    
    /**
     * Projeyi sil
     */
    @DELETE(ApiConstants.Endpoints.DELETE_PROJECT)
    suspend fun deleteProject(
        @Header(ApiConstants.Headers.AUTHORIZATION) token: String,
        @Path("id") projectId: String
    ): Response<Unit>
}
