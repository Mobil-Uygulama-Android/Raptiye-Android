package tr.edu.bilimankara20307006.taskflow.ui.localization

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.Locale

/**
 * Lokalizasyon Yöneticisi - iOS LocalizationManager ile birebir aynı
 * Türkçe ve İngilizce dil desteği
 */
class LocalizationManager(context: Context) : ViewModel() {
    private val prefs = context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
    
    var currentLocale by mutableStateOf(
        prefs.getString("app_locale", "tr") ?: "tr"
    )
        private set
    
    fun setLocale(locale: String) {
        currentLocale = locale
        prefs.edit().putString("app_locale", locale).apply()
    }
    
    fun localizedString(key: String): String {
        return when (currentLocale) {
            "tr" -> getTurkishString(key)
            "en" -> getEnglishString(key)
            else -> getTurkishString(key)
        }
    }
    
    private fun getTurkishString(key: String): String {
        return when (key) {
            // Genel
            "Projects" -> "Projeler"
            "Notifications" -> "Bildirimler"
            "Settings" -> "Ayarlar"
            "Profile" -> "Profil"
            "SignOut" -> "Çıkış Yap"
            "Save" -> "Kaydet"
            "Cancel" -> "İptal"
            "Delete" -> "Sil"
            "Edit" -> "Düzenle"
            "User" -> "Kullanıcı"
            "Search" -> "Ara"
            "SearchProjects" -> "Projelerde ara"
            
            // Ayarlar
            "AppSettings" -> "Uygulama Ayarları"
            "DarkMode" -> "Koyu Tema"
            "Language" -> "Dil Ayarları"
            "Turkish" -> "Türkçe"
            "English" -> "İngilizce"
            "Help" -> "Yardım"
            "About" -> "Hakkında"
            "ProfileInformation" -> "Profil Bilgileri"
            "LanguageSelection" -> "Dil Seçimi"
            "ThemeSelection" -> "Tema Seçimi"
            "SystemTheme" -> "Sistem Ayarı"
            "LightTheme" -> "Açık Tema"
            "DarkTheme" -> "Koyu Tema"
            
            // Bildirimler
            "NoNotificationsMessage" -> "Henüz bildiriminiz bulunmuyor."
            
            // Proje
            "MyProjects" -> "Projelerim"
            "ProjectDetails" -> "Proje Detayları"
            "NewProject" -> "Yeni Proje"
            "AllProjects" -> "Tüm Projeler"
            "ActiveProjects" -> "Aktif Projeler"
            "CompletedProjects" -> "Tamamlanan Projeler"
            
            // Filtreler
            "Sort" -> "Sırala"
            "Filter" -> "Filtrele"
            "FilterOptionAll" -> "Tümü"
            "FilterOptionActive" -> "Aktif"
            "FilterOptionCompleted" -> "Tamamlanan"
            "SortOptionDate" -> "Tarih"
            "SortOptionName" -> "İsim"
            "SortOptionProgress" -> "İlerleme"
            
            // Task
            "Tasks" -> "Görevler"
            "TaskDetails" -> "Görev Detayları"
            "NewTask" -> "Yeni Görev"
            "DueDate" -> "Bitiş Tarihi"
            "Assignee" -> "Atanan"
            "Status" -> "Durum"
            "Priority" -> "Öncelik"
            "Description" -> "Açıklama"
            "Comments" -> "Yorumlar"
            "AddComment" -> "Yorum Ekle"
            "AddTask" -> "Görev Ekle"
            
            // Team
            "TeamLeader" -> "Takım Lideri"
            "TeamMembers" -> "Takım Üyeleri"
            
            // Analytics
            "Analytics" -> "Analitikler"
            "ProjectAnalytics" -> "Proje Analitiği"
            "Progress" -> "İlerleme"
            "Completion" -> "Tamamlanma"
            "TotalTasks" -> "Toplam Görevler"
            "CompletedTasks" -> "Tamamlanan Görevler"
            "InProgressTasks" -> "Devam Eden Görevler"
            "PendingTasks" -> "Bekleyen Görevler"
            "Overview" -> "Genel Bakış"
            "Team" -> "Ekip"
            "TaskCompletionRate" -> "Görev Tamamlanma Oranı"
            "Last30Days" -> "Son 30 Gün"
            "Completed" -> "Tamamlandı"
            "InProgress" -> "Devam Ediyor"
            "Pending" -> "Bekliyor"
            "ProjectTimeline" -> "Proje Zaman Çizelgesi"
            "CurrentProject" -> "Mevcut Proje"
            "Days" -> "gün"
            "Week" -> "Hafta"
            "ProgressContentHere" -> "İlerleme içeriği burada"
            "TeamContentHere" -> "Ekip içeriği burada"
            "Back" -> "Geri"
            
            // Auth
            "CreateAccount" -> "Hesap Oluştur"
            "SignUp" -> "Kayıt Ol"
            "SignIn" -> "Giriş Yap"
            "FullName" -> "Ad Soyad"
            "Email" -> "E-posta"
            "Password" -> "Şifre"
            "ConfirmPassword" -> "Şifre Tekrar"
            "StartByCreatingYourAccount" -> "Hesabınızı oluşturarak başlayın"
            "AlreadyHaveAccount" -> "Hesabınız var mı?"
            "DontHaveAccount" -> "Hesabın yok mu?"
            "PasswordsDoNotMatch" -> "Şifreler eşleşmiyor"
            "Welcome" -> "Hoş Geldiniz"
            "WelcomeBack" -> "Hoş Geldiniz"
            "ForgotPassword" -> "Şifremi unuttum"
            
            // Kanban
            "KanbanBoard" -> "Kanban Panosu"
            
            else -> key
        }
    }
    
    private fun getEnglishString(key: String): String {
        return when (key) {
            // General
            "Projects" -> "Projects"
            "Notifications" -> "Notifications"
            "Settings" -> "Settings"
            "Profile" -> "Profile"
            "SignOut" -> "Sign Out"
            "Save" -> "Save"
            "Cancel" -> "Cancel"
            "Delete" -> "Delete"
            "Edit" -> "Edit"
            "User" -> "User"
            "Search" -> "Search"
            "SearchProjects" -> "Search in projects"
            
            // Settings
            "AppSettings" -> "App Settings"
            "DarkMode" -> "Dark Mode"
            "Language" -> "Language"
            "Turkish" -> "Turkish"
            "English" -> "English"
            "Help" -> "Help"
            "About" -> "About"
            "ProfileInformation" -> "Profile Information"
            "LanguageSelection" -> "Language Selection"
            "ThemeSelection" -> "Theme Selection"
            "SystemTheme" -> "System Default"
            "LightTheme" -> "Light Theme"
            "DarkTheme" -> "Dark Theme"
            
            // Notifications
            "NoNotificationsMessage" -> "You have no notifications yet."
            
            // Project
            "MyProjects" -> "My Projects"
            "ProjectDetails" -> "Project Details"
            "NewProject" -> "New Project"
            "AllProjects" -> "All Projects"
            "ActiveProjects" -> "Active Projects"
            "CompletedProjects" -> "Completed Projects"
            
            // Filters
            "Sort" -> "Sort"
            "Filter" -> "Filter"
            "FilterOptionAll" -> "All"
            "FilterOptionActive" -> "Active"
            "FilterOptionCompleted" -> "Completed"
            "SortOptionDate" -> "Date"
            "SortOptionName" -> "Name"
            "SortOptionProgress" -> "Progress"
            
            // Task
            "Tasks" -> "Tasks"
            "TaskDetails" -> "Task Details"
            "NewTask" -> "New Task"
            "DueDate" -> "Due Date"
            "Assignee" -> "Assignee"
            "Status" -> "Status"
            "Priority" -> "Priority"
            "Description" -> "Description"
            "Comments" -> "Comments"
            "AddComment" -> "Add Comment"
            "AddTask" -> "Add Task"
            
            // Team
            "TeamLeader" -> "Team Leader"
            "TeamMembers" -> "Team Members"
            
            // Analytics
            "Analytics" -> "Analytics"
            "ProjectAnalytics" -> "Project Analytics"
            "Progress" -> "Progress"
            "Completion" -> "Completion"
            "TotalTasks" -> "Total Tasks"
            "CompletedTasks" -> "Completed Tasks"
            "InProgressTasks" -> "In Progress Tasks"
            "PendingTasks" -> "Pending Tasks"
            "Overview" -> "Overview"
            "Team" -> "Team"
            "TaskCompletionRate" -> "Task Completion Rate"
            "Last30Days" -> "Last 30 Days"
            "Completed" -> "Completed"
            "InProgress" -> "In Progress"
            "Pending" -> "Pending"
            "ProjectTimeline" -> "Project Timeline"
            "CurrentProject" -> "Current Project"
            "Days" -> "days"
            "Week" -> "Week"
            "ProgressContentHere" -> "Progress content here"
            "TeamContentHere" -> "Team content here"
            "Back" -> "Back"
            
            // Auth
            "CreateAccount" -> "Create Account"
            "SignUp" -> "Sign Up"
            "SignIn" -> "Sign In"
            "FullName" -> "Full Name"
            "Email" -> "Email"
            "Password" -> "Password"
            "ConfirmPassword" -> "Confirm Password"
            "StartByCreatingYourAccount" -> "Start by creating your account"
            "AlreadyHaveAccount" -> "Already have an account?"
            "DontHaveAccount" -> "Don't have an account?"
            "PasswordsDoNotMatch" -> "Passwords do not match"
            "Welcome" -> "Welcome"
            "WelcomeBack" -> "Welcome Back"
            "ForgotPassword" -> "Forgot password"
            
            // Kanban
            "KanbanBoard" -> "Kanban Board"
            
            else -> key
        }
    }
    
    companion object {
        @Volatile
        private var instance: LocalizationManager? = null
        
        fun getInstance(context: Context): LocalizationManager {
            return instance ?: synchronized(this) {
                instance ?: LocalizationManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
