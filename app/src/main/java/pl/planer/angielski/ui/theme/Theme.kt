package pl.planer.angielski.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Ciepła, „papierowa” paleta inspirowana drukowanym planerem.
val Coral = Color(0xFFE8604C)
val Navy = Color(0xFF2D3A5C)
val Mint = Color(0xFF5BB89A)
val Sun = Color(0xFFF4B942)
val Sky = Color(0xFF6CA6E0)
val Lilac = Color(0xFFA98BD6)

/** Kolory akcentów kart i nawyków – rotowane po kolei. */
val Accents = listOf(Coral, Mint, Sky, Sun, Lilac, Navy)

private val Light = lightColorScheme(
    primary = Coral,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD3),
    onPrimaryContainer = Color(0xFF3E0500),
    secondary = Navy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE2F9),
    onSecondaryContainer = Color(0xFF131C33),
    tertiary = Mint,
    background = Color(0xFFFFFBF5),
    onBackground = Color(0xFF221A17),
    surface = Color(0xFFFFFBF5),
    onSurface = Color(0xFF221A17),
    surfaceVariant = Color(0xFFF3E9E1),
    onSurfaceVariant = Color(0xFF5A4F49),
    surfaceContainer = Color(0xFFF8EEE6),
    surfaceContainerLow = Color(0xFFFFF4EC),
    surfaceContainerHigh = Color(0xFFF3E7DD),
    outlineVariant = Color(0xFFE2D4C9),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFFFB4A6),
    onPrimary = Color(0xFF5F1508),
    primaryContainer = Color(0xFF7F2A1C),
    onPrimaryContainer = Color(0xFFFFDAD3),
    secondary = Color(0xFFBFC6E3),
    onSecondary = Color(0xFF283048),
    secondaryContainer = Color(0xFF3F4760),
    onSecondaryContainer = Color(0xFFDCE2F9),
    tertiary = Color(0xFF8ED6BC),
    background = Color(0xFF1A1513),
    onBackground = Color(0xFFF0DFD9),
    surface = Color(0xFF1A1513),
    onSurface = Color(0xFFF0DFD9),
    surfaceVariant = Color(0xFF3A302C),
    onSurfaceVariant = Color(0xFFD8C2BB),
    surfaceContainer = Color(0xFF261F1C),
    surfaceContainerLow = Color(0xFF221B18),
    surfaceContainerHigh = Color(0xFF312926),
    outlineVariant = Color(0xFF53433E),
)

private val base = Typography()
private val AppTypography = base.copy(
    headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
)

@Composable
fun PlannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = AppTypography,
        content = content,
    )
}
