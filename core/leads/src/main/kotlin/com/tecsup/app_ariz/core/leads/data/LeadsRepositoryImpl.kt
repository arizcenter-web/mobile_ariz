package com.tecsup.app_ariz.core.leads.data

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.leads.domain.LeadRequest
import com.tecsup.app_ariz.core.leads.domain.LeadTarget
import com.tecsup.app_ariz.core.leads.domain.LeadValidator
import com.tecsup.app_ariz.core.leads.domain.LeadsRepository
import com.tecsup.app_ariz.core.network.safeApiCall
import javax.inject.Inject

internal class LeadsRepositoryImpl @Inject constructor(
    private val api: LeadsApi,
) : LeadsRepository {

    override suspend fun send(request: LeadRequest): AppResult<Unit> =
        safeApiCall { api.create(request.toBody()) }
}

internal fun LeadRequest.toBody() = CreateLeadRequest(
    propertyId = (target as? LeadTarget.Property)?.id,
    projectId = (target as? LeadTarget.Project)?.id,
    name = name.trim(),
    email = email.trim(),
    phone = phone.trim().ifEmpty { null },
    message = LeadValidator.messageWithDni(message, dni),
    source = if (target is LeadTarget.Project) "PROJECT_FORM" else "PROPERTY_FORM",
)
