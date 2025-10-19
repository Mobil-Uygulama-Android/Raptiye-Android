package tr.edu.bilimankara20307006.taskflow.ui.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import tr.edu.bilimankara20307006.taskflow.ui.localization.LocalizationManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToMain: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager.getInstance(context) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    
    // Animasyon state'leri
    var logoVisible by remember { mutableStateOf(false) }
    var logoInPosition by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }
    
    // Animasyonları başlat
    LaunchedEffect(Unit) {
        delay(100) // Küçük gecikme
        logoVisible = true
        delay(400) // Logo belirdikten sonra
        logoInPosition = true
        delay(300) // Logo yerine oturduktan sonra
        titleVisible = true
        delay(200) // Başlık göründükten sonra
        formVisible = true
        delay(250) // Form göründükten sonra
        buttonVisible = true
    }
    
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onNavigateToMain()
        }
    }
    
    // Tema renklerini MaterialTheme'den al
    val darkBackground = MaterialTheme.colorScheme.background
    val inputBackground = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val greenColor = Color(0xFF4CAF50)
    
    // Animasyon değerleri
    val logoScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.3f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "logoScale"
    )
    
    val logoAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )
    
    val logoOffsetY by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (logoInPosition) 0.dp else (-100).dp,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "logoOffsetY"
    )
    
    val titleAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "titleAlpha"
    )
    
    val formOffsetY by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (formVisible) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "formOffsetY"
    )
    
    val formAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (formVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "formAlpha"
    )
    
    val buttonOffsetY by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (buttonVisible) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "buttonOffsetY"
    )
    
    val buttonAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (buttonVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "buttonAlpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo - Ortada belirir ve yerine kayar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(y = logoOffsetY)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .background(greenColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Raptiye",
                    tint = greenColor,
                    modifier = Modifier
                        .size(60.dp)
                        .rotate(45f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Başlık - Fade in
            Text(
                text = "Raptiye",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.alpha(titleAlpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = localizationManager.localizedString("WelcomeBack"),
                fontSize = 18.sp,
                color = textSecondaryColor,
                modifier = Modifier.alpha(titleAlpha)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Form - Aşağıdan yukarı kayar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = formOffsetY)
                    .alpha(formAlpha)
            ) {
                Text(
                    text = localizationManager.localizedString("WelcomeBack"),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = localizationManager.localizedString("SignIn"),
                fontSize = 16.sp,
                color = textSecondaryColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { 
                    Text(
                        localizationManager.localizedString("Email"), 
                        color = textSecondaryColor
                    ) 
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = localizationManager.localizedString("Email"),
                        tint = textSecondaryColor
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = greenColor,
                    unfocusedContainerColor = inputBackground,
                    focusedContainerColor = inputBackground,
                    unfocusedTextColor = textColor,
                    focusedTextColor = textColor,
                    cursorColor = greenColor
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { 
                    Text(
                        localizationManager.localizedString("Password"), 
                        color = textSecondaryColor
                    ) 
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = localizationManager.localizedString("Password"),
                        tint = textSecondaryColor
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Şifreyi gizle" else "Şifreyi göster",
                            tint = textSecondaryColor
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = greenColor,
                    unfocusedContainerColor = inputBackground,
                    focusedContainerColor = inputBackground,
                    unfocusedTextColor = textColor,
                    focusedTextColor = textColor,
                    cursorColor = greenColor
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = localizationManager.localizedString("ForgotPassword"),
                fontSize = 14.sp,
                color = greenColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { },
                textAlign = TextAlign.End
            )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Buton - Aşağıdan yukarı kayar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = buttonOffsetY)
                    .alpha(buttonAlpha)
            ) {
            Button(
                onClick = {
                    authViewModel.signIn(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = greenColor,
                    contentColor = Color.White
                ),
                enabled = email.isNotEmpty() && password.isNotEmpty()
            ) {
                Text(
                    text = localizationManager.localizedString("SignIn"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizationManager.localizedString("DontHaveAccount") + " ",
                    fontSize = 14.sp,
                    color = textSecondaryColor
                )
                Text(
                    text = localizationManager.localizedString("SignUp"),
                    fontSize = 14.sp,
                    color = greenColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        onNavigateToSignUp()
                    }
                )
            }
            }
            
            if (authState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = authState.errorMessage ?: "",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            
            if (authState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    color = greenColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}