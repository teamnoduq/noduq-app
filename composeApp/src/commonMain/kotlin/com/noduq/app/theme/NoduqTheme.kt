package com.noduq.app.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.noduq.app.resources.Res
import com.noduq.app.resources.outfit_medium
import com.noduq.app.resources.outfit_regular
import com.noduq.app.resources.outfit_semibold
import org.jetbrains.compose.resources.Font

object NoduqColors {
    val night = Color(0xFF021113)
    val ink = Color(0xFFD3F6FB)
    val cyan = Color(0xFF88E7F3)
    val deep = Color(0xFF0F2595)
    val spark = Color(0xFF4034EB)
    val inset = Color(0xFF03181B)
    val raised = Color(0xFF0A2226)
    val card = Color(0xFF121E24)
    val line = Color(0x29D3F6FB)
    val muted = Color(0x99D3F6FB)
    val danger = Color(0xFFFF8A80)
    val ok = Color(0xFF8EF0C8)
}

object NoduqMotion {
    val easeOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    const val pressMs = 140
    const val selectMs = 200
    /** Beat so the card color reads before the screen starts moving. */
    const val selectLeadMs = 56
    const val screenMs = 200
    const val fadeMs = 160
}

private val scheme = darkColorScheme(
    primary = NoduqColors.cyan,
    onPrimary = NoduqColors.night,
    primaryContainer = NoduqColors.deep,
    onPrimaryContainer = NoduqColors.ink,
    secondary = NoduqColors.deep,
    onSecondary = NoduqColors.ink,
    secondaryContainer = Color(0xFF0B1A4A),
    onSecondaryContainer = NoduqColors.ink,
    tertiary = NoduqColors.spark,
    onTertiary = NoduqColors.ink,
    background = NoduqColors.night,
    onBackground = NoduqColors.ink,
    surface = NoduqColors.night,
    onSurface = NoduqColors.ink,
    surfaceVariant = NoduqColors.raised,
    onSurfaceVariant = NoduqColors.muted,
    outline = NoduqColors.line,
    outlineVariant = NoduqColors.line,
    error = NoduqColors.danger,
    onError = NoduqColors.night,
    errorContainer = Color(0x33FF8A80),
    onErrorContainer = NoduqColors.danger,
    inversePrimary = NoduqColors.deep,
    scrim = Color(0xCC021113),
)

@Composable
fun NoduqTheme(content: @Composable () -> Unit) {
    val outfit = rememberOutfit()
    val typography = remember(outfit) { noduqTypography(outfit) }
    MaterialTheme(
        colorScheme = scheme,
        typography = typography,
        content = content,
    )
}

@Composable
private fun rememberOutfit(): FontFamily {
    val regular = Font(Res.font.outfit_regular, FontWeight.Normal)
    val medium = Font(Res.font.outfit_medium, FontWeight.Medium)
    val semibold = Font(Res.font.outfit_semibold, FontWeight.SemiBold)
    return remember(regular, medium, semibold) {
        FontFamily(regular, medium, semibold)
    }
}

private fun noduqTypography(outfit: FontFamily) = Typography(
    displayLarge = TextStyle(
        fontFamily = outfit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 42.sp,
        letterSpacing = (-1.6).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = outfit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.9).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = outfit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.5).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = outfit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.4.sp,
    ),
)
