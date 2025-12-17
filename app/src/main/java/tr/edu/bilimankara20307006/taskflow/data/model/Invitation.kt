package tr.edu.bilimankara20307006.taskflow.data.model

import java.util.UUID

/**
 * Invitation Model - Single source of truth for invitations
 * Backend-first design: Push notifications are triggers, this is state
 */
data class Invitation(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val projectName: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderEmail: String? = null,
    val receiverId: String = "",
    val receiverEmail: String? = null,
    val status: InvitationStatus = InvitationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // 7 days
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "projectId" to projectId,
            "projectName" to projectName,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderEmail" to (senderEmail ?: ""),
            "receiverId" to receiverId,
            "receiverEmail" to (receiverEmail ?: ""),
            "status" to status.value,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "expiresAt" to expiresAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Invitation {
            return Invitation(
                id = map["id"] as? String ?: "",
                projectId = map["projectId"] as? String ?: "",
                projectName = map["projectName"] as? String ?: "",
                senderId = map["senderId"] as? String ?: "",
                senderName = map["senderName"] as? String ?: "",
                senderEmail = (map["senderEmail"] as? String)?.takeIf { it.isNotEmpty() },
                receiverId = map["receiverId"] as? String ?: "",
                receiverEmail = (map["receiverEmail"] as? String)?.takeIf { it.isNotEmpty() },
                status = InvitationStatus.fromString(map["status"] as? String ?: "pending"),
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L,
                expiresAt = (map["expiresAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

enum class InvitationStatus(val value: String) {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    EXPIRED("expired");

    companion object {
        fun fromString(value: String): InvitationStatus {
            return values().find { it.value == value } ?: PENDING
        }
    }
}
