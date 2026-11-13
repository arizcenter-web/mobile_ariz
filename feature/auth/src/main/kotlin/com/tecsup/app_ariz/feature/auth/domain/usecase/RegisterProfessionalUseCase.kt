package com.tecsup.app_ariz.feature.auth.domain.usecase

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.onSuccess
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.feature.auth.domain.model.RegisterProfessionalData
import com.tecsup.app_ariz.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterProfessionalUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val session: SessionManager,
) {
    suspend operator fun invoke(data: RegisterProfessionalData): AppResult<User> =
        repository.registerProfessional(data.normalized()).onSuccess(session::onSignedIn)

    /** Recorta espacios y convierte los textos vacíos en `null` (el backend valida `@IsOptional`). */
    private fun RegisterProfessionalData.normalized() = copy(
        firstName = firstName.cleaned(),
        lastName = lastName.cleaned(),
        dni = dni.cleaned(),
        businessName = businessName.cleaned(),
        ruc = ruc.cleaned(),
        email = email.trim(),
        phone = phone.trim(),
        whatsapp = whatsapp.trim(),
        city = city.trim(),
        districts = districts.cleaned(),
        address = address.cleaned(),
        website = website.cleaned(),
        socialLinks = socialLinks.cleaned(),
    )

    private fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}
