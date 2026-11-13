package com.tecsup.app_ariz.feature.account.ui.account

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HelpOutline
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
import com.tecsup.app_ariz.feature.account.R
import com.tecsup.app_ariz.core.designsystem.R as DesignSystemR

@Composable
fun AccountScreen(
    onLoginClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onClubClick: () -> Unit,
    onInfoClick: () -> Unit,
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
        MenuRow(icon = Icons.Outlined.CardGiftcard, label = stringResource(R.string.account_menu_club), onClick = onClubClick)
        MenuRow(icon = Icons.Outlined.HelpOutline, label = stringResource(R.string.account_menu_info), onClick = onInfoClick)
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
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArizColors.SectionCanvas)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ArizColors.Brand)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.size(84.dp).background(ArizColors.Accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = user.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                    color = ArizColors.Brand,
                )
            }
            Text(
                text = user.name,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = androidx.compose.ui.graphics.Color.White,
                textAlign = TextAlign.Center,
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
            )
            Text(
                text = stringResource(user.role.labelRes()).uppercase(),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp),
                color = ArizColors.Accent,
            )
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MenuRow(
                icon = Icons.Outlined.FavoriteBorder,
                label = stringResource(R.string.account_menu_favorites),
                onClick = onFavoritesClick,
            )
            MenuRow(icon = Icons.Outlined.CardGiftcard, label = stringResource(R.string.account_menu_club), onClick = onClubClick)
            MenuRow(icon = Icons.Outlined.HelpOutline, label = stringResource(R.string.account_menu_info), onClick = onInfoClick)
            Surface(
                onClick = onSignOut,
                enabled = !isSigningOut,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ArizColors.Edge),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
private fun MenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ArizColors.Edge),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = ArizColors.Brand)
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
        )
    }
}
