package com.tecsup.app_ariz.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Place
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.model.project.ProjectCategory
import com.tecsup.app_ariz.core.model.project.ProjectStage
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.core.ui.format.formatArea
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.core.ui.format.publishedLabel

private val CardShape = RoundedCornerShape(12.dp)
private val ImageHeight = 112.dp

/** `FeaturedListingCard` del web: foto con etiquetas, precio, título, ubicación, ficha y botón amarillo. */
@Composable
fun PropertyCard(
    property: PropertySummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
) {
    val published = publishedLabel(property.publishedAtMillis)
    CatalogCardSurface(onClick = onClick, modifier = modifier) {
        CardImage(url = property.coverImageUrl) {
            Badge(
                text = if (property.operation == ListingOperation.RENT) "ALQUILER" else "VENTA",
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            )
            if (published?.isNew == true) {
                Badge(text = "NUEVO", modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
            }
            if (onFavoriteClick != null) {
                FavoriteHeart(
                    isFavorite = isFavorite,
                    onClick = onFavoriteClick,
                    contentDescription = if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(8.dp).weight(1f)) {
            Text(
                text = formatPrice(property.price, property.currency),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Black),
                color = ArizColors.Brand,
            )
            Text(
                text = property.title,
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = ArizColors.Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            MetaLine(Icons.Outlined.Place, "${property.district}, ${property.department}")
            published?.let { MetaLine(Icons.Outlined.AccessTime, it.relative) }
            DataGrid(
                cells = listOf(
                    "Área" to formatArea(property.areaM2),
                    "Dorm." to property.bedrooms.toString(),
                    "Baños" to property.bathrooms.toString(),
                    "Estac." to property.parkingSpaces.toString(),
                ),
            )
            Column(modifier = Modifier.weight(1f)) {}
            CardButton(text = "Ver propiedad", onClick = onClick)
        }
    }
}

/** `ProjectCard` del web: estado del proyecto, desarrolladora, nombre, "Desde" y ficha. */
@Composable
fun ProjectCard(
    project: ProjectSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val published = publishedLabel(project.publishedAtMillis)
    CatalogCardSurface(onClick = onClick, modifier = modifier) {
        CardImage(url = project.coverImageUrl) {
            Badge(
                text = project.stage.label(),
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                small = true,
            )
            if (published?.isNew == true) {
                Badge(text = "NUEVO", modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
            }
        }
        Column(modifier = Modifier.padding(8.dp).weight(1f)) {
            Text(
                text = project.developer.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                ),
                color = ArizColors.Accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = project.name,
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = ArizColors.Brand,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Desde ${formatPrice(project.priceFrom, project.currency)}",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = ArizColors.Brand,
            )
            published?.let { MetaLine(Icons.Outlined.AccessTime, it.relative) }
            DataGrid(
                cells = listOf(
                    "Área" to formatArea(project.minAreaM2),
                    (if (project.category == ProjectCategory.DEPARTAMENTOS) "departamentos" else "lotes") to
                        project.totalUnits.toString(),
                    "Entrega" to project.stage.deliveryLabel(),
                    "Distrito" to project.district,
                ),
            )
            Column(modifier = Modifier.weight(1f)) {}
            CardButton(text = "Ver proyecto", onClick = onClick)
        }
    }
}

private fun ProjectStage.label() = when (this) {
    ProjectStage.PRE_SALE -> "En preventa"
    ProjectStage.UNDER_CONSTRUCTION -> "En construcción"
    ProjectStage.DELIVERED -> "Entregado"
}

private fun ProjectStage.deliveryLabel() = when (this) {
    ProjectStage.PRE_SALE -> "Preventa"
    ProjectStage.UNDER_CONSTRUCTION -> "En construcción"
    ProjectStage.DELIVERED -> "Entregado"
}

@Composable
private fun CatalogCardSurface(
    onClick: () -> Unit,
    modifier: Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize(), content = content)
    }
}

/** `ListingImage` del web: foto con degradado de marca abajo; sin foto, degradado azul. */
@Composable
private fun CardImage(url: String?, overlay: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ImageHeight)
            .background(Brush.linearGradient(listOf(Color(0xFF2A3B85), ArizColors.Brand))),
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.38f to Color.Transparent,
                        1f to ArizColors.Brand.copy(alpha = 0.72f),
                    ),
                ),
        )
        overlay()
    }
}

@Composable
private fun Badge(text: String, modifier: Modifier = Modifier, small: Boolean = false) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ArizColors.Accent)
            .padding(horizontal = if (small) 6.dp else 10.dp, vertical = if (small) 2.dp else 4.dp),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = if (small) 9.sp else 10.sp,
            fontWeight = FontWeight.Black,
        ),
        color = ArizColors.OnAccent,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun MetaLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = ArizColors.Muted)
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = ArizColors.Muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** `DataGrid` del web en su variante angosta: 2 columnas de etiqueta + valor; omite las celdas vacías. */
@Composable
private fun DataGrid(cells: List<Pair<String, String>>) {
    val visible = cells.filter { it.second.isNotBlank() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ArizColors.Edge))
        visible.chunked(2).forEach { pair ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            ),
                            color = ArizColors.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = ArizColors.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (pair.size == 1) Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CardButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(34.dp).padding(top = 0.dp),
        shape = RoundedCornerShape(4.dp),
        color = ArizColors.Accent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Black),
                color = ArizColors.OnAccent,
            )
        }
    }
}
