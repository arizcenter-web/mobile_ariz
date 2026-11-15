package com.tecsup.app_ariz.core.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Protege un formulario con cambios sin guardar. Devuelve la acción de "volver" que debe usar la pantalla (el botón
 * de la cabecera): si hay cambios ([dirty]) pregunta antes de salir; si no, sale directo. El botón atrás del sistema
 * y el gesto de volver pasan por la misma pregunta.
 */
@Composable
fun rememberLeaveGuard(
    dirty: Boolean,
    onLeave: () -> Unit,
    title: String = "¿Salir sin guardar?",
    text: String = "Tienes cambios sin guardar. Si sales ahora, se perderán.",
    confirmLabel: String = "Sí, salir",
    dismissLabel: String = "Seguir editando",
): () -> Unit {
    var asking by rememberSaveable { mutableStateOf(false) }
    val currentDirty by rememberUpdatedState(dirty)
    val currentLeave by rememberUpdatedState(onLeave)

    BackHandler(enabled = dirty) { asking = true }
    if (asking && dirty) {
        ConfirmDialog(
            title = title,
            text = text,
            confirmLabel = confirmLabel,
            dismissLabel = dismissLabel,
            onConfirm = {
                asking = false
                currentLeave()
            },
            onDismiss = { asking = false },
            tone = ConfirmTone.WARNING,
            eyebrow = "Cambios sin guardar",
        )
    }
    return remember { { if (currentDirty) asking = true else currentLeave() } }
}
