package com.tecsup.app_ariz.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors

/**
 * `PropertyPhotoGallery` del web: fotos deslizables con contador "2 / 8". Sin fotos se ve el
 * degradado de marca con [emptyLabel]. Una sombra arriba mantiene legibles la barra de estado y el
 * botón de volver. El texto de "sin fotos" lo pone quien llama, que es quien tiene sus recursos de idioma.
 */
@Composable
fun PhotoGallery(
    images: List<String>,
    emptyLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Brush.linearGradient(listOf(Color(0xFF2A3B85), ArizColors.Brand))),
    ) {
        if (images.isEmpty()) {
            Text(
                text = emptyLabel,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { images.size })
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            if (images.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ArizColors.Brand.copy(alpha = 0.75f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent))),
        )
        content()
    }
}
