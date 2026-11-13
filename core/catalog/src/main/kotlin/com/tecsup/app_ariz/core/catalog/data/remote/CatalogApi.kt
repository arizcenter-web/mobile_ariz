package com.tecsup.app_ariz.core.catalog.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** Catálogo público del backend (no requiere sesión). Los parámetros `null` no se envían. */
internal interface CatalogApi {

    @GET("properties")
    suspend fun properties(
        @Query("sort") sort: String? = null,
        @Query("pageSize") pageSize: Int,
        @Query("page") page: Int = 1,
        @Query("operationType") operationType: String? = null,
        @Query("propertyType") propertyType: String? = null,
        @Query("district") district: String? = null,
        @Query("priceMin") priceMin: Int? = null,
        @Query("priceMax") priceMax: Int? = null,
        @Query("bedroomsMin") bedroomsMin: Int? = null,
        @Query("areaMin") areaMin: Int? = null,
    ): PagedDto<PropertyDto>

    @GET("projects")
    suspend fun projects(
        @Query("origin") origin: String? = null,
        @Query("sort") sort: String? = null,
        @Query("pageSize") pageSize: Int,
        @Query("page") page: Int = 1,
        @Query("stage") stage: String? = null,
        @Query("district") district: String? = null,
        @Query("currency") currency: String? = null,
        @Query("priceMax") priceMax: Int? = null,
    ): PagedDto<ProjectDto>
}
