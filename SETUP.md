# 🚀 Task-Flow Android Setup Rehberi

Bu döküman, projeyi arkadaşlarınızla senkronize etmeniz ve çalıştırmanız için detaylı adımları içerir.

## 📋 İçindekiler

1. [Projeyi İndirme](#projeyi-indirme)
2. [İlk Kurulum](#ilk-kurulum)
3. [Güncellemeleri Alma](#güncellemeleri-alma)
4. [Yaygın Sorunlar](#yaygın-sorunlar)

---

## 📥 Projeyi İndirme

### Yöntem 1: Git Clone (Önerilen)

```bash
# Terminal veya PowerShell'de
cd Desktop
git clone https://github.com/Mobil-Uygulama-Android/Task-Flow-Android.git
cd Task-Flow-Android
```

### Yöntem 2: Mevcut Projeyi Güncelleme

Eğer projeniz zaten varsa:

```bash
cd "c:\Users\KULLANICI_ADI\OneDrive\Desktop\Task-Flow-Android\Task-Flow-Android"

# Uzak repository'yi kontrol et
git remote -v

# Güncellemeleri çek
git fetch origin
git pull origin main
```

---

## 🛠 İlk Kurulum

### 1. Gereksinimler

- ✅ Android Studio Hedgehog (2023.1.1) veya üzeri
- ✅ JDK 17 (Android Studio ile birlikte gelir)
- ✅ Minimum 8GB RAM
- ✅ 5GB boş disk alanı

### 2. Android Studio Ayarları

```plaintext
File → Settings (Ctrl+Alt+S)
├── Appearance & Behavior → System Settings → Android SDK
│   └── SDK Platforms: Android 14.0 (API 34), Android 15.0 (API 35)
│   └── SDK Tools: Android SDK Build-Tools 34+
└── Build, Execution, Deployment → Build Tools → Gradle
    └── Gradle JDK: jbr-17 (veya JDK 17)
```

### 3. Projeyi Açma

1. **Android Studio'yu başlat**
2. **File → Open**
3. **Task-Flow-Android klasörünü seç** (build.gradle.kts'nin olduğu klasör)
4. **"Trust Project"** butonuna tıkla
5. **Gradle sync** otomatik başlayacak (1-5 dakika sürebilir)

### 4. İlk Çalıştırma

```plaintext
1. Tools → Device Manager
2. Create Device → Pixel 6 (veya başka bir cihaz)
3. System Image: Android 14.0 (API 34) - x86_64
4. Finish
5. Run butonuna bas (Shift+F10)
```

---

## 🔄 Güncellemeleri Alma

### Arkadaşlarınızın Kodlarını Çekmek

```bash
# 1. Mevcut değişikliklerinizi kaydedin
git status

# 2. Eğer değişiklikleriniz varsa, commit edin
git add .
git commit -m "Değişikliklerim"

# 3. Güncellemeleri çekin
git pull origin main

# 4. Çakışma varsa, çözün ve commit edin
```

### Otomatik Güncelleme (VS Code)

1. **Terminal'i aç** (Ctrl+`)
2. **Komutu çalıştır:**
```bash
git fetch origin
git pull origin main
```

### Android Studio'da

```plaintext
VCS → Git → Pull
└── Remote: origin
└── Branch: main
└── OK
```

---

## ⚠️ Yaygın Sorunlar

### 1. "Gradle sync failed"

**Çözüm:**
```bash
# Cache temizle
./gradlew clean

# Gradle wrapper'ı güncelle
./gradlew wrapper --gradle-version=8.2

# Android Studio'yu yeniden başlat
File → Invalidate Caches → Invalidate and Restart
```

### 2. "SDK not found"

**Çözüm:**
```plaintext
File → Project Structure → SDK Location
└── Android SDK location: C:\Users\[USER]\AppData\Local\Android\Sdk
```

### 3. "Git pull conflict"

**Çözüm:**
```bash
# Mevcut branch'inizi yedekleyin
git branch backup-$(date +%Y%m%d)

# Çakışmaları force ile çözün (DİKKAT: Yerel değişiklikler kaybolur)
git fetch origin
git reset --hard origin/main

# VEYA manuel çözüm:
git pull origin main
# Çakışma olan dosyaları düzenleyin
git add .
git commit -m "Merge conflicts resolved"
```

### 4. "Emulator is slow"

**Çözüm:**
```plaintext
AVD Manager → Edit (pencil icon) → Show Advanced Settings
├── Graphics: Hardware - GLES 2.0
├── Boot option: Cold boot
└── RAM: 4096 MB (veya daha fazla)
```

### 5. "App crashes on launch"

**Çözüm:**
```bash
# 1. Clean build
./gradlew clean

# 2. Rebuild
Build → Rebuild Project

# 3. Uninstall old app
adb uninstall tr.edu.bilimankara20307006.taskflow

# 4. Fresh install
./gradlew installDebug
```

---

## 🔍 Proje Durumunu Kontrol Etme

### Git Durumu

```bash
# Hangi branch'tesiniz?
git branch

# Son commit'ler
git log --oneline -5

# Değişiklikleriniz
git status

# Uzak repository ile fark
git fetch origin
git log HEAD..origin/main --oneline
```

### Gradle Durumu

```bash
# Gradle version
./gradlew --version

# Dependencies listesi
./gradlew app:dependencies

# Task listesi
./gradlew tasks
```

---

## 📊 Kodunuzu Paylaşma

### 1. Değişikliklerinizi Commit Edin

```bash
# Değişiklikleri görün
git status

# Dosyaları staging area'ya ekleyin
git add .

# Commit mesajı yazın
git commit -m "feat: Yeni özellik eklendi"
```

### 2. GitHub'a Push Edin

```bash
# Ana branch'e push edin
git push origin main

# VEYA yeni branch oluşturun
git checkout -b feature/yeni-ozellik
git push origin feature/yeni-ozellik
```

---

## 🎯 Best Practices

### Commit Mesajları

```
feat: Yeni özellik ekle
fix: Bug düzeltmesi
refactor: Kod iyileştirmesi
docs: Dokümantasyon güncelleme
style: Kod stili düzeltme
test: Test ekleme
```

### Branch Stratejisi

```
main           → Stabil kod
develop        → Geliştirme branch
feature/*      → Yeni özellikler
bugfix/*       → Bug düzeltmeleri
```

### Günlük Workflow

```bash
# 1. Her gün işe başlarken
git pull origin main

# 2. Çalışırken sık sık kaydet
git add .
git commit -m "Kısmi çalışma kaydı"

# 3. Gün sonunda paylaş
git push origin main
```

---

## 🆘 Yardım

### Terminal Komutları Çalışmıyor?

**Windows PowerShell:**
```powershell
# Git'in kurulu olduğunu kontrol et
git --version

# Eğer kurulu değilse: https://git-scm.com/download/win
```

### Android Studio İpuçları

```plaintext
# Hızlı arama
Double Shift

# Dosya ara
Ctrl+Shift+N

# Kod formatla
Ctrl+Alt+L

# Terminal aç
Alt+F12

# Build çalıştır
Shift+F10
```

---

## 📞 İletişim

Sorun yaşarsanız:
1. GitHub Issues açın
2. Ekip arkadaşlarınıza sorun
3. Bu dökümanı güncelleyin

---

**Son Güncelleme:** 2025-01-20
**Versiyon:** 1.0.0
