package tr.edu.bilimankara20307006.taskflow.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * JWT token'ı güvenli şekilde saklayan ve yöneten singleton sınıf.
 * 
 * EncryptedSharedPreferences kullanarak token'ları şifreli olarak saklar.
 * 
 * Kullanım:
 * ```kotlin
 * // Başlangıçta initialize edin (Application veya MainActivity'de)
 * TokenManager.initialize(context)
 * 
 * // Token kaydet
 * TokenManager.saveToken("eyJhbGc...")
 * 
 * // Token al
 * val token = TokenManager.getToken()
 * 
 * // Token sil (logout)
 * TokenManager.clearToken()
 * ```
 */
object TokenManager {
    private const val PREFS_NAME = "TaskFlowSecurePrefs"
    private const val KEY_JWT_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    
    private var sharedPreferences: SharedPreferences? = null
    
    /**
     * Application başlangıcında çağrılmalı.
     * Şifreli SharedPreferences oluşturur.
     */
    fun initialize(context: Context) {
        if (sharedPreferences == null) {
            try {
                // MasterKey oluştur (Android Keystore'da saklanır)
                val masterKey = MasterKey.Builder(context.applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                
                // Şifreli SharedPreferences oluştur
                sharedPreferences = EncryptedSharedPreferences.create(
                    context.applicationContext,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                // Fallback: Normal SharedPreferences kullan (güvenlik riski var!)
                sharedPreferences = context.applicationContext.getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )
            }
        }
    }
    
    /**
     * JWT token'ı şifreli olarak saklar.
     * @param token Backend'den alınan JWT token
     */
    fun saveToken(token: String) {
        sharedPreferences?.edit()?.apply {
            putString(KEY_JWT_TOKEN, token)
            apply()
        }
    }
    
    /**
     * Saklanan JWT token'ı getirir.
     * @return JWT token veya null
     */
    fun getToken(): String? {
        return sharedPreferences?.getString(KEY_JWT_TOKEN, null)
    }
    
    /**
     * Token'ın Bearer formatında döner.
     * @return "Bearer eyJhbGc..." formatında token
     */
    fun getBearerToken(): String? {
        val token = getToken()
        return if (token != null) "Bearer $token" else null
    }
    
    /**
     * Kullanıcı ID'sini saklar.
     * @param userId MongoDB _id değeri
     */
    fun saveUserId(userId: String) {
        sharedPreferences?.edit()?.apply {
            putString(KEY_USER_ID, userId)
            apply()
        }
    }
    
    /**
     * Saklanan kullanıcı ID'sini getirir.
     * @return Kullanıcı ID'si veya null
     */
    fun getUserId(): String? {
        return sharedPreferences?.getString(KEY_USER_ID, null)
    }
    
    /**
     * Kullanıcı email'ini saklar.
     * @param email Kullanıcı email adresi
     */
    fun saveUserEmail(email: String) {
        sharedPreferences?.edit()?.apply {
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }
    
    /**
     * Saklanan email'i getirir.
     * @return Email adresi veya null
     */
    fun getUserEmail(): String? {
        return sharedPreferences?.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Token ve tüm kullanıcı bilgilerini temizler (logout).
     */
    fun clearToken() {
        sharedPreferences?.edit()?.apply {
            remove(KEY_JWT_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            apply()
        }
    }
    
    /**
     * Tüm verileri temizler.
     */
    fun clearAll() {
        sharedPreferences?.edit()?.clear()?.apply()
    }
    
    /**
     * Kullanıcının login olup olmadığını kontrol eder.
     * @return Token varsa true, yoksa false
     */
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
