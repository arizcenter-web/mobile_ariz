package com.tecsup.app_ariz.feature.professional.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.feature.professional.R
import com.tecsup.app_ariz.feature.professional.domain.ProfessionalType
import com.tecsup.app_ariz.feature.professional.domain.ProjectStatus
import com.tecsup.app_ariz.feature.professional.domain.PropertyStatus

/** Envuelve una pantalla del panel y muestra en un aviso inferior el resultado de la última acción. */
@Composable
internal fun WithNotice(notice: PanelNotice?, onShown: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val host = remember { SnackbarHostState() }
    LaunchedEffect(notice) {
        notice?.let {
            host.showSnackbar(
                when (it) {
                    is PanelNotice.Success -> it.message
                    is PanelNotice.Failure -> it.message
                },
            )
            onShown()
        }
    }
    Box(modifier.fillMaxSize()) {
        content()
        SnackbarHost(host, modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp))
    }
}

internal fun PropertyStatus.labelRes() = when (this) {
    PropertyStatus.DRAFT -> R.string.pro_status_draft
    PropertyStatus.PENDING_REVIEW -> R.string.pro_status_pending
    PropertyStatus.PUBLISHED -> R.string.pro_status_published
    PropertyStatus.PAUSED -> R.string.pro_status_paused
    PropertyStatus.SOLD -> R.string.pro_status_sold
    PropertyStatus.RENTED -> R.string.pro_status_rented
    PropertyStatus.REJECTED -> R.string.pro_status_rejected
    PropertyStatus.ARCHIVED -> R.string.pro_status_archived
}

internal fun PropertyStatus.color(): Color = when (this) {
    PropertyStatus.PUBLISHED -> ArizColors.Success
    PropertyStatus.PENDING_REVIEW -> ArizColors.Warning
    PropertyStatus.PAUSED -> ArizColors.Warning
    PropertyStatus.REJECTED -> ArizColors.Error
    else -> ArizColors.Muted
}

internal fun ProjectStatus.labelRes() = when (this) {
    ProjectStatus.DRAFT -> R.string.pro_status_draft
    ProjectStatus.PENDING_REVIEW -> R.string.pro_status_pending
    ProjectStatus.PUBLISHED -> R.string.pro_status_published
    ProjectStatus.PAUSED -> R.string.pro_status_paused
    ProjectStatus.SOLD -> R.string.pro_status_sold
    ProjectStatus.REJECTED -> R.string.pro_status_rejected
    ProjectStatus.ARCHIVED -> R.string.pro_status_archived
}

internal fun ProjectStatus.color(): Color = when (this) {
    ProjectStatus.PUBLISHED -> ArizColors.Success
    ProjectStatus.PENDING_REVIEW, ProjectStatus.PAUSED -> ArizColors.Warning
    ProjectStatus.REJECTED -> ArizColors.Error
    else -> ArizColors.Muted
}

internal fun ProfessionalType.labelRes() = when (this) {
    ProfessionalType.INDEPENDENT_AGENT -> R.string.pro_profile_type_independent
    ProfessionalType.REAL_ESTATE_AGENCY -> R.string.pro_profile_type_agency
    ProfessionalType.DEVELOPER -> R.string.pro_profile_type_developer
}

internal fun sourceLabelRes(source: String) = when (source) {
    "PROPERTY_FORM" -> R.string.pro_source_property_form
    "PROJECT_FORM" -> R.string.pro_source_project_form
    "WHATSAPP" -> R.string.pro_source_whatsapp
    "QUICK_QUESTION" -> R.string.pro_source_quick_question
    "PHONE" -> R.string.pro_source_phone
    else -> R.string.pro_source_other
}
