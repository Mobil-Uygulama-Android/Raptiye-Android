package tr.edu.bilimankara20307006.taskflow.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import tr.edu.bilimankara20307006.taskflow.data.manager.NotificationManager
import tr.edu.bilimankara20307006.taskflow.data.model.Notification
import tr.edu.bilimankara20307006.taskflow.data.model.NotificationActionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Bildirim ViewModel
 */
data class NotificationState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NotificationViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()
    
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var notificationListener: ListenerRegistration? = null
    
    init {
        startRealTimeNotifications()
    }
    
    /**
     * Gerçek zamanlı bildirim dinleme başlat
     */
    private fun startRealTimeNotifications() {
        val userId = auth.currentUser?.uid ?: return
        
        println("📡 Gerçek zamanlı bildirim dinleme başlatılıyor: $userId")
        
        // Önce mevcut listener'ı temizle
        notificationListener?.remove()
        
        notificationListener = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                println("🔥 Snapshot listener tetiklendi!")
                
                if (error != null) {
                    println("❌ Bildirim dinleme hatası: ${error.message}")
                    println("❌ Hata detayı: ${error.localizedMessage}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    println("🔍 Snapshot alındı: ${snapshot.documents.size} doküman")
                    println("🔍 Snapshot metadata: from cache = ${snapshot.metadata.isFromCache}")
                    
                    val notifications = snapshot.documents.mapNotNull { doc ->
                        try {
                            println("📄 Döküman ID: ${doc.id}")
                            println("📄 Döküman data: ${doc.data}")
                            val notification = Notification.fromMap(doc.data ?: return@mapNotNull null)
                            println("✅ Bildirim decode edildi: ${notification.title}")
                            notification
                        } catch (e: Exception) {
                            println("⚠️ Bildirim decode hatası: ${e.message}")
                            println("⚠️ Hata stack: ${e.stackTrace.joinToString()}")
                            null
                        }
                    }.sortedByDescending { it.createdAt }
                    
                    val unreadCount = notifications.count { !it.isRead }
                    
                    _state.value = _state.value.copy(
                        notifications = notifications,
                        unreadCount = unreadCount,
                        isLoading = false,
                        errorMessage = null
                    )
                    
                    println("🔔 Bildirimler güncellendi: ${notifications.size} toplam, $unreadCount okunmadı")
                    println("🔔 İlk bildirim: ${notifications.firstOrNull()?.title ?: "YOK"}")
                } else {
                    println("⚠️ Snapshot null")
                }
            }
    }
    
    override fun onCleared() {
        super.onCleared()
        notificationListener?.remove()
        println("📴 Bildirim listener temizlendi")
    }
    
    /**
     * Bildirimleri yükle
     */
    fun loadNotifications() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            
            _state.value = _state.value.copy(isLoading = true)
            
            NotificationManager.getUserNotifications(userId)
                .onSuccess { notifications ->
                    val unreadCount = notifications.count { !it.isRead }
                    _state.value = _state.value.copy(
                        notifications = notifications,
                        unreadCount = unreadCount,
                        isLoading = false,
                        errorMessage = null
                    )
                    println("✅ ${notifications.size} bildirim yüklendi, ${unreadCount} okunmadı")
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                    println("❌ Bildirim yükleme hatası: ${error.message}")
                }
        }
    }
    
    /**
     * Bildirimi okundu olarak işaretle
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            NotificationManager.markAsRead(notificationId)
                .onSuccess {
                    // Lokal state'i güncelle
                    val updatedNotifications = _state.value.notifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    val unreadCount = updatedNotifications.count { !it.isRead }
                    
                    _state.value = _state.value.copy(
                        notifications = updatedNotifications,
                        unreadCount = unreadCount
                    )
                    println("✅ Bildirim okundu işaretlendi")
                }
                .onFailure { error ->
                    println("❌ Okundu işaretleme hatası: ${error.message}")
                }
        }
    }
    
    /**
     * Bildirimi okunmadı olarak işaretle
     */
    fun markAsUnread(notificationId: String) {
        viewModelScope.launch {
            NotificationManager.markAsUnread(notificationId)
                .onSuccess {
                    // Lokal state'i güncelle
                    val updatedNotifications = _state.value.notifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = false)
                        } else {
                            notification
                        }
                    }
                    val unreadCount = updatedNotifications.count { !it.isRead }
                    
                    _state.value = _state.value.copy(
                        notifications = updatedNotifications,
                        unreadCount = unreadCount
                    )
                    println("✅ Bildirim okunmadı işaretlendi")
                }
                .onFailure { error ->
                    println("❌ Okunmadı işaretleme hatası: ${error.message}")
                }
        }
    }
    
    /**
     * Tüm bildirimleri okundu olarak işaretle
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            
            NotificationManager.markAllAsRead(userId)
                .onSuccess {
                    // Lokal state'i güncelle
                    val updatedNotifications = _state.value.notifications.map { notification ->
                        notification.copy(isRead = true)
                    }
                    
                    _state.value = _state.value.copy(
                        notifications = updatedNotifications,
                        unreadCount = 0
                    )
                    println("✅ Tüm bildirimler okundu işaretlendi")
                }
                .onFailure { error ->
                    println("❌ Tümünü okundu işaretleme hatası: ${error.message}")
                }
        }
    }
    
    /**
     * Bildirimi sil
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            NotificationManager.deleteNotification(notificationId)
                .onSuccess {
                    // Lokal state'den kaldır
                    val updatedNotifications = _state.value.notifications.filter { 
                        it.id != notificationId 
                    }
                    val unreadCount = updatedNotifications.count { !it.isRead }
                    
                    _state.value = _state.value.copy(
                        notifications = updatedNotifications,
                        unreadCount = unreadCount
                    )
                    println("✅ Bildirim silindi")
                }
                .onFailure { error ->
                    println("❌ Bildirim silme hatası: ${error.message}")
                }
        }
    }
    
    /**
     * Test bildirimi gönder (debug için)
     */
    fun sendTestNotification() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            
            println("🧪 Test bildirimi gönderiliyor...")
            
            NotificationManager.sendNotification(
                toUserId = userId,
                title = "Test Bildirimi",
                message = "Bu bir test bildirimidir. Bildirim sistemi çalışıyor! 🎉",
                type = NotificationActionType.SYSTEM_ANNOUNCEMENT,
                data = mapOf("test" to true)
            ).onSuccess {
                println("✅ Test bildirimi gönderildi")
                // Bildirimleri yeniden yükle
                loadNotifications()
            }.onFailure { error ->
                println("❌ Test bildirimi hatası: ${error.message}")
            }
        }
    }
    
    /**
     * Hata mesajını temizle
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
    
    /**
     * Proje davetini kabul et
     */
    fun acceptProjectInvitation(notificationId: String) {
        viewModelScope.launch {
            val result = NotificationManager.acceptProjectInvitation(notificationId)
            if (result.isFailure) {
                println("❌ Davet kabul etme hatası: ${result.exceptionOrNull()?.message}")
            }
        }
    }
    
    /**
     * Proje davetini reddet
     */
    fun declineProjectInvitation(notificationId: String) {
        viewModelScope.launch {
            val result = NotificationManager.declineProjectInvitation(notificationId)
            if (result.isFailure) {
                println("❌ Davet reddetme hatası: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}