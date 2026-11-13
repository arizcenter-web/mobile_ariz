package com.tecsup.app_ariz.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** El web solo define tema claro (`color-scheme: light`), asi que la app tambien. */
private val ArizColorScheme = lightColorScheme(
    primary = ArizColors.Brand,
    onPrimary = ArizColors.OnBrand,
    primaryContainer = ArizColors.BrandSoft,
    onPrimaryContainer = ArizColors.Brand,
    secondary = ArizColors.Accent,
    onSecondary = ArizColors.OnAccent,
    secondaryContainer = ArizColors.Accent,
    onSecondaryContainer = ArizColors.OnAccent,
    background = ArizColors.Canvas,
    onBackground = ArizColors.Ink,
    surface = ArizColors.Surface,
    onSurface = ArizColors.Ink,
    surfaceVariant = ArizColors.SurfaceMuted,
    onSurfaceVariant = ArizColors.Muted,
    surfaceContainerLowest = ArizColors.Surface,
    surfaceContainerLow = ArizColors.Surface,
    surfaceContainer = ArizColors.SurfaceMuted,
    surfaceContainerHigh = ArizColors.SurfaceMuted,
    outline = ArizColors.Muted,
    outlineVariant = ArizColors.Edge,
    error = ArizColors.Error,
    onError = Color.White,
)

/** Colores semanticos que Material3 no modela (exito, aviso, info, WhatsApp). */
@Immutable
data class ArizExtraColors(
    val success: Color = ArizColors.Success,
    val warning: Color = ArizColors.Warning,
    val info: Color = ArizColors.Info,
    val whatsApp: Color = ArizColors.WhatsApp,
    val accentHover: Color = ArizColors.AccentHover,
)

val LocalArizExtraColors = staticCompositionLocalOf { ArizExtraColors() }

object ArizTheme {
    val extraColors: ArizExtraColors
        @Composable get() = LocalArizExtraColors.current
}

@Composable
fun ArizTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalArizExtraColors provides ArizExtraColors()) {
        MaterialTheme(
            colorScheme = ArizColorScheme,
            typography = ArizTypography,
            shapes = ArizShapes,
            content = content,
        )
    }
}
