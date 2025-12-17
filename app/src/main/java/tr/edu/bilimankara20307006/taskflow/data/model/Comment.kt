package tr.edu.bilimankara20307006.taskflow.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Yorum Data Modeli - Firestore uyumlu
 */
data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    // Eski alanlar (geriye dönük uyumluluk için)
    @Deprecated("Use userName instead")
    val text: String = message,
    @Deprecated("Use User fields instead")
    val author: User? = null,
    @Deprecated("Use timestamp instead")
    val createdDate: Date = Date(timestamp)
) {
    /**
     * Yorum tarihi formatlanmış
     */
    val formattedDate: String
        get() {
            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }
    
    /**
     * Firestore'a kaydetmek için Map'e çevir
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "taskId" to taskId,
            "userId" to userId,
            "userName" to userName,
            "userAvatar" to userAvatar,
            "message" to message,
            "timestamp" to timestamp
        )
    }
    
    companion object {
        /**
         * Firestore dökümanından Comment oluştur
         */
        fun fromMap(map: Map<String, Any?>): Comment? {
            return try {
                Comment(
                    id = map["id"] as? String ?: UUID.randomUUID().toString(),
                    taskId = map["taskId"] as? String ?: "",
                    userId = map["userId"] as? String ?: "",
                    userName = map["userName"] as? String ?: "",
                    userAvatar = map["userAvatar"] as? String,
                    message = map["message"] as? String ?: "",
                    timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                println("❌ Comment parse error: ${e.message}")
                null
            }
        }
    }
}
