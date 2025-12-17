package tr.edu.bilimankara20307006.taskflow.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Proje Durumu
 */
enum class ProjectStatus {
    ACTIVE,      // Aktif
    COMPLETED,   // Tamamlandı
    ARCHIVED     // Arşivlenmiş
}

/**
 * Proje Data Modeli - iOS Project.swift ile aynı yapı
 * Rol bazlı yetkilendirme sistemi ile
 */
data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val iconName: String = "folder",
    val iconColor: String = "blue",
    val ownerId: String = "",
    val teamMemberIds: List<String> = emptyList(),
    val teamLeader: User? = null,
    val teamMembers: List<User> = emptyList(),
    val members: List<ProjectMember> = emptyList(), // Yeni: Rol bazlı üyeler
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    val dueDate: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Eski alanlar (geriye dönük uyumluluk için)
    val createdDate: Date = Date(createdAt),
    val isCompleted: Boolean = (status == ProjectStatus.COMPLETED),
    val tasksCount: Int = 0,
    val completedTasksCount: Int = 0
) {
    /**
     * Son teslim tarihi formatlanmış
     */
    val formattedDueDate: String
        get() = dueDate ?: ""
    
    /**
     * Proje ilerleme yüzdesi
     */
    val progressPercentage: Double
        get() = if (tasksCount > 0) {
            completedTasksCount.toDouble() / tasksCount.toDouble()
        } else {
            0.0
        }
    
    /**
     * Kullanıcının bu projedeki rolünü al
     */
    fun getUserRole(userId: String): ProjectRole {
        return when {
            ownerId == userId -> ProjectRole.OWNER
            else -> members.find { it.user.uid == userId }?.role ?: ProjectRole.MEMBER
        }
    }
    
    /**
     * Kullanıcının üye ekleme/çıkarma yetkisi var mı?
     */
    fun canUserManageMembers(userId: String): Boolean {
        return getUserRole(userId).canManageMembers()
    }
    
    /**
     * Kullanıcının proje ayarlarını değiştirme yetkisi var mı?
     */
    fun canUserEditSettings(userId: String): Boolean {
        return getUserRole(userId).canEditProjectSettings()
    }
    
    /**
     * Kullanıcının görev yönetimi yetkisi var mı?
     */
    fun canUserManageTasks(userId: String): Boolean {
        return getUserRole(userId).canManageTasks()
    }
    
    companion object {
        /**
         * Örnek projeler - Demo amaçlı
         */
        val sampleProjects = listOf(
            Project(
                title = "Proje 1: Web Sitesi Tasarımı",
                description = "Web sitesi tasarımı ve geliştirme",
                iconName = "list",
                iconColor = "green",
                status = ProjectStatus.ACTIVE,
                tasksCount = 15,
                completedTasksCount = 0
            ),
            Project(
                title = "Proje 2: Mobil Uygulama Geliştirme",
                description = "Android ve iOS uygulaması geliştirme",
                iconName = "list",
                iconColor = "green",
                status = ProjectStatus.ACTIVE,
                tasksCount = 12,
                completedTasksCount = 6
            ),
            Project(
                title = "Proje 3: Pazarlama Kampanyası",
                description = "Dijital pazarlama stratejisi ve kampanya yönetimi",
                iconName = "list",
                iconColor = "orange",
                status = ProjectStatus.COMPLETED,
                tasksCount = 8,
                completedTasksCount = 8
            )
        )
    }
}
