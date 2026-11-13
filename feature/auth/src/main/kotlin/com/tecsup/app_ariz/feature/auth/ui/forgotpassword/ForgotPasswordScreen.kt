package com.tecsup.app_ariz.feature.auth.ui.forgotpassword

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.app_ariz.core.designsystem.component.ArizPrimaryButton
import com.tecsup.app_ariz.core.designsystem.component.ArizTextField
import com.tecsup.app_ariz.core.designsystem.theme.ArizTheme
import com.tecsup.app_ariz.feature.auth.R
import com.tecsup.app_ariz.feature.auth.ui.component.AuthFooterLink
import com.tecsup.app_ariz.feature.auth.ui.component.AuthFormLayout
import com.tecsup.app_ariz.feature.auth.ui.component.FormErrorText
import com.tecsup.app_ariz.feature.auth.ui.component.asMessage

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ForgotPasswordContent(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
fun ForgotPasswordContent(
    state: ForgotPasswordUiState,
    onAction: (ForgotPasswordAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthFormLayout(
        eyebrow = stringResource(R.string.auth_forgot_eyebrow),
        title = stringResource(R.string.auth_forgot_title),
        onBackClick = onBackClick,
        subtitle = stringResource(R.string.auth_forgot_subtitle),
        modifier = modifier,
    ) {
        ArizTextField(
            value = state.email,
            onValueChange = { onAction(ForgotPasswordAction.EmailChanged(it)) },
            label = stringResource(R.string.auth_field_email),
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.auth_placeholder_email),
            errorText = state.emailError.asMessage(),
            enabled = !state.isLoading,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            onImeAction = { onAction(ForgotPasswordAction.Submit) },
        )
        if (state.isSent) {
            Text(
                text = stringResource(R.string.auth_forgot_sent),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        FormErrorText(state.error)
        ArizPrimaryButton(
            text = stringResource(R.string.auth_forgot_submit),
            onClick = { onAction(ForgotPasswordAction.Submit) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isLoading,
        )
        AuthFooterLink(
            link = stringResource(R.string.auth_forgot_back_link),
            onClick = onBackClick,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordContentPreview() {
    ArizTheme {
        ForgotPasswordContent(state = ForgotPasswordUiState(isSent = true), onAction = {}, onBackClick = {})
    }
}
