package com.tecsup.app_ariz.feature.account.ui.account

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.core.model.Role
import com.tecsup.app_ariz.core.model.User
import com.tecsup.app_ariz.core.session.SessionState
import com.tecsup.app_ariz.core.ui.LoadingView
import com.tecsup.app_ariz.feature.account.R

@Composable
fun AccountScreen(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountContent(
        state = state,
        onAction = viewModel::onAction,
        onLoginClick = onLoginClick,
        modifier = modifier,
    )
}

@Composable
fun AccountContent(
    state: AccountUiState,
    onAction: (AccountAction) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val session = state.session) {
        SessionState.Restoring -> LoadingView(modifier)
        SessionState.SignedOut -> SignedOutContent(onLoginClick, modifier)
        is SessionState.SignedIn -> SignedInContent(
            user = session.user,
            isSigningOut = state.isSigningOut,
            onSignOut = { onAction(AccountAction.SignOut) },
            modifier = modifier,
        )
    }
}

@Composable
private fun SignedOutContent(onLoginClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.account_signed_out_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.account_signed_out_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ArizPrimaryButton(
            text = stringResource(R.string.account_sign_in),
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SignedInContent(
    user: User,
    isSigningOut: Boolean,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(user.name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(user.email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(user.role.labelRes()), style = MaterialTheme.typography.labelLarge)
        OutlinedButton(
            onClick = onSignOut,
            enabled = !isSigningOut,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) {
            Text(stringResource(R.string.account_sign_out))
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
        )
    }
}
