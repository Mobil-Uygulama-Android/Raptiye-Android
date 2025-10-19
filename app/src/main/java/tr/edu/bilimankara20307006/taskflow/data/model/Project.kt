package tr.edu.bilimankara20307006.taskflow.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Proje Durumu
 */
enum class ProjectStatus {
    TODO,        // Yapılacaklar
    IN_PROGRESS, // Devam Ediyor
    COMPLETED    // Tamamlandı
}

/**
 * Proje Data Modeli - iOS Project.swift ile aynı yapı
 */
data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val iconName: String = "folder",
    val iconColor: String = "blue",
    val createdDate: Date = Date(),
    val dueDate: Date? = null,
    val isCompleted: Boolean = false,
    val status: ProjectStatus = ProjectStatus.TODO,
    val tasksCount: Int = 0,
    val completedTasksCount: Int = 0
) {
    /**
     * Son teslim tarihi formatlanmış
     */
    val formattedDueDate: String
        get() {
            if (dueDate == null) return ""
            val formatter = SimpleDateFormat("dd MMMM", Locale("tr", "TR"))
            return "Son teslim: ${formatter.format(dueDate)}"
        }
    /**
     * Proje ilerleme yüzdesi
     */
    val progressPercentage: Double
        get() = if (tasksCount > 0) {
            completedTasksCount.toDouble() / tasksCount.toDouble()
        } else {
            0.0
        }
    
    companion object {
        /**
         * Örnek projeler - iOS'taki sampleProjects ile aynı
         */
        val sampleProjects = listOf(
            Project(
                title = "Proje 1: Web Sitesi Tasarımı",
                description = "Web sitesi tasarımı ve geliştirme",
                iconName = "list",
                iconColor = "green",
                dueDate = Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000), // 10 gün sonra
                status = ProjectStatus.TODO,
                tasksCount = 15,
                completedTasksCount = 0
            ),
            Project(
                title = "Proje 2: Mobil Uygulama Geliştirme",
                description = "Android ve iOS uygulaması geliştirme",
                iconName = "list",
                iconColor = "green",
                dueDate = Date(System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000), // 15 gün sonra
                status = ProjectStatus.IN_PROGRESS,
                tasksCount = 12,
                completedTasksCount = 6
            ),
            Project(
                title = "Proje 3: Pazarlama Kampanyası",
                description = "Dijital pazarlama stratejisi ve kampanya yönetimi",
                iconName = "list",
                iconColor = "orange",
                dueDate = Date(System.currentTimeMillis() + 20 * 24 * 60 * 60 * 1000), // 20 gün sonra
                status = ProjectStatus.COMPLETED,
                tasksCount = 8,
                completedTasksCount = 8,
                isCompleted = true
            ),
            Project(
                title = "Proje Yönetimi Uygulaması",
                description = "Proje yönetimi uygulamasının tasarımı ve geliştirme süreci",
                iconName = "phone_android",
                iconColor = "mint",
                status = ProjectStatus.IN_PROGRESS,
                tasksCount = 15,
                completedTasksCount = 8
            ),
            Project(
                title = "E-ticaret Uygulaması",
                description = "E-ticaret uygulamasının tasarımı",
                iconName = "shopping_cart",
                iconColor = "orange",
                status = ProjectStatus.TODO,
                tasksCount = 12,
                completedTasksCount = 5
            ),
            Project(
                title = "Sosyal Medya Uygulaması",
                description = "Sosyal medya uygulamasının tasarımı",
                iconName = "forum",
                iconColor = "green",
                status = ProjectStatus.TODO,
                tasksCount = 8,
                completedTasksCount = 3
            )
        )
    }
}
