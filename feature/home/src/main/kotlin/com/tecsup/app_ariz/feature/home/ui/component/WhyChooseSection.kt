package com.tecsup.app_ariz.feature.home.ui.component

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.feature.home.R

private val CardHeight = 240.dp

private enum class Reason(
    val icon: ImageVector,
    @StringRes val title: Int,
    @StringRes val description: Int,
    @StringRes val backTitle: Int,
    val points: List<Int>,
) {
    VERIFIED(
        Icons.Outlined.Verified,
        R.string.home_why_verified_title,
        R.string.home_why_verified_description,
        R.string.home_why_verified_back_title,
        listOf(R.string.home_why_verified_point_1, R.string.home_why_verified_point_2, R.string.home_why_verified_point_3),
    ),
    LOCATION(
        Icons.Outlined.PinDrop,
        R.string.home_why_location_title,
        R.string.home_why_location_description,
        R.string.home_why_location_back_title,
        listOf(R.string.home_why_location_point_1, R.string.home_why_location_point_2, R.string.home_why_location_point_3),
    ),
    VALUES(
        Icons.Outlined.ShowChart,
        R.string.home_why_values_title,
        R.string.home_why_values_description,
        R.string.home_why_values_back_title,
        listOf(R.string.home_why_values_point_1, R.string.home_why_values_point_2, R.string.home_why_values_point_3),
    ),
    SUPPORT(
        Icons.Outlined.Handshake,
        R.string.home_why_support_title,
        R.string.home_why_support_description,
        R.string.home_why_support_back_title,
        listOf(R.string.home_why_support_point_1, R.string.home_why_support_point_2, R.string.home_why_support_point_3),
    ),
    REGISTERED(
        Icons.Outlined.Shield,
        R.string.home_why_registered_title,
        R.string.home_why_registered_description,
        R.string.home_why_registered_back_title,
        listOf(R.string.home_why_registered_point_1, R.string.home_why_registered_point_2, R.string.home_why_registered_point_3),
    ),
}

/** "Por qué elegir Ariz": cinco tarjetas en dos columnas que giran al tocarlas (`why-choose-ariz-section.tsx`). */
@Composable
internal fun WhyChooseSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 40.dp),
    ) {
        Text(
            text = stringResource(R.string.home_why_eyebrow),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            ),
            color = ArizColors.Accent,
        )
        Text(
            text = stringResource(R.string.home_why_title),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp, fontWeight = FontWeight.Black),
            color = ArizColors.Heading,
        )
        Text(
            text = stringResource(R.string.home_why_description),
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = ArizColors.BodyMuted,
        )

        Column(modifier = Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Reason.entries.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { reason -> FlipReasonCard(reason, Modifier.weight(1f)) }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/** Cara frontal (resumen) y trasera (3 datos útiles); gira en el eje Y con un toque. */
@Composable
private fun FlipReasonCard(reason: Reason, modifier: Modifier = Modifier) {
    var flipped by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "flip",
    )
    val showBack = rotation > 90f
    val cameraDistance = 14 * LocalDensity.current.density

    Box(
        modifier = modifier
            .height(CardHeight)
            .graphicsLayer {
                rotationY = rotation
                this.cameraDistance = cameraDistance
            }
            .clickable(role = Role.Button) { flipped = !flipped },
    ) {
        if (showBack) {
            // Contenido trasero girado 180° para que no se vea en espejo.
            ReasonBack(reason, Modifier.graphicsLayer { rotationY = 180f })
        } else {
            ReasonFront(reason)
        }
    }
}

@Composable
private fun ReasonFront(reason: Reason) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .shadow(2.dp, shape, clip = false)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, ArizColors.Edge, shape)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(ArizColors.Accent.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(reason.icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = ArizColors.Accent)
        }
        Text(
            text = stringResource(reason.title),
            modifier = Modifier.padding(top = 10.dp),
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.Black),
            color = ArizColors.Heading,
        )
        Text(
            text = stringResource(reason.description),
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
            color = ArizColors.BodyMuted,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.home_why_tap_more),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = ArizColors.Brand,
        )
    }
}

@Composable
private fun ReasonBack(reason: Reason, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .fillMaxSize()
            .shadow(8.dp, shape, clip = false)
            .background(ArizColors.Brand, shape)
            .padding(14.dp),
    ) {
        Text(
            text = stringResource(R.string.home_why_back_eyebrow),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
            ),
            color = ArizColors.Accent,
        )
        Text(
            text = stringResource(reason.backTitle),
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Black),
            color = Color.White,
        )
        Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            reason.points.forEach { point ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.padding(top = 5.dp).size(6.dp).background(ArizColors.Accent, CircleShape))
                    Text(
                        text = stringResource(point),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.home_why_back_return),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
            color = Color.White.copy(alpha = 0.8f),
        )
    }
}
