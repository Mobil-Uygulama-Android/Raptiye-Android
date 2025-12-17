package tr.edu.bilimankara20307006.taskflow.data.manager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import tr.edu.bilimankara20307006.taskflow.data.model.Notification
import tr.edu.bilimankara20307006.taskflow.data.model.NotificationActionType
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull

/**
 * Bildirim Yöneticisi - iOS NotificationManager ile aynı yapı
 */
object NotificationManager {
    
    private const val COLLECTION_NOTIFICATIONS = "notifications"
    private const val COLLECTION_USERS = "users"
    
    // FCM Server Key - Gerçek push notification gönderiliyor
    // iOS ekibi FCM token'ı Firestore'a kaydetmeli
    private const val FCM_DEBUG_MODE = false
    
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val messaging = FirebaseMessaging.getInstance()
    
    /**
     * Singleton instance'ı getir
     */
    fun getInstance(): NotificationManager = this
    
    /**
     * FCM Token al ve Firestore'a kaydet
     */
    suspend fun updateFCMToken(): Result<String> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturum açmamış"))
            
            val token = messaging.token.await()
            println("🔑 FCM Token alındı: ${token.take(20)}...${token.takeLast(10)}")
            
            // Token'ı kullanıcı belgesinde güncelle
            val tokenData = mapOf(
                "fcmToken" to token, 
                "tokenUpdatedAt" to System.currentTimeMillis(),
                "platform" to "android",
                "email" to currentUser.email,
                "uid" to currentUser.uid,
                "deviceInfo" to mapOf(
                    "platform" to "android",
                    "manufacturer" to android.os.Build.MANUFACTURER,
                    "model" to android.os.Build.MODEL,
                    "osVersion" to android.os.Build.VERSION.RELEASE
                )
            )
            
            db.collection(COLLECTION_USERS)
                .document(currentUser.uid)
                .update(tokenData)
                .await()
            
            println("✅ FCM Token Firestore'a kaydedildi")
            println("   User: ${currentUser.email}")
            println("   Platform: android")
            println("   Time: ${System.currentTimeMillis()}")
            
            // Cross-platform sync için token verification
            verifyTokenRegistration(currentUser.uid, token)
            
            Result.success(token)
        } catch (e: Exception) {
            println("❌ FCM Token hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Token kaydını doğrula - cross-platform sync için
     */
    private suspend fun verifyTokenRegistration(userId: String, token: String) {
        try {
            val userDoc = db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            
            val savedToken = userDoc.getString("fcmToken")
            val platform = userDoc.getString("platform")
            
            println("🔍 Token verification:")
            println("   Saved token matches: ${savedToken == token}")
            println("   Platform: $platform")
            println("   User exists: ${userDoc.exists()}")
            
        } catch (e: Exception) {
            println("⚠️ Token verification failed: ${e.message}")
        }
    }
    
    /**
     * Bildirim gönder - hem Firestore'a kaydet hem de FCM ile push notification gönder
     */
    suspend fun sendNotification(
        toUserId: String,
        title: String,
        message: String,
        type: NotificationActionType,
        projectId: String? = null,
        projectName: String? = null,
        taskId: String? = null,
        taskTitle: String? = null,
        invitationStatus: String? = null,
        data: Map<String, Any> = emptyMap(),
        fromUserNameOverride: String? = null
    ): Result<Notification> {
        return try {
            val currentUser = auth.currentUser
            val fromUserId = currentUser?.uid
            val fromUserName = fromUserNameOverride ?: currentUser?.displayName
            
            println("📬 sendNotification çağrıldı:")
            println("   📝 Title: $title")
            println("   💬 Message: $message")
            println("   👤 From: $fromUserName (override: $fromUserNameOverride)")
            println("   📁 Project: $projectName (ID: $projectId)")
            
            // Notification oluştur
            val notification = Notification(
                title = title,
                message = message,
                type = type,
                userId = toUserId,
                fromUserId = fromUserId,
                fromUserName = fromUserName,
                projectId = projectId,
                projectName = projectName,
                taskId = taskId,
                taskTitle = taskTitle,
                invitationStatus = invitationStatus,
                data = data
            )
            
            // Firestore'a kaydet
            db.collection(COLLECTION_NOTIFICATIONS)
                .document(notification.id)
                .set(notification.toMap())
                .await()
            
            println("✅ Bildirim Firestore'a kaydedildi: ${notification.id}")
            
            // FCM push notification gönder
            sendPushNotificationToUser(toUserId, notification)
            
            Result.success(notification)
        } catch (e: Exception) {
            println("❌ Bildirim gönderme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Kullanıcının bildirimlerini al
     */
    suspend fun getUserNotifications(userId: String): Result<List<Notification>> {
        return try {
            val snapshot = db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50) // Son 50 bildirim
                .get()
                .await()
            
            val notifications = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Notification.fromMap(data)
                } catch (e: Exception) {
                    println("⚠️ Bildirim parse hatası: ${e.message}")
                    null
                }
            }
            
            println("📬 ${notifications.size} bildirim alındı")
            Result.success(notifications)
        } catch (e: Exception) {
            println("❌ Bildirimleri alma hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Bildirimi okundu olarak işaretle
     */
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true)
                .await()
            
            println("✅ Bildirim okundu: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Bildirim okundu işaretleme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Bildirimi okunmadı olarak işaretle
     */
    suspend fun markAsUnread(notificationId: String): Result<Unit> {
        return try {
            db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", false)
                .await()
            
            println("✅ Bildirim okunmadı işaretlendi: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Bildirim okunmadı işaretleme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Tüm bildirimleri okundu olarak işaretle
     */
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val batch = db.batch()
            
            val snapshot = db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            
            batch.commit().await()
            
            println("✅ Tüm bildirimler okundu işaretlendi")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Tüm bildirimleri okundu işaretleme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Bildirimi sil
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .await()
            
            println("🗑️ Bildirim silindi: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Bildirim silme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Okunmamış bildirim sayısını al
     */
    suspend fun getUnreadCount(userId: String): Result<Int> {
        return try {
            val snapshot = db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val count = snapshot.documents.size
            println("📊 Okunmamış bildirim sayısı: $count")
            Result.success(count)
        } catch (e: Exception) {
            println("❌ Okunmamış sayı alma hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    // MARK: - Özel bildirim fonksiyonları
    
    /**
     * Proje daveti bildirimi gönder - iOS ile uyumlu davet sistemi
     */
    suspend fun sendProjectInvitation(
        toUserId: String,
        projectId: String,
        projectName: String
    ): Result<Notification> {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return Result.failure(Exception("Kullanıcı oturum açmamış"))
        }
        
        // Kullanıcı adını Firestore'dan al
        val inviterName = try {
            val userDoc = db.collection("users")
                .document(currentUser.uid)
                .get()
                .await()
            val name = userDoc.getString("fullName") ?: userDoc.getString("email") ?: currentUser.displayName ?: "Bilinmeyen Kullanıcı"
            println("🔍 Davet gönderen: $name (UID: ${currentUser.uid})")
            name
        } catch (e: Exception) {
            println("⚠️ Kullanıcı adı alınamadı: ${e.message}")
            currentUser.displayName ?: currentUser.email ?: "Bilinmeyen Kullanıcı"
        }
        
        println("📨 Proje daveti hazırlanıyor:")
        println("   👤 Davet gönderen: $inviterName")
        println("   📁 Proje: $projectName")
        println("   🎯 Alıcı: $toUserId")
        
        // Hedef kullanıcının bilgilerini kontrol et
        try {
            val targetUserDoc = db.collection("users").document(toUserId).get().await()
            val targetEmail = targetUserDoc.getString("email")
            val targetPlatform = targetUserDoc.getString("platform")
            val targetToken = targetUserDoc.getString("fcmToken")
            println("📱 Hedef kullanıcı:")
            println("   Email: $targetEmail")
            println("   Platform: $targetPlatform")
            println("   FCM Token: ${if (targetToken.isNullOrEmpty()) "❌ YOK!" else "✅ VAR"}")
            
            if (targetToken.isNullOrEmpty()) {
                println("⚠️⚠️⚠️ UYARI: Hedef kullanıcının FCM token'ı yok!")
                println("💡 iOS kullanıcısı uygulamayı açıp yeniden giriş yapmalı")
            }
        } catch (e: Exception) {
            println("❌ Hedef kullanıcı bilgileri alınamadı: ${e.message}")
        }
        
        return sendNotification(
            toUserId = toUserId,
            title = "Proje Daveti",
            message = "$inviterName sizi \"$projectName\" projesine davet etti",
            type = NotificationActionType.PROJECT_INVITATION,
            projectId = projectId,
            projectName = projectName,
            invitationStatus = "pending",
            fromUserNameOverride = inviterName
        )
    }
    
    /**
     * Projeden ayrılma bildirimi gönder (takım liderine)
     */
    suspend fun sendProjectMemberLeft(
        toUserId: String, // Takım lideri
        projectId: String,
        projectName: String,
        memberName: String
    ): Result<Notification> {
        return sendNotification(
            toUserId = toUserId,
            title = "Projeden Ayrılma",
            message = "$memberName \"$projectName\" projesinden ayrıldı",
            type = NotificationActionType.PROJECT_MEMBER_LEFT,
            projectId = projectId,
            projectName = projectName,
            fromUserNameOverride = memberName
        )
    }
    
    /**
     * Görev atama bildirimi gönder
     */
    suspend fun sendTaskAssigned(
        toUserId: String,
        taskId: String,
        taskTitle: String,
        projectId: String,
        projectName: String
    ): Result<Notification> {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return Result.failure(Exception("Kullanıcı oturum açmamış"))
        }
        
        // Kullanıcı adını Firestore'dan al
        val assignerName = try {
            val userDoc = db.collection("users")
                .document(currentUser.uid)
                .get()
                .await()
            userDoc.getString("fullName") ?: userDoc.getString("email") ?: currentUser.displayName ?: "Bilinmeyen Kullanıcı"
        } catch (e: Exception) {
            currentUser.displayName ?: currentUser.email ?: "Bilinmeyen Kullanıcı"
        }
        
        return sendNotification(
            toUserId = toUserId,
            title = "Görev Atandı",
            message = "$assignerName size \"$taskTitle\" görevini atadı",
            type = NotificationActionType.TASK_ASSIGNED,
            projectId = projectId,
            projectName = projectName,
            taskId = taskId,
            taskTitle = taskTitle,
            fromUserNameOverride = assignerName
        )
    }
    
    /**
     * Kullanıcıya FCM push notification gönder
     */
    private suspend fun sendPushNotificationToUser(userId: String, notification: Notification) {
        try {
            // Kullanıcının FCM token'ını al
            val userDoc = db.collection(COLLECTION_USERS).document(userId).get().await()
            val fcmToken = userDoc.getString("fcmToken")
            val userEmail = userDoc.getString("email")
            val platform = userDoc.getString("platform") ?: "unknown"
            
            println("📄 Kullanıcı bilgileri: $userEmail")
            println("📱 Platform: $platform")
            
            if (fcmToken.isNullOrEmpty()) {
                println("⚠️ Kullanıcının FCM token'ı bulunamadı: $userId")
                println("   Email: $userEmail")
                println("   Platform: $platform")
                println("   💡 Çözüm: Kullanıcı uygulamayı açıp yeniden giriş yapmalı")
                return
            }
            
            println("📤 FCM Push notification hazırlanıyor...")
            println("   Target: $userEmail")
            println("   Token: ${fcmToken.take(20)}...${fcmToken.takeLast(10)}")
            println("   Title: ${notification.title}")
            println("   Message: ${notification.message}")
            
            if (FCM_DEBUG_MODE) {
                // Debug mode - sadece log
                println("🔍 DEBUG MODE: FCM simulation")
                println("   🎤 Firestore'a yazdık, real-time listener tetikleyecek")
                println("   📡 Gerçek push için Firebase Cloud Functions gerekli")
            } else {
                // Production mode - gerçek FCM gönder
                sendFCMMessage(fcmToken, notification, platform)
            }
            
        } catch (e: Exception) {
            println("❌ Push notification gönderme hatası: ${e.message}")
        }
    }
    
    /**
     * Gerçek FCM API ile push notification gönder
     */
    private suspend fun sendFCMMessage(fcmToken: String, notification: Notification, platform: String) {
        try {
            println("📡 FCM Push notification gönderiliyor...")
            println("   🎯 Token: ${fcmToken.take(15)}...")
            println("   📱 Platform: $platform")
            println("   💬 Title: ${notification.title}")
            println("   💬 Body: ${notification.message}")
            
            // FCM API kullanarak gerçek push notification gönder
            val client = OkHttpClient()
            val serverKey = "AAAA_aWNGYg:APA91bH_tYqHrJbkQPGtH9qhTQ8XDZg_Ue4yT5YN1VJEt8kRfW6pD9XQlmnWUfgY3zOh8PIBqpN7rGw4p6VmR2jCQ5Ew0G_XyZF7UkAqE8LpPxR1bKSt6cDnH9J_rY4oFwL3"
            
            val fcmPayload = JSONObject().apply {
                put("to", fcmToken)
                
                // iOS ve Android için farklı payload formatları
                if (platform.contains("ios", ignoreCase = true)) {
                    // iOS için notification ve data aynı anda gönder
                    put("notification", JSONObject().apply {
                        put("title", notification.title)
                        put("body", notification.message)
                        put("sound", "default")
                        put("badge", "1")
                        put("mutable_content", true)
                    })
                    put("priority", "high")
                    put("content_available", true)
                    
                    // iOS için custom keys
                    put("mutableContent", true)
                    put("contentAvailable", true)
                } else {
                    // Android için standart FCM formatı
                    put("notification", JSONObject().apply {
                        put("title", notification.title)
                        put("body", notification.message)
                        put("sound", "default")
                        put("click_action", "FLUTTER_NOTIFICATION_CLICK")
                    })
                }
                
                // Data payload (her iki platform için aynı)
                put("data", JSONObject().apply {
                    put("type", notification.type.name)
                    put("notificationId", notification.id)
                    put("title", notification.title)
                    put("body", notification.message)
                    notification.projectId?.let { put("projectId", it) }
                    notification.projectName?.let { put("projectName", it) }
                    notification.taskId?.let { put("taskId", it) }
                    notification.fromUserName?.let { put("fromUserName", it) }
                    notification.invitationStatus?.let { put("invitationStatus", it) }
                    
                    // Add custom data fields (includes deeplink for invitations)
                    notification.data.forEach { (key, value) ->
                        put(key, value)
                    }
                    
                    // iOS için click_action
                    if (platform.contains("ios", ignoreCase = true)) {
                        put("click_action", "NOTIFICATION_ACTION")
                    }
                })
            }
            
            println("📦 FCM Payload:")
            println(fcmPayload.toString(2))
            
            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                fcmPayload.toString()
            )
            
            val request = Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .addHeader("Authorization", "key=$serverKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                println("✅ FCM Push notification başarıyla gönderildi!")
                println("   📊 Response: ${response.body?.string()}")
            } else {
                println("❌ FCM Push notification hatası: ${response.code}")
                println("   📊 Error: ${response.body?.string()}")
            }
            
            response.close()
            
        } catch (e: Exception) {
            println("❌ FCM API hatası: ${e.message}")
        }
    }
    
    /**
     * Proje davetini kabul et
     */
    suspend fun acceptProjectInvitation(notificationId: String): Result<Unit> {
        return try {
            val notificationDoc = db.collection(COLLECTION_NOTIFICATIONS).document(notificationId).get().await()
            if (!notificationDoc.exists()) {
                return Result.failure(Exception("Bildirim bulunamadı"))
            }
            
            val notificationData = notificationDoc.data!!
            val projectId = notificationData["projectId"] as? String ?: return Result.failure(Exception("Proje ID bulunamadı"))
            val userId = notificationData["userId"] as? String ?: return Result.failure(Exception("Kullanıcı ID bulunamadı"))
            
            println("📝 Davet kabul ediliyor:")
            println("   Bildirim ID: $notificationId")
            println("   Proje ID: $projectId")
            println("   Kullanıcı ID: $userId")
            
            // Önce bildirim durumunu güncelle
            db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update(mapOf(
                    "invitationStatus" to "accepted",
                    "isRead" to true
                ))
                .await()
            
            println("✅ invitationStatus 'accepted' olarak güncellendi")
            
            // FirebaseManager'dan gerçek addTeamMember'ı çağır (direkt ekleme)
            val addResult = tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager.addTeamMemberDirectly(userId, projectId)
            
            if (addResult.isSuccess) {
                println("✅ Proje daveti kabul edildi ve kullanıcı projeye eklendi")
                Result.success(Unit)
            } else {
                Result.failure(addResult.exceptionOrNull() ?: Exception("Projeye ekleme hatası"))
            }
            
        } catch (e: Exception) {
            println("❌ Davet kabul etme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Proje davetini reddet
     */
    suspend fun declineProjectInvitation(notificationId: String): Result<Unit> {
        return try {
            // Bildirim durumunu güncelle
            db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update(mapOf(
                    "invitationStatus" to "declined",
                    "isRead" to true
                ))
                .await()
            
            println("✅ Proje daveti reddedildi")
            Result.success(Unit)
            
        } catch (e: Exception) {
            println("❌ Davet reddetme hatası: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Send invitation notification (trigger only, state is in invitation document)
     * 
     * This creates:
     * 1. Notification document (for display in notifications screen)
     * 2. Push notification (deeplinks to invitation detail screen)
     * 
     * Accept/Reject happens in-app through InvitationManager backend APIs
     */
    suspend fun sendProjectInvitationNotification(
        toUserId: String,
        invitationId: String,
        projectId: String,
        projectName: String
    ): Result<Notification> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            
            // Get sender name
            val senderDoc = db.collection(COLLECTION_USERS)
                .document(currentUser.uid)
                .get()
                .await()
            
            val senderName = senderDoc.getString("fullName")
                ?: senderDoc.getString("email")
                ?: currentUser.email
                ?: "Unknown"
            
            // Create notification document (for display in app + push notification)
            val notification = Notification(
                userId = toUserId,
                type = NotificationActionType.PROJECT_INVITATION,
                title = "Project Invitation",
                message = "$senderName invited you to \"$projectName\"",
                data = mapOf(
                    "invitationId" to invitationId,
                    "projectId" to projectId,
                    "projectName" to projectName,
                    "senderId" to currentUser.uid,
                    "senderName" to senderName,
                    "deeplink" to "taskflow://invitation/$invitationId"
                ),
                isRead = false
            )
            
            db.collection(COLLECTION_NOTIFICATIONS)
                .document(notification.id)
                .set(notification.toMap())
                .await()
            
            println("✅ Notification created: ${notification.id}")
            println("🔗 Deeplink: taskflow://invitation/$invitationId")
            
            // Send push notification using existing infrastructure
            sendPushNotificationToUser(toUserId, notification)
            
            Result.success(notification)
        } catch (e: Exception) {
            println("❌ Send invitation notification error: ${e.message}")
            Result.failure(e)
        }
    }
}