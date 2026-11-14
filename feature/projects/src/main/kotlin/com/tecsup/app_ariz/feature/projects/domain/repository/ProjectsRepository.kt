package com.tecsup.app_ariz.feature.projects.domain.repository

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectDetail
import com.tecsup.app_ariz.feature.projects.domain.model.PublicPlan

interface ProjectsRepository {
    suspend fun getProjectDetail(slug: String): AppResult<ProjectDetail>

    /** Plano publicado con el código del asesor; `null` si el proyecto no tiene plano publicado. */
    suspend fun getSitePlan(slug: String, code: String): AppResult<PublicPlan?>

    /** Descarga la ficha PDF de un lote o departamento y devuelve la ruta del archivo (en la caché). */
    suspend fun downloadSheet(slug: String, id: String, apartment: Boolean, code: String, fileName: String): AppResult<String>
}
