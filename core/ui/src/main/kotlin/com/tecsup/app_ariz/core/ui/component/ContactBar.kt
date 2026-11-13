package com.tecsup.app_ariz.core.ui.component

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizSecondaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors

private val WhatsAppGreen = Color(0xFF25D366)

/**
 * Barra fija inferior con los canales de contacto del anunciante (llamar y WhatsApp). No se dibuja
 * si no hay ninguno. `whatsappMessage` ya viene traducido por quien llama.
 */
@Composable
fun ContactBar(
    whatsapp: String?,
    phone: String?,
    whatsappMessage: String,
    whatsappLabel: String,
    callLabel: String,
    modifier: Modifier = Modifier,
    messageLabel: String? = null,
    onMessageClick: (() -> Unit)? = null,
) {
    val digits = whatsapp?.filter { it.isDigit() }?.takeIf { it.isNotEmpty() }
    val callNumber = phone?.takeIf { it.isNotBlank() }
    if (digits == null && callNumber == null && onMessageClick == null) return
    val uriHandler = LocalUriHandler.current

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (onMessageClick != null && messageLabel != null) {
                ArizPrimaryButton(
                    text = messageLabel,
                    onClick = onMessageClick,
                    modifier = Modifier.weight(1f),
                )
            }
            if (callNumber != null) {
                ArizSecondaryButton(
                    onClick = { uriHandler.openUri("tel:$callNumber") },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(callLabel, fontWeight = FontWeight.Medium, color = ArizColors.Brand)
                }
            }
            if (digits != null) {
                Surface(
                    onClick = { uriHandler.openUri("https://wa.me/$digits?text=${Uri.encode(whatsappMessage)}") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = WhatsAppGreen,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = whatsappLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
