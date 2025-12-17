package tr.edu.bilimankara20307006.taskflow.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import tr.edu.bilimankara20307006.taskflow.MainActivity
import tr.edu.bilimankara20307006.taskflow.R
import tr.edu.bilimankara20307006.taskflow.notification.AcceptInvitationActivity
import tr.edu.bilimankara20307006.taskflow.notification.NotificationActionReceiver

/**
 * Firebase Cloud Messaging Service - iOS APNs'e benzer
 */
class RaptiyeFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val CHANNEL_ID = "raptiye_notifications"
        private const val CHANNEL_NAME = "Raptiye Bildirimleri"
        private const val NOTIFICATION_ID = 1001
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    /**
     * Yeni FCM token alındığında çağrılır
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("🔑 Yeni FCM Token: $token")
        
        // Token'ı Firestore'a kaydet (kullanıcı oturum açmışsa)
        saveTokenToFirestore(token)
    }
    
    /**
     * FCM token'ı Firestore'a kaydet
     */
    private fun saveTokenToFirestore(token: String) {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            if (currentUser != null) {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val userDoc = db.collection("users").document(currentUser.uid)
                
                userDoc.update(
                    mapOf(
                        "fcmToken" to token,
                        "tokenUpdatedAt" to System.currentTimeMillis(),
                        "platform" to "android"
                    )
                )
                println("✅ FCM Token Firestore'a kaydedildi")
            } else {
                println("⚠️ Kullanıcı oturum açmamış, token kaydedilemedi")
            }
        } catch (e: Exception) {
            println("❌ Token kaydetme hatası: ${e.message}")
        }
    }
    
    /**
     * Push notification geldiğinde çağrılır
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        println("📩 FCM Message alındı: ${remoteMessage.from}")
        
        // Notification payload'ını al
        val notification = remoteMessage.notification
        val data = remoteMessage.data
        
        // Title ve body belirleme
        val title = notification?.title ?: data["title"] ?: "Raptiye"
        val body = notification?.body ?: data["message"] ?: ""
        
        // Notification type
        val notificationType = data["type"] ?: "OTHER"
        val invitationId = data["invitationId"]
        val deeplink = data["deeplink"]
        val projectId = data["projectId"]
        val taskId = data["taskId"]
        
        println("📱 Bildirim gösteriliyor: $title - $body")
        println("   Type: $notificationType")
        println("   Deeplink: $deeplink")
        
        // Local notification göster
        showNotification(
            title = title,
            message = body,
            notificationType = notificationType,
            deeplink = deeplink,
            invitationId = invitationId,
            projectId = projectId,
            taskId = taskId
        )
    }
    
    /**
     * Local notification göster - with deeplink support
     */
    private fun showNotification(
        title: String,
        message: String,
        notificationType: String,
        deeplink: String? = null,
        invitationId: String? = null,
        projectId: String? = null,
        taskId: String? = null
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Unique notification ID for this specific notification
        val androidNotificationId = System.currentTimeMillis().toInt()
        
        // MainActivity'yi açmak için intent (with deeplink support)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // Deeplink varsa, intent'e data olarak ekle
            if (!deeplink.isNullOrEmpty()) {
                data = android.net.Uri.parse(deeplink)
                println("🔗 Setting deeplink as intent data: $deeplink")
            }
            
            // Bildirime özel data ekle
            putExtra("notification_type", notificationType)
            invitationId?.let { putExtra("invitation_id", it) }
            projectId?.let { putExtra("project_id", it) }
            taskId?.let { putExtra("task_id", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            androidNotificationId, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Notification builder
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(Color.parseColor("#66D68C"))
        
        // For PROJECT_INVITATION: Don't add action buttons, just use deeplink
        // The invitation screen will show Accept/Reject buttons
        if (notificationType == "PROJECT_INVITATION") {
            println("✅ Invitation notification created with deeplink: $deeplink")
            println("   Tap notification to open invitation detail screen")
        }
        
        notificationManager.notify(androidNotificationId, notificationBuilder.build())
    }
    
    /**
     * Notification channel oluştur (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Raptiye uygulama bildirimleri"
                enableLights(true)
                lightColor = Color.parseColor("#66D68C")
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}