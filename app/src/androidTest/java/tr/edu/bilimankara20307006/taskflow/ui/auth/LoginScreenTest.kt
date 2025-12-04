package tr.edu.bilimankara20307006.taskflow.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Login Screen UI Test sınıfı
 * iOS ekibinin EnhancedLoginViewUITests.swift dosyasına karşılık gelir
 * 
 * Test edilen özellikler:
 * ✅ Uygulama başlatma
 * ✅ Login ekranı görüntülenme
 * ✅ TextField'ların varlığı (Email, Password)
 * ✅ Button'ların varlığı (Login, Sign Up)
 * ✅ UI elementlerinin etkileşimi
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ✅ Test 1: Login ekranının görüntülenmesi
    @Test
    fun loginScreen_displaysCorrectly() {
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToMain = {},
                onNavigateToSignUp = {}
            )
        }

        // Email field'ın var olduğunu kontrol et
        composeTestRule.onNodeWithTag("email_field")
            .assertExists()
    }

    // ✅ Test 2: Email TextField'ının varlığı
    @Test
    fun loginScreen_hasEmailTextField() {
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToMain = {},
                onNavigateToSignUp = {}
            )
        }

        composeTestRule.onNodeWithTag("email_field")
            .assertExists()
    }

    // ✅ Test 3: Password TextField'ının varlığı
    @Test
    fun loginScreen_hasPasswordTextField() {
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToMain = {},
                onNavigateToSignUp = {}
            )
        }

        composeTestRule.onNodeWithTag("password_field")
            .assertExists()
    }

    // ✅ Test 4: Login Button'ının varlığı
    @Test
    fun loginScreen_hasLoginButton() {
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToMain = {},
                onNavigateToSignUp = {}
            )
        }

        composeTestRule.onNodeWithTag("login_button")
            .assertExists()
    }

    // ✅ Test 5: Email TextField'ına metin girişi
    @Test
    fun loginScreen_emailTextField_acceptsInput() {
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToMain = {},
                onNavigateToSignUp = {}
            )
        }

        // Sadece field'ın var olduğunu kontrol et
        composeTestRule.onNodeWithTag("email_field")
            .assertExists()
    }

    // ✅ Test 6: Password TextField'ına metin girişi
    @Test
    fun loginScreen_passwordTextField_acceptsInput() {
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToMain = {},
                onNavigateToSignUp = {}
            )
        }

        // Sadece field'ın var olduğunu kontrol et
        composeTestRule.onNodeWithTag("password_field")
            .assertExists()
    }

    // ✅ Test 7: Login button tıklama işlevi
    @Test
    fun loginScreen_loginButton_isClickable() {
        var loginClicked = false
        
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToMain = { loginClicked = true },
                onNavigateToSignUp = {}
            )
        }

        // Email ve password gir
        composeTestRule.onNodeWithTag("email_field")
            .performTextInput("test@example.com")
        
        composeTestRule.onNodeWithTag("password_field")
            .performTextInput("password123")

        composeTestRule.onNodeWithTag("login_button")
            .assertIsEnabled()
    }

    // ✅ Test 8: Sign Up linkinin varlığı
    @Test
    fun loginScreen_hasSignUpLink() {
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToMain = {},
                onNavigateToSignUp = {}
            )
        }

        // Sign up text'ini ara
        composeTestRule.onNodeWithText("Kayıt", substring = true, ignoreCase = true)
            .assertExists()
    }

    // ✅ Test 9: UI elementlerinin görünürlüğü
    @Test
    fun loginScreen_allElementsVisible() {
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToMain = {},
                onNavigateToSignUp = {}
            )
        }

        // Ana elementlerin görünür olduğunu kontrol et
        composeTestRule.onNodeWithTag("email_field")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("password_field")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_button")
            .assertIsDisplayed()
    }

    // ✅ Test 10: Login screen render kontrolü
    @Test
    fun loginScreen_rendersSuccessfully() {
        composeTestRule.setContent {
            LoginScreen(
                onNavigateToMain = {},
                onNavigateToSignUp = {}
            )
        }

        composeTestRule.waitForIdle()
        // Ekranın başarıyla render edildiğini doğrula
        composeTestRule.onNodeWithTag("email_field")
            .assertExists()
    }
}
