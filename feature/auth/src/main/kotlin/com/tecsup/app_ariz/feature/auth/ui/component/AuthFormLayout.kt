package com.tecsup.app_ariz.feature.auth.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.designsystem.component.ArizAlert
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.designsystem.R as DesignSystemR

private val CardMaxWidth = 430.dp
private val TopBarHeight = 72.dp

/**
 * Pantalla de autenticación con el aspecto de `AuthShell` (centeredBackground) del web: foto de fondo
 * con velo azul de marca al 65 %, tarjeta blanca centrada con el logo y, debajo, la cabecera
 * (etiqueta amarilla, título liviano y subtítulo) y el contenido.
 */
@Composable
fun AuthFormLayout(
    eyebrow: String,
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(DesignSystemR.drawable.ariz_auth_background),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
        )

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
            // Barra superior FIJA con la salida (la barra inferior no se muestra en estas pantallas).
            // Está fuera del área con scroll: la tarjeta se recorta en su borde inferior y nunca
            // pasa por detrás de la flecha.
            Box(modifier = Modifier.fillMaxWidth().height(TopBarHeight)) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val availableHeight = maxHeight
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = availableHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().widthIn(max = CardMaxWidth),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, ArizColors.Edge),
                        shadowElevation = 12.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Image(
                                painter = painterResource(DesignSystemR.drawable.ariz_logo),
                                contentDescription = "Ariz Proyectos",
                                modifier = Modifier.height(40.dp).align(Alignment.CenterHorizontally),
                                contentScale = ContentScale.Fit,
                            )
                            AuthHeader(eyebrow = eyebrow, title = title, subtitle = subtitle)
                            content()
                        }
                    }
                }
            }
        }
    }
}

/** `AuthEyebrow` + `AuthTitle` + `AuthSubtitle` del web, centrados. */
@Composable
private fun AuthHeader(eyebrow: String, title: String, subtitle: String?) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            ),
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Enlace de pie de formulario: "¿Aún no tienes cuenta? **Registrarse**". Toda la fila es pulsable. */
@Composable
fun AuthFooterLink(
    link: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    prefix: String? = null,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    Text(
        text = buildAnnotatedString {
            if (prefix != null) append("$prefix ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = linkColor)) { append(link) }
        },
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

/** Error general devuelto por el backend (credenciales inválidas, límite de intentos, red...). */
@Composable
fun FormErrorText(error: AppError?, modifier: Modifier = Modifier) {
    if (error == null) return
    ArizAlert(message = error.message, modifier = modifier)
}
