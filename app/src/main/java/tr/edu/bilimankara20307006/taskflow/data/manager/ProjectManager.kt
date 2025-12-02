package tr.edu.bilimankara20307006.taskflow.data.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import tr.edu.bilimankara20307006.taskflow.data.model.Project

/**
 * ProjectManager - iOS'taki ProjectManager.swift dosyasının Android karşılığı
 * Firestore ile proje yönetimi yapar
 */
class ProjectManager : ViewModel() {
    
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private var listener: ListenerRegistration? = null
    
    init {
        println("📦 ProjectManager initialized")
    }
    
    /**
     * Firestore'dan projeleri gerçek zamanlı dinlemeye başlar
     */
    fun setupListener() {
        println("🔄 setupListener called")
        
        val userId = auth.currentUser?.uid
        if (userId == null) {
            println("⚠️ setupListener: No user logged in, skipping listener setup")
            return
        }
        
        println("👤 setupListener: User ID = $userId")
        
        // Eski listener varsa kaldır
        listener?.remove()
        
        // Yeni listener ekle
        listener = db.collection("users")
            .document(userId)
            .collection("projects")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = error.localizedMessage
                    println("❌ Firestore listener hatası: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot == null) {
                    println("⚠️ No documents in snapshot")
                    return@addSnapshotListener
                }
                
                val projectsList = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Project::class.java)
                    } catch (e: Exception) {
                        println("❌ Proje parse hatası: ${e.message}")
                        null
                    }
                }
                
                _projects.value = projectsList
                println("✅ ${projectsList.size} proje yüklendi")
            }
    }
    
    /**
     * Firestore'dan projeleri bir kez çeker
     */
    suspend fun fetchProjects() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "Kullanıcı oturum açmamış"
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("projects")
                .get()
                .await()
            
            val projectsList = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Project::class.java)
                } catch (e: Exception) {
                    println("❌ Proje parse hatası: ${e.message}")
                    null
                }
            }
            
            _projects.value = projectsList
            println("✅ ${projectsList.size} proje yüklendi")
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage
            println("❌ Proje yükleme hatası: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Yeni proje oluşturur
     */
    suspend fun createProject(project: Project): Result<Unit> {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            return Result.failure(Exception("Kullanıcı oturum açmamış"))
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val projectRef = db.collection("users")
                .document(userId)
                .collection("projects")
                .document(project.id)
            
            projectRef.set(project).await()
            
            println("✅ Proje oluşturuldu: ${project.title}")
            Result.success(Unit)
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage
            println("❌ Proje oluşturma hatası: ${e.message}")
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Projeyi günceller
     */
    suspend fun updateProject(project: Project): Result<Unit> {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            return Result.failure(Exception("Kullanıcı oturum açmamış"))
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val projectRef = db.collection("users")
                .document(userId)
                .collection("projects")
                .document(project.id)
            
            projectRef.set(project).await()
            
            println("✅ Proje güncellendi: ${project.title}")
            Result.success(Unit)
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage
            println("❌ Proje güncelleme hatası: ${e.message}")
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Projeyi siler
     */
    suspend fun deleteProject(projectId: String): Result<Unit> {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            return Result.failure(Exception("Kullanıcı oturum açmamış"))
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            db.collection("users")
                .document(userId)
                .collection("projects")
                .document(projectId)
                .delete()
                .await()
            
            println("✅ Proje silindi: $projectId")
            Result.success(Unit)
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage
            println("❌ Proje silme hatası: ${e.message}")
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Listener'ı temizler
     */
    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        println("🧹 ProjectManager cleaned up")
    }
}
