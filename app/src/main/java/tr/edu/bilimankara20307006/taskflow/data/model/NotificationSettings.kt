package tr.edu.bilimankara20307006.taskflow.data.model

/**
 * Bildirim ayarları
 */
data class NotificationSettings(
    val pushEnabled: Boolean = true,
    val taskReminderEnabled: Boolean = true,
    val projectUpdateEnabled: Boolean = true,
    val teamActivityEnabled: Boolean = false,
    val emailEnabled: Boolean = false,
    val taskDeadlineDays: Int = 1, // 1-30 gün
    val projectDeadlineDays: Int = 3 // 1-30 gün
) {
    /**
     * Firestore'a dönüştür
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "pushEnabled" to pushEnabled,
            "taskReminderEnabled" to taskReminderEnabled,
            "projectUpdateEnabled" to projectUpdateEnabled,
            "teamActivityEnabled" to teamActivityEnabled,
            "emailEnabled" to emailEnabled,
            "taskDeadlineDays" to taskDeadlineDays,
            "projectDeadlineDays" to projectDeadlineDays
        )
    }
    
    companion object {
        /**
         * Firestore'dan dönüştür
         */
        fun fromMap(map: Map<String, Any>): NotificationSettings {
            return NotificationSettings(
                pushEnabled = map["pushEnabled"] as? Boolean ?: true,
                taskReminderEnabled = map["taskReminderEnabled"] as? Boolean ?: true,
                projectUpdateEnabled = map["projectUpdateEnabled"] as? Boolean ?: true,
                teamActivityEnabled = map["teamActivityEnabled"] as? Boolean ?: false,
                emailEnabled = map["emailEnabled"] as? Boolean ?: false,
                taskDeadlineDays = (map["taskDeadlineDays"] as? Long)?.toInt() ?: 1,
                projectDeadlineDays = (map["projectDeadlineDays"] as? Long)?.toInt() ?: 3
            )
        }
    }
}
