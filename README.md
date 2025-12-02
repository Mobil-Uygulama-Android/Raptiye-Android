# Raptiye Android 📋

iOS Raptiye uygulamasının Android karşılığı. Proje yönetiminizi kolaylaştırın!

## 🚀 Özellikler

### ✅ Tamamlanmış Özellikler

#### 🔐 Authentication
- ✅ Login ekranı (email/password)
- ✅ Sign up ekranı
- ✅ Otomatik oturum yönetimi
- ✅ **Backend API entegrasyonu** (Node.js + MongoDB)
- ✅ JWT token authentication
- ✅ Şifreli token storage (EncryptedSharedPreferences)

#### 📊 Proje Yönetimi
- ✅ Proje listeleme (arama, filtreleme, sıralama)
- ✅ **Backend'den gerçek zamanlı proje yükleme**
- ✅ Proje oluşturma ve düzenleme
- ✅ Proje detay ekranı
- ✅ Kanban panosu görünümü
- ✅ Proje istatistikleri ve analytics
- ✅ Loading states ve error handling

#### ✅ Görev Yönetimi
- Görev ekleme/düzenleme
- Görev tamamlama
- Görev detay görünümü
- Yorum sistemi
- Görev atama

#### 👥 Takım Özellikleri
- Takım üyesi ekleme
- Takım lideri belirleme
- Üye profilleri

#### 🎨 UI/UX
- Material Design 3
- Dark/Light mode
- Smooth animasyonlar
- iOS benzeri tab bar
- Gradient backgrounds
- Türkçe/İngilizce dil desteği

## 🛠 Teknolojiler

### Core
- **Kotlin** 1.9.0+ - Modern Android development
- **Jetpack Compose** - Declarative UI framework
- **Material Design 3** - Design system
- **Coroutines & Flow** - Asynchronous programming

### Networking
- **Retrofit** 2.9.0 - HTTP client
- **OkHttp** 4.12.0 - Network layer
- **Gson** 2.10.1 - JSON serialization
- **Logging Interceptor** - Network debugging

### Security
- **EncryptedSharedPreferences** - Secure token storage
- **JWT Authentication** - Token-based auth

### Backend
- **Node.js + Express** - REST API server
- **MongoDB Atlas** - Cloud database
- **bcrypt** - Password hashing

### Android Jetpack
- **Navigation Compose** - Ekran geçişleri
- **ViewModel** - State management
- **Lifecycle** - Lifecycle-aware components
- **Activity Compose** - Compose integration

### Mimari
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern** - Data layer abstraction
- **Singleton Pattern** (ThemeManager, LocalizationManager, TokenManager)
- **State Management** with StateFlow
- **Clean Architecture** - Separation of concerns

### UI/UX
- Compose animations
- Material 3 theming
- Custom tab bar
- Responsive design

## 📁 Proje Yapısı

```
app/src/main/java/tr/edu/bilimankara20307006/taskflow/
├── data/
│   ├── model/
│   │   ├── Comment.kt
│   │   ├── Project.kt
│   │   ├── ProjectAnalytics.kt
│   │   ├── Task.kt
│   │   └── User.kt
│   ├── network/
│   │   ├── ApiConstants.kt
│   │   ├── RetrofitClient.kt
│   │   ├── api/
│   │   │   ├── AuthApiService.kt
│   │   │   ├── ProjectApiService.kt
│   │   │   └── TaskApiService.kt
│   │   └── model/
│   │       ├── AuthModels.kt
│   │       ├── ProjectModels.kt
│   │       └── TaskModels.kt
│   ├── repository/
│   │   ├── NetworkResult.kt
│   │   ├── AuthRepository.kt
│   │   ├── ProjectRepository.kt
│   │   └── TaskRepository.kt
│   └── storage/
│       └── TokenManager.kt
├── ui/
│   ├── analytics/
│   │   └── ProjectAnalyticsScreen.kt
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   ├── LoginScreen.kt
│   │   └── SignUpScreen.kt
│   ├── localization/
│   │   └── LocalizationManager.kt
│   ├── main/
│   │   ├── MainScreen.kt
│   │   └── MainTabScreen.kt
│   ├── profile/
│   │   └── ProfileEditScreen.kt
│   ├── project/
│   │   ├── AddProjectDialog.kt
│   │   ├── ProjectBoardScreen.kt
│   │   ├── ProjectDetailScreen.kt
│   │   ├── ProjectListScreen.kt
│   │   └── ProjectListViewModel.kt
│   ├── settings/
│   │   └── NotificationSettingsScreen.kt
│   ├── task/
│   │   └── TaskDetailScreen.kt
│   └── theme/
│       ├── Theme.kt
│       ├── ThemeManager.kt
│       └── Type.kt
├── FirebaseManager.kt
└── MainActivity.kt
```

## 📱 Ekran Görüntüleri

### Login Ekranı
- iOS ile birebir aynı mavi gradient
- Raptiye logosu ve welcome metinleri
- Email/şifre input alanları
- "Şifremi Unuttum?" linki

### Ana Ekran
- Kullanıcı karşılama mesajı
- Görev listesi
- Görev ekleme/tamamlama
- Çıkış yapma

## 🔧 Kurulum

### Gereksinimler
- Android Studio Hedgehog (2023.1.1) veya üzeri
- JDK 17
- Android SDK 24-35
- Kotlin 1.9.0+

### Adımlar

1. **Projeyi Clone Edin**
```bash
git clone https://github.com/Mobil-Uygulama-Android/Task-Flow-Android.git
cd Task-Flow-Android
```

2. **Android Studio'da Açın**
   - File → Open → Proje klasörünü seçin
   - Gradle sync otomatik başlayacak

3. **Backend Sunucusunu Başlatın** (Opsiyonel - Test kullanıcıları mevcuttur)
   
   iOS ekibinin backend'ini çalıştırın:
   ```bash
   cd project-auth-backend
   npm install
   node server.js
   ```
   
   Sunucu `http://localhost:3000` adresinde çalışacak.

4. **Android Uygulamasını Çalıştırın**
   - Emülatör veya fiziksel cihaz seçin
   - Run butonuna basın (Shift+F10)
   
5. **Giriş Yapın**
   
   Test kullanıcıları:
   ```
   Email: testuser@mail.com
   Password: 123456
   
   veya
   
   Email: bilgehan@mail.com
   Password: 123456
   ```

### Backend URL Yapılandırması

Backend URL'sini değiştirmek için `ApiConstants.kt` dosyasını düzenleyin:

```kotlin
// Dosya: app/src/main/java/.../data/network/ApiConstants.kt

object ApiConstants {
    // Android Emülatör için:
    const val BASE_URL = "http://10.0.2.2:3000/api/"
    
    // Gerçek cihaz için (Mac IP'nizi kullanın):
    // const val BASE_URL = "http://192.168.1.X:3000/api/"
}
```

**Detaylı backend entegrasyon rehberi için:** [BACKEND_INTEGRATION.md](BACKEND_INTEGRATION.md)

### Komut Satırından Çalıştırma
```bash
# Debug APK oluştur
./gradlew assembleDebug

# Cihaza yükle ve çalıştır
./gradlew installDebug

# Testleri çalıştır
./gradlew test
```

## � Firebase Setup (Opsiyonel)

Şu anda proje **mock data** ile çalışmaktadır. Firebase entegrasyonu için:

### 1. Firebase Console'da Proje Oluşturun
- [Firebase Console](https://console.firebase.google.com/) → Add Project
- Android app ekleyin (package: `tr.edu.bilimankara20307006.taskflow`)

### 2. google-services.json İndirin
- Firebase Console → Project Settings → Download `google-services.json`
- Dosyayı `app/` klasörüne kopyalayın

### 3. Dependencies Ekleyin
`build.gradle.kts` (project-level):
```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

`app/build.gradle.kts`:
```kotlin
plugins {
    id("com.google.gms.google-services")
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
}
```

### 4. FirebaseManager'ı Güncelleyin
`FirebaseManager.kt` dosyasındaki placeholder kodları gerçek Firebase kodu ile değiştirin.

## 📱 Kullanım

### Login Bilgileri (Mock Mode)
- **Email:** herhangi bir email
- **Password:** herhangi bir şifre (min 6 karakter)

### Temel Özellikler
1. **Projeler Tab:** Proje listesi, ekleme, düzenleme
2. **Bildirimler Tab:** Bildirim merkezi
3. **Ayarlar Tab:** Tema, dil, profil ayarları

### Kısayollar
- **Yeni Proje:** Projects ekranında yeşil + butonu
- **Kanban Görünümü:** Projects ekranında board ikonu
- **Analytics:** Projects ekranında chart ikonu
- **Dark Mode:** Settings → Theme → Dark Theme

## 🐛 Bilinen Sorunlar ve Çözümler

### Gradle Sync Hatası
```bash
# Cache temizle
./gradlew clean

# Dependencies'leri güncelle
./gradlew --refresh-dependencies
```

### Emülatör Çok Yavaş
- AVD Manager → Hardware → Use Host GPU

### Compose Preview Çalışmıyor
- Build → Rebuild Project
- File → Invalidate Caches and Restart

## 🚀 Deployment

### Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK
```bash
./gradlew assembleRelease
# Not: Signing key gereklidir
```

## �👥 Geliştirici Ekibi

Bu proje **Mobil-Uygulama-Android** organizasyonu tarafından geliştirilmektedir.

### Katkıda Bulunanlar
- UI/UX Implementation
- Backend Integration (hazır)
- Testing & QA

## 📝 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 🔗 Bağlantılar

- [GitHub Repository](https://github.com/Mobil-Uygulama-Android/Task-Flow-Android)
- [iOS Version](https://github.com/Mobil-Uygulama-IOS/task-flow-3)
- [Firebase Documentation](https://firebase.google.com/docs/android/setup)
