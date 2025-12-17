package tr.edu.bilimankara20307006.taskflow.notification

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.R
import tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager

/**
 * Invisible Activity to handle ACCEPT action
 * More reliable than BroadcastReceiver for notification actions
 */
class AcceptInvitationActivity : ComponentActivity() {

    companion object {
        private const val TAG = "AcceptInvitation"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_PROJECT_ID = "project_id"
        const val EXTRA_PROJECT_NAME = "project_name"
        const val EXTRA_SENDER_NAME = "sender_name"
        const val EXTRA_ANDROID_NOTIFICATION_ID = "android_notification_id"
        private const val CHANNEL_ID = "raptiye_notifications"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "🎯 AcceptInvitationActivity started")
        
        // Extract data from intent
        val notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
        val projectId = intent.getStringExtra(EXTRA_PROJECT_ID)
        val projectName = intent.getStringExtra(EXTRA_PROJECT_NAME) ?: "Project"
        val senderName = intent.getStringExtra(EXTRA_SENDER_NAME) ?: "Someone"
        val androidNotificationId = intent.getIntExtra(EXTRA_ANDROID_NOTIFICATION_ID, 0)

        if (notificationId == null || projectId == null) {
            Log.e(TAG, "❌ Missing required data")
            finish()
            return
        }

        Log.d(TAG, "✅ Processing ACCEPT: notification=$notificationId, project=$projectId")

        // Update notification immediately
        updateNotificationToAccepted(projectName, senderName, androidNotificationId)

        // Process acceptance in background
        processAcceptance(notificationId, projectId)

        // Finish immediately (invisible activity)
        finish()
    }

    private fun updateNotificationToAccepted(
        projectName: String,
        senderName: String,
        androidNotificationId: Int
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create updated notification without action buttons
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Invitation Accepted")
            .setContentText("You accepted the invitation to \"$projectName\"")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You accepted $senderName's invitation to \"$projectName\" project"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()

        notificationManager.notify(androidNotificationId, notification)
        
        Log.d(TAG, "📱 Notification updated to show 'Accepted'")
    }

    private fun processAcceptance(notificationId: String, projectId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                
                if (currentUserId == null) {
                    Log.e(TAG, "❌ User not authenticated")
                    return@launch
                }

                Log.d(TAG, "🔄 Updating Firestore notification status...")

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

                Log.d(TAG, "🔄 Adding user to project...")

                // Add user to project
                val result = FirebaseManager.addTeamMemberDirectly(currentUserId, projectId)
                if (result.isSuccess) {
                    Log.d(TAG, "✅ User added to project successfully")
                } else {
                    Log.e(TAG, "❌ Failed to add user to project: ${result.exceptionOrNull()?.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error processing acceptance: ${e.message}", e)
            }
        }
    }
}
