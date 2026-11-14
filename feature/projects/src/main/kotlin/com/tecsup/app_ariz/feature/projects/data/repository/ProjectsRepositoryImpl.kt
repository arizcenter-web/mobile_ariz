package com.tecsup.app_ariz.feature.projects.data.repository

import android.content.Context
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.feature.projects.data.mapper.toDomain
import com.tecsup.app_ariz.feature.projects.data.mapper.toPublicPlan
import com.tecsup.app_ariz.feature.projects.data.remote.api.ProjectsApi
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectDetail
import com.tecsup.app_ariz.feature.projects.domain.model.PublicPlan
import com.tecsup.app_ariz.feature.projects.domain.repository.ProjectsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File
import javax.inject.Inject

internal class ProjectsRepositoryImpl @Inject constructor(
    private val api: ProjectsApi,
    private val json: Json,
    @ApplicationContext private val context: Context,
) : ProjectsRepository {

    override suspend fun getProjectDetail(slug: String): AppResult<ProjectDetail> =
        safeApiCall { api.projectBySlug(slug) }.map { it.toDomain() }

    override suspend fun getSitePlan(slug: String, code: String): AppResult<PublicPlan?> =
        safeApiCall { api.sitePlan(slug, code.trim().uppercase()).string() }.map { body ->
            val plan = body.takeIf { it.isNotBlank() && it != "null" }?.let { json.parseToJsonElement(it) as? JsonObject }
            plan?.toPublicPlan()
        }

    override suspend fun downloadSheet(slug: String, id: String, apartment: Boolean, code: String, fileName: String): AppResult<String> =
        safeApiCall {
            withContext(Dispatchers.IO) {
                val normalized = code.trim().uppercase()
                val body = if (apartment) api.apartmentSheet(slug, id, normalized) else api.lotSheet(slug, id, normalized)
                val dir = File(context.cacheDir, "project-files").apply { mkdirs() }
                val file = File(dir, fileName)
                body.byteStream().use { input -> file.outputStream().use { input.copyTo(it) } }
                file.absolutePath
            }
        }
}
