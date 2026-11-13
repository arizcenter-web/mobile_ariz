package com.tecsup.app_ariz.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.core.favorites.ui.FavoritesUi
import com.tecsup.app_ariz.core.favorites.ui.rememberFavorites
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons
import com.tecsup.app_ariz.feature.home.R
import com.tecsup.app_ariz.feature.home.navigation.HomeDestination
import com.tecsup.app_ariz.feature.home.ui.component.CatalogSections
import com.tecsup.app_ariz.feature.home.ui.component.CategoryShowcase
import com.tecsup.app_ariz.feature.home.ui.component.HeroSearchOverlap
import com.tecsup.app_ariz.feature.home.ui.component.HomeHero
import com.tecsup.app_ariz.feature.home.ui.component.HomeSearchEntry
import com.tecsup.app_ariz.feature.home.ui.component.WhyChooseSection
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onDestinationClick: (HomeDestination) -> Unit,
    onPropertyClick: (slug: String) -> Unit,
    onProjectClick: (slug: String) -> Unit,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    LightStatusBarIcons()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val favorites = rememberFavorites(
        onLoginRequired = onLoginRequired,
        onError = { message -> scope.launch { snackbarHostState.showSnackbar(message) } },
    )
    val comingSoon = stringResource(R.string.home_coming_soon)
    // Destinos que todavía no existen en la app: se avisa en lugar de dejar el botón muerto.
    val onNavigate: (HomeDestination?) -> Unit = { destination ->
        if (destination != null) {
            onDestinationClick(destination)
        } else {
            scope.launch { snackbarHostState.showSnackbar(comingSoon) }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = ArizColors.SectionCanvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        HomeContent(
            state = state,
            onAction = viewModel::onAction,
            onNavigate = onNavigate,
            onPropertyClick = onPropertyClick,
            onProjectClick = onProjectClick,
            favorites = favorites,
            modifier = Modifier.padding(padding),
        )
    }
}

/** Contenido sin estado: recibe solo callbacks, por eso se puede previsualizar. */
@Composable
fun HomeContent(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    onNavigate: (HomeDestination?) -> Unit,
    onPropertyClick: (slug: String) -> Unit,
    onProjectClick: (slug: String) -> Unit,
    favorites: FavoritesUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArizColors.SectionCanvas)
            .verticalScroll(rememberScrollState()),
    ) {
        // zIndex: el buscador sobresale del hero y debe dibujarse encima de la sección siguiente.
        Box(modifier = Modifier.zIndex(1f)) {
            HomeHero(
                onClubClick = { onNavigate(HomeDestination.REWARDS) },
                onPropertiesClick = { onNavigate(HomeDestination.SEARCH) },
                onProjectsClick = { onNavigate(HomeDestination.PROJECTS) },
            )
            HomeSearchEntry(
                onClick = { onNavigate(HomeDestination.SEARCH) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .offset(y = HeroSearchOverlap),
            )
        }

        CategoryShowcase(
            onCategoryClick = { category -> onNavigate(category.destination) },
            modifier = Modifier.padding(top = HeroSearchOverlap + 32.dp, bottom = 28.dp),
        )
        CatalogSections(
            isLoading = state.isLoading,
            feed = state.feed,
            error = state.error,
            onRetry = { onAction(HomeAction.Retry) },
            onPropertyClick = { onPropertyClick(it.slug) },
            onProjectClick = { onProjectClick(it.slug) },
            favorites = favorites,
            onAllPropertiesClick = { onNavigate(HomeDestination.SEARCH) },
            onAllProjectsClick = { onNavigate(HomeDestination.PROJECTS) },
        )
        WhyChooseSection()
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun HomeContentPreview() {
    ArizTheme { HomeContent(state = HomeUiState(isLoading = false), onAction = {}, onNavigate = {}, onPropertyClick = {}, onProjectClick = {}, favorites = FavoritesUi()) }
}
