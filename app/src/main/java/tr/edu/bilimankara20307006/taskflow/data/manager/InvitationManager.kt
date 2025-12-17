package tr.edu.bilimankara20307006.taskflow.data.manager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import tr.edu.bilimankara20307006.taskflow.data.model.Invitation
import tr.edu.bilimankara20307006.taskflow.data.model.InvitationStatus

/**
 * Invitation Manager - Backend-first invitation system
 * 
 * Design Philosophy:
 * - Push notifications are TRIGGERS only, never state mutators
 * - Invitations are Firestore documents (single source of truth)
 * - Accept/Reject happens through backend APIs
 * - Both iOS and Android listen to the same invitation documents
 * 
 * This ensures iOS and Android behave identically without modifying iOS code.
 */
object InvitationManager {
    
    private const val COLLECTION_INVITATIONS = "invitations"
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_PROJECTS = "projects"
    
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Create invitation - Called by sender (iOS or Android)
     * 
     * Flow:
     * 1. Create invitation document
     * 2. Create notification document
     * 3. Send push notification
     */
    suspend fun createInvitation(
        receiverEmail: String,
        projectId: String,
        projectName: String
    ): Result<Invitation> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            
            // Get sender info from Firestore
            val senderDoc = db.collection(COLLECTION_USERS)
                .document(currentUser.uid)
                .get()
                .await()
            
            val senderName = senderDoc.getString("fullName")
                ?: senderDoc.getString("email")
                ?: currentUser.email
                ?: "Unknown"
            
            // Get receiver by email
            val receiverQuery = db.collection(COLLECTION_USERS)
                .whereEqualTo("email", receiverEmail)
                .limit(1)
                .get()
                .await()
            
            if (receiverQuery.isEmpty) {
                return Result.failure(Exception("User not found"))
            }
            
            val receiverDoc = receiverQuery.documents[0]
            val receiverId = receiverDoc.id
            
            // Create invitation
            val invitation = Invitation(
                projectId = projectId,
                projectName = projectName,
                senderId = currentUser.uid,
                senderName = senderName,
                senderEmail = currentUser.email,
                receiverId = receiverId,
                receiverEmail = receiverEmail,
                status = InvitationStatus.PENDING
            )
            
            // Save to Firestore
            db.collection(COLLECTION_INVITATIONS)
                .document(invitation.id)
                .set(invitation.toMap())
                .await()
            
            println("✅ Invitation created: ${invitation.id}")
            
            // Create notification for receiver
            NotificationManager.sendProjectInvitationNotification(
                toUserId = receiverId,
                invitationId = invitation.id,
                projectId = projectId,
                projectName = projectName
            )
            
            Result.success(invitation)
        } catch (e: Exception) {
            println("❌ Create invitation error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Accept invitation - Called when user taps Accept in-app
     * 
     * Backend API that:
     * 1. Updates invitation status to "accepted"
     * 2. Adds user to project teamMembers
     * 3. Both iOS and Android listen to status change via Firestore listener
     */
    suspend fun acceptInvitation(invitationId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            
            println("🔵 Accepting invitation: $invitationId")
            
            // Get invitation
            val invitationDoc = db.collection(COLLECTION_INVITATIONS)
                .document(invitationId)
                .get()
                .await()
            
            if (!invitationDoc.exists()) {
                return Result.failure(Exception("Invitation not found"))
            }
            
            val invitation = Invitation.fromMap(invitationDoc.data!!)
            
            // Verify current user is the receiver
            if (invitation.receiverId != currentUser.uid) {
                return Result.failure(Exception("Not authorized"))
            }
            
            // Verify invitation is pending
            if (invitation.status != InvitationStatus.PENDING) {
                return Result.failure(Exception("Invitation already processed"))
            }
            
            // Update invitation status
            db.collection(COLLECTION_INVITATIONS)
                .document(invitationId)
                .update(
                    mapOf(
                        "status" to InvitationStatus.ACCEPTED.value,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            
            println("✅ Invitation status updated to 'accepted'")
            
            // Add user to project
            val projectRef = db.collection(COLLECTION_PROJECTS).document(invitation.projectId)
            val projectDoc = projectRef.get().await()
            
            if (!projectDoc.exists()) {
                return Result.failure(Exception("Project not found"))
            }
            
            // Get user info
            val userDoc = db.collection(COLLECTION_USERS)
                .document(currentUser.uid)
                .get()
                .await()
            
            val userMap = mapOf(
                "uid" to currentUser.uid,
                "email" to (userDoc.getString("email") ?: currentUser.email ?: ""),
                "displayName" to (userDoc.getString("fullName") ?: userDoc.getString("displayName") ?: ""),
                "photoUrl" to (userDoc.getString("photoUrl") ?: ""),
                "addedAt" to System.currentTimeMillis()
            )
            
            // Add to teamMembers
            projectRef.update("teamMembers", com.google.firebase.firestore.FieldValue.arrayUnion(userMap))
                .await()
            
            println("✅ User added to project: ${invitation.projectId}")
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Accept invitation error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Reject invitation - Called when user taps Reject in-app
     */
    suspend fun rejectInvitation(invitationId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            
            println("🔵 Rejecting invitation: $invitationId")
            
            // Get invitation
            val invitationDoc = db.collection(COLLECTION_INVITATIONS)
                .document(invitationId)
                .get()
                .await()
            
            if (!invitationDoc.exists()) {
                return Result.failure(Exception("Invitation not found"))
            }
            
            val invitation = Invitation.fromMap(invitationDoc.data!!)
            
            // Verify current user is the receiver
            if (invitation.receiverId != currentUser.uid) {
                return Result.failure(Exception("Not authorized"))
            }
            
            // Update invitation status
            db.collection(COLLECTION_INVITATIONS)
                .document(invitationId)
                .update(
                    mapOf(
                        "status" to InvitationStatus.REJECTED.value,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            
            println("✅ Invitation status updated to 'rejected'")
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Reject invitation error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Listen to user's invitations (real-time)
     * Both iOS and Android use this
     */
    fun listenToInvitations(
        userId: String,
        onInvitationsChanged: (List<Invitation>) -> Unit
    ): ListenerRegistration {
        return db.collection(COLLECTION_INVITATIONS)
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("status", InvitationStatus.PENDING.value)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ Invitation listener error: ${error.message}")
                    return@addSnapshotListener
                }
                
                val invitations = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Invitation.fromMap(doc.data ?: return@mapNotNull null)
                    } catch (e: Exception) {
                        println("⚠️ Parse error: ${e.message}")
                        null
                    }
                } ?: emptyList()
                
                println("📬 Invitations updated: ${invitations.size} pending")
                onInvitationsChanged(invitations)
            }
    }
    
    /**
     * Get single invitation (for deeplink navigation)
     */
    suspend fun getInvitation(invitationId: String): Result<Invitation> {
        return try {
            val doc = db.collection(COLLECTION_INVITATIONS)
                .document(invitationId)
                .get()
                .await()
            
            if (!doc.exists()) {
                return Result.failure(Exception("Invitation not found"))
            }
            
            val invitation = Invitation.fromMap(doc.data!!)
            Result.success(invitation)
        } catch (e: Exception) {
            println("❌ Get invitation error: ${e.message}")
            Result.failure(e)
        }
    }
}
