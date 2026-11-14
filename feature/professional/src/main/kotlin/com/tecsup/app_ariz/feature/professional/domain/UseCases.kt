package com.tecsup.app_ariz.feature.professional.domain

import com.tecsup.app_ariz.core.common.AppResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/** Todo lo que necesitan el dashboard y las estadísticas. */
data class PanelData(
    val profile: ProfessionalProfile,
    val properties: List<OwnerProperty>,
    val projects: List<OwnerProject>,
    val leads: List<OwnerLead>,
    val subscription: Subscription?,
    val stats: ProfessionalStats,
)

/**
 * Carga el panel en paralelo. El perfil es imprescindible; propiedades, proyectos, consultas y plan son
 * secciones del dashboard: si alguna falla se muestra vacía (como hace el web con sus `catch`).
 */
class LoadPanelUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()): AppResult<PanelData> = coroutineScope {
        val profile = async { repository.getProfile() }
        val properties = async { repository.getProperties() }
        val projects = async { repository.getProjects() }
        val leads = async { repository.getLeads() }
        val subscription = async { repository.getSubscription() }

        when (val profileResult = profile.await()) {
            is AppResult.Failure -> profileResult
            is AppResult.Success -> {
                val props = (properties.await() as? AppResult.Success)?.data.orEmpty()
                val projs = (projects.await() as? AppResult.Success)?.data.orEmpty()
                val lds = (leads.await() as? AppResult.Success)?.data.orEmpty()
                val sub = (subscription.await() as? AppResult.Success)?.data
                AppResult.Success(
                    PanelData(
                        profile = profileResult.data,
                        properties = props,
                        projects = projs,
                        leads = lds,
                        subscription = sub,
                        stats = StatsCalculator.calculate(props, projs, lds, sub, nowMillis),
                    ),
                )
            }
        }
    }
}

class GetMyPropertiesUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke() = repository.getProperties()
}

class PublishPropertyUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(id: String) = repository.publishProperty(id)
}

class DeletePropertyUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(id: String) = repository.deleteProperty(id)
}

class GetPropertyDraftUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(id: String) = repository.getPropertyDraft(id)
}

class SavePropertyUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(id: String?, draft: PropertyDraft, asAdmin: Boolean = false) = repository.saveProperty(id, draft, asAdmin)
}

class UploadPhotoUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(uri: String, folder: String = "ariz/properties") = repository.uploadPhoto(uri, folder)
}

class GetMyProjectsUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke() = repository.getProjects()
}

class PublishProjectUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(id: String, asAdmin: Boolean = false) = repository.publishProject(id, asAdmin)
}

class DeleteProjectUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(id: String) = repository.deleteProject(id)
}

class GetMyLeadsUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke() = repository.getLeads()
}

/** Planes disponibles y la suscripción actual, para la pantalla de suscripción. */
data class PlansData(val plans: List<Plan>, val subscription: Subscription?)

class LoadPlansUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(): AppResult<PlansData> = coroutineScope {
        val plans = async { repository.getPlans() }
        val subscription = async { repository.getSubscription() }
        when (val result = plans.await()) {
            is AppResult.Failure -> result
            is AppResult.Success -> AppResult.Success(
                PlansData(result.data, (subscription.await() as? AppResult.Success)?.data),
            )
        }
    }
}

class RequestPlanUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(plan: Plan, receiptUrl: String?) = repository.requestPlan(plan.id, receiptUrl)
}

class GetProfileUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke() = repository.getProfile()
}

class UpdateProfileUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(update: ProfileUpdate) = repository.updateProfile(update)
}

class ChangePasswordUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(current: String, new: String) = repository.changePassword(current, new)
}

class GetSettingsUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke() = repository.getSettings()
}

class UpdateSettingsUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(settings: NotificationSettings) = repository.updateSettings(settings)
}

class GetProjectDraftUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(id: String, asAdmin: Boolean = false) = repository.getProjectDraft(id, asAdmin)
}

class UpdateProjectUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(id: String, draft: ProjectDraft) = repository.updateProject(id, draft)
}

class CreateProjectUseCase @Inject constructor(private val repository: ProfessionalRepository) {
    suspend operator fun invoke(draft: ProjectDraft, asAdmin: Boolean = false) = repository.createProject(draft, asAdmin)
}
