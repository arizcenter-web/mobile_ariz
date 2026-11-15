package com.tecsup.app_ariz.feature.account.ui.account

import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import com.tecsup.app_ariz.core.ui.component.ConfirmDialog
import com.tecsup.app_ariz.core.ui.statusBarScrim
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizColors
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.core.model.Role
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.session.SessionState
import com.tecsup.app_ariz.core.ui.LightStatusBarIcons
import com.tecsup.app_ariz.core.ui.LoadingView
import com.tecsup.app_ariz.core.ui.component.TabHeader
import com.tecsup.app_ariz.feature.account.R
import com.tecsup.app_ariz.core.designsystem.R as DesignSystemR

@Composable
fun AccountScreen(
    onLoginClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onClubClick: () -> Unit,
    onInfoClick: () -> Unit,
    onPanelClick: (Role) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountContent(
        state = state,
        onAction = viewModel::onAction,
        onLoginClick = onLoginClick,
        onFavoritesClick = onFavoritesClick,
        onClubClick = onClubClick,
        onInfoClick = onInfoClick,
        onPanelClick = onPanelClick,
        modifier = modifier,
    )
}

@Composable
fun AccountContent(
    state: AccountUiState,
    onAction: (AccountAction) -> Unit,
    onLoginClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onClubClick: () -> Unit,
    onInfoClick: () -> Unit,
    onPanelClick: (Role) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val session = state.session) {
        SessionState.Restoring -> LoadingView(modifier)
        SessionState.SignedOut -> SignedOutContent(onLoginClick, onClubClick, onInfoClick, modifier)
        is SessionState.SignedIn -> SignedInContent(
            user = session.user,
            isSigningOut = state.isSigningOut,
            onSignOut = { onAction(AccountAction.SignOut) },
            onFavoritesClick = onFavoritesClick,
            onClubClick = onClubClick,
            onInfoClick = onInfoClick,
            onPanelClick = { onPanelClick(session.user.role) },
            modifier = modifier,
        )
    }
}

/** Invitación a iniciar sesión, con el logo y el azul de marca. */
@Composable
private fun SignedOutContent(
    onLoginClick: () -> Unit,
    onClubClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArizColors.SectionCanvas)
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(DesignSystemR.drawable.ariz_logo),
            contentDescription = "Ariz Proyectos",
            modifier = Modifier.height(48.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = stringResource(R.string.account_signed_out_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = ArizColors.Brand,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.account_signed_out_description),
            style = MaterialTheme.typography.bodyLarge,
            color = ArizColors.Muted,
            textAlign = TextAlign.Center,
        )
        ArizPrimaryButton(
            text = stringResource(R.string.account_sign_in),
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
        )
        Surface(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Column {
                MenuRow(icon = Icons.Outlined.CardGiftcard, label = stringResource(R.string.account_menu_club), onClick = onClubClick)
                MenuDivider()
                MenuRow(icon = Icons.Outlined.HelpOutline, label = stringResource(R.string.account_menu_info), onClick = onInfoClick)
            }
        }
    }
}

/**
 * Perfil: cabecera azul con la inicial del usuario, nombre, correo y rol; debajo, los accesos
 * rápidos y "Cerrar sesión".
 */
@Composable
private fun SignedInContent(
    user: User,
    isSigningOut: Boolean,
    onSignOut: () -> Unit,
    onFavoritesClick: () -> Unit,
    onClubClick: () -> Unit,
    onInfoClick: () -> Unit,
    onPanelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    if (confirmSignOut) {
        ConfirmDialog(
            title = stringResource(R.string.account_sign_out_confirm_title),
            text = stringResource(R.string.account_sign_out_confirm_text),
            confirmLabel = stringResource(R.string.account_sign_out_confirm_action),
            dismissLabel = stringResource(R.string.account_sign_out_cancel),
            eyebrow = stringResource(R.string.account_sign_out_confirm_eyebrow),
            onConfirm = {
                confirmSignOut = false
                onSignOut()
            },
            onDismiss = { confirmSignOut = false },
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArizColors.SectionCanvas)
            .statusBarScrim()
            .verticalScroll(rememberScrollState()),
    ) {
        // Misma cabecera que Buscar y Favoritos: rol, nombre y correo, con la inicial a la derecha.
        TabHeader(
            eyebrow = stringResource(user.role.labelRes()),
            title = user.name,
            subtitle = user.email,
            trailing = {
                Box(modifier = Modifier.size(56.dp).background(ArizColors.Accent, CircleShape), contentAlignment = Alignment.Center) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = ArizColors.Brand,
                    )
                }
            },
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Accesos agrupados en una sola tarjeta, separados por una línea fina.
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Column {
                    MenuRow(icon = Icons.Outlined.Dashboard, label = stringResource(user.role.panelLabelRes()), onClick = onPanelClick)
                    MenuDivider()
                    MenuRow(icon = Icons.Outlined.FavoriteBorder, label = stringResource(R.string.account_menu_favorites), onClick = onFavoritesClick)
                    MenuDivider()
                    MenuRow(icon = Icons.Outlined.CardGiftcard, label = stringResource(R.string.account_menu_club), onClick = onClubClick)
                    MenuDivider()
                    MenuRow(icon = Icons.Outlined.HelpOutline, label = stringResource(R.string.account_menu_info), onClick = onInfoClick)
                }
            }
            Surface(
                onClick = { confirmSignOut = true },
                enabled = !isSigningOut,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = ArizColors.Error.copy(alpha = 0.08f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(20.dp), tint = ArizColors.Error)
                    Text(
                        text = stringResource(R.string.account_sign_out),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ArizColors.Error,
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 70.dp), thickness = 1.dp, color = ArizColors.SurfaceMuted)
}

@Composable
private fun MenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), color = androidx.compose.ui.graphics.Color.Transparent) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(40.dp).background(ArizColors.BrandSoft, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = ArizColors.Brand)
            }
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = ArizColors.Ink,
            )
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = ArizColors.Muted)
        }
    }
}

@StringRes
private fun Role.panelLabelRes(): Int = when (this) {
    Role.ADMIN -> R.string.account_menu_panel_admin
    Role.USER -> R.string.account_menu_panel_customer
    Role.REAL_ESTATE -> R.string.account_menu_panel_professional
}

@StringRes
private fun Role.labelRes(): Int = when (this) {
    Role.ADMIN -> R.string.account_role_admin
    Role.USER -> R.string.account_role_user
    Role.REAL_ESTATE -> R.string.account_role_real_estate
}

@Preview(showBackground = true)
@Composable
private fun AccountSignedInPreview() {
    ArizTheme {
        AccountContent(
            state = AccountUiState(
                session = SessionState.SignedIn(User("u1", "Ana Pérez", "ana@correo.com", Role.USER, null)),
            ),
            onAction = {},
            onLoginClick = {},
            onFavoritesClick = {},
            onClubClick = {},
            onInfoClick = {},
            onPanelClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountSignedOutPreview() {
    ArizTheme {
        AccountContent(
            state = AccountUiState(session = SessionState.SignedOut),
            onAction = {},
            onLoginClick = {},
            onFavoritesClick = {},
            onClubClick = {},
            onInfoClick = {},
            onPanelClick = {},
        )
    }
}
