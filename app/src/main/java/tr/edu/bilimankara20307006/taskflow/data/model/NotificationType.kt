package tr.edu.bilimankara20307006.taskflow.data.model

/**
 * Bildirim türleri
 */
enum class NotificationType(
    val displayName: String,
    val defaultEnabled: Boolean
) {
    PUSH(
        displayName = "Push Bildirimleri",
        defaultEnabled = true
    ),
    TASK_REMINDER(
        displayName = "Görev Hatırlatıcıları",
        defaultEnabled = true
    ),
    PROJECT_UPDATE(
        displayName = "Proje Güncellemeleri",
        defaultEnabled = true
    ),
    TEAM_ACTIVITY(
        displayName = "Ekip Aktiviteleri",
        defaultEnabled = false
    ),
    EMAIL(
        displayName = "E-posta Bildirimleri",
        defaultEnabled = false
    );
    
    companion object {
        /**
         * String'den NotificationType'a dönüştür
         */
        fun fromString(value: String): NotificationType {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                PUSH
            }
        }
        
        /**
         * Tüm bildirim türlerinin varsayılan durumlarını döndür
         */
        fun getDefaultSettings(): Map<NotificationType, Boolean> {
            return values().associateWith { it.defaultEnabled }
        }
    }
}
