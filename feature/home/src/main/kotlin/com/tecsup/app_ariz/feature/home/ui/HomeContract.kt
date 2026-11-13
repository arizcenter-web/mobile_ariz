package com.tecsup.app_ariz.feature.home.ui

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.feature.home.domain.model.HomeFeed

data class HomeUiState(
    val isLoading: Boolean = true,
    val feed: HomeFeed? = null,
    val error: AppError? = null,
)

sealed interface HomeAction {
    data object Retry : HomeAction
}
