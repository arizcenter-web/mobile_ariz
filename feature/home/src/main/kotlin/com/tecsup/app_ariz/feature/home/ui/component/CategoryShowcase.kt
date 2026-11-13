package com.tecsup.app_ariz.feature.home.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.feature.home.R
import com.tecsup.app_ariz.feature.home.navigation.HomeDestination

/** Una tarjeta de "¿Qué quieres hacer hoy?". `destination == null`: aún no existe la pantalla. */
internal enum class HomeCategory(
    @StringRes val label: Int,
    @StringRes val subtitle: Int,
    val icon: ImageVector,
    val destination: HomeDestination?,
) {
    BUY(R.string.home_category_buy_label, R.string.home_category_buy_subtitle, Icons.Outlined.Home, HomeDestination.SEARCH),
    PROJECTS(R.string.home_category_projects_label, R.string.home_category_projects_subtitle, Icons.Outlined.Apartment, HomeDestination.PROJECTS),
    INVEST(R.string.home_category_invest_label, R.string.home_category_invest_subtitle, Icons.AutoMirrored.Outlined.TrendingUp, HomeDestination.INVESTMENTS),
    SELL(R.string.home_category_sell_label, R.string.home_category_sell_subtitle, Icons.Outlined.Sell, HomeDestination.SELL),
    BUILD(R.string.home_category_build_label, R.string.home_category_build_subtitle, Icons.Outlined.Construction, HomeDestination.BUILD),
    RENT(R.string.home_category_rent_label, R.string.home_category_rent_subtitle, Icons.Outlined.Key, HomeDestination.SEARCH),
}

/** Título "¿Qué quieres hacer hoy?" y fila deslizable de tarjetas azules (`category-showcase.tsx`). */
@Composable
internal fun CategoryShowcase(
    onCategoryClick: (HomeCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.home_categories_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 26.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                ),
                color = ArizColors.Brand,
            )
            Text(
                text = stringResource(R.string.home_categories_hint),
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = ArizColors.Muted,
            )
        }
        LazyRow(
            modifier = Modifier.padding(top = 20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(HomeCategory.entries, key = { it.name }) { category ->
                CategoryCard(category = category, onClick = { onCategoryClick(category) })
            }
        }
    }
}

@Composable
private fun CategoryCard(category: HomeCategory, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(6.dp)
    Column(
        modifier = modifier
            .width(175.dp)
            .height(144.dp)
            .shadow(8.dp, shape, clip = false)
            .background(ArizColors.Brand, shape)
            .border(1.dp, ArizColors.Brand, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = Color.White,
        )
        Text(
            text = stringResource(category.label),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 14.5.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Black,
            ),
            color = ArizColors.Accent,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(category.subtitle),
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )
        Box(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.size(20.dp).background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = ArizColors.Brand,
            )
        }
    }
}
