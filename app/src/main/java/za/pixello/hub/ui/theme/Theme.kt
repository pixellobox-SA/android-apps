package za.pixello.hub.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Fallback brand palette (Pixello pink/violet) used when wallpaper colours are off.
private val Light = lightColorScheme(
    primary = Color(0xFF7A3FD1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBDCFF),
    onPrimaryContainer = Color(0xFF2A0A5C),
    secondary = Color(0xFFB0307E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD8EC),
    onSecondaryContainer = Color(0xFF3D0028),
    tertiary = Color(0xFF00658E),
    tertiaryContainer = Color(0xFFC6E7FF),
    background = Color(0xFFFEF7FF),
    surface = Color(0xFFFEF7FF),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFD3BBFF),
    onPrimary = Color(0xFF41128A),
    primaryContainer = Color(0xFF5A2BAF),
    onPrimaryContainer = Color(0xFFEBDCFF),
    secondary = Color(0xFFFFAEDA),
    onSecondary = Color(0xFF5E1145),
    secondaryContainer = Color(0xFF8C1E63),
    onSecondaryContainer = Color(0xFFFFD8EC),
    tertiary = Color(0xFF82CFFF),
    tertiaryContainer = Color(0xFF004C6C),
    background = Color(0xFF141218),
    surface = Color(0xFF141218),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HubTheme(
    dynamicColor: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> Dark
        else -> Light
    }
    MaterialExpressiveTheme(
        colorScheme = colors,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
