package com.tecsup.app_ariz.feature.info.data

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.feature.info.domain.ContactReport
import com.tecsup.app_ariz.feature.info.domain.ContactRepository
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject

/** Cuerpo de `POST /contact-reports`. El teléfono vacío no se envía (el backend lo valida si viene). */
@Serializable
internal data class ContactReportRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val topic: String,
    val message: String,
)

internal interface ContactApi {

    @POST("contact-reports")
    suspend fun create(@Body body: ContactReportRequest)
}

internal class ContactRepositoryImpl @Inject constructor(
    private val api: ContactApi,
) : ContactRepository {

    override suspend fun send(report: ContactReport): AppResult<Unit> = safeApiCall { api.create(report.toBody()) }
}

internal fun ContactReport.toBody() = ContactReportRequest(
    name = name.trim(),
    email = email.trim(),
    phone = phone.trim().ifEmpty { null },
    // El backend limita el asunto a 80 caracteres.
    topic = topic.trim().take(80),
    message = message.trim(),
)
