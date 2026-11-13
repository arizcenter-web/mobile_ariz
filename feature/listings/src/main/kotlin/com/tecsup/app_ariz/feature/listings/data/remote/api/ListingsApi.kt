package com.tecsup.app_ariz.feature.listings.data.remote.api

import com.tecsup.app_ariz.feature.listings.data.remote.dto.PropertyDetailDto
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Cuerpo de `POST /alerts/price`; `targetPrice` es opcional y el web no lo envía. */
@Serializable
internal data class PriceAlertRequest(val propertyId: String, val targetPrice: Double? = null)

internal interface ListingsApi {

    @GET("properties/{slug}")
    suspend fun propertyBySlug(@Path("slug") slug: String): PropertyDetailDto

    /** Requiere sesión: avisa al usuario cuando la propiedad baje de precio. */
    @POST("alerts/price")
    suspend fun createPriceAlert(@Body body: PriceAlertRequest)
}
