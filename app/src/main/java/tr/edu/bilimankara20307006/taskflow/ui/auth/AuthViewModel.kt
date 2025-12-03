package tr.edu.bilimankara20307006.taskflow.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tr.edu.bilimankara20307006.taskflow.data.model.User
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
            if (email.isEmpty() || password.isEmpty()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "E-posta ve şifre boş olamaz"
                )
                return@launch
            }
            
            try {
                // Firebase Authentication
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                
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
                    
                    println("✅ Giriş başarılı: ${user.displayName}")
                    println("✅ User ID: ${user.uid}")
                    println("✅ Email: ${user.email}")
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Giriş başarısız"
                    )
                }
            } catch (e: Exception) {
                println("❌ Giriş hatası: ${e.message}")
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Giriş başarısız"
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
            if (email.isEmpty() || password.isEmpty()) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "E-posta ve şifre boş olamaz"
                )
                return@launch
            }
            
            if (password.length < 6) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Şifre en az 6 karakter olmalı"
                )
                return@launch
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
                        email = firebaseUser.email ?: "",
                        displayName = finalUsername,
                        photoUrl = firebaseUser.photoUrl?.toString()
                    )
                    
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        user = user,
                        errorMessage = null
                    )
                    
                    println("✅ Kayıt başarılı: $finalUsername")
                    println("✅ User ID: ${user.uid}")
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Kayıt başarısız"
                    )
                }
            } catch (e: Exception) {
                println("❌ Kayıt hatası: ${e.message}")
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Kayıt başarısız"
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
}