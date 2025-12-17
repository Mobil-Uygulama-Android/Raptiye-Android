# Cross-Platform Invitation System Architecture

## 🎯 Design Philosophy

This is a **production-grade, deterministic, cross-platform invitation system** designed to work identically on both iOS and Android **without modifying iOS code**. The system is built on the principle that:

> **Push notifications are TRIGGERS only, never state mutators. The backend (Firestore) is the single source of truth.**

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER ACTIONS                            │
│  iOS User sends invitation → Android User receives → Accepts   │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND (Firestore)                          │
│  • invitations/{invitationId} - Single source of truth          │
│  • notifications/{notificationId} - Display data only           │
│  • users/{userId} - FCM tokens, user info                       │
│  • projects/{projectId} - Team members                          │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                    PUSH NOTIFICATION (FCM/APNs)                 │
│  • Triggers only - does NOT change state                        │
│  • Contains deeplink: taskflow://invitation/{invitationId}      │
│  • iOS and Android receive identical payload                    │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                    IN-APP HANDLING                              │
│  • Deeplink opens InvitationDetailScreen                        │
│  • User taps Accept/Reject                                      │
│  • Backend API updates invitation status                        │
│  • Real-time listener updates UI on both platforms              │
└─────────────────────────────────────────────────────────────────┘
```

## 📊 Data Models

### 1. Invitation Document (Firestore)
**Path:** `invitations/{invitationId}`

```kotlin
data class Invitation(
    val id: String,                          // UUID
    val projectId: String,
    val projectName: String,
    val senderId: String,
    val senderName: String,
    val senderEmail: String?,
    val receiverId: String,
    val receiverEmail: String,
    val status: InvitationStatus,            // PENDING, ACCEPTED, REJECTED, EXPIRED
    val createdAt: Long,
    val updatedAt: Long,
    val expiresAt: Long                      // 7 days from creation
)
```

**Why separate from notifications?**
- Invitations are **state** (persistent, tracked)
- Notifications are **display** (temporary, dismissible)
- iOS and Android must read from the same invitation document

### 2. Notification Document (Firestore)
**Path:** `notifications/{notificationId}`

```kotlin
data class Notification(
    val id: String,
    val userId: String,
    val type: NotificationActionType,        // PROJECT_INVITATION
    val title: String,
    val message: String,
    val data: Map<String, String>,           // invitationId, projectId, etc.
    val isRead: Boolean,
    val createdAt: Long
)
```

**Purpose:** Display-only. Used for in-app notifications screen. Not the source of truth for invitation state.

### 3. Push Notification Payload (FCM/APNs)
```json
{
  "notification": {
    "title": "Project Invitation",
    "body": "John invited you to Mobile App"
  },
  "data": {
    "type": "PROJECT_INVITATION",
    "invitationId": "inv-123",
    "projectId": "proj-456",
    "notificationId": "notif-789",
    "deeplink": "taskflow://invitation/inv-123"
  },
  "apns": {
    "payload": {
      "aps": {
        "alert": {
          "title": "Project Invitation",
          "body": "John invited you to Mobile App"
        },
        "sound": "default"
      }
    }
  }
}
```

## 🔄 Complete Flow

### Sending Invitation (iOS or Android)

1. **Sender initiates invitation**
   ```kotlin
   InvitationManager.createInvitation(
       receiverEmail = "user@example.com",
       projectId = "proj-123",
       projectName = "Mobile App"
   )
   ```

2. **Backend creates invitation document**
   ```
   invitations/inv-abc123
   {
     id: "inv-abc123",
     projectId: "proj-123",
     projectName: "Mobile App",
     senderId: "user-sender",
     senderName: "John Doe",
     receiverId: "user-receiver",
     status: "pending",
     createdAt: 1234567890
   }
   ```

3. **Backend creates notification document**
   ```
   notifications/notif-xyz789
   {
     id: "notif-xyz789",
     userId: "user-receiver",
     type: "PROJECT_INVITATION",
     data: { invitationId: "inv-abc123" },
     isRead: false
   }
   ```

4. **Backend sends push notification**
   - FCM for Android
   - APNs for iOS
   - Contains deeplink: `taskflow://invitation/inv-abc123`

### Receiving Invitation (iOS or Android)

1. **User sees push notification**
   - Title: "Project Invitation"
   - Body: "John Doe invited you to Mobile App"
   - **NO ACTION BUTTONS** on notification

2. **User taps notification**
   - Android: MainActivity receives intent with deeplink data
   - Navigation to `InvitationDetailScreen(invitationId)`

3. **Invitation detail screen loads**
   ```kotlin
   InvitationManager.getInvitation(invitationId)
       .onSuccess { invitation ->
           // Display invitation details
           // Show Accept/Reject buttons
       }
   ```

4. **User taps Accept**
   ```kotlin
   InvitationManager.acceptInvitation(invitationId)
       .onSuccess {
           // Update invitation status to "accepted"
           // Add user to project teamMembers
           // Navigate back to home
       }
   ```

5. **Real-time synchronization**
   - Firestore updates invitation document
   - Both iOS and Android listeners receive update
   - UIs update automatically
   - Sender sees acceptance notification

## 🔑 Key Components (Android)

### 1. InvitationManager.kt
**Purpose:** Backend API for invitation operations

**Methods:**
- `createInvitation()` - Create new invitation
- `acceptInvitation()` - Accept invitation (backend update)
- `rejectInvitation()` - Reject invitation (backend update)
- `getInvitation()` - Fetch single invitation (for deeplink)
- `listenToInvitations()` - Real-time listener

**Why it exists:** Single source of truth for invitation logic. Both iOS and Android use equivalent managers.

### 2. InvitationDetailScreen.kt
**Purpose:** In-app UI for accepting/rejecting invitations

**Features:**
- Displays invitation details (project, sender, date)
- Accept/Reject buttons
- Loading states
- Success/error messages
- Auto-navigation after action

**Why it exists:** Push notification action buttons are unreliable. In-app actions are deterministic and work on all devices.

### 3. RaptiyeFirebaseMessagingService.kt
**Purpose:** Receive FCM push notifications

**Behavior:**
- Parses FCM payload
- Extracts deeplink
- Creates local notification
- **No state changes** - just displays notification

**Key change:**
```kotlin
// OLD: Action buttons on notification (unreliable)
// NEW: Deeplink to invitation detail screen (reliable)
val intent = Intent(this, MainActivity::class.java).apply {
    data = android.net.Uri.parse(deeplink) // taskflow://invitation/inv-123
}
```

### 4. MainActivity.kt
**Purpose:** Handle deeplinks

**Behavior:**
- Receives intent with deeplink URI
- Extracts invitation ID from `taskflow://invitation/{id}`
- Navigates to `InvitationDetailScreen`

**Code:**
```kotlin
private fun handleDeepLink(intent: Intent) {
    val data: Uri? = intent.data
    if (data?.scheme == "taskflow" && data.host == "invitation") {
        val invitationId = data.path?.removePrefix("/")
        navController.navigate("invitation/$invitationId")
    }
}
```

## ✅ Why This Architecture Works

### 1. **Cross-Platform Consistency**
- iOS and Android read from same Firestore documents
- Same invitation model, same status values
- Real-time listeners sync state instantly

### 2. **No iOS Code Changes Required**
- Android adapts to use backend-driven flow
- Push notifications are just triggers (iOS already works this way)
- Both platforms use in-app screens for actions

### 3. **Deterministic Behavior**
- No reliance on unreliable notification action buttons
- Backend APIs are testable and predictable
- Firestore is authoritative state

### 4. **Device Compatibility**
- Works on Xiaomi, Samsung, Huawei (notification button issues)
- Works when app is killed (deeplink restoration)
- Works offline (Firestore persistence)

### 5. **Maintainability**
- Clear separation of concerns
- Single source of truth (Firestore)
- Easy to debug (check Firestore documents)

## 🚨 Anti-Patterns to Avoid

### ❌ DON'T: Update state from notification actions
```kotlin
// BAD: Notification button directly updates Firestore
notificationBuilder.addAction("Accept", acceptPendingIntent)
// In BroadcastReceiver:
db.collection("invitations").document(id).update("status", "accepted")
```

**Why:** Unreliable on some devices, doesn't work when app is killed

### ✅ DO: Use deeplinks to in-app screens
```kotlin
// GOOD: Notification opens invitation detail screen
val intent = Intent(this, MainActivity::class.java).apply {
    data = Uri.parse("taskflow://invitation/$invitationId")
}
// In InvitationDetailScreen:
InvitationManager.acceptInvitation(invitationId) // Backend API
```

### ❌ DON'T: Store invitation state in notification document
```kotlin
// BAD: Notification document tracks status
notifications/notif-123 { status: "accepted" }
```

**Why:** Notifications are display-only, not source of truth

### ✅ DO: Use separate invitation document
```kotlin
// GOOD: Invitation document is source of truth
invitations/inv-123 { status: "accepted" }
notifications/notif-123 { invitationId: "inv-123" } // Reference only
```

## 📱 Testing Checklist

- [ ] iOS → Android invitation works
- [ ] Android → iOS invitation works
- [ ] Accept button works (in-app)
- [ ] Reject button works (in-app)
- [ ] Deeplink opens correct invitation
- [ ] Notification appears on both platforms
- [ ] Invitation status syncs in real-time
- [ ] Works on Xiaomi devices
- [ ] Works on Samsung devices
- [ ] Works when app is killed
- [ ] Works when app is in background
- [ ] Expired invitations show correct state
- [ ] Already-accepted invitations show correct state

## 🔮 Future Enhancements

1. **Invitation expiration**: Automatically reject invitations after 7 days
2. **Multiple pending invitations**: Priority queue or list view
3. **Invitation analytics**: Track acceptance rates
4. **Rich push notifications**: Show project thumbnail
5. **Smart reply**: Quick accept from notification (iOS 15+)

## 📚 References

- [Invitation.kt](app/src/main/java/tr/edu/bilimankara20307006/taskflow/data/model/Invitation.kt)
- [InvitationManager.kt](app/src/main/java/tr/edu/bilimankara20307006/taskflow/data/manager/InvitationManager.kt)
- [InvitationDetailScreen.kt](app/src/main/java/tr/edu/bilimankara20307006/taskflow/ui/screens/InvitationDetailScreen.kt)
- [RaptiyeFirebaseMessagingService.kt](app/src/main/java/tr/edu/bilimankara20307006/taskflow/fcm/RaptiyeFirebaseMessagingService.kt)
- [MainActivity.kt](app/src/main/java/tr/edu/bilimankara20307006/taskflow/MainActivity.kt)

---

**Last Updated:** 2024-01-15  
**Architecture Version:** 2.0 (Backend-First Invitation System)
