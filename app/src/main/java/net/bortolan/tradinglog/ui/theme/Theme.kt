package net.bortolan.tradinglog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.bortolan.tradinglog.db.TransactionType


val LightThemeColors = lightColors(
    primary = Purple700,
    primaryVariant = Purple800,
    onPrimary = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Red300,
    onError = Color.White
)

val DarkThemeColors = darkColors(
    primary = Purple300,
    primaryVariant = Purple600,
    onPrimary = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    error = Red800,
    onError = Color.Black
)

val Colors.snackbarAction: Color
    @Composable
    get() = if (isLight) Purple300 else Purple700

val Colors.progressIndicatorBackground: Color
    @Composable
    get() = if (isLight) Color.Black.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.24f)

val Colors.stroke: Color
    @Composable
    get() = if (isLight) Color(0xFFF1F1F1) else Color.White

val Colors.arrow: Color
    @Composable
    get() = if (isLight) Color(0xFFDDDDDD) else Color.White

val Colors.red: Color
    @Composable
    get() = Red300

val Colors.green: Color
    @Composable
    get() = Green300

@Composable
fun Colors.lightChip(type: TransactionType): Color {
    return when(type) {
        TransactionType.DEPOSIT -> green
        TransactionType.WITHDRAWAL -> red
        TransactionType.BUY -> green
        TransactionType.SELL -> red
        TransactionType.CONVERT -> Purple300
    }
}

@Composable
fun TradingLogTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkThemeColors
    } else {
        LightThemeColors
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}