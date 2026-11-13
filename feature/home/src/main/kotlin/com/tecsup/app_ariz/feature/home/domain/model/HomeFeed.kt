package com.tecsup.app_ariz.feature.home.domain.model

import com.tecsup.app_ariz.core.model.project.ProjectSummary
import com.tecsup.app_ariz.core.model.property.PropertySummary

/** Las tres filas del inicio: proyectos de Ariz, propiedades recientes y proyectos destacados de agentes. */
data class HomeFeed(
    val arizProjects: List<ProjectSummary>,
    val featuredProperties: List<PropertySummary>,
    val featuredProjects: List<ProjectSummary>,
)
