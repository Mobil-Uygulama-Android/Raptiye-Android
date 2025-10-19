package tr.edu.bilimankara20307006.taskflow.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Tema Yöneticisi - iOS ThemeManager ile birebir aynı
 * Açık/Koyu tema kontrolü + Sistem ayarı desteği
 */
class ThemeManager(private val context: Context) : ViewModel() {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        
        @Volatile
        private var instance: ThemeManager? = null
        
        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // Seçili tema modu: "system", "light", "dark"
    private var _themeMode by mutableStateOf(prefs.getString("themeMode", THEME_SYSTEM) ?: THEME_SYSTEM)
    val themeMode: String
        get() = _themeMode
    
    // Gerçek tema durumu (sistem ayarını dikkate alarak)
    val isDarkMode: Boolean
        get() = when (_themeMode) {
            THEME_LIGHT -> false
            THEME_DARK -> true
            else -> isSystemInDarkMode()
        }
    
    private fun isSystemInDarkMode(): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
    
    fun setThemeMode(mode: String) {
        _themeMode = mode
        prefs.edit().putString("themeMode", mode).apply()
    }
    
    // Eski API uyumluluğu için
    fun toggleTheme() {
        setThemeMode(if (isDarkMode) THEME_LIGHT else THEME_DARK)
    }
}
