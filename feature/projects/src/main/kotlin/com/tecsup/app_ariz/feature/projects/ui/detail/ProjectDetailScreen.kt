package com.tecsup.app_ariz.feature.projects.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.leads.domain.LeadTarget
import com.tecsup.app_ariz.core.leads.ui.LeadFormSheet
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons
import com.tecsup.app_ariz.core.ui.component.ContactBar
import com.tecsup.app_ariz.core.ui.component.PhotoGallery
import com.tecsup.app_ariz.feature.projects.R
import com.tecsup.app_ariz.feature.projects.domain.model.ProjectDetail
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectAdvertiserCard
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectBadges
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectDescription
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectFeatures
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectHeader
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectLocationSection
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectLots
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectMedia
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectMilestones
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectPrice
import com.tecsup.app_ariz.feature.projects.ui.component.ProjectSpecStrip

@Composable
fun ProjectDetailScreen(
    onBackClick: () -> Unit,
    onPlanClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LightStatusBarIcons()
    ProjectDetailContent(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onPlanClick = onPlanClick,
        modifier = modifier,
    )
}

/** Contenido sin estado: recibe el [ProjectDetailUiState] y callbacks, así se previsualiza y se prueba. */
@Composable
fun ProjectDetailContent(
    state: ProjectDetailUiState,
    onAction: (ProjectDetailAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPlanClick: () -> Unit = {},
) {
    val detail = state.detail
    val whatsAppMessage = stringResource(R.string.projects_whatsapp_message, detail?.name.orEmpty())
    var showLeadForm by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = ArizColors.SectionCanvas,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (detail != null) ContactBar(
                whatsapp = detail.advertiser.whatsapp,
                phone = detail.advertiser.phone,
                whatsappMessage = whatsAppMessage,
                whatsappLabel = stringResource(R.string.projects_contact_whatsapp),
                callLabel = stringResource(R.string.projects_contact_call),
                messageLabel = stringResource(R.string.projects_contact_message),
                onMessageClick = { showLeadForm = true },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArizColors.Brand)
                }

                detail == null -> Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                ) {
                    Text(
                        text = state.error?.message.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArizColors.Muted,
                        textAlign = TextAlign.Center,
                    )
                    ArizPrimaryButton(text = stringResource(R.string.projects_retry), onClick = { onAction(ProjectDetailAction.Retry) })
                }

                else -> DetailBody(detail, onPlanClick)
            }
            BackButton(onClick = onBackClick)
        }
    }

    if (showLeadForm && detail != null) {
        LeadFormSheet(
            target = LeadTarget.Project(detail.id),
            defaultMessage = stringResource(R.string.projects_lead_default_message),
            onDismiss = { showLeadForm = false },
        )
    }
}

@Composable
private fun DetailBody(detail: ProjectDetail, onPlanClick: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PhotoGallery(images = detail.gallery, emptyLabel = stringResource(R.string.projects_detail_no_photos)) {
            ProjectBadges(
                project = detail,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ProjectHeader(detail)
            ProjectPrice(detail)
            detail.tourUrl?.let { url ->
                Surface(
                    onClick = { uriHandler.openUri(url) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, ArizColors.Brand),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.projects_tour),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = ArizColors.Brand,
                        )
                    }
                }
            }
            ProjectSpecStrip(detail)
            ProjectDescription(detail.description)
            ProjectFeatures(detail)
            if (detail.hasPublishedSitePlan) {
                Surface(
                    onClick = onPlanClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, ArizColors.Brand),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.projects_plan_button),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = ArizColors.Brand,
                        )
                    }
                }
            }
            ProjectLots(detail)
            ProjectMilestones(detail)
            ProjectMedia(detail, onOpenUrl = { uriHandler.openUri(it) })
            ProjectLocationSection(
                project = detail,
                onOpenMapsClick = {
                    uriHandler.openUri(
                        "https://www.google.com/maps/search/?api=1&query=${detail.location.latitude},${detail.location.longitude}",
                    )
                },
            )
            ProjectAdvertiserCard(detail)
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .statusBarsPadding()
            .padding(start = 8.dp, top = 8.dp)
            .background(Color.Black.copy(alpha = 0.3f), CircleShape),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = stringResource(R.string.projects_back),
            tint = Color.White,
        )
    }
}
