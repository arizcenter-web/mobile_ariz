package com.tecsup.app_ariz.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Bathtub
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.SquareFoot
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.core.ui.format.formatArea
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.core.ui.format.publishedLabel

private val WhatsAppGreen = Color(0xFF25D366)

/**
 * `ListingCard` del web en su versión móvil (una por fila): foto grande con etiquetas, precio,
 * título, ubicación, descripción, características y los botones "Ver propiedad" / "Contactar".
 */
@Composable
fun PropertyListItem(
    property: PropertySummary,
    onClick: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
) {
    val published = publishedLabel(property.publishedAtMillis)
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
        shadowElevation = 2.dp,
    ) {
        Column {
            ListImage(
                property = property,
                isNew = published?.isNew == true,
                isFavorite = isFavorite,
                onFavoriteClick = onFavoriteClick,
            )
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatPrice(property.price, property.currency),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = ArizColors.Brand,
                    )
                    if (property.operation == ListingOperation.RENT) {
                        Text(
                            text = " / mes",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                            color = ArizColors.Muted,
                        )
                    }
                }
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    color = ArizColors.Ink,
                )
                Meta(Icons.Outlined.Place, "${property.district}, ${property.department}")
                published?.let { Meta(Icons.Outlined.AccessTime, it.relative) }
                if (property.description.isNotBlank()) {
                    Text(
                        text = property.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                        color = ArizColors.Muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Specs(property)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActionButton(
                        text = "Ver propiedad",
                        container = ArizColors.Accent,
                        content = ArizColors.OnAccent,
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                    )
                    ActionButton(
                        text = "Contactar",
                        container = WhatsAppGreen,
                        content = Color.White,
                        onClick = onContactClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ListImage(property: PropertySummary, isNew: Boolean, isFavorite: Boolean, onFavoriteClick: (() -> Unit)?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .background(Brush.linearGradient(listOf(Color(0xFF2A3B85), ArizColors.Brand))),
    ) {
        if (property.coverImageUrl != null) {
            AsyncImage(
                model = property.coverImageUrl,
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
        Pill(
            text = when (property.operation) {
                ListingOperation.RENT -> "ALQUILER"
                ListingOperation.SALE -> "VENTA"
                ListingOperation.OTHER -> "INVERSIÓN"
            },
            container = ArizColors.Brand,
            content = Color.White,
            modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
        )
        if (isNew) {
            Pill("NUEVO", ArizColors.Accent, ArizColors.Brand, Modifier.align(Alignment.BottomStart).padding(10.dp))
        }
        if (onFavoriteClick != null) {
            FavoriteHeart(
                isFavorite = isFavorite,
                onClick = onFavoriteClick,
                contentDescription = if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
            )
        }
        if (property.photoCount > 1) {
            Pill(
                text = "${property.photoCount} fotos",
                container = ArizColors.Brand.copy(alpha = 0.75f),
                content = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
            )
        }
    }
}

@Composable
private fun Pill(text: String, container: Color, content: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.clip(RoundedCornerShape(50)).background(container).padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp),
        color = content,
    )
}

@Composable
private fun Meta(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArizColors.Muted)
        Text(text, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = ArizColors.Muted)
    }
}

/** `ListingSpecs` del web: área, dormitorios, baños y estacionamientos; omite los que valen 0. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Specs(property: PropertySummary) {
    val specs = listOf(
        Icons.Outlined.SquareFoot to formatArea(property.areaM2),
        Icons.Outlined.Hotel to if (property.bedrooms > 0) "${property.bedrooms} dorm." else "",
        Icons.Outlined.Bathtub to if (property.bathrooms > 0) "${property.bathrooms} baños" else "",
        Icons.Outlined.DirectionsCar to if (property.parkingSpaces > 0) "${property.parkingSpaces} estac." else "",
    ).filter { it.second.isNotEmpty() }
    if (specs.isEmpty()) return
    FlowRow(
        modifier = Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        specs.forEach { (icon, text) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArizColors.Brand.copy(alpha = 0.55f))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    color = ArizColors.Muted,
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(6.dp),
        color = container,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = content,
            )
        }
    }
}
