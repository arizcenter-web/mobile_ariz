package com.tecsup.app_ariz.feature.professional.data

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.media.MediaRepository
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.feature.professional.domain.NotificationSettings
import com.tecsup.app_ariz.feature.professional.domain.OwnerLead
import com.tecsup.app_ariz.feature.professional.domain.OwnerProject
import com.tecsup.app_ariz.feature.professional.domain.OwnerProperty
import com.tecsup.app_ariz.feature.professional.domain.Plan
import com.tecsup.app_ariz.feature.professional.domain.ProfessionalProfile
import com.tecsup.app_ariz.feature.professional.domain.ProfessionalRepository
import com.tecsup.app_ariz.feature.professional.domain.ProfileUpdate
import com.tecsup.app_ariz.feature.professional.domain.ProjectDraft
import com.tecsup.app_ariz.feature.professional.domain.PropertyDraft
import com.tecsup.app_ariz.feature.professional.domain.Subscription
import com.tecsup.app_ariz.feature.professional.domain.UploadedPhoto
import com.tecsup.app_ariz.core.common.AppError
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

internal class ProfessionalRepositoryImpl @Inject constructor(
    private val api: ProfessionalApi,
    private val json: Json,
    private val media: MediaRepository,
) : ProfessionalRepository {

    override suspend fun getProfile(): AppResult<ProfessionalProfile> = safeApiCall { api.profile() }.map { it.toDomain() }

    override suspend fun updateProfile(update: ProfileUpdate): AppResult<ProfessionalProfile> =
        safeApiCall { api.updateProfile(update.toBody()) }.map { it.toDomain() }

    override suspend fun changePassword(current: String, new: String): AppResult<Unit> =
        safeApiCall { api.changePassword(PasswordChangeRequest(current, new)) }

    override suspend fun getSettings(): AppResult<NotificationSettings> = safeApiCall { api.settings() }.map { it.toDomain() }

    override suspend fun updateSettings(settings: NotificationSettings): AppResult<NotificationSettings> =
        safeApiCall { api.updateSettings(settings.toDto()) }.map { it.toDomain() }

    override suspend fun getProperties(): AppResult<List<OwnerProperty>> =
        safeApiCall { api.myProperties() }.map { list -> list.map { it.toDomain() } }

    override suspend fun getPropertyDraft(id: String): AppResult<PropertyDraft> =
        safeApiCall { api.propertyForEdit(id) }.map { it.toDraft() }

    override suspend fun saveProperty(id: String?, draft: PropertyDraft, asAdmin: Boolean): AppResult<OwnerProperty> =
        safeApiCall {
            if (id == null && asAdmin) api.createAdminProperty(draft.toBody())
            else if (id == null) api.createProperty(draft.toBody()) else api.updateProperty(id, draft.toBody())
        }.map { it.toDomain() }

    override suspend fun publishProperty(id: String): AppResult<OwnerProperty> =
        safeApiCall { api.publishProperty(id) }.map { it.toDomain() }

    override suspend fun deleteProperty(id: String): AppResult<Unit> = safeApiCall { api.deleteProperty(id) }

    override suspend fun getProjects(): AppResult<List<OwnerProject>> =
        safeApiCall { api.myProjects() }.map { list -> list.map { it.toDomain() } }

    override suspend fun createProject(draft: ProjectDraft, asAdmin: Boolean): AppResult<String> =
        safeApiCall { if (asAdmin) api.createAdminProject(draft.toBody()) else api.createProject(draft.toBody()) }.map { it["id"]?.jsonPrimitive?.content.orEmpty() }

    override suspend fun getProjectDraft(id: String, asAdmin: Boolean): AppResult<ProjectDraft> =
        safeApiCall {
            if (asAdmin) api.adminProject(id)
            else api.myProjectsRaw().filterIsInstance<JsonObject>().firstOrNull { it["id"]?.jsonPrimitive?.content == id }
        }.let { result ->
            when (result) {
                is AppResult.Failure -> result
                is AppResult.Success -> {
                    val project = result.data
                    val blocked = project?.notEditableReason()
                    when {
                        project == null -> AppResult.Failure(AppError.NotFound())
                        blocked != null -> AppResult.Failure(AppError.Validation(blocked))
                        else -> AppResult.Success(project.toProjectDraft())
                    }
                }
            }
        }

    override suspend fun updateProject(id: String, draft: ProjectDraft): AppResult<Unit> =
        safeApiCall { api.updateProject(id, draft.toUpdateBody(json)) }.map { }

    override suspend fun publishProject(id: String, asAdmin: Boolean): AppResult<Unit> =
        safeApiCall { if (asAdmin) api.publishAdminProject(id) else api.publishProject(id) }.map { }

    override suspend fun deleteProject(id: String): AppResult<Unit> = safeApiCall { api.deleteProject(id) }

    override suspend fun getLeads(): AppResult<List<OwnerLead>> =
        safeApiCall { api.myLeads() }.map { list -> list.map { it.toDomain() } }

    override suspend fun getPlans(): AppResult<List<Plan>> = safeApiCall { api.plans() }.map { list -> list.map { it.toDomain() } }

    override suspend fun getSubscription(): AppResult<Subscription?> =
        safeApiCall { api.mySubscription().string() }.map { body ->
            if (body.isBlank() || body == "null") null else json.decodeFromString<SubscriptionDto>(body).toDomain()
        }

    override suspend fun requestPlan(planId: String, receiptUrl: String?): AppResult<Subscription> =
        safeApiCall { api.requestPlan(planId, PlanRequestBody(receiptUrl)) }.map { it.toDomain() }

    override suspend fun uploadPhoto(uri: String, folder: String): AppResult<UploadedPhoto> =
        media.uploadImage(uri, folder).map { UploadedPhoto(url = it.url, publicId = it.publicId) }
}
