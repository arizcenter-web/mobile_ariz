package com.tecsup.app_ariz.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens tomados 1:1 de Ariz_Web_frontend/src/shared/styles/tokens.css.
 * Si cambia el web, se actualiza aqui.
 */
object ArizColors {
    val Brand = Color(0xFF00124F)        // --brand
    val BrandHover = Color(0xFF0B2170)   // --brand-hover
    val BrandSoft = Color(0xFFE8ECF8)    // --brand-soft
    val OnBrand = Color(0xFFFFFFFF)      // --on-brand

    val Accent = Color(0xFFF2A900)       // --accent
    val AccentHover = Color(0xFFD99700)  // --accent-hover
    val OnAccent = Color(0xFF00124F)     // --on-accent

    val WhatsApp = Color(0xFF25D366)     // --wa (identidad de marca)
    val WhatsAppHover = Color(0xFF1EB257)

    val Canvas = Color(0xFFF5F6FA)       // --canvas (fondo de pantalla)
    val Surface = Color(0xFFFFFFFF)      // --surface
    val SurfaceMuted = Color(0xFFEEF0F6) // --surface-muted
    val Edge = Color(0xFFDDE1EC)         // --edge (bordes)
    val FieldBorder = Color(0xFF8793AD)  // borde de los campos de formulario (login-form.tsx)

    val Ink = Color(0xFF0B1437)          // --ink (texto principal)
    val Muted = Color(0xFF646D8A)        // --muted (texto secundario)

    val Success = Color(0xFF2F7D4F)
    val Warning = Color(0xFFB7791F)
    val Error = Color(0xFFB3402E)
    val Info = Color(0xFF2F6FB3)
}
