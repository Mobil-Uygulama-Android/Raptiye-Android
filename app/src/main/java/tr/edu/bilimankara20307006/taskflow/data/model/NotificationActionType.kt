package tr.edu.bilimankara20307006.taskflow.data.model

enum class NotificationActionType {
    PROJECT_INVITATION,
    PROJECT_MEMBER_LEFT,
    PROJECT_MEMBER_ADDED,
    PROJECT_DELETED,
    TASK_ASSIGNED,
    TASK_COMPLETED,
    TASK_DUE_SOON,
    TASK_OVERDUE,
    TASK_COMMENT,
    SYSTEM_ANNOUNCEMENT,
    REMINDER,
    GENERAL;
    
    companion object {
        fun fromString(value: String): NotificationActionType {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                GENERAL
            }
        }
    }
}