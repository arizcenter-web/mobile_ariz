package com.tecsup.app_ariz.core.ui.component

import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Close
import com.tecsup.app_ariz.core.ui.statusBarScrim
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.unit.Dp
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
            .statusBarScrim()
            .verticalScroll(rememberScrollState()),
    ) {
        PanelHeader(onBackClick = onBackClick, backDescription = backDescription) {
            eyebrow?.let { EyebrowChip(it) }
            Text(
                text = title,
                modifier = Modifier.padding(top = if (eyebrow != null) 12.dp else 0.dp),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, lineHeight = 34.sp),
                color = Color.White,
            )
            subtitle?.let {
                Text(it, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.78f))
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

/**
 * Cabecera azul con las esquinas inferiores redondeadas: el botón de volver respira bajo la barra de estado
 * y el título queda separado de él. [content] es el bloque de texto (etiqueta, título, subtítulo).
 */
@Composable
fun PanelHeader(
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    backDescription: String = "Volver",
    contentMinHeight: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ArizColors.Brand, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
    ) {
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.14f), CircleShape),
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = backDescription, tint = Color.White)
            }
            Spacer(Modifier.height(24.dp))
        } else {
            Spacer(Modifier.height(12.dp))
        }
        Column(Modifier.fillMaxWidth().heightIn(min = contentMinHeight), content = content)
    }
}

/** Alto mínimo del texto de las cabeceras de las pestañas: las cuatro (Buscar, Favoritos, Cuenta...) miden lo mismo. */
val TabHeaderContentHeight = 132.dp

/**
 * Cabecera de las pestañas principales: etiqueta, título y descripción sobre el azul de marca, con el mismo
 * alto y el mismo diseño en todas. [trailing] es un dato opcional a la derecha del título.
 */
@Composable
fun TabHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    PanelHeader(onBackClick = null, modifier = modifier, contentMinHeight = TabHeaderContentHeight) {
        EyebrowChip(eyebrow)
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
            )
            trailing?.invoke()
        }
        Text(subtitle, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.78f))
    }
}

/** Etiqueta pequeña en cápsula sobre fondo azul (sustituye al texto amarillo suelto). */
@Composable
fun EyebrowChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier.background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp),
        color = ArizColors.Accent,
    )
}

@Composable
fun PanelCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

/** Número grande con su etiqueta y una nota (las `StatCard` del dashboard del web). */
@Composable
fun StatTile(label: String, value: String, caption: String?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
                color = ArizColors.Muted,
            )
            Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), color = ArizColors.Brand)
            caption?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ArizColors.Muted) }
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
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(40.dp).background(ArizColors.BrandSoft, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = ArizColors.Brand)
            }
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

/** Tipo de confirmación: decide el color (franja, icono, etiqueta y botón principal) y el icono del diálogo. */
enum class ConfirmTone { DANGER, WARNING, NEUTRAL }

/**
 * Diálogo de confirmación de la app (mismo diseño que el del web): franja de color arriba, icono en recuadro suave,
 * etiqueta, título, texto y dos botones (secundario y principal). El color depende de [tone]: rojo para acciones
 * destructivas (cerrar sesión, eliminar), ámbar para avisos (cambios sin guardar) y azul de marca para lo neutro.
 */
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    tone: ConfirmTone = ConfirmTone.DANGER,
    eyebrow: String? = null,
) {
    val accent = when (tone) {
        ConfirmTone.DANGER -> ArizColors.Error
        ConfirmTone.WARNING -> ArizColors.Warning
        ConfirmTone.NEUTRAL -> ArizColors.Brand
    }
    val buttonColor = when (tone) {
        ConfirmTone.DANGER -> ArizColors.Error
        ConfirmTone.WARNING -> ArizColors.Accent
        ConfirmTone.NEUTRAL -> ArizColors.Brand
    }
    val onButton = if (tone == ConfirmTone.WARNING) ArizColors.Brand else Color.White
    val label = eyebrow ?: when (tone) {
        ConfirmTone.DANGER -> "Confirma esta acción"
        ConfirmTone.WARNING -> "Atención"
        ConfirmTone.NEUTRAL -> "Confirmación"
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
            Column {
                Box(Modifier.fillMaxWidth().height(6.dp).background(accent))
                Column(Modifier.padding(horizontal = 22.dp).padding(top = 20.dp, bottom = 22.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(48.dp).background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Icon(if (tone == ConfirmTone.NEUTRAL) Icons.Outlined.Info else Icons.Outlined.Warning, contentDescription = null, modifier = Modifier.size(26.dp), tint = accent)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Outlined.Close, contentDescription = dismissLabel, modifier = Modifier.size(20.dp), tint = ArizColors.Muted)
                        }
                    }
                    Text(
                        text = label.uppercase(),
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp),
                        color = accent,
                    )
                    Text(
                        text = title,
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = ArizColors.Brand,
                    )
                    Text(text = text, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = ArizColors.Muted)
                    Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, ArizColors.Edge),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(dismissLabel, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = ArizColors.Brand, textAlign = TextAlign.Center)
                            }
                        }
                        Surface(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = buttonColor,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(confirmLabel, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = onButton, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}
