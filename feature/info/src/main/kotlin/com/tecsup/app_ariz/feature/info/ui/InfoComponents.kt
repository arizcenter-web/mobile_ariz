package com.tecsup.app_ariz.feature.info.ui

import com.tecsup.app_ariz.core.ui.statusBarScrim
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons
import com.tecsup.app_ariz.core.ui.component.EyebrowChip
import com.tecsup.app_ariz.core.ui.component.PanelHeader
import com.tecsup.app_ariz.feature.info.R

/**
 * Estructura de las páginas informativas: cabecera azul con la etiqueta amarilla, el título y el
 * subtítulo (como el `<section className="bg-brand">` del web) y, debajo, el contenido sobre fondo claro.
 */
@Composable
internal fun InfoPageLayout(
    eyebrow: String,
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
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
        PanelHeader(onBackClick = onBackClick, backDescription = stringResource(R.string.info_back)) {
            EyebrowChip(eyebrow)
            Text(
                text = title,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, lineHeight = 34.sp),
                color = Color.White,
            )
            subtitle?.let {
                Text(text = it, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.78f))
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

/** Tarjeta blanca con borde, la base de casi todos los bloques del web (`rounded-card border bg-surface`). */
@Composable
internal fun InfoCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
internal fun Eyebrow(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp),
        color = ArizColors.Muted,
    )
}

@Composable
internal fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
        color = ArizColors.Brand,
    )
}

@Composable
internal fun BodyText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp), color = ArizColors.Muted)
}

/** Icono dentro de un cuadro azul claro, como los `size-11 rounded-card bg-brand/10` del web. */
@Composable
internal fun IconBadge(icon: ImageVector) {
    Box(
        modifier = Modifier.size(44.dp).background(ArizColors.BrandSoft, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = ArizColors.Brand)
    }
}

/** Botón relleno de marca (`bg-brand text-on-brand`). */
@Composable
internal fun BrandButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, filled: Boolean = true) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(46.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (filled) ArizColors.Brand else MaterialTheme.colorScheme.surface,
        border = if (filled) null else BorderStroke(1.dp, ArizColors.Edge),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                color = if (filled) Color.White else ArizColors.Brand,
                textAlign = TextAlign.Center,
            )
        }
    }
}
