package com.fahh.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val AppColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
    error = Error
)

@Composable
fun FahhTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AppColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Premium Glassmorphism modifier for a frosted glass look.
 */
fun Modifier.premiumGlass(
    shape: Shape = RoundedCornerShape(16.dp),
    alpha: Float = 0.1f
) = this
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = (alpha + 0.05f).coerceIn(0f, 1f)),
                Color.White.copy(alpha = (alpha - 0.05f).coerceIn(0f, 1f))
            )
        ),
        shape = shape
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.05f)
            )
        ),
        shape = shape
    )

/**
 * High-end inner blur shadow/glow utility.
 */
fun Modifier.premiumGlow(
    color: Color = Primary.copy(alpha = 0.15f),
    radius: Dp = 20.dp
) = this.drawBehind {
    drawCircle(
        color = color,
        radius = radius.toPx(),
        center = center,
        style = Fill
    )
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
