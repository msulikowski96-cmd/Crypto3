package com.example.cryptograms.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

object Palette {
    val Background = Color(0xFF16171B)
    val Surface = Color(0xFF1C1D21)
    val SurfaceHigh = Color(0xFF24252A)
    val Key = Color(0xFF26272C)
    val Border = Color(0xFF3A3B41)
    val TileBorder = Color(0xFF4A4B52)
    val TileSelected = Color(0xFF34353C)
    val TileMatching = Color(0xFF2A2C33)
    val TextPrimary = Color(0xFFF4F4F2)
    val TextSecondary = Color(0xFF9A9BA1)
    val TextMuted = Color(0xFF6C6D73)
    val Green = Color(0xFF3FA372)
    val GreenText = Color(0xFF4CB383)
    val GreenBg = Color(0xFF1B3A2B)
    val Amber = Color(0xFFD9A441)
    val AmberBg = Color(0xFF3A3020)
    val RedText = Color(0xFFE5675F)
    val RedBg = Color(0xFF3D2223)
    val Dot = Color(0x0DFFFFFF)
}

val SerifFamily = FontFamily.Serif

private val colors = darkColorScheme(
    primary = Palette.TextPrimary,
    onPrimary = Palette.Background,
    background = Palette.Background,
    onBackground = Palette.TextPrimary,
    surface = Palette.Surface,
    onSurface = Palette.TextPrimary,
    surfaceVariant = Palette.SurfaceHigh,
    onSurfaceVariant = Palette.TextSecondary,
    outline = Palette.Border,
    surfaceContainerLow = Palette.Surface,
    surfaceContainer = Palette.Surface,
)

@Composable
fun CryptogramsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors) {
        CompositionLocalProvider(LocalContentColor provides Palette.TextPrimary, content = content)
    }
}

/** The faint dotted grid seen behind every screen. */
fun Modifier.dottedBackground(): Modifier = drawBehind {
    drawRect(Palette.Background)
    val step = 22.dp.toPx()
    val radius = 1.dp.toPx()
    var y = step / 2
    while (y < size.height) {
        var x = step / 2
        while (x < size.width) {
            drawCircle(Palette.Dot, radius, Offset(x, y))
            x += step
        }
        y += step
    }
}
