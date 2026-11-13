package com.tecsup.app_ariz.core.catalog.data.repository

import com.tecsup.app_ariz.core.catalog.data.mapper.toDomain
import com.tecsup.app_ariz.core.catalog.data.mapper.toProjectQuery
import com.tecsup.app_ariz.core.catalog.data.mapper.toQuery
import com.tecsup.app_ariz.core.catalog.data.remote.CatalogApi
import com.tecsup.app_ariz.core.catalog.domain.CatalogRepository
import com.tecsup.app_ariz.core.catalog.domain.ProjectOrigin
import com.tecsup.app_ariz.core.catalog.domain.ProjectSort
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.model.common.Page
import com.tecsup.app_ariz.core.model.project.ProjectFilters
import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.core.model.property.PropertySummary
import com.tecsup.app_ariz.core.network.safeApiCall
import javax.inject.Inject

internal class CatalogRepositoryImpl @Inject constructor(
    private val api: CatalogApi,
) : CatalogRepository {

    override suspend fun recentProperties(pageSize: Int, page: Int): AppResult<Page<PropertySummary>> =
        safeApiCall { api.properties(sort = "recent", pageSize = pageSize, page = page) }
            .map { dto -> dto.toDomain { it.toDomain() } }

    override suspend fun searchProperties(
        filters: PropertyFilters,
        pageSize: Int,
        page: Int,
    ): AppResult<Page<PropertySummary>> {
        val query = filters.toQuery()
        return safeApiCall {
            api.properties(
                sort = query.sort,
                pageSize = pageSize,
                page = page,
                operationType = query.operationType,
                propertyType = query.propertyType,
                district = query.district,
                priceMin = query.priceMin,
                priceMax = query.priceMax,
                bedroomsMin = query.bedroomsMin,
                areaMin = query.areaMin,
            )
        }.map { dto -> dto.toDomain { it.toDomain() } }
    }

    override suspend fun projects(
        pageSize: Int,
        origin: ProjectOrigin?,
        sort: ProjectSort?,
        page: Int,
    ): AppResult<Page<ProjectSummary>> =
        safeApiCall {
            api.projects(origin = origin?.apiValue, sort = sort?.apiValue, pageSize = pageSize, page = page)
        }.map { dto -> dto.toDomain { it.toDomain() } }

    override suspend fun searchProjects(
        filters: ProjectFilters,
        pageSize: Int,
        page: Int,
    ): AppResult<Page<ProjectSummary>> {
        val query = filters.toProjectQuery()
        return safeApiCall {
            api.projects(
                origin = query.origin,
                sort = query.sort,
                pageSize = pageSize,
                page = page,
                stage = query.stage,
                district = query.district,
                currency = query.currency,
                priceMax = query.priceMax,
            )
        }.map { dto -> dto.toDomain { it.toDomain() } }
    }
}
