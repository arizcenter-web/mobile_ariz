package com.tecsup.app_ariz.core.ui

import android.app.Activity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.core.view.WindowCompat

/**
 * Cuántas pantallas visibles piden iconos claros y cuál era el aspecto original de la barra. Durante una
 * transición entre pestañas conviven la pantalla que sale y la que entra: sin este contador, la que sale
 * restauraba los iconos oscuros después de que la que entra los había puesto claros.
 */
private object LightIconsRequests {
    var count = 0
    var original: Boolean? = null
}

/**
 * Pantallas con una foto o fondo oscuro bajo la barra de estado (Inicio, detalle de propiedad, cabeceras azules):
 * mientras están visibles, los iconos de la barra de estado se dibujan claros y después se restauran.
 */
@Composable
fun LightStatusBarIcons() {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        if (controller != null) {
            if (LightIconsRequests.count == 0) LightIconsRequests.original = controller.isAppearanceLightStatusBars
            LightIconsRequests.count++
            controller.isAppearanceLightStatusBars = false
        }
        onDispose {
            if (controller != null) {
                LightIconsRequests.count = (LightIconsRequests.count - 1).coerceAtLeast(0)
                if (LightIconsRequests.count == 0) {
                    LightIconsRequests.original?.let { controller.isAppearanceLightStatusBars = it }
                    LightIconsRequests.original = null
                }
            }
        }
    }
}

/**
 * Dibuja una franja del color de la cabecera sobre la barra de estado, por encima del contenido. Sin ella, al hacer
 * scroll el contenido claro pasa por debajo de la hora y los iconos (que son blancos) y dejan de verse.
 */
fun Modifier.statusBarScrim(color: Color = Color(0xFF00124F)): Modifier = composed {
    val top = WindowInsets.statusBars.getTop(LocalDensity.current).toFloat()
    drawWithContent {
        drawContent()
        drawRect(color, size = Size(size.width, top))
    }
}
