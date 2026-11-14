package com.tecsup.app_ariz.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons

/**
 * Página de un panel (profesional, cliente o administrador): cabecera azul con volver, la etiqueta
 * amarilla del panel, el título y, debajo, el contenido sobre fondo claro con scroll.
 */
@Composable
fun PanelPage(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    backDescription: String = "Volver",
    content: @Composable ColumnScope.() -> Unit,
) {
    LightStatusBarIcons()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArizColors.SectionCanvas)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(ArizColors.Brand).statusBarsPadding().padding(bottom = 20.dp)) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp).background(Color.White.copy(alpha = 0.15f), CircleShape),
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = backDescription, tint = Color.White)
            }
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                eyebrow?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp),
                        color = ArizColors.Accent,
                    )
                }
                Text(text = title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), color = Color.White)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f)) }
            }
            Box(Modifier.fillMaxWidth().padding(top = 8.dp).height(4.dp).background(ArizColors.Accent))
        }
        Column(
            modifier = Modifier.padding(16.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
fun PanelCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

/** Número grande con su etiqueta y una nota (las `StatCard` del dashboard del web). */
@Composable
fun StatTile(label: String, value: String, caption: String?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(3.dp).background(ArizColors.Accent))
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp),
                    color = ArizColors.Muted,
                    textAlign = TextAlign.Center,
                )
                Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
                caption?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted, textAlign = TextAlign.Center) }
            }
        }
    }
}

@Composable
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
        color = color,
    )
}

/** Fila de menú con icono y flecha (los accesos del panel). */
@Composable
fun PanelMenuRow(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, caption: String? = null) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = ArizColors.Brand)
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
                caption?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted) }
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = ArizColors.Muted)
        }
    }
}

/** Etiqueta y valor en una fila (datos del perfil, de una suscripción, de un registro). */
@Composable
fun PanelField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp),
            color = ArizColors.Muted,
        )
        Text(text = value.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = ArizColors.Ink)
    }
}

@Composable
fun PanelLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ArizColors.Brand) }
}

@Composable
fun PanelError(message: String, retryLabel: String, modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted, textAlign = TextAlign.Center)
        ArizPrimaryButton(text = retryLabel, onClick = onRetry)
    }
}

@Composable
fun PanelEmpty(title: String, description: String? = null, modifier: Modifier = Modifier) {
    PanelCard(modifier) {
        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
fun PanelSwitchRow(label: String, description: String?, checked: Boolean, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ArizColors.Ink)
            description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted) }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = ArizColors.Brand),
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } },
    )
}
