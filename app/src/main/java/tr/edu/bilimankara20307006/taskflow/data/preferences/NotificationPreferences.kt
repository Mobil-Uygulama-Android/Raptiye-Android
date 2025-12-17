package tr.edu.bilimankara20307006.taskflow.data.preferences

import android.content.Context
import android.content.SharedPreferences
import tr.edu.bilimankara20307006.taskflow.data.model.NotificationSettings

/**
 * SharedPreferences ile bildirim ayarlarını yönetir
 */
class NotificationPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "notification_settings"
        private const val KEY_PUSH_ENABLED = "push_enabled"
        private const val KEY_TASK_REMINDER_ENABLED = "task_reminder_enabled"
        private const val KEY_PROJECT_UPDATE_ENABLED = "project_update_enabled"
        private const val KEY_TEAM_ACTIVITY_ENABLED = "team_activity_enabled"
        private const val KEY_EMAIL_ENABLED = "email_enabled"
        private const val KEY_TASK_DEADLINE_DAYS = "task_deadline_days"
        private const val KEY_PROJECT_DEADLINE_DAYS = "project_deadline_days"
    }
    
    /**
     * Bildirim ayarlarını kaydet
     */
    fun saveSettings(settings: NotificationSettings) {
        prefs.edit().apply {
            putBoolean(KEY_PUSH_ENABLED, settings.pushEnabled)
            putBoolean(KEY_TASK_REMINDER_ENABLED, settings.taskReminderEnabled)
            putBoolean(KEY_PROJECT_UPDATE_ENABLED, settings.projectUpdateEnabled)
            putBoolean(KEY_TEAM_ACTIVITY_ENABLED, settings.teamActivityEnabled)
            putBoolean(KEY_EMAIL_ENABLED, settings.emailEnabled)
            putInt(KEY_TASK_DEADLINE_DAYS, settings.taskDeadlineDays)
            putInt(KEY_PROJECT_DEADLINE_DAYS, settings.projectDeadlineDays)
            apply()
        }
    }
    
    /**
     * Bildirim ayarlarını oku
     */
    fun getSettings(): NotificationSettings {
        return NotificationSettings(
            pushEnabled = prefs.getBoolean(KEY_PUSH_ENABLED, true),
            taskReminderEnabled = prefs.getBoolean(KEY_TASK_REMINDER_ENABLED, true),
            projectUpdateEnabled = prefs.getBoolean(KEY_PROJECT_UPDATE_ENABLED, true),
            teamActivityEnabled = prefs.getBoolean(KEY_TEAM_ACTIVITY_ENABLED, false),
            emailEnabled = prefs.getBoolean(KEY_EMAIL_ENABLED, false),
            taskDeadlineDays = prefs.getInt(KEY_TASK_DEADLINE_DAYS, 1),
            projectDeadlineDays = prefs.getInt(KEY_PROJECT_DEADLINE_DAYS, 3)
        )
    }
    
    /**
     * Ayarları temizle
     */
    fun clearSettings() {
        prefs.edit().clear().apply()
    }
}
