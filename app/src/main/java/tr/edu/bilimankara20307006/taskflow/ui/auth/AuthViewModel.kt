package tr.edu.bilimankara20307006.taskflow.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tr.edu.bilimankara20307006.taskflow.data.model.User
import tr.edu.bilimankara20307006.taskflow.data.repository.AuthRepository
import tr.edu.bilimankara20307006.taskflow.data.repository.NetworkResult
import tr.edu.bilimankara20307006.taskflow.data.storage.TokenManager

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    
    private val authRepository = AuthRepository.getInstance()
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // App başladığında token varsa otomatik login yap
        checkExistingSession()
    }
    
    /**
     * Mevcut token'ı kontrol eder ve varsa kullanıcı bilgilerini yükler.
     */
    private fun checkExistingSession() {
        if (TokenManager.isLoggedIn()) {
            viewModelScope.launch {
                _authState.value = _authState.value.copy(isLoading = true)
                
                when (val result = authRepository.getCurrentUser()) {
                    is NetworkResult.Success -> {
                        val userResponse = result.data
                        val user = User(
                            uid = userResponse.id,
                            email = userResponse.email,
                            displayName = userResponse.displayName ?: userResponse.username,
                            photoUrl = userResponse.photoUrl
                        )
                        
                        _authState.value = _authState.value.copy(
                            isAuthenticated = true,
                            isLoading = false,
                            user = user,
                            errorMessage = null
                        )
                    }
                    is NetworkResult.Error -> {
                        // Token geçersiz/expired, logout yap
                        TokenManager.clearToken()
                        _authState.value = _authState.value.copy(
                            isAuthenticated = false,
                            isLoading = false,
                            user = null
                        )
                    }
                    is NetworkResult.Loading -> {
                        // Bu durum normalde olmaz
                    }
                }
            }
        }
    }
    
    /**
     * Kullanıcı girişi yapar.
     * Backend'e POST /auth/login isteği gönderir.
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
            
            // API call
            when (val result = authRepository.login(email, password)) {
                is NetworkResult.Success -> {
                    val loginResponse = result.data
                    
                    // Token'ı kaydet
                    TokenManager.saveToken(loginResponse.token)
                    TokenManager.saveUserId(loginResponse.user.id)
                    TokenManager.saveUserEmail(loginResponse.user.email)
                    
                    // User modelini oluştur
                    val user = User(
                        uid = loginResponse.user.id,
                        email = loginResponse.user.email,
                        displayName = loginResponse.user.displayName ?: loginResponse.user.username,
                        photoUrl = loginResponse.user.photoUrl
                    )
                    
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        user = user,
                        errorMessage = null
                    )
                }
                is NetworkResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        isLoading = false,
                        user = null,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    // Bu durum manuel olarak handle edildi
                }
            }
        }
    }
    
    /**
     * Yeni kullanıcı kaydı yapar.
     * Backend'e POST /auth/register isteği gönderir.
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
            
            // API call
            when (val result = authRepository.register(email, password, finalUsername)) {
                is NetworkResult.Success -> {
                    val registerResponse = result.data
                    
                    // Token'ı kaydet
                    TokenManager.saveToken(registerResponse.token)
                    TokenManager.saveUserId(registerResponse.user.id)
                    TokenManager.saveUserEmail(registerResponse.user.email)
                    
                    // User modelini oluştur
                    val user = User(
                        uid = registerResponse.user.id,
                        email = registerResponse.user.email,
                        displayName = registerResponse.user.displayName ?: registerResponse.user.username,
                        photoUrl = registerResponse.user.photoUrl
                    )
                    
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        user = user,
                        errorMessage = null
                    )
                }
                is NetworkResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        isLoading = false,
                        user = null,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    // Bu durum manuel olarak handle edildi
                }
            }
        }
    }
    
    /**
     * Kullanıcı çıkışı yapar.
     * Token'ı siler ve state'i sıfırlar.
     */
    fun signOut() {
        viewModelScope.launch {
            // Backend'e logout isteği gönder (opsiyonel)
            authRepository.logout()
            
            // Token'ı sil
            TokenManager.clearToken()
            
            // State'i sıfırla
            _authState.value = AuthState()
        }
    }
    
    /**
     * Hata mesajını temizler.
     */
    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }
}