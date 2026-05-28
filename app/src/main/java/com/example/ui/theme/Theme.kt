package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ContainerBlue,
    onPrimary = SecondaryBlue,
    primaryContainer = PrimaryBlue,
    onPrimaryContainer = Color.White,
    secondary = TextMedium,
    onSecondary = Color.White,
    background = SecondaryBlue,
    onBackground = Color.White,
    surface = Color(0xFF131316),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1F22),
    onSurfaceVariant = Color.LightGray,
    outline = Color(0xFF43474E),
    outlineVariant = Color(0xFF2E3034),
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = ContainerBlue,
    onPrimaryContainer = SecondaryBlue,
    secondary = TextMedium,
    onSecondary = Color.White,
    background = BackgroundBlue,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    surfaceVariant = SurfaceBlue,
    onSurfaceVariant = TextMedium,
    outline = BorderLight,
    outlineVariant = CardBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color disabled by default to preserve the exact High Density design branding
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
