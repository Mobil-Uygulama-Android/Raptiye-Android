package tr.edu.bilimankara20307006.taskflow.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tr.edu.bilimankara20307006.taskflow.data.model.User
import tr.edu.bilimankara20307006.taskflow.data.manager.NotificationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.UserProfileChangeRequest

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val auth: FirebaseAuth = Firebase.auth
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // App başladığında mevcut kullanıcıyı kontrol et
        checkExistingSession()
    }
    
    /**
     * Firebase'den mevcut kullanıcıyı kontrol eder.
     */
    private fun checkExistingSession() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "Kullanıcı",
                photoUrl = firebaseUser.photoUrl?.toString()
            )
            
            _authState.value = _authState.value.copy(
                isAuthenticated = true,
                isLoading = false,
                user = user,
                errorMessage = null
            )
        }
    }
    
    /**
     * Firebase ile kullanıcı girişi yapar.
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            // Input validation
            when {
                email.isEmpty() -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Lütfen e-posta adresinizi girin"
                    )
                    return@launch
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Geçerli bir e-posta adresi girin (örn: kullanici@email.com)"
                    )
                    return@launch
                }
                password.isEmpty() -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Lütfen şifrenizi girin"
                    )
                    return@launch
                }
                password.length < 6 -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Şifre en az 6 karakter olmalıdır"
                    )
                    return@launch
                }
            }
            
            try {
                // Firebase Authentication
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    println("🔑 Android Login Başarılı:")
                    println("   User ID (UID): ${firebaseUser.uid}")
                    println("   Email: ${firebaseUser.email}")
                    println("   Display Name: ${firebaseUser.displayName}")
                    
                    val user = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "Kullanıcı",
                        photoUrl = firebaseUser.photoUrl?.toString()
                    )
                    
                    // Kullanıcı Firestore'da yoksa kaydet (mevcut kullanıcılar için)
                    val userEmail = user.email
                    if (!userEmail.isNullOrEmpty()) {
                        println("🔍 Kullanıcı Firestore'da kontrol ediliyor...")
                        val searchResult = tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager.searchUserByEmail(userEmail)
                        if (searchResult.isSuccess && searchResult.getOrNull() == null) {
                            println("💾 Kullanıcı Firestore'da yok, kaydediliyor...")
                            val saveResult = tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager.saveUserToFirestore(user)
                            if (saveResult.isSuccess) {
                                println("✅ Mevcut kullanıcı Firestore'a kaydedildi")
                            }
                        } else {
                            println("✅ Kullanıcı zaten Firestore'da mevcut")
                        }
                    }
                    
                    // Eski projeleri migrate et
                    viewModelScope.launch {
                        try {
                            println("🔧 Proje migration başlıyor...")
                            val result = tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager.migrateOldProjects()
                            if (result.isSuccess) {
                                val count = result.getOrNull() ?: 0
                                println("✅ Migration tamamlandı: $count proje güncellendi")
                            }
                        } catch (e: Exception) {
                            println("⚠️ Migration hatası (göz ardı edildi): ${e.message}")
                        }
                    }
                    
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        user = user,
                        errorMessage = null
                    )
                    
                    // FCM Token'ı güncelle
                    updateFCMTokenAfterAuth()
                    
                    println("✅ Giriş başarılı: ${user.displayName}")
                    println("✅ User ID: ${user.uid}")
                    println("✅ Email: ${user.email}")
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Giriş başarısız. Lütfen bilgilerinizi kontrol edin."
                    )
                }
            } catch (e: Exception) {
                println("❌ Giriş hatası: ${e.message}")
                val errorMsg = when {
                    e.message?.contains("no user record", ignoreCase = true) == true ||
                    e.message?.contains("invalid-credential", ignoreCase = true) == true -> 
                        "Bu e-posta ile kayıtlı kullanıcı bulunamadı. Lütfen önce üye olun."
                    e.message?.contains("wrong-password", ignoreCase = true) == true ||
                    e.message?.contains("invalid-credential", ignoreCase = true) == true -> 
                        "E-posta veya şifre hatalı. Lütfen tekrar deneyin."
                    e.message?.contains("too-many-requests", ignoreCase = true) == true -> 
                        "Çok fazla başarısız giriş denemesi. Lütfen daha sonra tekrar deneyin."
                    e.message?.contains("network", ignoreCase = true) == true -> 
                        "İnternet bağlantınızı kontrol edin."
                    else -> "Giriş başarısız: ${e.localizedMessage ?: "Bilinmeyen hata"}"
                }
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }
    
    /**
     * Firebase ile yeni kullanıcı kaydı yapar.
     */
    fun signUp(email: String, password: String, username: String? = null) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            // Input validation
            when {
                email.isEmpty() -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Lütfen e-posta adresinizi girin"
                    )
                    return@launch
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Geçerli bir e-posta adresi girin (örn: kullanici@email.com)"
                    )
                    return@launch
                }
                password.isEmpty() -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Lütfen şifrenizi girin"
                    )
                    return@launch
                }
                password.length < 6 -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Şifre en az 6 karakter olmalıdır"
                    )
                    return@launch
                }
            }
            
            // Username yoksa email'den oluştur
            val finalUsername = username?.takeIf { it.isNotEmpty() } 
                ?: email.substringBefore("@")
            
            try {
                println("📝 Kayıt denemesi")
                println("   - Email: $email")
                println("   - İsim: $finalUsername")
                
                // Firebase Authentication - Kullanıcı oluştur
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    println("✅ Firebase kullanıcı oluşturuldu: ${firebaseUser.uid}")
                    
                    // Display name güncelle
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(finalUsername)
                        .build()
                    
                    firebaseUser.updateProfile(profileUpdates).await()
                    println("✅ Display name güncellendi: $finalUsername")
                    
                    val user = User(
                        uid = firebaseUser.uid,
                        displayName = finalUsername,
                        email = firebaseUser.email,
                        photoUrl = firebaseUser.photoUrl?.toString(),
                        createdAt = System.currentTimeMillis()
                    )
                    
                    // Kullanıcıyı Firestore'a kaydet (iOS gibi) - Email arama için gerekli
                    println("💾 Kullanıcı Firestore'a kaydediliyor...")
                    val saveResult = tr.edu.bilimankara20307006.taskflow.data.firebase.FirebaseManager.saveUserToFirestore(user)
                    if (saveResult.isFailure) {
                        println("⚠️ Firestore'a kaydetme hatası: ${saveResult.exceptionOrNull()?.message}")
                    } else {
                        println("✅ Kullanıcı Firestore'a kaydedildi - Email ile arama yapılabilir")
                    }
                    
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        user = user,
                        errorMessage = null
                    )
                    
                    // FCM Token'ı güncelle
                    updateFCMTokenAfterAuth()
                    
                    println("✅ Kayıt başarılı: $finalUsername")
                    println("✅ User ID: ${user.uid}")
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Kayıt başarısız. Lütfen tekrar deneyin."
                    )
                }
            } catch (e: Exception) {
                println("❌ Kayıt hatası: ${e.message}")
                val errorMsg = when {
                    e.message?.contains("email-already-in-use", ignoreCase = true) == true -> 
                        "Bu e-posta adresi zaten kullanılıyor. Lütfen giriş yapın veya başka bir e-posta deneyin."
                    e.message?.contains("invalid-email", ignoreCase = true) == true -> 
                        "Geçersiz e-posta adresi. Lütfen kontrol edin."
                    e.message?.contains("weak-password", ignoreCase = true) == true -> 
                        "Şifreniz çok zayıf. En az 6 karakter kullanın."
                    e.message?.contains("network", ignoreCase = true) == true -> 
                        "İnternet bağlantınızı kontrol edin."
                    else -> "Kayıt başarısız: ${e.localizedMessage ?: "Bilinmeyen hata"}"
                }
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }
    
    /**
     * Firebase'den kullanıcı çıkışı yapar.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _authState.value = AuthState()
                println("✅ Çıkış başarılı")
            } catch (e: Exception) {
                println("❌ Çıkış hatası: ${e.message}")
                _authState.value = _authState.value.copy(
                    errorMessage = e.localizedMessage
                )
            }
        }
    }
    
    /**
     * Şifre sıfırlama e-postası gönderir.
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                auth.sendPasswordResetEmail(email).await()
                println("✅ Şifre sıfırlama e-postası gönderildi: $email")
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                println("❌ Şifre sıfırlama hatası: ${e.message}")
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage
                )
            }
        }
    }
    
    /**
     * Kullanıcının display name'ini günceller.
     */
    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    
                    currentUser.updateProfile(profileUpdates).await()
                    
                    // State'i güncelle
                    _authState.value.user?.let { user ->
                        val updatedUser = user.copy(displayName = name)
                        _authState.value = _authState.value.copy(user = updatedUser)
                    }
                    
                    println("✅ Display Name güncellendi: $name")
                }
            } catch (e: Exception) {
                println("❌ Display Name güncelleme hatası: ${e.message}")
            }
        }
    }
    
    /**
     * Hata mesajını temizler.
     */
    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }    
    /**
     * Kullanıcı oturum açtıktan sonra FCM token'ı güncelle
     */
    private fun updateFCMTokenAfterAuth() {
        viewModelScope.launch {
            try {
                val result = NotificationManager.getInstance().updateFCMToken()
                if (result.isSuccess) {
                    println("🔑 FCM Token başarıyla güncellendi")
                } else {
                    println("⚠️ FCM Token güncelleme hatası: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                println("❌ FCM Token güncelleme hatası: ${e.message}")
            }
        }
    }}