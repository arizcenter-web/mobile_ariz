package com.tecsup.app_ariz.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * El web usa la pila del sistema (Segoe UI / system-ui). En Android el equivalente es la fuente
 * del sistema (Roboto). Si se adopta una fuente propia, se cambia solo [ArizFontFamily].
 */
val ArizFontFamily: FontFamily = FontFamily.Default

val ArizTypography = Typography(
    displaySmall = TextStyle(fontFamily = ArizFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = ArizFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = ArizFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = ArizFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = ArizFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = ArizFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = ArizFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontFamily = ArizFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)
