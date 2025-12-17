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
            "Add" -> "Ekle"
            "User" -> "Kullanıcı"
            "Search" -> "Ara"
            "SearchProjects" -> "Projelerde ara"
            "Loading" -> "Yükleniyor"
            
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
            "TaskTitle" -> "Görev Başlığı"
            "TaskDescription" -> "Görev Açıklaması"
            "EnterTaskTitle" -> "Görev başlığını girin"
            "EnterTaskDescription" -> "Görev açıklamasını girin"
            "PleaseEnterTaskTitle" -> "Lütfen görev başlığını girin"
            "SelectDate" -> "Tarih Seç"
            "LowPriority" -> "Düşük"
            "Warning" -> "Uyarı"
            "OK" -> "Tamam"
            "Cancel" -> "İptal"
            "Save" -> "Kaydet"
            
            // Team
            "TeamLeader" -> "Takım Lideri"
            "TeamMembers" -> "Takım Üyeleri"
            "AddMember" -> "Üye Ekle"
            "NoTeamMembers" -> "Henüz takım üyesi eklenmedi"
            "NoMembers" -> "Üye Yok"
            "AddMembersToAssignTasks" -> "Görev atayabilmek için önce projeye üye ekleyin"
            "AddTeamMember" -> "Proje Daveti Gönder"
            "AddTeamMemberToProject" -> "Projeye Davet Gönder"
            "SearchByEmail" -> "E-posta adresi ile arayın"
            "EmailAddress" -> "E-posta Adresi"
            "Clear" -> "Temizle"
            "SearchUser" -> "Kullanıcı Ara"
            "UserNotFound" -> "Kullanıcı bulunamadı. Lütfen e-posta adresini kontrol edin."
            "SearchError" -> "Arama hatası"
            "AddToProject" -> "Proje Daveti Gönder"
            "CurrentTeam" -> "Mevcut Ekip"
            "NoTeamMembersYet" -> "Henüz ekip üyesi yok"
            "ProjectLeader" -> "Proje Lideri"
            "Success" -> "Başarılı"
            "MemberAddedSuccessfully" -> "Proje daveti başarıyla gönderildi"
            "Error" -> "Hata"
            "OK" -> "Tamam"
            
            // Analytics
            "Analytics" -> "Analitikler"
            "Statistics" -> "İstatistikler"
            "ProjectAnalytics" -> "Proje Analitiği"
            "Progress" -> "İlerleme"
            "Completion" -> "Tamamlanma"
            "TotalTasks" -> "Toplam Görevler"
            "Total" -> "Toplam"
            "CompletedTasks" -> "Tamamlanan Görevler"
            "InProgressTasks" -> "Devam Eden Görevler"
            "PendingTasks" -> "Bekleyen Görevler"
            "Overview" -> "Genel Bakış"
            "Team" -> "Ekip"
            "TaskCompletionRate" -> "Görev Tamamlanma Oranı"
            "TaskStatus" -> "Görev Durumu"
            "PriorityDistribution" -> "Öncelik Dağılımı"
            "HighPriority" -> "Yüksek Öncelik"
            "MediumPriority" -> "Orta Öncelik"
            "LowPriority" -> "Düşük Öncelik"
            "TeamPerformance" -> "Takım Performansı"
            "Last30Days" -> "Son 30 Gün"
            "Completed" -> "Tamamlandı"
            "InProgress" -> "Devam Eden"
            "Todo" -> "Yapılacak"
            "Pending" -> "Bekliyor"
            "ProjectBoard" -> "Proje Panosu"
            "ProjectDashboard" -> "Proje Panosu"
            "NoTasksYet" -> "Henüz görev yok"
            "NoTodoTasks" -> "Yapılacak görev yok"
            "NoInProgressTasks" -> "Devam eden görev yok"
            "NoCompletedTasks" -> "Tamamlanmış görev yok"
            "Unassigned" -> "Atanmamış"
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
            "ResetPassword" -> "Şifreyi Sıfırla"
            "ResetPasswordMessage" -> "Şifre sıfırlama bağlantısı göndermek için e-posta adresinizi girin."
            "ResetPasswordEmailSent" -> "Şifre sıfırlama e-postası gönderildi! Lütfen e-posta kutunuzu kontrol edin."
            "Send" -> "Gönder"
            
            // Kanban
            "KanbanBoard" -> "Kanban Panosu"
            
            // Project Actions
            "EditProject" -> "Projeyi Düzenle"
            "DeleteProject" -> "Projeyi Sil"
            
            // About Screen
            "AboutRaptiye" -> "Raptiye Hakkında"
            "AboutRaptiyeDescription" -> "Raptiye, ekiplerin etkili bir şekilde işbirliği yapmasına, görevleri takip etmesine ve hedeflerine birlikte ulaşmasına yardımcı olan modern bir proje yönetim uygulamasıdır."
            "KeyFeatures" -> "Temel Özellikler"
            "ProjectManagement" -> "Proje Yönetimi"
            "ProjectManagementDesc" -> "Birden fazla proje oluşturun ve yönetin"
            "TaskTracking" -> "Görev Takibi"
            "TaskTrackingDesc" -> "Tüm görevlerinizi takip edin"
            "TeamCollaboration" -> "Ekip İşbirliği"
            "TeamCollaborationDesc" -> "Ekibinizle birlikte çalışın"
            "AnalyticsFeature" -> "Analitikler"
            "AnalyticsDesc" -> "Detaylı analitiklerle ilerlemeyi takip edin"
            "MultiLanguage" -> "Çoklu Dil"
            "MultiLanguageDesc" -> "Türkçe ve İngilizce desteği"
            "PrivacyPolicy" -> "Gizlilik Politikası"
            "TermsOfService" -> "Kullanım Koşulları"
            "AllRightsReserved" -> "Tüm hakları saklıdır."
            
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
            "Add" -> "Add"
            "User" -> "User"
            "Search" -> "Search"
            "SearchProjects" -> "Search in projects"
            "Loading" -> "Loading"
            
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
            "TaskTitle" -> "Task Title"
            "TaskDescription" -> "Task Description"
            "EnterTaskTitle" -> "Enter task title"
            "EnterTaskDescription" -> "Enter task description"
            "PleaseEnterTaskTitle" -> "Please enter task title"
            "SelectDate" -> "Select Date"
            "LowPriority" -> "Low"
            "Warning" -> "Warning"
            "OK" -> "OK"
            "Cancel" -> "Cancel"
            "Save" -> "Save"
            
            // Team
            "TeamLeader" -> "Team Leader"
            "TeamMembers" -> "Team Members"
            "AddMember" -> "Add Member"
            "NoTeamMembers" -> "No team members added yet"
            "NoMembers" -> "No Members"
            "AddMembersToAssignTasks" -> "Add members to the project first to assign tasks"
            "AddTeamMember" -> "Send Project Invitation"
            "AddTeamMemberToProject" -> "Send Invitation to Project"
            "SearchByEmail" -> "Search by email address"
            "EmailAddress" -> "Email Address"
            "Clear" -> "Clear"
            "SearchUser" -> "Search User"
            "UserNotFound" -> "User not found. Please check the email address."
            "SearchError" -> "Search error"
            "AddToProject" -> "Send Project Invitation"
            "CurrentTeam" -> "Current Team"
            "NoTeamMembersYet" -> "No team members yet"
            "ProjectLeader" -> "Project Leader"
            "Success" -> "Success"
            "MemberAddedSuccessfully" -> "Project invitation sent successfully"
            "Error" -> "Error"
            "OK" -> "OK"
            
            // Analytics
            "Analytics" -> "Analytics"
            "Statistics" -> "Statistics"
            "ProjectAnalytics" -> "Project Analytics"
            "Progress" -> "Progress"
            "Completion" -> "Completion"
            "TotalTasks" -> "Total Tasks"
            "Total" -> "Total"
            "CompletedTasks" -> "Completed Tasks"
            "InProgressTasks" -> "In Progress Tasks"
            "PendingTasks" -> "Pending Tasks"
            "Overview" -> "Overview"
            "Team" -> "Team"
            "TaskCompletionRate" -> "Task Completion Rate"
            "TaskStatus" -> "Task Status"
            "PriorityDistribution" -> "Priority Distribution"
            "HighPriority" -> "High Priority"
            "MediumPriority" -> "Medium Priority"
            "LowPriority" -> "Low Priority"
            "TeamPerformance" -> "Team Performance"
            "Last30Days" -> "Last 30 Days"
            "Completed" -> "Completed"
            "InProgress" -> "In Progress"
            "Todo" -> "To Do"
            "Pending" -> "Pending"
            "ProjectBoard" -> "Project Board"
            "ProjectDashboard" -> "Project Dashboard"
            "NoTasksYet" -> "No tasks yet"
            "NoTodoTasks" -> "No tasks to do"
            "NoInProgressTasks" -> "No tasks in progress"
            "NoCompletedTasks" -> "No completed tasks"
            "Unassigned" -> "Unassigned"
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
            "ResetPassword" -> "Reset Password"
            "ResetPasswordMessage" -> "Enter your email address to receive a password reset link."
            "ResetPasswordEmailSent" -> "Password reset email sent! Please check your inbox."
            "Send" -> "Send"
            
            // Kanban
            "KanbanBoard" -> "Kanban Board"
            
            // Project Actions
            "EditProject" -> "Edit Project"
            "DeleteProject" -> "Delete Project"
            
            // About Screen
            "AboutRaptiye" -> "About Raptiye"
            "AboutRaptiyeDescription" -> "Raptiye is a modern project management application that helps teams collaborate effectively, track tasks, and achieve their goals together."
            "KeyFeatures" -> "Key Features"
            "ProjectManagement" -> "Project Management"
            "ProjectManagementDesc" -> "Create and manage multiple projects"
            "TaskTracking" -> "Task Tracking"
            "TaskTrackingDesc" -> "Keep track of all your tasks"
            "TeamCollaboration" -> "Team Collaboration"
            "TeamCollaborationDesc" -> "Work together with your team"
            "AnalyticsFeature" -> "Analytics"
            "AnalyticsDesc" -> "Track progress with detailed analytics"
            "MultiLanguage" -> "Multi-language"
            "MultiLanguageDesc" -> "Support for Turkish and English"
            "PrivacyPolicy" -> "Privacy Policy"
            "TermsOfService" -> "Terms of Service"
            "AllRightsReserved" -> "All rights reserved."
            
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
