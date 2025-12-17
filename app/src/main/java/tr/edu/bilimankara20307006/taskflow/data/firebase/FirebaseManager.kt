package tr.edu.bilimankara20307006.taskflow.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import tr.edu.bilimankara20307006.taskflow.data.model.Comment
import tr.edu.bilimankara20307006.taskflow.data.model.Project
import tr.edu.bilimankara20307006.taskflow.data.model.ProjectMember
import tr.edu.bilimankara20307006.taskflow.data.model.ProjectRole
import tr.edu.bilimankara20307006.taskflow.data.model.Task
import tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus
import tr.edu.bilimankara20307006.taskflow.data.model.ProjectStatus
import tr.edu.bilimankara20307006.taskflow.data.model.User

/**
 * Firebase Manager
 * iOS'taki FirebaseManager.swift'in Android versiyonu
 * Firebase Firestore işlemlerini yönetir
 */
object FirebaseManager {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    // Firestore Collections
    private const val COLLECTION_PROJECTS = "projects"
    private const val COLLECTION_TASKS = "tasks"
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_COMMENTS = "comments"
    
    /**
     * Mevcut kullanıcının UID'sini döner
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    /**
     * Mevcut kullanıcı oturum açmış mı?
     */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    
    // ==================== PROJECT OPERATIONS ====================
    
    /**
     * Firestore dökümanından Project nesnesi oluşturur - Manuel mapping
     */
    private fun documentToProject(doc: com.google.firebase.firestore.DocumentSnapshot): Project? {
        return try {
            val data = doc.data ?: return null
            
            val teamLeaderMap = data["teamLeader"] as? Map<*, *>
            val teamLeader = teamLeaderMap?.let { map ->
                try {
                    User(
                        uid = map["uid"] as? String ?: "",
                        displayName = map["displayName"] as? String,
                        email = map["email"] as? String,
                        photoUrl = map["photoUrl"] as? String,
                        createdAt = try {
                            when (val ts = map["createdAt"]) {
                                is com.google.firebase.Timestamp -> ts.toDate().time
                                is Long -> ts
                                is Number -> ts.toLong()
                                else -> null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            val teamMembersList = data["teamMembers"] as? List<*>
            val teamMembers = teamMembersList?.mapNotNull { item ->
                try {
                    val map = item as? Map<*, *> ?: return@mapNotNull null
                    User(
                        uid = map["uid"] as? String ?: "",
                        displayName = map["displayName"] as? String,
                        email = map["email"] as? String,
                        photoUrl = map["photoUrl"] as? String,
                        createdAt = try {
                            when (val ts = map["createdAt"]) {
                                is com.google.firebase.Timestamp -> ts.toDate().time
                                is Long -> ts
                                is Number -> ts.toLong()
                                else -> null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            
            // Yeni: Rol bazlı üyeleri parse et
            val membersList = data["members"] as? List<*>
            val members = membersList?.mapNotNull { item ->
                try {
                    val map = item as? Map<*, *> ?: return@mapNotNull null
                    tr.edu.bilimankara20307006.taskflow.data.model.ProjectMember.fromMap(
                        map.mapKeys { it.key.toString() }.mapValues { it.value ?: "" }
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            
            Project(
                id = doc.id,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                iconName = data["iconName"] as? String ?: "folder",
                iconColor = data["iconColor"] as? String ?: "blue",
                ownerId = data["ownerId"] as? String ?: "",
                teamMemberIds = (data["teamMemberIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                teamLeader = teamLeader,
                teamMembers = teamMembers,
                members = members, // Yeni: Rol bazlı üyeler
                status = when (data["status"] as? String) {
                    "completed" -> ProjectStatus.COMPLETED
                    "archived" -> ProjectStatus.ARCHIVED
                    else -> ProjectStatus.ACTIVE
                },
                dueDate = data["dueDate"] as? String,
                createdAt = (data["createdAt"] as? Long) ?: 0L,
                updatedAt = (data["updatedAt"] as? Long) ?: 0L,
                tasksCount = ((data["tasksCount"] as? Number)?.toInt()) ?: 0,
                completedTasksCount = ((data["completedTasksCount"] as? Number)?.toInt()) ?: 0
            )
        } catch (e: Exception) {
            println("❌ Project parse hatası: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Kullanıcının tüm projelerini getirir
     */
    suspend fun getProjects(): Result<List<Project>> {
        return try {
            val userId = getCurrentUserId() 
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            val snapshot = db.collection(COLLECTION_PROJECTS)
                .whereArrayContains("teamMemberIds", userId)
                .get()
                .await()
            
            // Client-side'da sıralama yap (index gerekmez)
            val projects = snapshot.documents.mapNotNull { doc ->
                documentToProject(doc)
            }.sortedByDescending { it.createdAt }
            
            Result.success(projects)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Real-time projeler dinleyicisi - iOS gibi
     * Projeler değiştiğinde otomatik günceller
     */
    fun observeProjects(onUpdate: (List<Project>) -> Unit, onError: (Exception) -> Unit) {
        val userId = getCurrentUserId()
        if (userId == null) {
            println("❌ observeProjects: Kullanıcı oturum açmamış")
            onError(Exception("Kullanıcı oturum açmamış"))
            return
        }
        
        println("🎧 observeProjects başlatılıyor - UserID: $userId")
        println("🔍 Collection: $COLLECTION_PROJECTS, TeamMemberIds içinde: $userId")
        
        db.collection(COLLECTION_PROJECTS)
            .whereArrayContains("teamMemberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ Real-time listener hatası: ${error.message}")
                    onError(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val projects = snapshot.documents.mapNotNull { doc ->
                        documentToProject(doc)
                    }.sortedByDescending { it.createdAt }
                    
                    println("🔄 Real-time güncelleme: ${projects.size} proje")
                    onUpdate(projects)
                }
            }
    }
    
    /**
     * Projeye ait görevleri getirir
     */
    suspend fun getTasks(projectId: String): Result<List<Task>> {
        return try {
            println("🔍 getTasks çağrıldı: projectId=$projectId")
            val snapshot = db.collection(COLLECTION_TASKS)
                .whereEqualTo("projectId", projectId)
                .get()
                .await()
            
            println("📊 Bulunan görev sayısı: ${snapshot.documents.size}")
            
            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    Task(
                        id = doc.id,
                        projectId = data["projectId"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        status = when (data["status"] as? String) {
                            "inProgress", "in_progress" -> tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.IN_PROGRESS
                            "done", "completed" -> tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.COMPLETED
                            else -> tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.TODO
                        },
                        priority = data["priority"] as? String ?: "medium",
                        assigneeId = data["assigneeId"] as? String ?: "",
                        creatorId = data["creatorId"] as? String ?: "",
                        dueDate = data["dueDate"] as? String,
                        createdAt = (data["createdAt"] as? Long) ?: 0L,
                        updatedAt = (data["updatedAt"] as? Long) ?: 0L
                    )
                } catch (e: Exception) {
                    println("❌ Görev parse hatası: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }.sortedBy { it.createdAt }
            
            println("✅ Parse edilen görev sayısı: ${tasks.size}")
            Result.success(tasks)
        } catch (e: Exception) {
            println("❌ getTasks hatası: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Real-time görev dinleyicisi - cross-platform sync için kritik
     * @param projectId Proje ID'si
     * @param onUpdate Görevler güncellendiğinde çağrılır
     * @param onError Hata durumunda çağrılır
     * @return ListenerRegistration Listener'ı iptal etmek için
     */
    fun observeTasks(
        projectId: String,
        onUpdate: (List<Task>) -> Unit,
        onError: (Exception) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        return db.collection(COLLECTION_TASKS)
            .whereEqualTo("projectId", projectId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ Real-time task listener hatası: ${error.message}")
                    onError(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            
                            Task(
                                id = doc.id,
                                projectId = data["projectId"] as? String ?: "",
                                title = data["title"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                status = when (data["status"] as? String) {
                                    "inProgress", "in_progress" -> tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.IN_PROGRESS
                                    "done", "completed" -> tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.COMPLETED
                                    else -> tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.TODO
                                },
                                priority = data["priority"] as? String ?: "medium",
                                assigneeId = data["assigneeId"] as? String ?: "",
                                creatorId = data["creatorId"] as? String ?: "",
                                dueDate = data["dueDate"] as? String,
                                createdAt = (data["createdAt"] as? Long) ?: 0L,
                                updatedAt = (data["updatedAt"] as? Long) ?: 0L
                            )
                        } catch (e: Exception) {
                            println("⚠️ Task parse hatası: ${e.message}")
                            null
                        }
                    }.sortedBy { it.createdAt }
                    
                    println("🔄 Real-time task güncelleme: ${tasks.size} görev")
                    onUpdate(tasks)
                }
            }
    }
    
    /**
     * Kullanıcının tüm görevlerini getirir (iOS gibi)
     */
    suspend fun getAllTasks(): Result<List<Task>> {
        return try {
            val userId = getCurrentUserId() 
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            // Önce kullanıcının projelerini al
            val projectsResult = getProjects()
            if (projectsResult.isFailure) {
                return Result.failure(projectsResult.exceptionOrNull() ?: Exception("Projeler alınamadı"))
            }
            
            val projects = projectsResult.getOrNull() ?: emptyList()
            val projectIds = projects.map { it.id }
            
            if (projectIds.isEmpty()) {
                return Result.success(emptyList())
            }
            
            // Tüm projelerin görevlerini al
            val allTasks = mutableListOf<Task>()
            for (projectId in projectIds) {
                val tasksResult = getTasks(projectId)
                if (tasksResult.isSuccess) {
                    allTasks.addAll(tasksResult.getOrNull() ?: emptyList())
                }
            }
            
            // Tarihe göre sırala (en yeni en üstte)
            val sortedTasks = allTasks.sortedByDescending { it.createdAt }
            
            Result.success(sortedTasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * ID'ye göre proje getirir
     */
    suspend fun getProjectById(projectId: String): Result<Project> {
        return try {
            println("🔍 getProjectById çağrıldı: $projectId")
            val doc = db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .get()
                .await()
            
            println("📄 Döküman alındı: exists=${doc.exists()}")
            
            if (!doc.exists()) {
                return Result.failure(Exception("Proje bulunamadı"))
            }
            
            // Önce parse etmeyi dene
            var project = documentToProject(doc)
            
            // Parse başarısız olduysa, eski proje olabilir - migration yap
            if (project == null) {
                println("⚠️ Proje parse edilemedi, migration yapılıyor...")
                val data = doc.data
                if (data != null) {
                    val hasTeamLeader = data.containsKey("teamLeader") && data["teamLeader"] != null
                    val hasTeamMembers = data.containsKey("teamMembers")
                    
                    if (!hasTeamLeader || !hasTeamMembers) {
                        val ownerId = data["ownerId"] as? String
                        if (ownerId != null) {
                            // Owner bilgilerini al
                            val ownerDoc = db.collection(COLLECTION_USERS)
                                .document(ownerId)
                                .get()
                                .await()
                            
                            val teamLeaderMap = if (ownerDoc.exists()) {
                                hashMapOf(
                                    "uid" to ownerId,
                                    "email" to ownerDoc.getString("email"),
                                    "displayName" to ownerDoc.getString("displayName"),
                                    "photoUrl" to ownerDoc.getString("photoUrl"),
                                    "createdAt" to try {
                                        ownerDoc.getTimestamp("createdAt")?.toDate()?.time
                                    } catch (e: Exception) {
                                        null
                                    }
                                )
                            } else null
                            
                            // Projeyi güncelle
                            val updates = mutableMapOf<String, Any?>(
                                "updatedAt" to System.currentTimeMillis()
                            )
                            
                            if (!hasTeamLeader) {
                                updates["teamLeader"] = teamLeaderMap
                            }
                            
                            if (!hasTeamMembers) {
                                updates["teamMembers"] = emptyList<Map<String, Any?>>()
                            }
                            
                            doc.reference.update(updates).await()
                            println("✅ Proje migration tamamlandı, tekrar yükleniyor...")
                            
                            // Tekrar yükle
                            val updatedDoc = doc.reference.get().await()
                            project = documentToProject(updatedDoc)
                        }
                    }
                }
            }
            
            if (project == null) {
                return Result.failure(Exception("Proje verisi parse edilemedi"))
            }
            
            println("✅ Project başarıyla oluşturuldu: ${project.title}")
            Result.success(project)
        } catch (e: Exception) {
            println("❌ Proje yükleme hatası: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Yeni proje oluşturur
     */
    suspend fun createProject(
        title: String,
        description: String,
        iconName: String = "folder",
        iconColor: String = "blue",
        dueDate: String? = null,
        teamMemberIds: List<String>? = null
    ): Result<Project> {
        return try {
            val userId = getCurrentUserId() 
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            println("📝 Yeni proje oluşturuluyor:")
            println("   UserID: $userId")
            println("   Proje: $title")
            println("   TeamMemberIds: $teamMemberIds")
            
            val members = teamMemberIds?.toMutableList() ?: mutableListOf()
            if (!members.contains(userId)) {
                members.add(userId)
            }
            
            // Proje sahibinin bilgilerini al
            val ownerDoc = db.collection(COLLECTION_USERS).document(userId).get().await()
            val teamLeaderMap = if (ownerDoc.exists()) {
                hashMapOf(
                    "uid" to userId,
                    "email" to ownerDoc.getString("email"),
                    "displayName" to ownerDoc.getString("displayName"),
                    "photoUrl" to ownerDoc.getString("photoUrl"),
                    "createdAt" to try {
                        ownerDoc.getTimestamp("createdAt")?.toDate()?.time
                    } catch (e: Exception) {
                        null
                    }
                )
            } else null
            
            // Tüm ekip üyelerinin bilgilerini al
            val teamMembersList = mutableListOf<Map<String, Any?>>()
            for (memberId in members) {
                try {
                    val memberDoc = db.collection(COLLECTION_USERS).document(memberId).get().await()
                    if (memberDoc.exists()) {
                        val memberMap = hashMapOf(
                            "uid" to memberId,
                            "email" to memberDoc.getString("email"),
                            "displayName" to memberDoc.getString("displayName"),
                            "photoUrl" to memberDoc.getString("photoUrl"),
                            "createdAt" to try {
                                memberDoc.getTimestamp("createdAt")?.toDate()?.time
                            } catch (e: Exception) {
                                null
                            }
                        )
                        teamMembersList.add(memberMap)
                    }
                } catch (e: Exception) {
                    println("⚠️ Üye bilgisi alınamadı: $memberId - ${e.message}")
                }
            }
            
            val projectData = hashMapOf(
                "title" to title,
                "description" to description,
                "iconName" to iconName,
                "iconColor" to iconColor,
                "ownerId" to userId,
                "teamMemberIds" to members,
                "teamLeader" to teamLeaderMap,
                "teamMembers" to teamMembersList,
                "status" to "active",
                "dueDate" to dueDate,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            
            val docRef = db.collection(COLLECTION_PROJECTS)
                .add(projectData)
                .await()
            
            // Projeyi geri oku
            val projectDoc = docRef.get().await()
            val project = projectDoc.data?.let { data ->
                // teamMembers listesini parse et
                val teamMembersData = (data["teamMembers"] as? List<*>)?.mapNotNull { memberData ->
                    (memberData as? Map<*, *>)?.let { map ->
                        User(
                            uid = map["uid"] as? String ?: "",
                            email = map["email"] as? String,
                            displayName = map["displayName"] as? String,
                            photoUrl = map["photoUrl"] as? String,
                            createdAt = map["createdAt"] as? Long
                        )
                    }
                } ?: emptyList()
                
                Project(
                    id = docRef.id,
                    title = data["title"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    iconName = data["iconName"] as? String ?: "folder",
                    iconColor = data["iconColor"] as? String ?: "blue",
                    ownerId = data["ownerId"] as? String ?: "",
                    teamMemberIds = (data["teamMemberIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    teamLeader = teamLeaderMap?.let { map ->
                        User(
                            uid = map["uid"] as? String ?: "",
                            email = map["email"] as? String,
                            displayName = map["displayName"] as? String,
                            photoUrl = map["photoUrl"] as? String,
                            createdAt = map["createdAt"] as? Long
                        )
                    },
                    teamMembers = teamMembersData,
                    status = ProjectStatus.ACTIVE,
                    dueDate = data["dueDate"] as? String,
                    createdAt = (data["createdAt"] as? Long) ?: 0L,
                    updatedAt = (data["updatedAt"] as? Long) ?: 0L
                )
            } ?: return Result.failure(Exception("Proje oluşturulamadı"))
            
            println("✅ Proje başarıyla oluşturuldu:")
            println("   Proje ID: ${project.id}")
            println("   TeamMemberIds: ${project.teamMemberIds}")
            println("   OwnerID: ${project.ownerId}")
            
            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Projeyi günceller
     */
    suspend fun updateProject(
        projectId: String,
        title: String? = null,
        description: String? = null,
        iconName: String? = null,
        iconColor: String? = null,
        status: String? = null,
        dueDate: String? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to System.currentTimeMillis()
            )
            
            title?.let { updates["title"] = it }
            description?.let { updates["description"] = it }
            iconName?.let { updates["iconName"] = it }
            iconColor?.let { updates["iconColor"] = it }
            status?.let { updates["status"] = it }
            dueDate?.let { updates["dueDate"] = it }
            
            db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Projeyi siler - Sadece proje sahibi silebilir
     */
    suspend fun deleteProject(projectId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                println("❌ Kullanıcı oturumu bulunamadı")
                return Result.failure(Exception("Lütfen önce giriş yapın"))
            }
            
            val currentUserId = currentUser.uid
            println("🔍 Silme işlemi başlatıldı")
            println("👤 Kullanıcı ID: $currentUserId")
            println("📦 Proje ID: $projectId")
            
            // Önce projeyi kontrol et
            val projectDoc = db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .get()
                .await()
            
            if (!projectDoc.exists()) {
                println("❌ Proje bulunamadı: $projectId")
                return Result.failure(Exception("Proje bulunamadı"))
            }
            
            val projectData = projectDoc.data
            println("📋 Proje verisi: $projectData")
            
            val projectOwnerId = projectDoc.getString("ownerId") ?: projectDoc.getString("userId")
            println("👑 Proje sahibi ID: $projectOwnerId")
            println("🔍 Owner kontrolü: projectOwnerId=$projectOwnerId, currentUserId=$currentUserId")
            
            if (projectOwnerId == null || projectOwnerId.isEmpty()) {
                println("⚠️ Proje sahibi bulunamadı, silme işlemine devam ediliyor...")
            } else if (projectOwnerId != currentUserId) {
                println("⛔ Yetki hatası: Kullanıcı proje sahibi değil")
                return Result.failure(Exception("Bu projeyi silme yetkiniz yok. Sadece proje sahibi silebilir."))
            }
            
            println("✅ Yetki kontrolü başarılı, silme işlemine devam ediliyor...")
            
            // Önce projeye ait tüm görevleri sil
            try {
                val tasks = db.collection(COLLECTION_TASKS)
                    .whereEqualTo("projectId", projectId)
                    .get()
                    .await()
                
                println("🗑️ ${tasks.documents.size} görev bulundu, siliniyor...")
                
                val batch = db.batch()
                tasks.documents.forEach { taskDoc ->
                    batch.delete(taskDoc.reference)
                }
                
                if (tasks.documents.isNotEmpty()) {
                    batch.commit().await()
                    println("✅ Tüm görevler silindi")
                }
            } catch (e: Exception) {
                println("⚠️ Görevler silinirken hata: ${e.message}")
                // Görev silme hatası projeyi silmeyi engellemez
            }
            
            // Proje silme işlemini dene
            println("🗑️ Proje silme işlemi başlıyor...")
            try {
                db.collection(COLLECTION_PROJECTS)
                    .document(projectId)
                    .delete()
                    .await()
                
                println("✅ Proje başarıyla silindi: $projectId")
                Result.success(Unit)
            } catch (deleteException: Exception) {
                println("❌ Proje silme hatası: ${deleteException::class.simpleName} - ${deleteException.message}")
                deleteException.printStackTrace()
                
                // Özel hata mesajları
                when {
                    deleteException.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true -> {
                        throw Exception("Yetki hatası: Bu projeyi silme izniniz yok. Firestore güvenlik kuralları güncellenmeli.")
                    }
                    deleteException.message?.contains("NOT_FOUND", ignoreCase = true) == true -> {
                        throw Exception("Proje zaten silinmiş veya bulunamıyor.")
                    }
                    deleteException.message?.contains("FAILED_PRECONDITION", ignoreCase = true) == true -> {
                        throw Exception("Silme işlemi başarısız: Önce görevlerin silinmesi gerekiyor.")
                    }
                    else -> {
                        throw Exception("Proje silme hatası: ${deleteException.message}")
                    }
                }
            }
        } catch (e: Exception) {
            val errorMsg = "❌ Proje silme hatası: ${e::class.simpleName} - ${e.message}"
            println(errorMsg)
            e.printStackTrace()
            
            // Firebase permission hatası için özel mesaj
            if (e.message?.contains("PERMISSION_DENIED") == true) {
                return Result.failure(Exception("Firebase erişim izni reddedildi. Lütfen Firebase Console'dan Firestore güvenlik kurallarını güncelleyin."))
            }
            
            Result.failure(Exception(e.message ?: "Proje silinirken beklenmeyen bir hata oluştu"))
        }
    }
    
    // ==================== TASK OPERATIONS ====================
    
    /**
     * Tüm projelerin görev istatistiklerini günceller (Migration için)
     */
    suspend fun updateAllProjectStats(): Result<Unit> {
        return try {
            val userId = getCurrentUserId() 
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            println("🔄 Tüm projelerin istatistikleri güncelleniyor...")
            
            // Kullanıcının tüm projelerini al
            val projectsSnapshot = db.collection(COLLECTION_PROJECTS)
                .whereArrayContains("teamMemberIds", userId)
                .get()
                .await()
            
            var updatedCount = 0
            
            for (projectDoc in projectsSnapshot.documents) {
                val projectId = projectDoc.id
                updateProjectTaskStats(projectId)
                updatedCount++
            }
            
            println("✅ $updatedCount proje istatistiği güncellendi")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Proje istatistikleri güncellenirken hata: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Projenin görev istatistiklerini günceller
     */
    private suspend fun updateProjectTaskStats(projectId: String) {
        try {
            // Projeye ait tüm görevleri al
            val tasksSnapshot = db.collection(COLLECTION_TASKS)
                .whereEqualTo("projectId", projectId)
                .get()
                .await()
            
            val totalTasks = tasksSnapshot.documents.size
            val completedTasks = tasksSnapshot.documents.count { doc ->
                val status = doc.getString("status")
                status == "done" || status == "completed"
            }
            
            println("📊 Proje istatistikleri güncelleniyor: projectId=$projectId, total=$totalTasks, completed=$completedTasks")
            
            // Proje istatistiklerini güncelle
            db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .update(mapOf(
                    "tasksCount" to totalTasks,
                    "completedTasksCount" to completedTasks,
                    "updatedAt" to System.currentTimeMillis()
                ))
                .await()
            
            println("✅ Proje istatistikleri güncellendi")
        } catch (e: Exception) {
            println("⚠️ Proje istatistikleri güncellenirken hata: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Kullanıcının tüm görevlerini getirir
     */
    suspend fun getAllUserTasks(): Result<List<Task>> {
        return try {
            val userId = getCurrentUserId() 
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            val snapshot = db.collection(COLLECTION_TASKS)
                .whereEqualTo("assigneeId", userId)
                .get()
                .await()
            
            // Client-side'da sıralama yap - Manuel mapping
            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    Task(
                        id = doc.id,
                        projectId = data["projectId"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        status = when (data["status"] as? String) {
                            "inProgress", "in_progress" -> tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.IN_PROGRESS
                            "done", "completed" -> tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.COMPLETED
                            else -> tr.edu.bilimankara20307006.taskflow.data.model.TaskStatus.TODO
                        },
                        priority = data["priority"] as? String ?: "medium",
                        assigneeId = data["assigneeId"] as? String ?: "",
                        creatorId = data["creatorId"] as? String ?: "",
                        dueDate = data["dueDate"] as? String,
                        createdAt = (data["createdAt"] as? Long) ?: 0L,
                        updatedAt = (data["updatedAt"] as? Long) ?: 0L
                    )
                } catch (e: Exception) {
                    println("❌ Görev parse hatası: ${e.message}")
                    null
                }
            }.sortedByDescending { it.createdAt }
            
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Yeni görev oluşturur
     */
    suspend fun createTask(
        projectId: String,
        title: String,
        description: String,
        status: String = "todo",
        priority: String = "medium",
        assigneeId: String? = null,
        dueDate: String? = null
    ): Result<Task> {
        return try {
            val userId = getCurrentUserId() 
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            val firestoreTask = FirestoreTask(
                projectId = projectId,
                title = title,
                description = description,
                status = status,
                priority = priority,
                assigneeId = assigneeId ?: userId,
                creatorId = userId,
                dueDate = dueDate,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            val docRef = db.collection(COLLECTION_TASKS)
                .add(firestoreTask)
                .await()
            
            // Proje istatistiklerini güncelle
            updateProjectTaskStats(projectId)
            
            val task = firestoreTask.toTask(docRef.id)
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Görevi günceller
     */
    suspend fun updateTask(
        taskId: String,
        title: String? = null,
        description: String? = null,
        status: String? = null,
        priority: String? = null,
        assigneeId: String? = null,
        dueDate: String? = null
    ): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
            
            // Önce task'ın bilgilerini al
            val taskDoc = db.collection(COLLECTION_TASKS)
                .document(taskId)
                .get()
                .await()
            
            val projectId = taskDoc.getString("projectId")
            val taskCreatorId = taskDoc.getString("creatorId")
            val taskAssigneeId = taskDoc.getString("assigneeId")
            
            // Yetki kontrolü: Sadece oluşturan veya atanan kişi güncelleyebilir
            if (currentUserId != taskCreatorId && currentUserId != taskAssigneeId) {
                return Result.failure(Exception("Bu görevi güncelleme yetkiniz yok"))
            }
            
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to System.currentTimeMillis()
            )
            
            title?.let { updates["title"] = it }
            description?.let { updates["description"] = it }
            status?.let { updates["status"] = it }
            priority?.let { updates["priority"] = it }
            assigneeId?.let { updates["assigneeId"] = it }
            dueDate?.let { updates["dueDate"] = it }
            
            db.collection(COLLECTION_TASKS)
                .document(taskId)
                .update(updates)
                .await()
            
            // Durum değiştiyse proje istatistiklerini güncelle
            if (status != null && projectId != null) {
                updateProjectTaskStats(projectId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Görev durumunu değiştirir
     */
    suspend fun toggleTaskStatus(taskId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
            
            val doc = db.collection(COLLECTION_TASKS)
                .document(taskId)
                .get()
                .await()
            
            val projectId = doc.getString("projectId")
            val taskCreatorId = doc.getString("creatorId")
            val taskAssigneeId = doc.getString("assigneeId")
            val currentStatus = doc.getString("status") ?: "todo"
            
            // Yetki kontrolü: Sadece oluşturan veya atanan kişi durumu değiştirebilir
            if (currentUserId != taskCreatorId && currentUserId != taskAssigneeId) {
                return Result.failure(Exception("Bu görevin durumunu değiştirme yetkiniz yok"))
            }
            
            val newStatus = when (currentStatus) {
                "todo" -> "inProgress"
                "inProgress" -> "done"
                "done" -> "todo"
                else -> "todo"
            }
            
            db.collection(COLLECTION_TASKS)
                .document(taskId)
                .update(mapOf(
                    "status" to newStatus,
                    "updatedAt" to System.currentTimeMillis()
                ))
                .await()
            
            // Proje istatistiklerini güncelle
            if (projectId != null) {
                updateProjectTaskStats(projectId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Görevi siler
     */
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            // Önce task'ın projectId'sini al
            val taskDoc = db.collection(COLLECTION_TASKS)
                .document(taskId)
                .get()
                .await()
            
            val projectId = taskDoc.getString("projectId")
            
            db.collection(COLLECTION_TASKS)
                .document(taskId)
                .delete()
                .await()
            
            // Proje istatistiklerini güncelle
            if (projectId != null) {
                updateProjectTaskStats(projectId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== USER MANAGEMENT (iOS ProjectManager.swift) ====================

    /**
     * Email ile kullanıcı ara - iOS searchUserByEmail
     */
    suspend fun searchUserByEmail(email: String): Result<User?> {
        return try {
            println("🔍 Firestore'da kullanıcı aranıyor: ${email.lowercase()}")
            
            val snapshot = db.collection(COLLECTION_USERS)
                .whereEqualTo("email", email.lowercase())
                .limit(1)
                .get()
                .await()
            
            println("📊 Bulunan döküman sayısı: ${snapshot.documents.size}")
            
            if (snapshot.documents.isEmpty()) {
                println("⚠️ Firestore'da kullanıcı bulunamadı")
                return Result.success(null)
            }
            
            val document = snapshot.documents.first()
            println("✅ Firestore'da kullanıcı bulundu: ${document.id}")
            
            // Manuel mapping yaparak Timestamp sorununu önle
            val user = User(
                uid = document.getString("uid") ?: document.id,
                email = document.getString("email"),
                displayName = document.getString("displayName"),
                photoUrl = document.getString("photoUrl"),
                createdAt = try {
                    val timestamp = document.getTimestamp("createdAt")
                    timestamp?.toDate()?.time
                } catch (e: Exception) {
                    null
                }
            )
            Result.success(user)
        } catch (e: Exception) {
            println("❌ Kullanıcı arama hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Projeye ekip üyesi ekle - iOS ile uyumlu davet sistemi
     * Artık direkt ekleme yapmaz, davet bildirimi gönderir
     */
    suspend fun addTeamMember(userId: String, projectId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId()
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            println("📤 Davet gönderiliyor - Proje: $projectId, Davet edilecek User ID: $userId")
            
            val projectRef = db.collection(COLLECTION_PROJECTS).document(projectId)
            val projectDoc = projectRef.get().await()
            
            if (!projectDoc.exists()) {
                println("❌ Proje bulunamadı")
                return Result.failure(Exception("Proje bulunamadı"))
            }
            
            val projectData = projectDoc.data ?: return Result.failure(Exception("Proje verisi okunamadı"))
            
            // Mevcut ekip üyelerini kontrol et
            val teamMemberIds = (projectData["teamMemberIds"] as? List<*>)
                ?.mapNotNull { it as? String } ?: emptyList()
            
            println("📋 Mevcut ekip üyeleri: $teamMemberIds")
            
            if (teamMemberIds.contains(userId)) {
                println("⚠️ Kullanıcı zaten ekip üyesi")
                return Result.failure(Exception("Kullanıcı zaten ekip üyesi"))
            }
            
            // Proje lideri mi kontrol et
            val ownerId = projectData["ownerId"] as? String
            if (ownerId == userId) {
                println("⚠️ Kullanıcı zaten proje lideri")
                return Result.failure(Exception("Kullanıcı zaten proje lideri"))
            }
            
            // Kullanıcı bilgisini al ve var olup olmadığını kontrol et
            println("📡 Firestore'dan kullanıcı bilgisi alınıyor: $userId")
            val userDoc = db.collection(COLLECTION_USERS).document(userId).get().await()
            
            if (!userDoc.exists()) {
                println("❌ Kullanıcı Firestore'da yok!")
                return Result.failure(Exception("Bu kullanıcı sistemde kayıtlı değil. Lütfen kullanıcının uygulamaya giriş yapması gerekiyor."))
            }
            
            // Proje daveti bildirimi gönder (direkt ekleme yapmaz)
            try {
                val projectName = projectData["title"] as? String ?: "Proje"
                
                tr.edu.bilimankara20307006.taskflow.data.manager.NotificationManager.sendProjectInvitation(
                    toUserId = userId,
                    projectId = projectId,
                    projectName = projectName
                )
                println("📧 Proje daveti bildirimi gönderildi - kullanıcının onayı bekleniyor")
            } catch (e: Exception) {
                println("⚠️ Bildirim gönderme hatası: ${e.message}")
                return Result.failure(Exception("Bildirim gönderilemedi"))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Davet gönderme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Projeye ekip üyesi direkt ekle - sadece davet kabul edildiğinde çağrılır
     */
    suspend fun addTeamMemberDirectly(userId: String, projectId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId()
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            println("✅ Davet kabul edildi - Kullanıcı projeye ekleniyor: $userId")
            
            val projectRef = db.collection(COLLECTION_PROJECTS).document(projectId)
            val projectDoc = projectRef.get().await()
            
            if (!projectDoc.exists()) {
                println("❌ Proje bulunamadı")
                return Result.failure(Exception("Proje bulunamadı"))
            }
            
            val projectData = projectDoc.data ?: return Result.failure(Exception("Proje verisi okunamadı"))
            
            // Kullanıcı bilgisini al
            val userDoc = db.collection(COLLECTION_USERS).document(userId).get().await()
            
            if (!userDoc.exists()) {
                println("❌ Kullanıcı bulunamadı")
                return Result.failure(Exception("Kullanıcı bulunamadı"))
            }
            
            // Manuel mapping yaparak Timestamp sorununu önle
            val user = User(
                uid = userDoc.getString("uid") ?: userDoc.id,
                email = userDoc.getString("email"),
                displayName = userDoc.getString("displayName"),
                photoUrl = userDoc.getString("photoUrl"),
                createdAt = try {
                    val timestamp = userDoc.getTimestamp("createdAt")
                    timestamp?.toDate()?.time
                } catch (e: Exception) {
                    null
                }
            )
            
            println("✅ Kullanıcı bilgisi alındı: ${user.displayName ?: user.email ?: "Unknown"}")
            
            // Mevcut teamMembers listesini al
            val teamMembers = (projectData["teamMembers"] as? List<*>)
                ?.mapNotNull { data ->
                    @Suppress("UNCHECKED_CAST")
                    val map = data as? Map<String, Any?> ?: return@mapNotNull null
                    User(
                        uid = map["uid"] as? String ?: "",
                        displayName = map["displayName"] as? String,
                        email = map["email"] as? String,
                        photoUrl = map["photoUrl"] as? String,
                        createdAt = (map["createdAt"] as? Number)?.toLong()
                    )
                }?.toMutableList() ?: mutableListOf()
            
            // Yeni kullanıcıyı ekle - HashMap olarak kaydet
            val userMap = hashMapOf(
                "uid" to user.uid,
                "email" to user.email,
                "displayName" to user.displayName,
                "photoUrl" to user.photoUrl,
                "createdAt" to user.createdAt
            )
            
            // Mevcut teamMembers'ı HashMap listesine dönüştür
            val teamMembersMapList = teamMembers.map { member ->
                hashMapOf(
                    "uid" to member.uid,
                    "email" to member.email,
                    "displayName" to member.displayName,
                    "photoUrl" to member.photoUrl,
                    "createdAt" to member.createdAt
                )
            }.toMutableList()
            
            // Yeni üyeyi ekle
            teamMembersMapList.add(userMap)
            val teamMemberIds = (projectData["teamMemberIds"] as? List<*>)
                ?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
            teamMemberIds.add(userId)
            
            // Firebase'e kaydet
            projectRef.update(
                mapOf(
                    "teamMembers" to teamMembersMapList,
                    "teamMemberIds" to teamMemberIds,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            
            println("✅ Kullanıcı başarıyla projeye eklendi: ${user.displayName ?: user.email ?: "Unknown"}")
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Kullanıcı ekleme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Projeden ekip üyesi çıkar - iOS removeTeamMember
     */
    suspend fun removeTeamMember(userId: String, projectId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId()
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            val projectRef = db.collection(COLLECTION_PROJECTS).document(projectId)
            val projectDoc = projectRef.get().await()
            
            if (!projectDoc.exists()) {
                return Result.failure(Exception("Proje bulunamadı"))
            }
            
            val projectData = projectDoc.data ?: return Result.failure(Exception("Proje verisi okunamadı"))
            
            // Proje sahibi mi kontrol et veya kullanıcı kendini mi çıkarıyor
            val ownerId = projectData["ownerId"] as? String
            val isOwnerRemovingSomeone = ownerId == currentUserId && userId != currentUserId
            val isUserLeavingProject = userId == currentUserId
            
            if (!isOwnerRemovingSomeone && !isUserLeavingProject) {
                return Result.failure(Exception("Bu işlem için yetkiniz yok. Sadece proje sahibi başkalarını çıkarabilir veya kendinizi çıkarabilirsiniz."))
            }
            
            println("🔍 Çıkarma işlemi: isOwnerRemovingSomeone=$isOwnerRemovingSomeone, isUserLeavingProject=$isUserLeavingProject")
            
            // Mevcut teamMembers ve teamMemberIds
            val teamMembers = (projectData["teamMembers"] as? List<*>)
                ?.mapNotNull { data ->
                    @Suppress("UNCHECKED_CAST")
                    val map = data as? Map<String, Any?> ?: return@mapNotNull null
                    User(
                        uid = map["uid"] as? String ?: "",
                        displayName = map["displayName"] as? String,
                        email = map["email"] as? String,
                        photoUrl = map["photoUrl"] as? String,
                        createdAt = (map["createdAt"] as? Number)?.toLong()
                    )
                }?.toMutableList() ?: mutableListOf()
            
            val teamMemberIds = (projectData["teamMemberIds"] as? List<*>)
                ?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
            
            // Yeni: members array'inden de çıkar (rol bazlı sistem için)
            val members = (projectData["members"] as? List<*>)
                ?.mapNotNull { data ->
                    @Suppress("UNCHECKED_CAST")
                    val map = data as? Map<String, Any?> ?: return@mapNotNull null
                    tr.edu.bilimankara20307006.taskflow.data.model.ProjectMember.fromMap(
                        map.mapKeys { it.key.toString() }.mapValues { it.value ?: "" }
                    )
                }?.toMutableList() ?: mutableListOf()
            
            // Kullanıcıyı tüm listelerden çıkar
            teamMembers.removeAll { it.uid == userId }
            teamMemberIds.remove(userId)
            members.removeAll { it.user.uid == userId }
            
            println("🗑️ Çıkarılıyor: userId=$userId, teamMembers=${teamMembers.size}, teamMemberIds=${teamMemberIds.size}, members=${members.size}")
            
            // Firebase'e kaydet
            projectRef.update(
                mapOf(
                    "teamMembers" to teamMembers,
                    "teamMemberIds" to teamMemberIds,
                    "members" to members.map { it.toMap() }, // Yeni: members array'i de güncelle
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            
            println("✅ Ekip üyesi çıkarıldı")
            
            // Bildirim gönder - eğer kullanıcı kendini çıkarıyorsa (projeden ayrılıyorsa)
            try {
                if (userId == currentUserId) { // Kendini çıkarıyor (projeden ayrılıyor)
                    val projectData = projectDoc.data
                    val projectName = projectData?.get("title") as? String ?: "Proje"
                    val ownerId = projectData?.get("ownerId") as? String
                    
                    // Kullanıcı adını Firestore'dan al
                    val memberName = try {
                        val userDoc = db.collection("users")
                            .document(currentUserId)
                            .get()
                            .await()
                        userDoc.getString("fullName") ?: userDoc.getString("email") ?: auth.currentUser?.displayName ?: "Bilinmeyen Kullanıcı"
                    } catch (e: Exception) {
                        auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Bilinmeyen Kullanıcı"
                    }
                    
                    if (ownerId != null && ownerId != currentUserId) {
                        tr.edu.bilimankara20307006.taskflow.data.manager.NotificationManager.sendProjectMemberLeft(
                            toUserId = ownerId,
                            projectId = projectId,
                            projectName = projectName,
                            memberName = memberName
                        )
                        println("📧 Projeden ayrılma bildirimi gönderildi")
                    }
                }
            } catch (e: Exception) {
                println("⚠️ Bildirim gönderme hatası (göz ardı edildi): ${e.message}")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Ekip üyesi çıkarma hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Yeni kullanıcıyı Firestore'a kaydet - iOS'taki gibi
     */
    suspend fun saveUserToFirestore(user: User): Result<Unit> {
        return try {
            // Firestore için map oluştur (Timestamp sorununu önlemek için)
            val userMap = hashMapOf(
                "uid" to user.uid,
                "email" to (user.email ?: ""),
                "displayName" to (user.displayName ?: ""),
                "photoUrl" to (user.photoUrl ?: ""),
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            
            db.collection(COLLECTION_USERS)
                .document(user.uid)
                .set(userMap)
                .await()
            
            println("✅ Kullanıcı Firestore'a kaydedildi: ${user.email}")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Kullanıcı kaydetme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    // ==================== NOTIFICATION SETTINGS ====================
    
    /**
     * Kullanıcının bildirim ayarlarını kaydet
     */
    suspend fun saveNotificationSettings(settings: tr.edu.bilimankara20307006.taskflow.data.model.NotificationSettings): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            db.collection(COLLECTION_USERS)
                .document(userId)
                .update("notificationSettings", settings.toMap())
                .await()
            
            println("✅ Bildirim ayarları kaydedildi")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Bildirim ayarları kaydetme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Kullanıcının bildirim ayarlarını oku
     */
    suspend fun getNotificationSettings(): Result<tr.edu.bilimankara20307006.taskflow.data.model.NotificationSettings> {
        return try {
            val userId = getCurrentUserId()
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            val doc = db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            
            if (!doc.exists()) {
                return Result.success(tr.edu.bilimankara20307006.taskflow.data.model.NotificationSettings())
            }
            
            val settingsMap = doc.get("notificationSettings") as? Map<String, Any>
            val settings = if (settingsMap != null) {
                tr.edu.bilimankara20307006.taskflow.data.model.NotificationSettings.fromMap(settingsMap)
            } else {
                tr.edu.bilimankara20307006.taskflow.data.model.NotificationSettings()
            }
            
            Result.success(settings)
        } catch (e: Exception) {
            println("❌ Bildirim ayarları okuma hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    // ==================== ROLE MANAGEMENT ====================
    
    /**
     * Projeye yeni üye ekler (rol ile)
     */
    suspend fun addProjectMember(
        projectId: String,
        userId: String,
        newMemberId: String,
        role: tr.edu.bilimankara20307006.taskflow.data.model.ProjectRole = tr.edu.bilimankara20307006.taskflow.data.model.ProjectRole.MEMBER
    ): Result<Unit> {
        return try {
            // Yetki kontrolü
            val project = getProjectById(projectId).getOrNull()
                ?: return Result.failure(Exception("Proje bulunamadı"))
            
            if (!project.canUserManageMembers(userId)) {
                return Result.failure(Exception("Bu işlem için yetkiniz yok"))
            }
            
            // Yeni üyenin bilgilerini al
            val memberDoc = db.collection(COLLECTION_USERS).document(newMemberId).get().await()
            if (!memberDoc.exists()) {
                return Result.failure(Exception("Kullanıcı bulunamadı"))
            }
            
            val memberData = hashMapOf(
                "userId" to newMemberId,
                "displayName" to memberDoc.getString("displayName"),
                "email" to memberDoc.getString("email"),
                "role" to role.name.lowercase(),
                "addedAt" to System.currentTimeMillis()
            )
            
            // Firestore'a ekle
            db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .update(
                    "members", com.google.firebase.firestore.FieldValue.arrayUnion(memberData),
                    "updatedAt", System.currentTimeMillis()
                )
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Projeden üye çıkarır (rol ile)
     */
    suspend fun removeProjectMember(
        projectId: String,
        userId: String,
        memberIdToRemove: String
    ): Result<Unit> {
        return try {
            // Yetki kontrolü
            val project = getProjectById(projectId).getOrNull()
                ?: return Result.failure(Exception("Proje bulunamadı"))
            
            if (!project.canUserManageMembers(userId)) {
                return Result.failure(Exception("Bu işlem için yetkiniz yok"))
            }
            
            // Owner çıkarılamaz
            if (project.ownerId == memberIdToRemove) {
                return Result.failure(Exception("Proje sahibi çıkarılamaz"))
            }
            
            // Üyeyi bul ve çıkar
            val updatedMembers = project.members.filter { it.user.uid != memberIdToRemove }
            val membersData = updatedMembers.map { it.toMap() }
            
            db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .update(
                    "members", membersData,
                    "updatedAt", System.currentTimeMillis()
                )
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Üyenin rolünü değiştirir
     */
    suspend fun updateMemberRole(
        projectId: String,
        userId: String,
        memberIdToUpdate: String,
        newRole: tr.edu.bilimankara20307006.taskflow.data.model.ProjectRole
    ): Result<Unit> {
        return try {
            // Sadece OWNER rol değiştirebilir
            val project = getProjectById(projectId).getOrNull()
                ?: return Result.failure(Exception("Proje bulunamadı"))
            
            if (!project.canUserEditSettings(userId)) {
                return Result.failure(Exception("Bu işlem için yetkiniz yok"))
            }
            
            // Owner'ın rolü değiştirilemez
            if (project.ownerId == memberIdToUpdate) {
                return Result.failure(Exception("Proje sahibinin rolü değiştirilemez"))
            }
            
            // Üyeyi bul ve güncelle
            val updatedMembers = project.members.map { member ->
                if (member.user.uid == memberIdToUpdate) {
                    member.copy(role = newRole)
                } else {
                    member
                }
            }
            val membersData = updatedMembers.map { it.toMap() }
            
            db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .update(
                    "members", membersData,
                    "updatedAt", System.currentTimeMillis()
                )
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Kullanıcının projedeki rolünü al
     */
    suspend fun getUserRoleInProject(
        projectId: String,
        userId: String
    ): Result<tr.edu.bilimankara20307006.taskflow.data.model.ProjectRole> {
        return try {
            val project = getProjectById(projectId).getOrNull()
                ?: return Result.failure(Exception("Proje bulunamadı"))
            
            Result.success(project.getUserRole(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Eski projeleri yeni formata günceller
     * teamLeader ve teamMembers alanlarını ekler
     */
    suspend fun migrateOldProjects(): Result<Int> {
        return try {
            val userId = getCurrentUserId()
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            println("🔧 Migration başlıyor...")
            
            // Kullanıcının tüm projelerini al
            val snapshot = db.collection(COLLECTION_PROJECTS)
                .whereArrayContains("teamMemberIds", userId)
                .get()
                .await()
            
            var migratedCount = 0
            
            snapshot.documents.forEach { doc ->
                try {
                    val data = doc.data ?: return@forEach
                    
                    // teamLeader yoksa veya teamMembers yoksa migration yap
                    val hasTeamLeader = data.containsKey("teamLeader") && data["teamLeader"] != null
                    val hasTeamMembers = data.containsKey("teamMembers")
                    
                    if (!hasTeamLeader || !hasTeamMembers) {
                        println("🔧 Migrating project: ${doc.id} - ${data["title"]}")
                        
                        val ownerId = data["ownerId"] as? String ?: return@forEach
                        
                        // Owner bilgilerini al
                        val ownerDoc = db.collection(COLLECTION_USERS)
                            .document(ownerId)
                            .get()
                            .await()
                        
                        val teamLeaderMap = if (ownerDoc.exists()) {
                            hashMapOf(
                                "uid" to ownerId,
                                "email" to ownerDoc.getString("email"),
                                "displayName" to ownerDoc.getString("displayName"),
                                "photoUrl" to ownerDoc.getString("photoUrl"),
                                "createdAt" to try {
                                    ownerDoc.getTimestamp("createdAt")?.toDate()?.time
                                } catch (e: Exception) {
                                    null
                                }
                            )
                        } else null
                        
                        // Mevcut teamMembers listesini kontrol et
                        val currentTeamMembers = data["teamMembers"] as? List<*>
                        val teamMembers = currentTeamMembers ?: emptyList<Map<String, Any?>>()
                        
                        // Projeyi güncelle
                        val updates = mutableMapOf<String, Any?>(
                            "updatedAt" to System.currentTimeMillis()
                        )
                        
                        if (!hasTeamLeader) {
                            updates["teamLeader"] = teamLeaderMap
                        }
                        
                        if (!hasTeamMembers) {
                            updates["teamMembers"] = teamMembers
                        }
                        
                        doc.reference.update(updates).await()
                        migratedCount++
                        println("✅ Migrated: ${data["title"]}")
                    }
                } catch (e: Exception) {
                    println("❌ Migration error for project ${doc.id}: ${e.message}")
                }
            }
            
            println("✅ Migration tamamlandı: $migratedCount proje güncellendi")
            Result.success(migratedCount)
        } catch (e: Exception) {
            println("❌ Migration hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    // ==================== COMMENT OPERATIONS ====================
    
    /**
     * Göreve yorum ekle
     * @param taskId Görev ID'si
     * @param message Yorum mesajı
     * @return Result<Comment> Başarılı ise yorum, başarısız ise hata
     */
    suspend fun addComment(taskId: String, message: String): Result<Comment> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
            
            // Kullanıcı bilgilerini al
            val userDoc = db.collection(COLLECTION_USERS)
                .document(currentUser.uid)
                .get()
                .await()
            
            val userName = userDoc.getString("displayName") 
                ?: userDoc.getString("display_name")
                ?: currentUser.displayName
                ?: currentUser.email
                ?: "Bilinmeyen Kullanıcı"
            
            val userAvatar = userDoc.getString("photoUrl")
                ?: userDoc.getString("photo_url")
                ?: currentUser.photoUrl?.toString()
            
            val comment = Comment(
                taskId = taskId,
                userId = currentUser.uid,
                userName = userName,
                userAvatar = userAvatar,
                message = message,
                timestamp = System.currentTimeMillis()
            )
            
            // Firestore'a kaydet
            db.collection(COLLECTION_COMMENTS)
                .document(comment.id)
                .set(comment.toMap())
                .await()
            
            println("✅ Yorum eklendi: ${comment.id}")
            Result.success(comment)
        } catch (e: Exception) {
            println("❌ Yorum ekleme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Görev yorumlarını getir (real-time)
     * @param taskId Görev ID'si
     * @param onComments Yorumlar güncellendiğinde çağrılacak callback
     * @return ListenerRegistration Listener'ı iptal etmek için
     */
    fun listenToComments(
        taskId: String,
        onComments: (List<Comment>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        return db.collection(COLLECTION_COMMENTS)
            .whereEqualTo("taskId", taskId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ Yorum dinleme hatası: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        Comment.fromMap(doc.data ?: emptyMap())
                    }
                    println("📝 ${comments.size} yorum alındı")
                    onComments(comments)
                }
            }
    }
    
    /**
     * Yorum sil
     * @param commentId Yorum ID'si
     * @return Result<Unit>
     */
    suspend fun deleteComment(commentId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
            
            // Yorumun sahibi mi kontrol et
            val commentDoc = db.collection(COLLECTION_COMMENTS)
                .document(commentId)
                .get()
                .await()
            
            val userId = commentDoc.getString("userId")
            if (userId != currentUser.uid) {
                return Result.failure(Exception("Bu yorumu silme yetkiniz yok"))
            }
            
            // Yorumu sil
            db.collection(COLLECTION_COMMENTS)
                .document(commentId)
                .delete()
                .await()
            
            println("✅ Yorum silindi: $commentId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Yorum silme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Göreve ait tüm yorumları getir (tek seferlik)
     * @param taskId Görev ID'si
     * @return Result<List<Comment>>
     */
    suspend fun getComments(taskId: String): Result<List<Comment>> {
        return try {
            val snapshot = db.collection(COLLECTION_COMMENTS)
                .whereEqualTo("taskId", taskId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()
            
            val comments = snapshot.documents.mapNotNull { doc ->
                Comment.fromMap(doc.data ?: emptyMap())
            }
            
            println("✅ ${comments.size} yorum getirildi")
            Result.success(comments)
        } catch (e: Exception) {
            println("❌ Yorum getirme hatası: ${e.message}")
            Result.failure(e)
        }
    }
}

// ==================== FIRESTORE DATA MODELS ====================

/**
 * Firestore'daki proje verisi için model
 */
data class FirestoreProject(
    val title: String = "",
    val description: String = "",
    val iconName: String = "folder",
    val iconColor: String = "blue",
    val ownerId: String = "",
    val owner_id: String = "",
    val teamMemberIds: List<String> = emptyList(),
    val team_member_ids: List<String> = emptyList(),
    val teamLeader: Map<String, Any?>? = null,
    val team_leader: Map<String, Any?>? = null,
    val teamMembers: List<Map<String, Any?>>? = null,
    val team_members: List<Map<String, Any?>>? = null,
    val status: String = "active",
    val dueDate: Any? = null,
    val due_date: Any? = null,
    val createdAt: Long = 0,
    val created_at: Long = 0,
    val updatedAt: Long = 0,
    val updated_at: Long = 0
) {
    fun toProject(id: String): Project {
        // iOS hem camelCase hem snake_case kullanabilir, her ikisini de dene
        val actualOwnerId = ownerId.ifEmpty { owner_id }
        val actualTeamMemberIds = if (teamMemberIds.isNotEmpty()) teamMemberIds else team_member_ids
        val actualTeamLeader = teamLeader ?: team_leader
        val actualTeamMembers = teamMembers ?: team_members
        val actualDueDate = dueDate ?: due_date
        val actualCreatedAt = if (createdAt > 0) createdAt else created_at
        val actualUpdatedAt = if (updatedAt > 0) updatedAt else updated_at
        
        // Debug log
        println("🔍 toProject - Project ID: $id, Title: $title")
        println("📊 teamLeader: $actualTeamLeader")
        println("📊 teamMembers: $actualTeamMembers")
        println("📊 teamMemberIds: $actualTeamMemberIds")
        
        // Parse teamLeader
        val leader = actualTeamLeader?.let { map ->
            println("👤 TeamLeader parsing: $map")
            try {
                User(
                    uid = map["uid"] as? String ?: map["user_id"] as? String ?: "",
                    displayName = map["displayName"] as? String ?: map["display_name"] as? String,
                    email = map["email"] as? String,
                    photoUrl = map["photoUrl"] as? String ?: map["photo_url"] as? String,
                    createdAt = (map["createdAt"] as? Number)?.toLong() ?: (map["created_at"] as? Number)?.toLong()
                )
            } catch (e: Exception) {
                println("❌ TeamLeader parse error: ${e.message}")
                null
            }
        }
        
        // Parse teamMembers
        val members = actualTeamMembers?.mapNotNull { map ->
            try {
                println("👥 TeamMember parsing: $map")
                User(
                    uid = map["uid"] as? String ?: map["user_id"] as? String ?: "",
                    displayName = map["displayName"] as? String ?: map["display_name"] as? String,
                    email = map["email"] as? String,
                    photoUrl = map["photoUrl"] as? String ?: map["photo_url"] as? String,
                    createdAt = (map["createdAt"] as? Number)?.toLong() ?: (map["created_at"] as? Number)?.toLong()
                )
            } catch (e: Exception) {
                println("❌ TeamMember parse error: ${e.message}")
                null
            }
        } ?: emptyList()
        
        println("✅ Parsed teamLeader: ${leader?.email}")
        println("✅ Parsed teamMembers count: ${members.size}")
        
        // Parse dueDate - iOS'tan Timestamp geliyorsa Date'e çevir
        val formattedDueDate = when (actualDueDate) {
            is com.google.firebase.Timestamp -> {
                val sdf = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("tr"))
                sdf.format(actualDueDate.toDate())
            }
            is Long -> {
                val sdf = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("tr"))
                sdf.format(java.util.Date(actualDueDate))
            }
            is String -> actualDueDate
            else -> null
        }
        
        println("📅 DueDate formatted: $formattedDueDate")
        
        return Project(
            id = id,
            title = title,
            description = description,
            iconName = iconName,
            iconColor = iconColor,
            ownerId = actualOwnerId,
            teamMemberIds = actualTeamMemberIds,
            teamLeader = leader,
            teamMembers = members,
            status = when (status) {
                "completed" -> ProjectStatus.COMPLETED
                "archived" -> ProjectStatus.ARCHIVED
                else -> ProjectStatus.ACTIVE
            },
            dueDate = formattedDueDate,
            createdAt = actualCreatedAt,
            updatedAt = actualUpdatedAt
        )
    }
}

/**
 * Firestore'daki görev verisi için model
 */
data class FirestoreTask(
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "todo",
    val priority: String = "medium",
    val assigneeId: String = "",
    val creatorId: String = "",
    val dueDate: Any? = null, // Timestamp veya String olabilir
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val completedAt: Long? = null
) {
    fun toTask(id: String): Task {
        return Task(
            id = id,
            projectId = projectId,
            title = title,
            description = description,
            status = when (status) {
                "inProgress", "in_progress" -> TaskStatus.IN_PROGRESS
                "done", "completed" -> TaskStatus.COMPLETED
                else -> TaskStatus.TODO
            },
            priority = priority,
            assigneeId = assigneeId,
            creatorId = creatorId,
            dueDate = dueDate?.toString(),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
