package com.vava8.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Vava8 品牌蓝白资讯风
val BrandBlue = Color(0xFF1E88E5)
val BrandBlueDark = Color(0xFF1565C0)
val Ink = Color(0xFF1A1F2C)
val InkSecondary = Color(0xFF6B7280)
val SoftBg = Color(0xFFF5F7FA)
val CardLine = Color(0xFFE8ECF1)
val AccentOrange = Color(0xFFFF7A1A)

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    secondary = BrandBlueDark,
    background = SoftBg,
    surface = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF0F3F8),
    outline = CardLine
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0D2137),
    secondary = Color(0xFF90CAF9),
    background = Color(0xFF0F1419),
    surface = Color(0xFF1A222C),
    onBackground = Color(0xFFE8EEF5),
    onSurface = Color(0xFFE8EEF5),
    surfaceVariant = Color(0xFF243040),
    outline = Color(0xFF334155)
)

enum class ReadingFontSize { Small, Medium, Large, ExtraLarge }

enum class ThemeMode {
    System, Light, Dark;

    fun label(): String = when (this) {
        System -> "随系统"
        Light -> "浅色"
        Dark -> "深色"
    }
}

data class ReaderPrefs(
    val fontSize: ReadingFontSize = ReadingFontSize.Medium,
    val themeMode: ThemeMode = ThemeMode.System,
    val noImageMode: Boolean = false
)

val LocalReaderPrefs = staticCompositionLocalOf { ReaderPrefs() }

@Composable
fun Vava8Theme(
    themeMode: ThemeMode = ThemeMode.System,
    readerPrefs: ReaderPrefs = ReaderPrefs(),
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colors = if (useDark) DarkColors else LightColors
    val bodyScale = when (readerPrefs.fontSize) {
        ReadingFontSize.Small -> 0.92f
        ReadingFontSize.Medium -> 1f
        ReadingFontSize.Large -> 1.12f
        ReadingFontSize.ExtraLarge -> 1.25f
    }
    CompositionLocalProvider(
        LocalReaderPrefs provides readerPrefs.copy(themeMode = themeMode)
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = MaterialTheme.typography.copy(
                displayLarge = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = colors.onBackground
                ),
                headlineMedium = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (20 * bodyScale).sp,
                    color = colors.onBackground
                ),
                titleMedium = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = (16 * bodyScale).sp,
                    lineHeight = (24 * bodyScale).sp,
                    color = colors.onBackground
                ),
                bodyLarge = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = (16 * bodyScale).sp,
                    lineHeight = (26 * bodyScale).sp,
                    color = colors.onBackground
                ),
                bodyMedium = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = (14 * bodyScale).sp,
                    lineHeight = (22 * bodyScale).sp,
                    color = colors.onBackground
                ),
                labelMedium = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = InkSecondary
                )
            ),
            content = content
        )
    }
}
