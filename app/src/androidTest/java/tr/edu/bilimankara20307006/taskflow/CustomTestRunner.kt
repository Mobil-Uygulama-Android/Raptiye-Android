package tr.edu.bilimankara20307006.taskflow

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Custom test runner that properly initializes the test environment
 */
class CustomTestRunner : AndroidJUnitRunner() {
    
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, TestApplication::class.java.name, context)
    }
}