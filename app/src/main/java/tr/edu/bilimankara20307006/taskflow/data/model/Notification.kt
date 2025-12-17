package tr.edu.bilimankara20307006.taskflow.data.model

import java.util.UUID

/**
 * Bildirim data modeli - iOS Notification.swift ile aynı yapı
 */
data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationActionType,
    val userId: String, // Bildirimi alacak kullanıcı
    val fromUserId: String? = null, // Bildirimi gönderen kullanıcı
    val fromUserName: String? = null,
    val projectId: String? = null,
    val projectName: String? = null,
    val taskId: String? = null,
    val taskTitle: String? = null,
    val isRead: Boolean = false,
    val invitationStatus: String? = null, // "pending", "accepted", "declined"
    val createdAt: Long = System.currentTimeMillis(),
    val data: Map<String, Any> = emptyMap() // Ek veriler için
) {
    /**
     * Firestore'a kaydetmek için Map'e dönüştür
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "message" to message,
            "type" to type.name,
            "userId" to userId,
            "fromUserId" to (fromUserId ?: ""),
            "fromUserName" to (fromUserName ?: ""),
            "projectId" to (projectId ?: ""),
            "projectName" to (projectName ?: ""),
            "taskId" to (taskId ?: ""),
            "taskTitle" to (taskTitle ?: ""),
            "isRead" to isRead,
            "invitationStatus" to (invitationStatus ?: ""),
            "createdAt" to createdAt,
            "data" to data
        )
    }
    
    companion object {
        /**
         * Firestore Map'inden Notification oluştur
         */
        fun fromMap(map: Map<String, Any>): Notification {
            return Notification(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                message = map["message"] as? String ?: "",
                type = NotificationActionType.fromString(map["type"] as? String ?: ""),
                userId = map["userId"] as? String ?: "",
                fromUserId = (map["fromUserId"] as? String)?.takeIf { it.isNotEmpty() },
                fromUserName = (map["fromUserName"] as? String)?.takeIf { it.isNotEmpty() },
                projectId = (map["projectId"] as? String)?.takeIf { it.isNotEmpty() },
                projectName = (map["projectName"] as? String)?.takeIf { it.isNotEmpty() },
                taskId = (map["taskId"] as? String)?.takeIf { it.isNotEmpty() },
                taskTitle = (map["taskTitle"] as? String)?.takeIf { it.isNotEmpty() },
                isRead = map["isRead"] as? Boolean ?: false,
                invitationStatus = (map["invitationStatus"] as? String)?.takeIf { it.isNotEmpty() },
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
                data = map["data"] as? Map<String, Any> ?: emptyMap()
            )
        }
    }
}