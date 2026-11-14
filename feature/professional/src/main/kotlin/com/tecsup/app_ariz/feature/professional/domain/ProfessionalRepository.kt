package com.tecsup.app_ariz.feature.professional.domain

import com.tecsup.app_ariz.core.common.AppResult

/** Panel del profesional: perfil, publicaciones, consultas, plan y fotos. */
interface ProfessionalRepository {
    suspend fun getProfile(): AppResult<ProfessionalProfile>
    suspend fun updateProfile(update: ProfileUpdate): AppResult<ProfessionalProfile>
    suspend fun changePassword(current: String, new: String): AppResult<Unit>
    suspend fun getSettings(): AppResult<NotificationSettings>
    suspend fun updateSettings(settings: NotificationSettings): AppResult<NotificationSettings>

    suspend fun getProperties(): AppResult<List<OwnerProperty>>
    suspend fun getPropertyDraft(id: String): AppResult<PropertyDraft>
    suspend fun saveProperty(id: String?, draft: PropertyDraft, asAdmin: Boolean = false): AppResult<OwnerProperty>
    suspend fun publishProperty(id: String): AppResult<OwnerProperty>
    suspend fun deleteProperty(id: String): AppResult<Unit>

    suspend fun getProjects(): AppResult<List<OwnerProject>>
    /** Crea el proyecto (borrador) y devuelve su id. */
    suspend fun createProject(draft: ProjectDraft, asAdmin: Boolean = false): AppResult<String>

    /** Carga un proyecto de lotización para editarlo (falla si solo se puede editar en el web). */
    suspend fun getProjectDraft(id: String, asAdmin: Boolean = false): AppResult<ProjectDraft>
    suspend fun updateProject(id: String, draft: ProjectDraft): AppResult<Unit>

    suspend fun publishProject(id: String, asAdmin: Boolean = false): AppResult<Unit>
    suspend fun deleteProject(id: String): AppResult<Unit>

    suspend fun getLeads(): AppResult<List<OwnerLead>>

    suspend fun getPlans(): AppResult<List<Plan>>
    suspend fun getSubscription(): AppResult<Subscription?>
    suspend fun requestPlan(planId: String, receiptUrl: String?): AppResult<Subscription>

    /** Sube una foto (uri de contenido) a Cloudinary con la firma del backend y devuelve su URL. */
    suspend fun uploadPhoto(uri: String, folder: String): AppResult<UploadedPhoto>
}
