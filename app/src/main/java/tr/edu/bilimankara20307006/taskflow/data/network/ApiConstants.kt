package tr.edu.bilimankara20307006.taskflow.data.network

/**
 * API Constants
 * iOS backend'inizin URL'sini buraya yazın
 * Örnek: "http://192.168.1.100:3000" (yerel geliştirme için)
 * Örnek: "https://your-api.herokuapp.com" (production için)
 */
object ApiConstants {
    
    // TODO: iOS arkadaşınızdan backend URL'sini alın
    // Localhost: http://localhost:3000/api/
    // Emülatör için: http://10.0.2.2:3000/api/ (localhost yerine)
    // Gerçek cihaz için: http://192.168.X.X:3000/api/ (Mac'in IP adresi)
    const val BASE_URL = "http://10.0.2.2:3000/api/"
    
    // API Endpoints (iOS backend ile uyumlu)
    object Endpoints {
        // Auth endpoints
        const val REGISTER = "auth/register"
        const val LOGIN = "auth/login"
        const val GET_CURRENT_USER = "auth/me"
        const val UPDATE_PROFILE = "auth/update"
        
        // User endpoints
        const val USERS = "users"
        const val USER_BY_ID = "users/{id}"
        
        // Project endpoints
        const val PROJECTS = "projects"
        const val PROJECT_BY_ID = "projects/{id}"
        const val CREATE_PROJECT = "projects"
        const val UPDATE_PROJECT = "projects/{id}"
        const val DELETE_PROJECT = "projects/{id}"
        
        // Task endpoints
        const val TASKS = "tasks"
        const val TASK_BY_ID = "tasks/{id}"
        const val CREATE_TASK = "tasks"
        const val UPDATE_TASK = "tasks/{id}"
        const val DELETE_TASK = "tasks/{id}"
        const val TOGGLE_TASK = "tasks/{id}/toggle"
        const val ADD_COMMENT = "tasks/{id}/comments"
        
        // Health check
        const val HEALTH = "health"
    }
    
    // HTTP Headers
    object Headers {
        const val AUTHORIZATION = "Authorization"
        const val CONTENT_TYPE = "Content-Type"
        const val ACCEPT = "Accept"
    }
    
    // Network Timeouts (seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
