package com.tecsup.app_ariz.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.app_ariz.R
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Tiempo mínimo que se ve la pantalla de bienvenida (el logo entra, aparece el nombre y respira un instante). */
private const val MinSplashMillis = 2600L

/**
 * Pantalla de bienvenida de marca: fondo blanco, el logo entra con un resorte, el nombre sube con fade, una línea
 * dorada se dibuja y una barra de carga fina cierra la secuencia. Avisa con [onFinished] cuando [ready] es verdadero
 * y pasó el tiempo mínimo.
 */
@Composable
fun ArizSplash(ready: Boolean, onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(24f) }
    val lineWidth = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    val minElapsed = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { logoAlpha.animateTo(1f, tween(450)) }
            launch { logoScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
            launch { progress.animateTo(1f, tween(MinSplashMillis.toInt() - 300, easing = FastOutSlowInEasing)) }
            launch {
                delay(500)
                launch { titleAlpha.animateTo(1f, tween(500)) }
                launch { titleOffset.animateTo(0f, tween(550, easing = FastOutSlowInEasing)) }
            }
            launch {
                delay(900)
                lineWidth.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
            }
            launch {
                delay(1200)
                subtitleAlpha.animateTo(1f, tween(500))
            }
            delay(MinSplashMillis)
            minElapsed.snapTo(1f)
        }
    }
    // Termina cuando pasó el tiempo mínimo y la sesión ya se validó.
    LaunchedEffect(ready, minElapsed.value) {
        if (ready && minElapsed.value >= 1f) onFinished()
    }

    Box(modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.align(Alignment.Center).padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Image(
                painter = painterResource(R.drawable.splash_logo),
                contentDescription = null,
                modifier = Modifier.height(140.dp).scale(logoScale.value).alpha(logoAlpha.value),
            )
            Text(
                "ARIZ",
                modifier = Modifier.padding(top = 28.dp).graphicsLayer { translationY = titleOffset.value.dp.toPx(); alpha = titleAlpha.value },
                color = ArizColors.Brand,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                "INMOBILIARIA",
                modifier = Modifier.graphicsLayer { translationY = titleOffset.value.dp.toPx(); alpha = titleAlpha.value },
                color = ArizColors.Accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center,
            )
            Box(
                Modifier.padding(top = 14.dp).width(120.dp * lineWidth.value).height(3.dp).clip(RoundedCornerShape(50)).background(ArizColors.Accent),
            )
            Text(
                "Encuentra, invierte y crece",
                modifier = Modifier.padding(top = 14.dp).alpha(subtitleAlpha.value),
                color = ArizColors.Muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
        LinearProgressIndicator(
            progress = { progress.value },
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 48.dp).width(140.dp).height(3.dp).clip(RoundedCornerShape(50)),
            color = ArizColors.Brand,
            trackColor = ArizColors.Brand.copy(alpha = 0.12f),
            drawStopIndicator = {},
        )
    }
}
