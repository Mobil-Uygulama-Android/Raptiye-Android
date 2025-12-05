package tr.edu.bilimankara20307006.taskflow.data.model

/**
 * User data class - iOS'taki User.swift ile birebir aynı
 */
data class User(
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val createdAt: Long? = null
) {
    // Backward compatibility
    val id: String get() = uid
    
    /**
     * Kullanıcı initials (ilk harf) - iOS'taki initials property'si
     */
    val initials: String
        get() {
            val name = displayName ?: return "U"
            return name.firstOrNull()?.uppercase() ?: "U"
        }
    
    companion object {
        /**
         * Örnek kullanıcılar - iOS'taki sampleUsers
         */
        val sampleUsers = listOf(
            User(uid = "1", displayName = "Emily Carter", email = "emily@example.com"),
            User(uid = "2", displayName = "David Lee", email = "david@example.com"),
            User(uid = "3", displayName = "Ahmet Yılmaz", email = "ahmet@example.com"),
            User(uid = "4", displayName = "Ayşe Demir", email = "ayse@example.com")
        )
    }
}