# Migration Guide: Old Notification System → New Invitation System

## Overview

The old system used notification documents to track invitation state and notification action buttons to accept/reject. This was unreliable on some Android devices (Xiaomi, Samsung) and didn't work well cross-platform.

The new system uses **separate invitation documents** as the source of truth, **deeplinks** for navigation, and **in-app screens** for actions. This is reliable on all devices and works identically on iOS and Android.

## What Changed

### Old Architecture ❌
```
Push Notification
    ↓
Notification Action Buttons (ACCEPT/REJECT)
    ↓
BroadcastReceiver/Activity
    ↓
Update notification.invitationStatus
```

**Problems:**
- Action buttons unreliable on some devices
- Doesn't work when app is killed
- State stored in notification document (not source of truth)
- Different behavior on iOS and Android

### New Architecture ✅
```
Push Notification (with deeplink)
    ↓
Tap Notification
    ↓
InvitationDetailScreen
    ↓
Accept/Reject Button
    ↓
InvitationManager Backend API
    ↓
Update invitation document
    ↓
Real-time listeners update UI
```

**Benefits:**
- Reliable on all devices
- Works when app is killed/background
- Firestore is source of truth
- Identical behavior on iOS and Android

## Step-by-Step Migration

### 1. Stop Using Old Methods

**DEPRECATED:**
```kotlin
// Don't use these anymore
NotificationManager.sendProjectInvitation(userId, projectId, projectName)
NotificationManager.acceptProjectInvitation(notificationId)
NotificationManager.declineProjectInvitation(notificationId)
```

**NEW:**
```kotlin
// Use InvitationManager instead
InvitationManager.createInvitation(userEmail, projectId, projectName)
InvitationManager.acceptInvitation(invitationId)
InvitationManager.rejectInvitation(invitationId)
```

### 2. Update Project Detail Screen

**Add invitation button:**
```kotlin
// In ProjectDetailScreen.kt

Button(
    onClick = { showInviteDialog = true }
) {
    Icon(Icons.Default.PersonAdd, contentDescription = null)
    Spacer(modifier = Modifier.width(8.dp))
    Text("Invite Team Member")
}

if (showInviteDialog) {
    InviteTeamMemberDialog(
        projectId = project.id,
        projectName = project.name,
        onDismiss = { showInviteDialog = false },
        onInvite = { email ->
            scope.launch {
                InvitationManager.createInvitation(
                    receiverEmail = email,
                    projectId = project.id,
                    projectName = project.name
                ).onSuccess {
                    // Show success toast
                    showInviteDialog = false
                }.onFailure { error ->
                    // Show error
                }
            }
        }
    )
}
```

**InviteTeamMemberDialog composable:**
```kotlin
@Composable
fun InviteTeamMemberDialog(
    projectId: String,
    projectName: String,
    onDismiss: () -> Unit,
    onInvite: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite Team Member") },
        text = {
            Column {
                Text("Enter the email address of the person you want to invite to \"$projectName\"")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onInvite(email) },
                enabled = email.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Send Invitation")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

### 3. Update Notifications Screen (Optional)

The existing NotificationsScreen can continue to work with notification documents. But you can enhance it to show invitation status:

```kotlin
// In NotificationsScreen.kt

LaunchedEffect(currentUser.uid) {
    // Listen to pending invitations
    InvitationManager.listenToInvitations(currentUser.uid) { invitations ->
        pendingInvitations = invitations
    }
}

// Display invitations section
if (pendingInvitations.isNotEmpty()) {
    Text(
        text = "Pending Invitations",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    
    pendingInvitations.forEach { invitation ->
        InvitationCard(
            invitation = invitation,
            onTap = {
                navController.navigate("invitation/${invitation.id}")
            }
        )
    }
}
```

### 4. Clean Up Old Code (Optional)

After migration, you can remove:

**Files to remove:**
- `AcceptInvitationActivity.kt` (no longer needed)
- `NotificationActionReceiver.kt` (no longer needed)

**Manifest to update:**
```xml
<!-- Remove these entries from AndroidManifest.xml -->
<activity
    android:name=".notification.AcceptInvitationActivity"
    ... />
    
<receiver
    android:name=".notification.NotificationActionReceiver"
    ... />
```

**NotificationManager methods to mark deprecated:**
```kotlin
@Deprecated("Use InvitationManager.createInvitation instead")
suspend fun sendProjectInvitation(...)

@Deprecated("Use InvitationManager.acceptInvitation instead")
suspend fun acceptProjectInvitation(...)
```

### 5. Test Migration

**Test checklist:**
- [ ] Create invitation from Android → iOS receives → accepts → syncs ✅
- [ ] Create invitation from iOS → Android receives → accepts → syncs ✅
- [ ] Deeplink opens correct invitation screen ✅
- [ ] Accept button works (updates Firestore + adds to team) ✅
- [ ] Reject button works (updates Firestore) ✅
- [ ] Works on Xiaomi devices ✅
- [ ] Works when app is killed ✅
- [ ] Real-time sync updates UI immediately ✅

## Backward Compatibility

### During Migration Period

If you want to support both old and new systems during migration:

```kotlin
// In RaptiyeFirebaseMessagingService.kt
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val data = remoteMessage.data
    val deeplink = data["deeplink"]
    val invitationId = data["invitationId"]
    
    // New system: Has deeplink
    if (!deeplink.isNullOrEmpty()) {
        showNotificationWithDeeplink(title, body, deeplink)
    }
    // Old system: No deeplink, has invitationId
    else if (!invitationId.isNullOrEmpty()) {
        showNotificationWithActionButtons(title, body, invitationId)
    }
    // Other notification types
    else {
        showNotification(title, body)
    }
}
```

### iOS Team Migration

The iOS team should also migrate to the new system for full benefits:

1. **Create InvitationManager** (equivalent to Android)
2. **Add deeplink handling** for `taskflow://invitation/{id}`
3. **Create InvitationDetailView** (SwiftUI)
4. **Update FCM payload** to include deeplink
5. **Listen to invitation documents** instead of notifications

**Sample iOS deeplink handling:**
```swift
.onOpenURL { url in
    if url.scheme == "taskflow", url.host == "invitation" {
        let invitationId = url.pathComponents.last ?? ""
        navigationPath.append(Route.invitationDetail(invitationId))
    }
}
```

## Troubleshooting

### Issue: Deeplink doesn't open app

**Solution:** Check `AndroidManifest.xml` has proper intent filter:
```xml
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="taskflow" android:host="invitation" />
</intent-filter>
```

### Issue: Navigation doesn't work after deeplink

**Solution:** Ensure `MainActivity` has `android:launchMode="singleTask"`:
```xml
<activity
    android:name=".MainActivity"
    android:launchMode="singleTask"
    ... />
```

### Issue: Invitation not found

**Solution:** Verify invitation was created in Firestore:
```
invitations/inv-{uuid}
{
  id: "inv-{uuid}",
  projectId: "...",
  receiverId: "...",
  status: "pending",
  ...
}
```

### Issue: Accept doesn't add user to project

**Solution:** Check `InvitationManager.acceptInvitation()` logs:
```kotlin
println("🔵 Accepting invitation: $invitationId")
println("✅ Invitation status updated to 'accepted'")
println("✅ User added to project: ${invitation.projectId}")
```

## Summary

**Old system:** Notification action buttons → Unreliable  
**New system:** Deeplinks + In-app screens → Reliable

**Migration effort:** ~2 hours  
**Benefit:** Cross-platform consistency, works on all devices

**Key files:**
- [InvitationManager.kt](app/src/main/java/tr/edu/bilimankara20307006/taskflow/data/manager/InvitationManager.kt) - Backend API
- [InvitationDetailScreen.kt](app/src/main/java/tr/edu/bilimankara20307006/taskflow/ui/screens/InvitationDetailScreen.kt) - UI
- [ARCHITECTURE.md](ARCHITECTURE.md) - Complete technical docs

---

**Questions?** Check [INVITATION_SYSTEM.md](INVITATION_SYSTEM.md) for implementation details.
