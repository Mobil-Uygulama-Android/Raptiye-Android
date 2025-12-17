# Cross-Platform Push Notifications Setup Guide

## Problem Analysis

### Current Issues
1. **iOS → Android**: Missing sender name, project name, and incorrect timestamp (1970)
2. **Android → iOS**: No notification received at all (FCM Legacy API doesn't work with APNs)

### Root Causes
- Inconsistent field naming between platforms
- iOS not saving FCM token to Firestore
- Using `serverTimestamp()` which returns null initially
- FCM Legacy API (server key) doesn't properly support iOS APNs
- No unified payload structure

---

## Solution Architecture

### Approach: Firestore + Real-time Listeners

Instead of FCM push notifications (which require Firebase Cloud Functions for iOS), use Firestore as the notification delivery mechanism:

1. Sender writes notification to Firestore `notifications` collection
2. Both iOS and Android listen to their notifications via real-time listeners
3. Show local notifications when new data arrives
4. No FCM server key needed, no Cloud Functions required

**Benefits:**
- 100% reliable cross-platform delivery
- No server-side code needed
- Works identically on iOS and Android
- No APNs certificate configuration

---

## Unified Data Model

### Firestore Schema

```json
{
  "id": "uuid-string",
  "type": "PROJECT_INVITATION",
  "userId": "recipient-user-id",
  "fromUserId": "sender-user-id",
  "fromUserName": "John Doe",
  "title": "Project Invitation",
  "message": "John Doe invited you to \"Mobile App\" project",
  "projectId": "project-uuid",
  "projectName": "Mobile App",
  "taskId": null,
  "taskTitle": null,
  "isRead": false,
  "invitationStatus": "pending",
  "createdAt": 1702656000000,
  "data": {}
}
```

### Field Standardization

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `id` | String | Unique notification ID | UUID |
| `type` | String | Notification type | "PROJECT_INVITATION" |
| `userId` | String | Recipient user ID | Firebase Auth UID |
| `fromUserId` | String | Sender user ID | Firebase Auth UID |
| `fromUserName` | String | Sender display name | "John Doe" |
| `title` | String | Notification title | "Project Invitation" |
| `message` | String | Full message | "John Doe invited you..." |
| `projectId` | String? | Related project ID | UUID or null |
| `projectName` | String? | Project name | "Mobile App" or null |
| `taskId` | String? | Related task ID | UUID or null |
| `taskTitle` | String? | Task title | "Implement feature" or null |
| `isRead` | Boolean | Read status | false |
| `invitationStatus` | String? | Invitation state | "pending"/"accepted"/"declined" |
| `createdAt` | Number | Timestamp in milliseconds | 1702656000000 |
| `data` | Map | Additional metadata | {} |

---

## Android Implementation (Kotlin)

### NotificationManager.kt

```kotlin
suspend fun sendProjectInvitation(
    toUserId: String,
    projectId: String,
    projectName: String
): Result<Unit> {
    val currentUser = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
    
    // Get sender name from Firestore users collection
    val senderName = try {
        val userDoc = db.collection("users")
            .document(currentUser.uid)
            .get()
            .await()
        userDoc.getString("fullName") 
            ?: userDoc.getString("email") 
            ?: currentUser.displayName 
            ?: "Unknown User"
    } catch (e: Exception) {
        currentUser.displayName ?: currentUser.email ?: "Unknown User"
    }
    
    val notificationId = UUID.randomUUID().toString()
    val currentTime = System.currentTimeMillis() // NOT serverTimestamp()
    
    val notificationData = mapOf(
        "id" to notificationId,
        "type" to "PROJECT_INVITATION",
        "userId" to toUserId,
        "fromUserId" to currentUser.uid,
        "fromUserName" to senderName,
        "title" to "Project Invitation",
        "message" to "$senderName invited you to \"$projectName\" project",
        "projectId" to projectId,
        "projectName" to projectName,
        "isRead" to false,
        "invitationStatus" to "pending",
        "createdAt" to currentTime,
        "data" to emptyMap<String, Any>()
    )
    
    return try {
        db.collection("notifications")
            .document(notificationId)
            .set(notificationData)
            .await()
        
        println("✅ Notification written to Firestore")
        Result.success(Unit)
    } catch (e: Exception) {
        println("❌ Failed to send notification: ${e.message}")
        Result.failure(e)
    }
}
```

### Real-time Listener (NotificationViewModel.kt)

```kotlin
fun startListening(userId: String) {
    notificationListener = db.collection("notifications")
        .whereEqualTo("userId", userId)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Listener error: ${error.message}")
                return@addSnapshotListener
            }
            
            val notifications = snapshot?.documents?.mapNotNull { doc ->
                Notification.fromMap(doc.data ?: return@mapNotNull null)
            } ?: emptyList()
            
            // Update UI state
            _state.value = _state.value.copy(
                notifications = notifications.sortedByDescending { it.createdAt },
                unreadCount = notifications.count { !it.isRead }
            )
            
            // Show local notification for new unread items
            notifications.filter { !it.isRead }.forEach { notification ->
                showLocalNotification(notification)
            }
        }
}

private fun showLocalNotification(notification: Notification) {
    // Android local notification using NotificationCompat
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    
    val notificationCompat = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(notification.title)
        .setContentText(notification.message)
        .setSmallIcon(R.drawable.ic_notification)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()
    
    notificationManager.notify(notification.id.hashCode(), notificationCompat)
}
```

---

## iOS Implementation (Swift)

### NotificationManager.swift

```swift
import FirebaseFirestore
import FirebaseAuth
import UserNotifications

class NotificationManager: ObservableObject {
    static let shared = NotificationManager()
    private let db = Firestore.firestore()
    private var notificationListener: ListenerRegistration?
    
    // Send project invitation
    func sendProjectInvitation(
        toUserId: String,
        projectId: String,
        projectName: String
    ) async throws {
        guard let currentUser = Auth.auth().currentUser else {
            throw NSError(domain: "NotificationManager", code: 401)
        }
        
        // Get sender name from Firestore
        let senderName: String
        do {
            let userDoc = try await db.collection("users")
                .document(currentUser.uid)
                .getDocument()
            
            senderName = userDoc.data()?["fullName"] as? String
                ?? userDoc.data()?["email"] as? String
                ?? currentUser.displayName
                ?? "Unknown User"
        } catch {
            senderName = currentUser.displayName ?? currentUser.email ?? "Unknown User"
        }
        
        let notificationId = UUID().uuidString
        let currentTime = Date().timeIntervalSince1970 * 1000 // Milliseconds
        
        let notificationData: [String: Any] = [
            "id": notificationId,
            "type": "PROJECT_INVITATION",
            "userId": toUserId,
            "fromUserId": currentUser.uid,
            "fromUserName": senderName,
            "title": "Project Invitation",
            "message": "\(senderName) invited you to \"\(projectName)\" project",
            "projectId": projectId,
            "projectName": projectName,
            "isRead": false,
            "invitationStatus": "pending",
            "createdAt": currentTime,
            "data": [:]
        ]
        
        try await db.collection("notifications")
            .document(notificationId)
            .setData(notificationData)
        
        print("✅ Notification written to Firestore")
    }
    
    // Start listening for notifications
    func startListening() {
        guard let userId = Auth.auth().currentUser?.uid else { return }
        
        notificationListener?.remove()
        
        notificationListener = db.collection("notifications")
            .whereField("userId", isEqualTo: userId)
            .addSnapshotListener { [weak self] snapshot, error in
                if let error = error {
                    print("❌ Listener error: \(error.localizedDescription)")
                    return
                }
                
                guard let documents = snapshot?.documents else { return }
                
                // Process new notifications
                for document in documents {
                    guard !snapshot!.metadata.hasPendingWrites else { continue }
                    
                    let data = document.data()
                    let isRead = data["isRead"] as? Bool ?? false
                    
                    if !isRead {
                        self?.showLocalNotification(data: data)
                    }
                }
            }
    }
    
    // Show local notification
    private func showLocalNotification(data: [String: Any]) {
        let title = data["title"] as? String ?? "Notification"
        let message = data["message"] as? String ?? ""
        
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = message
        content.sound = .default
        content.badge = 1
        content.userInfo = data
        
        let request = UNNotificationRequest(
            identifier: UUID().uuidString,
            content: content,
            trigger: nil
        )
        
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("❌ Local notification error: \(error)")
            } else {
                print("✅ Local notification shown")
            }
        }
    }
    
    func stopListening() {
        notificationListener?.remove()
    }
}
```

### AppDelegate.swift

```swift
import FirebaseCore
import UserNotifications

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        FirebaseApp.configure()
        
        // Request notification permission
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .sound, .badge]
        ) { granted, error in
            print(granted ? "✅ Notification permission granted" : "❌ Permission denied")
        }
        
        UNUserNotificationCenter.current().delegate = self
        
        return true
    }
}

extension AppDelegate: UNUserNotificationCenterDelegate {
    // Show notifications while app is in foreground
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .badge])
    }
    
    // Handle notification tap
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        print("📱 Notification tapped: \(userInfo)")
        
        // Navigate to relevant screen based on notification type
        if let type = userInfo["type"] as? String {
            switch type {
            case "PROJECT_INVITATION":
                // Navigate to notifications/invitations screen
                break
            default:
                break
            }
        }
        
        completionHandler()
    }
}
```

### ContentView.swift

```swift
struct ContentView: View {
    @StateObject private var notificationManager = NotificationManager.shared
    
    var body: some View {
        TabView {
            // Your tabs...
        }
        .onAppear {
            if Auth.auth().currentUser != nil {
                notificationManager.startListening()
            }
        }
        .onDisappear {
            notificationManager.stopListening()
        }
    }
}
```

---

## Testing Checklist

### iOS → Android
- [ ] iOS user sends invitation
- [ ] Android receives real-time update
- [ ] Android shows local notification
- [ ] Sender name is correct
- [ ] Project name is correct
- [ ] Timestamp is current (not 1970)

### Android → iOS
- [ ] Android user sends invitation
- [ ] iOS receives real-time update
- [ ] iOS shows local notification
- [ ] Sender name is correct
- [ ] Project name is correct
- [ ] Timestamp is current (not 1970)

### Both Platforms
- [ ] Accept invitation works
- [ ] Decline invitation works
- [ ] Notification appears in notifications list
- [ ] Tapping notification navigates to correct screen
- [ ] Badge count updates correctly

---

## Troubleshooting

### "No notifications received"
**Check:**
1. Is the user logged in? (`Auth.auth().currentUser != nil`)
2. Is the listener started? (Check console logs)
3. Is Firestore data written? (Check Firebase Console)
4. Is `userId` field correct in notification document?

### "Timestamp shows 1970"
**Cause:** Using `FieldValue.serverTimestamp()` which returns null initially.  
**Fix:** Use client-side timestamp:
```kotlin
// Kotlin
val timestamp = System.currentTimeMillis()
```
```swift
// Swift
let timestamp = Date().timeIntervalSince1970 * 1000
```

### "Sender name is 'Unknown User'"
**Cause:** `fullName` field not populated in Firestore `users` collection.  
**Fix:** When user signs up, save their name:
```kotlin
db.collection("users").document(userId).set(mapOf(
    "email" to email,
    "fullName" to fullName,
    "createdAt" to System.currentTimeMillis()
))
```

### "iOS not showing notifications"
**Check:**
1. Notification permission granted? (Settings → App → Notifications)
2. `UNUserNotificationCenter.current().delegate` set in AppDelegate?
3. Are you testing on a physical device? (Simulator has limitations)
4. Check Xcode console for error logs

---

## Migration from FCM Push to Firestore Listeners

If you have existing FCM push notification code, migrate to Firestore listeners:

### What to Remove
- FCM server key
- FCM token registration
- `sendPushNotificationToUser()` functions
- APNs configuration

### What to Keep
- Notification data model
- UI components (notification cards, badges)
- Navigation logic

### What to Add
- Firestore real-time listeners on both platforms
- Local notification display logic
- User status tracking in Firestore

---

## Performance Considerations

**Firestore Reads:**
- Each notification = 1 read
- Real-time listener updates = additional reads
- Free tier: 50K reads/day
- For 1000 users × 10 notifications/day = 10K reads (well within limits)

**Optimization:**
- Use `.limit(50)` to fetch only recent notifications
- Filter by `isRead` to reduce listener updates
- Archive old notifications after 30 days

---

## Production Deployment

1. **Enable Firestore in Firebase Console**
2. **Set Firestore Security Rules:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /notifications/{notificationId} {
      // Users can read their own notifications
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
      
      // Users can write notifications to others
      allow create: if request.auth != null;
      
      // Users can update their own notifications (mark as read, accept/decline)
      allow update: if request.auth != null && resource.data.userId == request.auth.uid;
    }
  }
}
```

3. **Deploy Apps:**
   - Android: `./gradlew assembleRelease`
   - iOS: Archive and upload to App Store Connect

4. **Monitor:**
   - Firebase Console → Firestore → Usage tab
   - Check for errors in Crashlytics
   - Monitor user feedback

---

## Summary

This solution provides **100% reliable cross-platform notifications** using Firestore real-time listeners instead of FCM push notifications. All data is consistent across platforms with standardized field names and proper timestamp handling.

**Key Benefits:**
✅ No FCM server configuration needed  
✅ No Cloud Functions required  
✅ Works identically on iOS and Android  
✅ Consistent data model  
✅ Easy to debug and test  
✅ Free for most use cases  

**Next Steps:**
1. Implement iOS NotificationManager (copy code above)
2. Test cross-platform notifications
3. Update Firestore security rules
4. Deploy to production
