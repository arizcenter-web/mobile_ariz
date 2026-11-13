package com.tecsup.app_ariz.core.leads.data

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Cuerpo de `POST /leads`. Solo uno de `propertyId` / `projectId` va informado; el otro es `null` y
 * no se serializa (el `Json` de la app usa `explicitNulls = false`).
 */
@Serializable
internal data class CreateLeadRequest(
    val propertyId: String? = null,
    val projectId: String? = null,
    val name: String,
    val email: String,
    val phone: String? = null,
    val message: String,
    val source: String,
)

internal interface LeadsApi {

    @POST("leads")
    suspend fun create(@Body body: CreateLeadRequest)
}
