package tr.edu.bilimankara20307006.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import tr.edu.bilimankara20307006.taskflow.ui.auth.AuthViewModel
import tr.edu.bilimankara20307006.taskflow.ui.auth.LoginScreen
import tr.edu.bilimankara20307006.taskflow.ui.auth.SignUpScreen
import tr.edu.bilimankara20307006.taskflow.ui.main.MainScreen
import tr.edu.bilimankara20307006.taskflow.ui.theme.RaptiyeTheme
import tr.edu.bilimankara20307006.taskflow.ui.theme.ThemeManager

/**
 * MainActivity - Android uygulamasının giriş noktası
 * iOS'taki Task_Flow_Versiyon_2App.swift dosyasının karşılığı
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Firebase'i initialize et - test environment'ta hata vermemesi için try-catch
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                println("🚀 MainActivity initialized")
                println("🔥 Firebase initialized")
            }
        } catch (e: Exception) {
            println("⚠️ Firebase initialization failed: ${e.message}")
        }
        
        setContent {
            val context = LocalContext.current
            val themeManager = remember { ThemeManager.getInstance(context) }
            
            RaptiyeTheme(useDarkTheme = themeManager.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RaptiyeApp()
                }
            }
        }
    }
}

@Composable
fun RaptiyeApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = {
            fadeIn(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + slideInHorizontally(
                initialOffsetX = { it / 2 },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + slideOutHorizontally(
                targetOffsetX = { -it / 2 },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + slideInHorizontally(
                initialOffsetX = { -it / 2 },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + slideOutHorizontally(
                targetOffsetX = { it / 2 },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToMain = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                }
            )
        }
        
        composable("signup") {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMain = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("main") {
            // MainTabScreen kullanılıyor - iOS'taki CustomTabView gibi
            tr.edu.bilimankara20307006.taskflow.ui.main.MainTabScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}