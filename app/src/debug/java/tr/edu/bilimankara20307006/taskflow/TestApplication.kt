package tr.edu.bilimankara20307006.taskflow

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Test Application class for instrumentation tests
 * This prevents Firebase initialization issues during testing
 */
class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase only if not already initialized
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            // Ignore Firebase errors during testing
            println("Firebase initialization skipped for testing: ${e.message}")
        }
    }
}