package tr.edu.bilimankara20307006.taskflow.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.R
import tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager

/**
 * Notification action button handler (ACCEPT/REJECT)
 * Production-grade implementation with Android 12+ compatibility
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationReceiver"
        const val ACTION_ACCEPT = "tr.edu.bilimankara20307006.taskflow.ACCEPT_INVITATION"
        const val ACTION_REJECT = "tr.edu.bilimankara20307006.taskflow.REJECT_INVITATION"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_PROJECT_ID = "project_id"
        const val EXTRA_PROJECT_NAME = "project_name"
        const val EXTRA_SENDER_NAME = "sender_name"
        private const val CHANNEL_ID = "raptiye_notifications"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "📥 Received action: ${intent.action}")

        val notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
        val projectId = intent.getStringExtra(EXTRA_PROJECT_ID)
        val projectName = intent.getStringExtra(EXTRA_PROJECT_NAME) ?: "Project"
        val senderName = intent.getStringExtra(EXTRA_SENDER_NAME) ?: "Someone"
        val androidNotificationId = intent.getIntExtra("android_notification_id", 0)

        if (notificationId == null || projectId == null) {
            Log.e(TAG, "❌ Missing required data: notificationId=$notificationId, projectId=$projectId")
            return
        }

        when (intent.action) {
            ACTION_ACCEPT -> {
                Log.d(TAG, "✅ ACCEPT clicked - Notification: $notificationId, Project: $projectId")
                handleAccept(context, notificationId, projectId, projectName, senderName, androidNotificationId)
            }
            ACTION_REJECT -> {
                Log.d(TAG, "❌ REJECT clicked - Notification: $notificationId")
                handleReject(context, notificationId, androidNotificationId)
            }
        }
    }

    private fun handleAccept(
        context: Context,
        notificationId: String,
        projectId: String,
        projectName: String,
        senderName: String,
        androidNotificationId: Int
    ) {
        // Update notification to show "Accepted" (no buttons)
        updateNotificationToAccepted(context, projectName, senderName, androidNotificationId)

        // Process acceptance in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "🔄 Processing invitation acceptance...")
                
                val db = FirebaseFirestore.getInstance()
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                
                if (currentUserId == null) {
                    Log.e(TAG, "❌ User not authenticated")
                    return@launch
                }

                // Update Firestore notification status
                db.collection("notifications")
                    .document(notificationId)
                    .update(
                        mapOf(
                            "invitationStatus" to "accepted",
                            "isRead" to true
                        )
                    )
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ Notification status updated to 'accepted'")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Failed to update notification: ${e.message}")
                    }

                // Add user to project
                val result = FirebaseManager.addTeamMemberDirectly(currentUserId, projectId)
                if (result.isSuccess) {
                    Log.d(TAG, "✅ User added to project successfully")
                } else {
                    Log.e(TAG, "❌ Failed to add user to project: ${result.exceptionOrNull()?.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in handleAccept: ${e.message}", e)
            }
        }
    }

    private fun handleReject(
        context: Context,
        notificationId: String,
        androidNotificationId: Int
    ) {
        // Update notification to show "Declined" (no buttons)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(androidNotificationId)

        // Process rejection in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "🔄 Processing invitation rejection...")
                
                val db = FirebaseFirestore.getInstance()

                // Update Firestore notification status
                db.collection("notifications")
                    .document(notificationId)
                    .update(
                        mapOf(
                            "invitationStatus" to "declined",
                            "isRead" to true
                        )
                    )
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ Notification status updated to 'declined'")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Failed to update notification: ${e.message}")
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in handleReject: ${e.message}", e)
            }
        }
    }

    private fun updateNotificationToAccepted(
        context: Context,
        projectName: String,
        senderName: String,
        androidNotificationId: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create updated notification without action buttons
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Invitation Accepted")
            .setContentText("You accepted the invitation to \"$projectName\"")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You accepted $senderName's invitation to \"$projectName\" project"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOngoing(false) // User can dismiss
            .build()

        notificationManager.notify(androidNotificationId, notification)
        
        Log.d(TAG, "📱 Notification updated to show 'Accepted'")
    }
}
