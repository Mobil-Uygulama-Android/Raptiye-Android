package tr.edu.bilimankara20307006.taskflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// iOS style light theme colors with green accent
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50), // Green (matching login logo)
    onPrimary = Color.White,
    secondary = Color(0xFF66BB6A), // Light green
    onSecondary = Color.White,
    tertiary = Color(0xFF81C784), // Lighter green
    onTertiary = Color.White,
    background = Color(0xFFF2F2F7), // iOS light background
    onBackground = Color(0xFF000000), // Black text on light background
    surface = Color(0xFFFFFFFF), // iOS card background (white)
    onSurface = Color(0xFF000000), // Black text on white surface
    surfaceVariant = Color(0xFFE5E5EA), // iOS search/input background (light gray)
    onSurfaceVariant = Color(0xFF3C3C43), // Dark gray text on light gray
    outline = Color(0xFFD1D1D6), // iOS separator color
    error = Color(0xFFFF3B30), // iOS red
    onError = Color.White,
)

// iOS style dark theme colors with green accent (for main app)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50), // Green (matching login logo)
    onPrimary = Color.White,
    secondary = Color(0xFF66BB6A), // Light green
    onSecondary = Color.White,
    tertiary = Color(0xFF81C784), // Lighter green
    onTertiary = Color.White,
    background = Color(0xFF1C1C1E), // iOS dark background
    onBackground = Color.White,
    surface = Color(0xFF2C2C2E), // iOS card background
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3A3A3C), // iOS search/input background
    onSurfaceVariant = Color(0xFF8E8E93), // iOS secondary text color
    outline = Color(0xFF48484A), // iOS dark separator
    error = Color(0xFFFF3B30), // iOS red
    onError = Color.White,
)

@Composable
fun TaskFlowTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}