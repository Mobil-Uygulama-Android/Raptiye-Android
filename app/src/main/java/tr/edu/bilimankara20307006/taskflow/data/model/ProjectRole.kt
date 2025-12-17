package tr.edu.bilimankara20307006.taskflow.data.model

/**
 * Proje Rol Sistemi
 * Hiyerarşik rol yapısı: OWNER > LEADER > MEMBER
 */
enum class ProjectRole(val displayName: String, val level: Int) {
    /**
     * Proje Sahibi - En yüksek yetki
     * - Projeyi silebilir
     * - Tüm üyeleri yönetebilir
     * - Rol atayabilir/değiştirebilir
     * - Proje ayarlarını değiştirebilir
     */
    OWNER("Proje Sahibi", 3),
    
    /**
     * Ekip Lideri - Orta seviye yetki
     * - Üye ekleyebilir/çıkarabilir
     * - Görev atayabilir
     * - Proje içeriğini düzenleyebilir
     */
    LEADER("Ekip Lideri", 2),
    
    /**
     * Üye - Temel yetki
     * - Sadece proje içeriğini görüntüleyebilir
     * - Atanan görevleri yapabilir
     * - Yorum yazabilir
     */
    MEMBER("Üye", 1);
    
    /**
     * Bu rolün belirtilen rolden daha yüksek yetkisi var mı?
     */
    fun isHigherThan(other: ProjectRole): Boolean {
        return this.level > other.level
    }
    
    /**
     * Bu rolün belirtilen rolden daha düşük veya eşit yetkisi var mı?
     */
    fun isLowerOrEqualTo(other: ProjectRole): Boolean {
        return this.level <= other.level
    }
    
    /**
     * Üye ekleme/çıkarma yetkisi var mı?
     */
    fun canManageMembers(): Boolean {
        return this == OWNER || this == LEADER
    }
    
    /**
     * Proje ayarlarını değiştirme yetkisi var mı?
     */
    fun canEditProjectSettings(): Boolean {
        return this == OWNER
    }
    
    /**
     * Görev oluşturma/atama yetkisi var mı?
     */
    fun canManageTasks(): Boolean {
        return this == OWNER || this == LEADER
    }
    
    /**
     * Sadece okuma yetkisi var mı?
     */
    fun isReadOnly(): Boolean {
        return this == MEMBER
    }
    
    companion object {
        /**
         * String'den ProjectRole'e dönüştür
         * Firestore'dan gelen değerler için
         */
        fun fromString(value: String): ProjectRole {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                MEMBER // Default olarak MEMBER
            }
        }
        
        /**
         * Firestore'a kaydedilecek string değer
         */
        fun ProjectRole.toFirestoreValue(): String {
            return this.name.lowercase()
        }
    }
}

/**
 * Kullanıcı ve rol bilgisini tutan sınıf
 */
data class ProjectMember(
    val user: User,
    val role: ProjectRole = ProjectRole.MEMBER,
    val addedAt: Long = System.currentTimeMillis()
) {
    /**
     * Firestore'a dönüştür
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to user.uid,
            "displayName" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "role" to role.name.lowercase(),
            "addedAt" to addedAt
        )
    }
    
    companion object {
        /**
         * Firestore'dan ProjectMember oluştur
         */
        fun fromMap(map: Map<String, Any>): ProjectMember? {
            return try {
                val user = User(
                    uid = map["userId"] as? String ?: return null,
                    displayName = map["displayName"] as? String,
                    email = map["email"] as? String
                )
                val role = ProjectRole.fromString(map["role"] as? String ?: "member")
                val addedAt = (map["addedAt"] as? Long) ?: System.currentTimeMillis()
                
                ProjectMember(user, role, addedAt)
            } catch (e: Exception) {
                null
            }
        }
    }
}
