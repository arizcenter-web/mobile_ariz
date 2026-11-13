package com.tecsup.app_ariz.feature.projects.ui.detail

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectDetail

data class ProjectDetailUiState(
    val isLoading: Boolean = true,
    val detail: ProjectDetail? = null,
    val error: AppError? = null,
)

sealed interface ProjectDetailAction {
    data object Retry : ProjectDetailAction
}
