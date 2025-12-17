package tr.edu.bilimankara20307006.taskflow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import com.google.firebase.FirebaseApp
import tr.edu.bilimankara20307006.taskflow.ui.auth.AuthViewModel
import tr.edu.bilimankara20307006.taskflow.ui.auth.LoginScreen
import tr.edu.bilimankara20307006.taskflow.ui.auth.SignUpScreen
import tr.edu.bilimankara20307006.taskflow.ui.main.MainScreen
import tr.edu.bilimankara20307006.taskflow.ui.theme.RaptiyeTheme
import tr.edu.bilimankara20307006.taskflow.ui.theme.ThemeManager
import tr.edu.bilimankara20307006.taskflow.data.manager.NotificationManager

/**
 * MainActivity - Android uygulamasının giriş noktası
 * iOS'taki Task_Flow_Versiyon_2App.swift dosyasının karşılığı
 */
class MainActivity : ComponentActivity() {
    
    private val deepLinkState = mutableStateOf<String?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge modu aktive et
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
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
        
        // Handle deeplink
        handleDeepLink(intent)
        
        // FCM Token'ını güncelle (kullanıcı oturum açtıktan sonra)
        updateFCMToken()
        
        setContent {
            val context = LocalContext.current
            val themeManager = remember { ThemeManager.getInstance(context) }
            
            RaptiyeTheme(useDarkTheme = themeManager.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RaptiyeApp(deepLinkState = deepLinkState)
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }
    
    /**
     * Handle deeplink: taskflow://invitation/{invitationId}
     */
    private fun handleDeepLink(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null && data.scheme == "taskflow") {
            val path = data.path // /invitation/{invitationId}
            println("🔗 Deeplink received: $data")
            println("   Scheme: ${data.scheme}")
            println("   Host: ${data.host}")
            println("   Path: $path")
            
            if (data.host == "invitation" && path != null) {
                val invitationId = path.removePrefix("/")
                deepLinkState.value = "invitation/$invitationId"
                println("✅ Will navigate to invitation: $invitationId")
            }
        }
    }
    
    /**
     * FCM Token'ını güncelle
     */
    private fun updateFCMToken() {
        lifecycleScope.launch {
            try {
                NotificationManager.getInstance().updateFCMToken()
                println("✅ FCM Token güncellendi")
            } catch (e: Exception) {
                println("⚠️ FCM Token güncelleme hatası: ${e.message}")
            }
        }
    }
}

@Composable
fun RaptiyeApp(deepLinkState: androidx.compose.runtime.MutableState<String?> = remember { mutableStateOf(null) }) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    
    // Kullanıcı durumunu dinle
    val authState = authViewModel.authState.collectAsState()
    val startDestination = if (authState.value.isAuthenticated) "main" else "login"
    
    // Handle deeplink navigation
    LaunchedEffect(deepLinkState.value) {
        val deepLink = deepLinkState.value
        if (deepLink != null && authState.value.isAuthenticated) {
            println("🔗 Navigating to deeplink: $deepLink")
            navController.navigate(deepLink)
            deepLinkState.value = null // Clear after navigation
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
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
        
        composable("invitation/{invitationId}") { backStackEntry ->
            val invitationId = backStackEntry.arguments?.getString("invitationId") ?: ""
            tr.edu.bilimankara20307006.taskflow.ui.screens.InvitationDetailScreen(
                invitationId = invitationId,
                navController = navController
            )
        }
    }
}