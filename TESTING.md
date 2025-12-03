# 🤖 Android Test Suite

iOS ekibinin test yapısına paralel olarak oluşturulmuş kapsamlı test suite'i.

## 📋 Test İçeriği

### 🧪 Unit Tests (JUnit)

#### 1. AuthViewModelTest.kt
iOS'taki `AuthViewModelTests.swift` ile aynı testleri içerir:

- ✅ ViewModel başlangıç değerleri
- ✅ Email format validasyonu (geçerli/geçersiz)
- ✅ Şifre uzunluk kontrolü (min 6 karakter)
- ✅ Boş şifre kontrolü
- ✅ User session yönetimi
- ✅ Loading state değişimleri
- ✅ Error message yönetimi

#### 2. ProjectManagerTest.kt
iOS'taki `ProjectManagerTests.swift` ile aynı testleri içerir:

- ✅ ProjectManager başlangıç değerleri
- ✅ Project modeli oluşturma ve özellikleri
- ✅ ProjectStatus enum testleri (TODO/IN_PROGRESS/COMPLETED)
- ✅ Task ilerleme hesaplaması (progressPercentage)
- ✅ Loading ve error state yönetimi
- ✅ Projects dizisi operasyonları
- ✅ Due date formatting

### 🎨 UI/E2E Tests (Jetpack Compose UI Test)

#### 3. LoginScreenTest.kt
iOS'taki `EnhancedLoginViewUITests.swift` ile aynı testleri içerir:

- ✅ Login ekranı görüntülenme
- ✅ TextField'ların varlığı (Email, Password)
- ✅ Button'ların varlığı (Login, Sign Up)
- ✅ UI elementlerinin etkileşimi
- ✅ Text input işlemleri
- ✅ Button tıklama testleri

#### 4. ProjectListScreenTest.kt
iOS'taki `ProjectListViewUITests.swift` ile aynı testleri içerir:

- ✅ Navigation elementlerinin varlığı
- ✅ Proje listesi görüntüleme
- ✅ Scroll işlevselliği
- ✅ İnteraktif elementler (button, list items)
- ✅ Boş liste durumu
- ✅ Tab bar kontrolü

#### 5. ProfileScreenTest.kt
iOS'taki `ProfileViewUITests.swift` ile aynı testleri içerir:

- ✅ UI elementlerinin varlığı
- ✅ Kullanıcı bilgileri gösterimi
- ✅ Profil düzenleme butonu
- ✅ Çıkış yapma butonu
- ✅ Ayarlar menüsü
- ✅ Scroll işlevselliği

## 🚀 Testleri Çalıştırma

### Unit Testleri Çalıştırma

```bash
# Tüm unit testleri çalıştır
./gradlew test

# Sadece debug variant için
./gradlew testDebugUnitTest

# Belirli bir test sınıfını çalıştır
./gradlew test --tests AuthViewModelTest

# Test raporu görüntüleme
open app/build/reports/tests/testDebugUnitTest/index.html
```

### UI Testleri Çalıştırma

```bash
# Emulator veya gerçek cihaz bağlı olmalı

# Tüm UI testlerini çalıştır
./gradlew connectedAndroidTest

# Belirli bir test sınıfını çalıştır
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=tr.edu.bilimankara20307006.taskflow.ui.auth.LoginScreenTest

# UI test raporu görüntüleme
open app/build/reports/androidTests/connected/index.html
```

### Android Studio'dan Çalıştırma

1. Test dosyasını açın
2. Class veya method yanındaki yeşil play butonuna tıklayın
3. "Run 'TestClassName'" seçeneğini seçin

## 🤖 GitHub Actions CI/CD

iOS ekibinin workflow'una paralel olarak `.github/workflows/android-ci.yml` dosyası oluşturuldu.

### Özellikler:

- ✅ Her push ve PR'da otomatik çalışma
- ✅ JDK 17 kurulumu
- ✅ Android SDK yapılandırması
- ✅ Gradle cache
- ✅ Lint kontrolü
- ✅ Unit testleri çalıştırma
- ✅ Debug APK build
- ✅ Test raporlarını artifact olarak yükleme
- ✅ Opsiyonel: Emulator'da UI testleri

### GitHub Secrets Ayarlama

Firebase için `google-services.json` dosyasını GitHub secrets'a ekleyin:

1. GitHub repository > Settings > Secrets and variables > Actions
2. "New repository secret" tıklayın
3. Name: `GOOGLE_SERVICES_JSON`
4. Value: `google-services.json` dosyasının içeriğini yapıştırın
5. "Add secret" tıklayın

## 📦 Test Bağımlılıkları

`app/build.gradle.kts` dosyasına eklenen test bağımlılıkları:

```kotlin
// Unit Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("androidx.arch.core:core-testing:2.2.0")
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("com.google.truth:truth:1.1.5")

// UI Testing
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("androidx.navigation:navigation-testing:2.7.6")
androidTestImplementation("io.mockk:mockk-android:1.13.8")
```

## 📊 Test Coverage

Test coverage raporu almak için:

```bash
# Coverage raporu oluştur
./gradlew testDebugUnitTestCoverage

# Raporu görüntüle
open app/build/reports/coverage/test/debug/index.html
```

## 🔄 iOS ile Karşılaştırma

| Feature | iOS | Android |
|---------|-----|---------|
| Unit Tests | ✅ XCTest | ✅ JUnit |
| UI Tests | ✅ XCUITest | ✅ Compose UI Test |
| Mocking | ✅ (protocol-based) | ✅ MockK |
| CI/CD | ✅ GitHub Actions | ✅ GitHub Actions |
| Code Coverage | ✅ Xcode | ✅ JaCoCo |
| Test Framework | ✅ Swift Testing | ✅ Kotlin + JUnit |

## 📝 Notlar

- iOS ekibi ile aynı test senaryoları uygulanmıştır
- Test isimleri ve yapısı iOS testleriyle paralel tutulmuştur
- Her iki platformda da aynı özelliklerin test edildiğinden emin olunmuştur
- CI/CD workflow'ları benzer şekilde yapılandırılmıştır

## 🐛 Sorun Giderme

### "Firebase is not initialized" hatası
- `google-services.json` dosyasının `app/` klasöründe olduğundan emin olun
- GitHub Actions için `GOOGLE_SERVICES_JSON` secret'ını ayarlayın

### UI testleri çalışmıyor
- Emulator veya cihazın bağlı olduğundan emin olun
- Developer options > Animator duration scale = 1x

### Test raporu görünmüyor
- `./gradlew test` komutunu çalıştırdıktan sonra
- `app/build/reports/tests/` klasörüne gidin

## 📚 Kaynaklar

- [Android Testing Documentation](https://developer.android.com/training/testing)
- [Jetpack Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [MockK Documentation](https://mockk.io/)
- [JUnit 4 Documentation](https://junit.org/junit4/)
