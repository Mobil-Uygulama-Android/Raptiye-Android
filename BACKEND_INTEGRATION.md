# 🔗 Task-Flow Android - Backend Entegrasyon Rehberi

## 📡 Backend Bağlantısı Kuruldu!

Task-Flow Android uygulaması, iOS ekibinin geliştirdiği **Node.js + MongoDB** backend'ine başarıyla bağlanmıştır.

---

## 🎯 Backend Mimarisi

```
┌─────────────────┐         HTTP/REST          ┌──────────────────┐
│  Android App    │ ◄────────────────────────► │  Node.js Server  │
│  (Kotlin)       │       Retrofit              │  (Express.js)    │
└─────────────────┘                             └──────────────────┘
                                                         │
                                                         │
                                                         ▼
                                                ┌──────────────────┐
                                                │  MongoDB Atlas   │
                                                │  (Cloud Database)│
                                                └──────────────────┘
```

**Önemli:** Android uygulaması veritabanına **doğrudan bağlanmaz**. Tüm işlemler HTTP API üzerinden yapılır.

---

## 🚀 Hızlı Başlangıç

### 1️⃣ Backend Sunucusunu Başlatın

iOS ekip arkadaşınızdan backend'i çalıştırmasını isteyin:

```bash
cd project-auth-backend
node server.js
```

Sunucu `http://localhost:3000` adresinde çalışacak.

---

### 2️⃣ Backend URL'sini Ayarlayın

**ApiConstants.kt** dosyasını açın:

```kotlin
// Dosya: app/src/main/java/.../data/network/ApiConstants.kt

object ApiConstants {
    // DURUM 1: Android Emülatör kullanıyorsanız
    const val BASE_URL = "http://10.0.2.2:3000/api/"
    
    // DURUM 2: Gerçek Android cihaz kullanıyorsanız
    // Mac'in IP adresini öğrenin: Terminal'de `ifconfig | grep "inet "`
    // const val BASE_URL = "http://192.168.1.X:3000/api/"  // X yerine IP
    
    // DURUM 3: Production (Heroku, AWS, vs.)
    // const val BASE_URL = "https://your-api.herokuapp.com/api/"
}
```

**IP Adresini Bulma:**
```bash
# Mac Terminal'de:
ifconfig | grep "inet " | grep -v 127.0.0.1

# Windows CMD'de:
ipconfig | findstr IPv4
```

---

### 3️⃣ Test Kullanıcısı ile Giriş Yapın

Uygulamayı çalıştırın ve şu bilgilerle giriş yapın:

```
Email: testuser@mail.com
Password: 123456

# veya

Email: bilgehan@mail.com
Password: 123456
```

---

## 🛠 Teknik Detaylar

### Kullanılan Kütüphaneler

```gradle
// Retrofit - HTTP Client
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// OkHttp - Network Layer
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Gson - JSON Serialization
implementation("com.google.code.gson:gson:2.10.1")

// Coroutines - Async Operations
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

---

### Proje Yapısı

```
app/src/main/java/.../data/
├── network/
│   ├── ApiConstants.kt            ← Backend URL ve endpoint'ler
│   ├── RetrofitClient.kt          ← Retrofit instance
│   ├── api/
│   │   ├── AuthApiService.kt      ← Auth API metodları
│   │   ├── ProjectApiService.kt   ← Project API metodları
│   │   └── TaskApiService.kt      ← Task API metodları
│   └── model/
│       ├── AuthModels.kt          ← Login/Register models
│       ├── ProjectModels.kt       ← Project models
│       └── TaskModels.kt          ← Task models
└── repository/
    ├── NetworkResult.kt           ← API sonuç wrapper'ı
    ├── AuthRepository.kt          ← Auth business logic
    ├── ProjectRepository.kt       ← Project business logic
    └── TaskRepository.kt          ← Task business logic
```

---

## 📡 API Endpoint'leri

### Authentication

| Method | Endpoint | Açıklama | Token Gerekli |
|--------|----------|----------|---------------|
| `POST` | `/auth/register` | Yeni kullanıcı kaydı | ❌ |
| `POST` | `/auth/login` | Kullanıcı girişi | ❌ |
| `GET` | `/auth/me` | Mevcut kullanıcı bilgisi | ✅ |
| `PUT` | `/auth/update` | Profil güncelleme | ✅ |

### Projects

| Method | Endpoint | Açıklama | Token Gerekli |
|--------|----------|----------|---------------|
| `GET` | `/projects` | Tüm projeleri listele | ✅ |
| `GET` | `/projects/{id}` | Proje detayı | ✅ |
| `POST` | `/projects` | Yeni proje oluştur | ✅ |
| `PUT` | `/projects/{id}` | Projeyi güncelle | ✅ |
| `DELETE` | `/projects/{id}` | Projeyi sil | ✅ |

### Tasks

| Method | Endpoint | Açıklama | Token Gerekli |
|--------|----------|----------|---------------|
| `GET` | `/tasks` | Tüm görevleri listele | ✅ |
| `GET` | `/tasks/{id}` | Görev detayı | ✅ |
| `POST` | `/tasks` | Yeni görev oluştur | ✅ |
| `PUT` | `/tasks/{id}` | Görevi güncelle | ✅ |
| `DELETE` | `/tasks/{id}` | Görevi sil | ✅ |
| `PUT` | `/tasks/{id}/toggle` | Tamamlanma durumu değiştir | ✅ |
| `POST` | `/tasks/{id}/comments` | Yorum ekle | ✅ |

---

## 💻 Örnek Kullanım

### Login İşlemi

```kotlin
// ViewModel içinde:
class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository.getInstance()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            
            when (result) {
                is NetworkResult.Success -> {
                    // Token'ı kaydet
                    val token = result.data.token
                    saveToken(token)
                    
                    // Kullanıcıyı güncelle
                    _user.value = result.data.user
                    _isAuthenticated.value = true
                }
                is NetworkResult.Error -> {
                    // Hata mesajını göster
                    _errorMessage.value = result.message
                }
                is NetworkResult.Loading -> {
                    // Loading göster
                }
            }
        }
    }
}
```

### Proje Oluşturma

```kotlin
// ViewModel içinde:
fun createProject(title: String, description: String) {
    viewModelScope.launch {
        val projectRepository = ProjectRepository.getInstance { getToken() }
        
        val result = projectRepository.createProject(
            title = title,
            description = description,
            iconName = "folder",
            iconColor = "blue"
        )
        
        when (result) {
            is NetworkResult.Success -> {
                // Yeni proje eklendi
                val newProject = result.data
                // UI'ı güncelle
            }
            is NetworkResult.Error -> {
                // Hata göster
            }
        }
    }
}
```

---

## 🔐 Authentication Flow

```
1. Kullanıcı email/password girer
   ↓
2. AuthRepository.login() çağrılır
   ↓
3. Retrofit POST isteği: /auth/login
   ↓
4. Backend JWT token döner
   ↓
5. Token UserDefaults'a kaydedilir
   ↓
6. Sonraki tüm isteklerde Header'a eklenir:
   Authorization: Bearer eyJhbGc...
```

---

## 🧪 Test Etme

### 1. Backend'i Test Et

Terminal'de:

```bash
# Backend çalışıyor mu?
curl http://localhost:3000/api/health

# Beklenen yanıt:
# {"status":"OK","message":"Raptiye API is running"...}
```

### 2. Login Test Et

```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"testuser@mail.com","password":"123456"}'
```

### 3. Android Logcat'te Network Loglarını Görün

```
RetrofitClient içinde HttpLoggingInterceptor aktif.
Logcat'te tüm request/response'ları görebilirsiniz:

D/OkHttp: --> POST /auth/login
D/OkHttp: {"email":"test@test.com","password":"123456"}
D/OkHttp: <-- 200 OK
D/OkHttp: {"success":true,"token":"eyJ...","data":{...}}
```

---

## 🐛 Sorun Giderme

### Problem 1: "Connection Refused" Hatası

**Sebep:** Backend çalışmıyor veya URL yanlış.

**Çözüm:**
1. Backend'in çalıştığından emin olun: `node server.js`
2. URL'yi kontrol edin (emülatör için `10.0.2.2`)
3. Firewall ayarlarını kontrol edin

---

### Problem 2: "Unauthorized" (401) Hatası

**Sebep:** Token geçersiz veya expired.

**Çözüm:**
1. Tekrar login olun
2. Token'ın doğru kaydedildiğini kontrol edin
3. Token'ın doğru header'a eklendiğini kontrol edin

---

### Problem 3: "Invalid Credentials" Hatası

**Sebep:** Email/şifre yanlış.

**Çözüm:**
Test kullanıcılarını kullanın:
```
testuser@mail.com / 123456
bilgehan@mail.com / 123456
```

---

## 📊 Database Yapısı

Backend MongoDB kullanıyor. Şema:

```javascript
// User Schema
{
  uid: String,
  username: String,
  email: String,
  password: String (hashed),
  displayName: String,
  photoUrl: String,
  createdAt: Date,
  updatedAt: Date
}

// Project Schema
{
  _id: ObjectId,
  title: String,
  description: String,
  iconName: String,
  iconColor: String,
  status: String, // "Yapılacaklar", "Devam Ediyor", "Tamamlandı"
  dueDate: Date,
  teamLeader: ObjectId (ref: User),
  teamMembers: [ObjectId] (ref: User),
  createdBy: ObjectId (ref: User),
  createdAt: Date,
  updatedAt: Date
}

// Task Schema
{
  _id: ObjectId,
  title: String,
  description: String,
  projectId: ObjectId (ref: Project),
  assignee: ObjectId (ref: User),
  priority: String, // "Düşük", "Orta", "Yüksek"
  isCompleted: Boolean,
  dueDate: Date,
  comments: [CommentSchema],
  createdBy: ObjectId (ref: User),
  createdAt: Date,
  updatedAt: Date
}
```

---

## 🎯 Sonraki Adımlar

### ✅ Tamamlananlar
- [x] Retrofit setup
- [x] API service interfaces
- [x] Network models
- [x] Repository pattern
- [x] Auth entegrasyonu

### 🔄 Devam Edenler
- [ ] AuthViewModel'i gerçek API ile güncelle
- [ ] ProjectListScreen'i backend'e bağla
- [ ] TaskDetailScreen'i backend'e bağla
- [ ] Offline cache (Room Database)
- [ ] Push notifications
- [ ] Real-time updates (WebSocket)

---

## 📞 Destek

### iOS Ekibi ile İletişim

Backend ile ilgili sorunlarda iOS ekibine danışın:
- Backend repository: `project-auth-backend/`
- Backend README: `project-auth-backend/README.md`

### Android Ekibi

Network sorunları için:
- `RetrofitClient.kt`'yi inceleyin
- Logcat'te network loglarını kontrol edin
- `ApiConstants.kt`'de URL'yi doğrulayın

---

## 🔗 Kaynaklar

- [Retrofit Documentation](https://square.github.io/retrofit/)
- [OkHttp Documentation](https://square.github.io/okhttp/)
- [Backend Repository](https://github.com/Mobil-Uygulama-IOS/task-flow-3/tree/main/project-auth-backend)
- [iOS Backend Integration Guide](https://github.com/Mobil-Uygulama-IOS/task-flow-3/blob/main/BACKEND_INTEGRATION.md)

---

**Son Güncelleme:** 2025-10-29  
**Backend Versiyonu:** 1.0.0  
**Android Min SDK:** 24 (Android 7.0)
