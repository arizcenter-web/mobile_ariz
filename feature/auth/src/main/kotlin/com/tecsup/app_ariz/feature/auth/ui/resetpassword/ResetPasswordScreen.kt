package com.tecsup.app_ariz.feature.auth.ui.resetpassword

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.feature.auth.R
import com.tecsup.app_ariz.feature.auth.ui.component.AuthFormLayout
import com.tecsup.app_ariz.feature.auth.ui.component.FormErrorText
import com.tecsup.app_ariz.feature.auth.ui.component.asMessage

@Composable
fun ResetPasswordScreen(
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ResetPasswordContent(
        state = state,
        onAction = viewModel::onAction,
        onDoneClick = onDoneClick,
        modifier = modifier,
    )
}

@Composable
fun ResetPasswordContent(
    state: ResetPasswordUiState,
    onAction: (ResetPasswordAction) -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthFormLayout(
        eyebrow = stringResource(R.string.auth_reset_eyebrow),
        title = stringResource(R.string.auth_reset_title),
        onBackClick = onDoneClick,
        subtitle = stringResource(R.string.auth_reset_subtitle),
        modifier = modifier,
    ) {
        if (state.isDone) {
            Text(
                text = stringResource(R.string.auth_reset_done),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            ArizPrimaryButton(
                text = stringResource(R.string.auth_reset_go_login),
                onClick = onDoneClick,
                modifier = Modifier.fillMaxWidth(),
            )
            return@AuthFormLayout
        }

        ArizTextField(
            value = state.password,
            onValueChange = { onAction(ResetPasswordAction.PasswordChanged(it)) },
            label = stringResource(R.string.auth_reset_new_password),
            modifier = Modifier.fillMaxWidth(),
            errorText = state.passwordError.asMessage(),
            enabled = !state.isLoading,
            isPassword = true,
        )
        ArizTextField(
            value = state.confirmation,
            onValueChange = { onAction(ResetPasswordAction.ConfirmationChanged(it)) },
            label = stringResource(R.string.auth_reset_confirm_password),
            modifier = Modifier.fillMaxWidth(),
            errorText = state.confirmationError.asMessage(),
            enabled = !state.isLoading,
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = { onAction(ResetPasswordAction.Submit) },
        )
        FormErrorText(state.error)
        ArizPrimaryButton(
            text = stringResource(R.string.auth_reset_submit),
            onClick = { onAction(ResetPasswordAction.Submit) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isLoading,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResetPasswordContentPreview() {
    ArizTheme {
        ResetPasswordContent(state = ResetPasswordUiState(), onAction = {}, onDoneClick = {})
    }
}
