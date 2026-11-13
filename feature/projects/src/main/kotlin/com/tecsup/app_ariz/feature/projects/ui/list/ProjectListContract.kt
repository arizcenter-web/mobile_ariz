package com.tecsup.app_ariz.feature.projects.ui.list

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.project.ProjectFilters
import com.tecsup.app_ariz.core.model.project.ProjectOriginFilter
import com.tecsup.app_ariz.core.model.project.ProjectSortOrder
import com.tecsup.app_ariz.core.model.project.ProjectStage
import com.tecsup.app_ariz.core.model.project.ProjectSummary

/** Presupuestos máximos del directorio, los mismos del web. */
val BudgetOptions: List<Int?> = listOf(null, 50_000, 100_000, 200_000, 500_000)

data class ProjectListUiState(
    /** Filtros ya aplicados: son los que produjeron [items]. */
    val filters: ProjectFilters = ProjectFilters(),
    /** Texto de distrito que el usuario escribe; se aplica con la tecla de búsqueda. */
    val districtText: String = "",
    val items: List<ProjectSummary> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val hasMore: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: AppError? = null,
)

sealed interface ProjectListAction {
    data class StageChanged(val value: ProjectStage?) : ProjectListAction
    data class DistrictChanged(val value: String) : ProjectListAction
    data object DistrictSubmitted : ProjectListAction
    data class OriginChanged(val value: ProjectOriginFilter?) : ProjectListAction
    data class CurrencyChanged(val value: Currency) : ProjectListAction
    data class BudgetChanged(val value: Int?) : ProjectListAction
    data class SortChanged(val value: ProjectSortOrder) : ProjectListAction
    data object LoadMore : ProjectListAction
    data object Retry : ProjectListAction
}
