package com.tecsup.app_ariz.feature.projects.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.model.project.ProjectCategory
import com.tecsup.app_ariz.core.model.project.ProjectStage
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.core.ui.format.publishedLabel
import com.tecsup.app_ariz.feature.projects.R

/** Tarjeta del directorio de proyectos (`s.card` del web): portada, nombre, desarrolladora, ubicación y precio. */
@Composable
fun ProjectListItem(project: ProjectSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val published = publishedLabel(project.publishedAtMillis)
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF2A3B85), ArizColors.Brand))),
            ) {
                if (project.coverImageUrl != null) {
                    AsyncImage(
                        model = project.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(0.38f to Color.Transparent, 1f to ArizColors.Brand.copy(alpha = 0.72f)),
                    ),
                )
                if (project.featured) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(ArizColors.Accent)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = ArizColors.Brand)
                        Text(
                            text = stringResource(R.string.projects_card_featured),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                            color = ArizColors.Brand,
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ArizColors.Brand,
                    maxLines = 2,
                )
                Text(
                    text = project.developer.ifBlank { stringResource(R.string.projects_card_developer_fallback) },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                    color = ArizColors.Accent,
                    maxLines = 1,
                )
                Meta(Icons.Outlined.Place, "${project.district}")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Meta(
                        Icons.Outlined.Layers,
                        if (project.category == ProjectCategory.DEPARTAMENTOS) {
                            stringResource(R.string.projects_card_apartments, project.totalUnits)
                        } else {
                            stringResource(R.string.projects_card_lots, project.totalUnits)
                        },
                    )
                    Text(
                        text = stringResource(project.stage.shortLabelRes()),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = ArizColors.Muted,
                    )
                }
                published?.let { Meta(Icons.Outlined.CalendarMonth, it.relative) }
                Text(
                    text = stringResource(R.string.projects_card_from, formatPrice(project.cheapestPrice ?: project.priceFrom, project.currency)),
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                )
                Surface(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(40.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = ArizColors.Accent,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.projects_card_view),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = ArizColors.OnAccent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Meta(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArizColors.Muted)
        Text(text, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = ArizColors.Muted)
    }
}

internal fun ProjectStage.shortLabelRes(): Int = when (this) {
    ProjectStage.PRE_SALE -> R.string.projects_stage_pre_sale
    ProjectStage.UNDER_CONSTRUCTION -> R.string.projects_stage_under_construction
    ProjectStage.DELIVERED -> R.string.projects_stage_delivered
}
