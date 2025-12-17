# iOS Bildirim Sistemi Kurulum Rehberi

## Problem
Android'den iOS'a gönderilen bildirimler düşmüyor çünkü FCM legacy API iOS için düzgün çalışmıyor.

## Çözüm: Firestore Real-time Listener

Android ve iOS uygulamaları Firestore'daki `notifications` collection'ını dinleyecek. Yeni bildirim geldiğinde local notification gösterecek.

---

## iOS Tarafında Yapılması Gerekenler

### 1. NotificationManager.swift Oluştur/Güncelle

```swift
import FirebaseFirestore
import FirebaseAuth
import UserNotifications

class NotificationManager: ObservableObject {
    static let shared = NotificationManager()
    private let db = Firestore.firestore()
    private var notificationListener: ListenerRegistration?
    
    // Firestore bildirim listener'ını başlat
    func startListeningForNotifications() {
        guard let userId = Auth.auth().currentUser?.uid else {
            print("❌ Kullanıcı oturum açmamış")
            return
        }
        
        print("📡 Bildirim listener başlatılıyor: \(userId)")
        
        // Önceki listener'ı temizle
        notificationListener?.remove()
        
        // Firestore'dan bildirimleri dinle
        notificationListener = db.collection("notifications")
            .whereField("userId", isEqualTo: userId)
            .addSnapshotListener { [weak self] snapshot, error in
                if let error = error {
                    print("❌ Bildirim dinleme hatası: \(error.localizedDescription)")
                    return
                }
                
                guard let documents = snapshot?.documents else {
                    print("⚠️ Snapshot boş")
                    return
                }
                
                print("🔍 \(documents.count) bildirim bulundu")
                
                // Yeni bildirimleri göster (metadata.hasPendingWrites ile backend'den gelenleri ayır)
                for document in documents {
                    guard !snapshot!.metadata.hasPendingWrites else { continue }
                    
                    let data = document.data()
                    let isRead = data["isRead"] as? Bool ?? false
                    
                    // Okunmamış bildirimleri göster
                    if !isRead {
                        let title = data["title"] as? String ?? "Bildirim"
                        let message = data["message"] as? String ?? ""
                        
                        print("📬 Yeni bildirim: \(title)")
                        self?.showLocalNotification(title: title, body: message, data: data)
                    }
                }
            }
    }
    
    // Local notification göster
    private func showLocalNotification(title: String, body: String, data: [String: Any]) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default
        content.badge = 1
        
        // Extra data ekle (tıklanınca kullanılacak)
        content.userInfo = data
        
        let request = UNNotificationRequest(
            identifier: UUID().uuidString,
            content: content,
            trigger: nil // Hemen göster
        )
        
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("❌ Local notification hatası: \(error.localizedDescription)")
            } else {
                print("✅ Local notification gösterildi")
            }
        }
    }
    
    // Listener'ı durdur
    func stopListening() {
        notificationListener?.remove()
        print("🛑 Bildirim listener durduruldu")
    }
}
```

### 2. AppDelegate.swift'te Bildirim İzni İste

```swift
import FirebaseCore
import UserNotifications

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    func application(_ application: UIApplication, 
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        // Firebase yapılandır
        FirebaseApp.configure()
        
        // Bildirim izni iste
        requestNotificationPermission()
        
        // Bildirim delegate'ini ayarla
        UNUserNotificationCenter.current().delegate = self
        
        return true
    }
    
    private func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if granted {
                print("✅ Bildirim izni verildi")
            } else {
                print("❌ Bildirim izni reddedildi: \(error?.localizedDescription ?? "")")
            }
        }
    }
}

// Bildirim delegate
extension AppDelegate: UNUserNotificationCenterDelegate {
    // Uygulama açıkken bildirim gelirse göster
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .sound, .badge])
    }
    
    // Bildirime tıklanınca
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        print("📱 Bildirime tıklandı: \(userInfo)")
        
        // TODO: Bildirimin tipine göre ilgili ekrana yönlendir
        // Örnek: PROJECT_INVITATION -> Projeler ekranı
        
        completionHandler()
    }
}
```

### 3. Ana View'da Listener'ı Başlat

```swift
import SwiftUI

struct ContentView: View {
    @StateObject private var notificationManager = NotificationManager.shared
    
    var body: some View {
        TabView {
            // Tab içerikleri...
        }
        .onAppear {
            // Kullanıcı giriş yaptığında listener'ı başlat
            if Auth.auth().currentUser != nil {
                notificationManager.startListeningForNotifications()
            }
        }
        .onDisappear {
            notificationManager.stopListening()
        }
    }
}
```

### 4. Proje Daveti Gönderirken Verileri Eksiksiz Yaz

```swift
func sendProjectInvitation(toUserId: String, projectId: String, projectName: String) async throws {
    guard let currentUser = Auth.auth().currentUser else {
        throw NSError(domain: "NotificationManager", code: 401)
    }
    
    // Kullanıcı adını Firestore'dan al
    let inviterName: String
    do {
        let userDoc = try await db.collection("users").document(currentUser.uid).getDocument()
        inviterName = userDoc.data()?["fullName"] as? String 
            ?? userDoc.data()?["email"] as? String 
            ?? currentUser.displayName 
            ?? "Bilinmeyen Kullanıcı"
    } catch {
        inviterName = currentUser.displayName ?? currentUser.email ?? "Bilinmeyen Kullanıcı"
    }
    
    let notificationId = UUID().uuidString
    let currentTime = Date().timeIntervalSince1970 * 1000 // Milisaniye
    
    let notificationData: [String: Any] = [
        "id": notificationId,
        "title": "Proje Daveti",
        "message": "\(inviterName) sizi \"\(projectName)\" projesine davet etti",
        "type": "PROJECT_INVITATION",
        "userId": toUserId,
        "fromUserId": currentUser.uid,
        "fromUserName": inviterName,
        "projectId": projectId,
        "projectName": projectName,
        "isRead": false,
        "invitationStatus": "pending",
        "createdAt": currentTime,
        "data": [:]
    ]
    
    try await db.collection("notifications").document(notificationId).setData(notificationData)
    print("✅ Bildirim Firestore'a yazıldı")
}
```

---

## Test Adımları

1. iOS uygulamasını başlat
2. Giriş yap
3. Logları kontrol et: `📡 Bildirim listener başlatılıyor`
4. Android'den iOS kullanıcısına proje daveti gönder
5. iOS'ta local notification görmeli ve logda: `📬 Yeni bildirim: Proje Daveti`

---

## Sorun Giderme

**Bildirim düşmüyorsa:**
- iOS Ayarlar > [Uygulama] > Bildirimler: Bildirimler açık mı?
- Xcode Console'da listener logları görünüyor mu?
- Firestore Console'da `notifications` collection'ına veri yazılıyor mu?
- `userId` alanı doğru kullanıcıya ait mi?

**Tarih yanlışsa:**
- `createdAt` alanı milisaniye cinsinden olmalı (Date().timeIntervalSince1970 * 1000)
- serverTimestamp kullanmayın, ilk yazımda null gelir

**İsim/Proje adı boşsa:**
- `fromUserName` ve `projectName` alanları boş string değil, gerçek değerler içermeli
- iOS'ta Firestore'dan kullanıcı adını çekin (displayName yerine)
