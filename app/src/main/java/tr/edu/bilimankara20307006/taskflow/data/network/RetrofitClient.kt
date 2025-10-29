package tr.edu.bilimankara20307006.taskflow.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tr.edu.bilimankara20307006.taskflow.data.network.api.AuthApiService
import tr.edu.bilimankara20307006.taskflow.data.network.api.ProjectApiService
import tr.edu.bilimankara20307006.taskflow.data.network.api.TaskApiService
import java.util.concurrent.TimeUnit

/**
 * Retrofit Client
 * Tüm API servislerini sağlar
 */
object RetrofitClient {
    
    /**
     * Logging Interceptor
     * Network isteklerini ve yanıtlarını loglar (debug için)
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    /**
     * Auth Interceptor
     * Her istekte otomatik olarak token ekler
     */
    private class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val originalRequest = chain.request()
            val token = tokenProvider()
            
            // Eğer token varsa, header'a ekle
            val newRequest = if (!token.isNullOrEmpty()) {
                originalRequest.newBuilder()
                    .header(ApiConstants.Headers.AUTHORIZATION, "Bearer $token")
                    .build()
            } else {
                originalRequest
            }
            
            return chain.proceed(newRequest)
        }
    }
    
    /**
     * OkHttp Client
     */
    private fun createOkHttpClient(tokenProvider: () -> String?): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(tokenProvider))
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Retrofit Instance
     */
    private fun createRetrofit(tokenProvider: () -> String?): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(createOkHttpClient(tokenProvider))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Auth API Service
     * Login/Register için token gerekmez
     */
    val authApi: AuthApiService by lazy {
        createRetrofit { null }.create(AuthApiService::class.java)
    }
    
    /**
     * Project API Service
     * Token gerektirir
     */
    fun getProjectApi(tokenProvider: () -> String?): ProjectApiService {
        return createRetrofit(tokenProvider).create(ProjectApiService::class.java)
    }
    
    /**
     * Task API Service
     * Token gerektirir
     */
    fun getTaskApi(tokenProvider: () -> String?): TaskApiService {
        return createRetrofit(tokenProvider).create(TaskApiService::class.java)
    }
}
