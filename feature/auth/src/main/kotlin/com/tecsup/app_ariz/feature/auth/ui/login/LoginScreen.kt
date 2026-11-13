package com.tecsup.app_ariz.feature.auth.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizLabeledDivider
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizSecondaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.feature.auth.R
import com.tecsup.app_ariz.feature.auth.ui.component.AuthFooterLink
import com.tecsup.app_ariz.feature.auth.ui.component.AuthFormLayout
import com.tecsup.app_ariz.feature.auth.ui.component.FormErrorText
import com.tecsup.app_ariz.feature.auth.ui.component.asMessage

/** Versión con ViewModel: conecta estado y eventos. La UI real está en [LoginContent]. */
@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.Authenticated -> onAuthenticated()
            }
        }
    }

    LoginContent(
        state = state,
        onAction = viewModel::onAction,
        onRegisterClick = onRegisterClick,
        onForgotPasswordClick = onForgotPasswordClick,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    AuthFormLayout(
        eyebrow = stringResource(R.string.auth_login_eyebrow),
        title = stringResource(R.string.auth_login_title),
        onBackClick = onBackClick,
        subtitle = stringResource(R.string.auth_login_subtitle),
        modifier = modifier,
    ) {
        ArizTextField(
            value = state.email,
            onValueChange = { onAction(LoginAction.EmailChanged(it)) },
            label = stringResource(R.string.auth_field_email),
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.auth_placeholder_email),
            errorText = state.emailError.asMessage(),
            enabled = !state.isLoading,
            keyboardType = KeyboardType.Email,
        )
        ArizTextField(
            value = state.password,
            onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
            label = stringResource(R.string.auth_field_password),
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.auth_placeholder_password),
            errorText = state.passwordError.asMessage(),
            enabled = !state.isLoading,
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = { onAction(LoginAction.Submit) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.rememberMe,
                    onCheckedChange = { onAction(LoginAction.RememberMeChanged(it)) },
                    enabled = !state.isLoading,
                )
                Text(
                    text = stringResource(R.string.auth_login_remember_me),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                )
            }
            Text(
                text = stringResource(R.string.auth_login_forgot_password),
                modifier = Modifier.clickable(onClick = onForgotPasswordClick).padding(vertical = 8.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        FormErrorText(state.error)
        ArizPrimaryButton(
            text = stringResource(R.string.auth_login_submit),
            onClick = { onAction(LoginAction.Submit) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isLoading,
        )
        if (state.isGoogleAvailable) {
            ArizLabeledDivider(label = stringResource(R.string.auth_login_or))
            ArizSecondaryButton(
                onClick = { onAction(LoginAction.GoogleSignIn(context)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.auth_login_google),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                )
            }
        }
        AuthFooterLink(
            prefix = stringResource(R.string.auth_login_no_account),
            link = stringResource(R.string.auth_login_register_link),
            onClick = onRegisterClick,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentPreview() {
    ArizTheme {
        LoginContent(
            state = LoginUiState(email = "ana@correo.com", isGoogleAvailable = true),
            onAction = {},
            onRegisterClick = {},
            onForgotPasswordClick = {},
            onBackClick = {},
        )
    }
}
