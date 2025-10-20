# 🤝 Katkıda Bulunma Rehberi

Task-Flow Android projesine katkıda bulunmak istediğiniz için teşekkürler!

## 📝 Kod Standartları

### Kotlin Style Guide

```kotlin
// ✅ İyi
class ProjectListScreen {
    private val TAG = "ProjectListScreen"
    
    fun loadProjects() {
        viewModelScope.launch {
            // Implementation
        }
    }
}

// ❌ Kötü
class projectlistscreen {
    fun LoadProjects() { }
}
```

### Dosya Yapısı

```
Her ekran için:
├── [Screen]Screen.kt      → UI composable
├── [Screen]ViewModel.kt   → Business logic
└── [Screen]State.kt       → State definitions (opsiyonel)
```

### Naming Conventions

| Tür | Örnek |
|-----|-------|
| Class | `ProjectListScreen`, `AuthViewModel` |
| Function | `loadProjects()`, `signIn()` |
| Variable | `projectList`, `isLoading` |
| Constant | `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT` |
| Composable | `ProjectCard()`, `TaskItem()` |

## 🔄 Git Workflow

### 1. Branch Oluşturma

```bash
# Feature branch
git checkout -b feature/proje-filtreleme

# Bug fix branch
git checkout -b bugfix/login-hatasi

# Hotfix branch
git checkout -b hotfix/crash-duzeltme
```

### 2. Commit Messages

Format: `<type>(<scope>): <subject>`

```bash
# Örnekler
git commit -m "feat(project): Proje filtreleme özelliği eklendi"
git commit -m "fix(auth): Login ekranı hata mesajı düzeltildi"
git commit -m "refactor(ui): Theme yapısı yeniden düzenlendi"
git commit -m "docs(readme): Kurulum adımları güncellendi"
```

**Types:**
- `feat`: Yeni özellik
- `fix`: Bug düzeltmesi
- `refactor`: Kod iyileştirmesi
- `style`: Format değişiklikleri
- `docs`: Dokümantasyon
- `test`: Test ekleme/düzeltme
- `chore`: Build/dependency güncellemeleri

### 3. Pull Request

```markdown
## Değişiklik Açıklaması
Proje filtreleme özelliği eklendi

## Değişiklikler
- ProjectListScreen'e filtre dropdown'u eklendi
- Filtreleme logic'i implement edildi
- UI testleri eklendi

## Test Edildi
- [x] Emülatörde test edildi
- [x] Gerçek cihazda test edildi
- [x] Dark mode'da test edildi

## Ekran Görüntüleri
[Ekran görüntüsü ekleyin]
```

## 🧪 Testing

### Unit Test Örneği

```kotlin
@Test
fun `project filtreleme dogru calisir`() {
    val projects = listOf(
        Project(title = "Proje 1", isCompleted = false),
        Project(title = "Proje 2", isCompleted = true)
    )
    
    val filtered = projects.filter { !it.isCompleted }
    
    assertEquals(1, filtered.size)
    assertEquals("Proje 1", filtered[0].title)
}
```

### UI Test Örneği

```kotlin
@Test
fun loginScreen_emailGirisi_basarili() {
    composeTestRule.setContent {
        LoginScreen(
            onNavigateToMain = {},
            onNavigateToSignUp = {}
        )
    }
    
    composeTestRule
        .onNodeWithTag("emailField")
        .performTextInput("test@example.com")
        
    composeTestRule
        .onNodeWithTag("emailField")
        .assertTextEquals("test@example.com")
}
```

## 📋 Checklist

Kod göndermeden önce:

- [ ] Kod çalışıyor mu?
- [ ] Hata mesajları var mı?
- [ ] Dark mode'da test edildi mi?
- [ ] Türkçe ve İngilizce dil desteği var mı?
- [ ] Animasyonlar düzgün çalışıyor mu?
- [ ] Yorum satırları gereksiz kod var mı?
- [ ] Git commit mesajı açıklayıcı mı?

## 🎨 UI Guidelines

### Spacing

```kotlin
// Tutarlı spacing kullanın
padding(horizontal = 20.dp, vertical = 16.dp)
Arrangement.spacedBy(12.dp)
```

### Colors

```kotlin
// Material Theme colors kullanın
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.onBackground

// Hardcoded color sadece gerekiyorsa
Color(0xFF4CAF50) // Success green
```

### Typography

```kotlin
Text(
    text = "Başlık",
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onBackground
)
```

## 🐛 Bug Report

Issue açarken şu formatı kullanın:

```markdown
## Bug Açıklaması
Login ekranında şifre göster butonu çalışmıyor

## Adımlar
1. Login ekranını aç
2. Şifre gir
3. Göz ikonuna tıkla

## Beklenen Davranış
Şifre görünür olmalı

## Gerçek Davranış
Hiçbir şey olmuyor

## Ekran Görüntüsü
[Ekran görüntüsü ekleyin]

## Ortam
- Android Studio: Hedgehog
- Emulator: Pixel 6 - API 34
- OS: Windows 11
```

## 💡 Feature Request

```markdown
## Özellik Açıklaması
Projeleri drag-drop ile sıralama

## Neden Gerekli
Kullanıcılar manuel sıralama yapabilmeli

## Önerilen Çözüm
LazyColumn'da reorderable modifier kullanılabilir

## Alternatifler
- Up/Down butonları
- Sıra numarası girişi
```

## 📚 Kaynaklar

- [Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html)
- [Jetpack Compose Guidelines](https://developer.android.com/jetpack/compose/guidelines)
- [Material Design 3](https://m3.material.io/)
- [Git Commit Conventions](https://www.conventionalcommits.org/)

## 🎓 Öğrenme Kaynakları

### Jetpack Compose
- [Official Documentation](https://developer.android.com/jetpack/compose)
- [Compose Pathway](https://developer.android.com/courses/pathways/compose)

### MVVM Pattern
- [Android MVVM Guide](https://developer.android.com/topic/architecture)

### Kotlin Coroutines
- [Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

## 👥 İletişim

- GitHub Issues: Sorular ve bug reports
- Pull Requests: Kod katkıları
- Discussions: Genel tartışmalar

---

**Mutlu Kodlamalar! 🚀**
