# 🔧 Demo Modu (Backend Olmadan Test)

## 📱 Mevcut Durum

Uygulama şu anda **Demo Mod** desteğiyle çalışmaktadır. Backend sunucusu kapalı olsa bile uygulamayı test edebilirsiniz!

---

## 🎯 Demo Mod Özellikleri

### ✅ Çalışan Özellikler (Backend Kapalıyken)

| Özellik | Durum | Açıklama |
|---------|-------|----------|
| **Login** | ✅ Mock | Herhangi bir email/şifre ile giriş yapabilirsiniz |
| **Sign Up** | ✅ Mock | Yeni kullanıcı kaydı yapabilirsiniz |
| **Proje Listesi** | ✅ Sample Data | Örnek projeler görüntülenir |
| **Proje Detayı** | ✅ Çalışıyor | Projelere tıklayıp detayları görebilirsiniz |
| **UI/UX** | ✅ Tam | Tüm animasyonlar ve geçişler çalışır |
| **Tema** | ✅ Çalışıyor | Dark/Light mode |
| **Dil** | ✅ Çalışıyor | TR/EN dil değiştirme |

### ⚠️ Sınırlı Özellikler

| Özellik | Durum | Açıklama |
|---------|-------|----------|
| **Proje Ekleme** | ⚠️ Local | Backend'e kaydedilmez, sadece UI'da görünür |
| **Proje Güncelleme** | ⚠️ Local | Değişiklikler kalıcı olmaz |
| **Proje Silme** | ⚠️ Local | Reload sonrası geri gelir |

---

## 🚀 Kullanım

### Demo Modda Giriş:

```
Email: test@test.com
Password: 123456

# veya herhangi bir email/şifre!
```

### Otomatik Fallback Davranışı:

1. **Login Denerken:**
   ```
   Backend'e istek gönderilir
   ↓
   Başarısız olursa
   ↓
   Mock authentication devreye girer
   ↓
   "Demo modda devam ediliyor" mesajı gösterilir
   ↓
   Ana ekrana geçiş yapılır
   ```

2. **Proje Listesi Yüklenirken:**
   ```
   Backend'den projeler istenir
   ↓
   Başarısız olursa
   ↓
   Sample data (Project.sampleProjects) gösterilir
   ↓
   "Örnek veriler gösteriliyor" mesajı
   ```

---

## 🔄 Backend Aktif Olduğunda

### Backend çalıştırıldığında otomatik geçiş:

1. **Backend'i Başlatın:**
   ```bash
   cd project-auth-backend
   node server.js
   ```

2. **Uygulamayı Yeniden Başlatın:**
   - Uygulamayı kapatın
   - Tekrar açın

3. **Gerçek Verilerle Giriş Yapın:**
   ```
   Email: testuser@mail.com
   Password: 123456
   ```

4. **Artık Gerçek Backend Kullanılıyor:**
   - ✅ Projeler MongoDB'den gelir
   - ✅ Yeni projeler veritabanına kaydedilir
   - ✅ Tüm değişiklikler kalıcı olur

---

## 🧪 Test Senaryoları

### Senaryo 1: Backend Kapalı
```
1. Uygulamayı açın
2. Herhangi bir email/şifre girin
3. Login butonuna basın
4. Snackbar: "Demo modda devam ediliyor"
5. Ana ekran açılır
6. Sample projeler listelenir
```

### Senaryo 2: Backend Açık
```
1. Backend'i başlatın (node server.js)
2. Uygulamayı açın
3. Test kullanıcısı ile giriş yapın
4. Gerçek projeler MongoDB'den yüklenir
5. Yeni proje ekleme çalışır
```

---

## 📝 Kod İçi Fallback Mekanizması

### AuthViewModel.kt:
```kotlin
when (val result = authRepository.login(email, password)) {
    is NetworkResult.Success -> {
        // Gerçek backend yanıtı
        val user = loginResponse.user
        TokenManager.saveToken(loginResponse.token)
        // ...
    }
    is NetworkResult.Error -> {
        // GEÇİCİ: Backend yokken mock authentication
        val mockUser = User(
            uid = "mock_user_${System.currentTimeMillis()}",
            email = email,
            displayName = email.substringBefore("@")
        )
        TokenManager.saveToken("mock_jwt_token")
        // ...
    }
}
```

### ProjectListViewModel.kt:
```kotlin
when (val result = projectRepository.getProjects()) {
    is NetworkResult.Success -> {
        // Gerçek backend verileri
        val projects = result.data.projects.map { it.toProject() }
        // ...
    }
    is NetworkResult.Error -> {
        // GEÇİCİ: Backend yokken sample data
        _state.value = _state.value.copy(
            projects = Project.sampleProjects,
            errorMessage = "Backend bağlantısı kurulamadı, örnek veriler gösteriliyor"
        )
    }
}
```

---

## ⚙️ Demo Modu Kapatma (Backend Hazırken)

Fallback kodunu kaldırmak için:

### 1. AuthViewModel.kt:
```kotlin
// ❌ KALDIR:
is NetworkResult.Error -> {
    // GEÇİCİ: Backend yokken mock authentication
    val mockUser = User(...)
    // ...
}

// ✅ GERİ EKLE:
is NetworkResult.Error -> {
    _authState.value = _authState.value.copy(
        isAuthenticated = false,
        errorMessage = result.message
    )
}
```

### 2. ProjectListViewModel.kt:
```kotlin
// ❌ KALDIR:
is NetworkResult.Error -> {
    _state.value = _state.value.copy(
        projects = Project.sampleProjects, // Bu satırı kaldır
        errorMessage = "Backend bağlantısı kurulamadı..."
    )
}

// ✅ GERİ EKLE:
is NetworkResult.Error -> {
    _state.value = _state.value.copy(
        isLoading = false,
        errorMessage = result.message
    )
}
```

---

## 🎓 Demo Modun Avantajları

1. **Geliştirme Kolaylığı**: Backend olmadan UI test edebilirsiniz
2. **Animasyon Kontrolü**: Tüm geçişleri ve animasyonları görebilirsiniz
3. **Bağımsız Çalışma**: iOS ekibine bağımlı kalmadan ilerleyebilirsiniz
4. **Hızlı Prototipleme**: Değişiklikleri hemen görebilirsiniz

---

## 📞 Yardım

**Sorun mu yaşıyorsunuz?**
- Backend mesajları Snackbar'da görünür
- Logcat'te network loglarını kontrol edin
- `BACKEND_INTEGRATION.md` dosyasına bakın

**Backend hazır olduğunda:**
- Fallback kodunu kaldırın
- Test kullanıcılarıyla giriş yapın
- Gerçek verileri görün!
