package com.tecsup.app_ariz.feature.home.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.feature.home.R

private const val HeroImageIntervalMs = 5_000L
private val HeroImages = listOf(
    R.drawable.home_hero_1,
    R.drawable.home_hero_2,
    R.drawable.home_hero_3,
    R.drawable.home_hero_4,
)

/** Alto de la parte del buscador que sobresale por debajo del hero (se compensa en la sección siguiente). */
val HeroSearchOverlap = 28.dp

/**
 * Hero del home del web: fotos de fondo rotando cada 5 s con velo azul, titular, subtítulo y los tres
 * botones amarillos, con la franja amarilla inferior. Ocupa el espacio bajo la barra de estado.
 */
@Composable
fun HomeHero(
    onClubClick: () -> Unit,
    onPropertiesClick: () -> Unit,
    onProjectsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        HeroBackground(images = HeroImages, modifier = Modifier.matchParentSize())
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ArizColors.HeroShade.copy(alpha = 0.55f),
                            ArizColors.HeroShade.copy(alpha = 0.35f),
                            ArizColors.HeroShade.copy(alpha = 0.6f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = HeroSearchOverlap + 44.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = stringResource(R.string.home_hero_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 31.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color.White,
            )
            Text(
                text = stringResource(R.string.home_hero_subtitle),
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Color.White.copy(alpha = 0.9f),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HeroActionButton(
                    text = stringResource(R.string.home_hero_cta_club),
                    onClick = onClubClick,
                    modifier = Modifier.weight(1f),
                )
                HeroActionButton(
                    text = stringResource(R.string.home_hero_cta_properties),
                    onClick = onPropertiesClick,
                    modifier = Modifier.weight(1f),
                )
                HeroActionButton(
                    text = stringResource(R.string.home_hero_cta_projects),
                    onClick = onProjectsClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(4.dp)
                .background(ArizColors.Accent),
        )
    }
}

@Composable
private fun HeroBackground(images: List<Int>, modifier: Modifier = Modifier) {
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(images.size) {
        while (true) {
            kotlinx.coroutines.delay(HeroImageIntervalMs)
            index = (index + 1) % images.size
        }
    }
    Crossfade(targetState = index, modifier = modifier, animationSpec = tween(durationMillis = 1_000), label = "hero") { current ->
        Image(
            painter = painterResource(images[current]),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

/** Botón amarillo del hero: `rounded-[6px] border-brand bg-accent text-brand font-black`. */
@Composable
private fun HeroActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .height(44.dp)
            .shadow(8.dp, shape, clip = false)
            .background(ArizColors.Accent, shape)
            .border(1.dp, ArizColors.Brand, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Black,
            ),
            color = ArizColors.OnAccent,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Buscador del hero. En el web es el formulario completo; en móvil es un campo que abre la pestaña
 * Buscar (la feature `search` es dueña del formulario). Se solapa sobre el borde inferior del hero.
 */
@Composable
fun HomeSearchEntry(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.home_search_content_description)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp).height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = ArizColors.Brand,
            )
            Text(
                text = stringResource(R.string.home_search_placeholder),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                color = ArizColors.Muted,
            )
        }
    }
}
