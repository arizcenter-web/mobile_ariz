package com.tecsup.app_ariz.feature.auth.ui.register

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
fun RegisterScreen(
    onRegistered: () -> Unit,
    onLoginClick: () -> Unit,
    onProfessionalClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RegisterEvent.Registered -> onRegistered()
            }
        }
    }

    RegisterContent(
        state = state,
        onAction = viewModel::onAction,
        onLoginClick = onLoginClick,
        onProfessionalClick = onProfessionalClick,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
fun RegisterContent(
    state: RegisterUiState,
    onAction: (RegisterAction) -> Unit,
    onLoginClick: () -> Unit,
    onProfessionalClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @Composable
    fun Field(
        field: RegisterField,
        labelRes: Int,
        keyboardType: KeyboardType = KeyboardType.Text,
        isPassword: Boolean = false,
        imeAction: ImeAction = ImeAction.Next,
    ) {
        ArizTextField(
            value = state.valueOf(field),
            onValueChange = { onAction(RegisterAction.FieldChanged(field, it)) },
            label = stringResource(labelRes),
            modifier = Modifier.fillMaxWidth(),
            errorText = state.fieldErrors[field].asMessage(),
            enabled = !state.isLoading,
            isPassword = isPassword,
            keyboardType = keyboardType,
            imeAction = imeAction,
            onImeAction = { if (imeAction == ImeAction.Done) onAction(RegisterAction.Submit) },
        )
    }

    AuthFormLayout(
        eyebrow = stringResource(R.string.auth_register_eyebrow),
        title = stringResource(R.string.auth_register_title),
        onBackClick = onBackClick,
        subtitle = stringResource(R.string.auth_register_subtitle),
        modifier = modifier,
    ) {
        Field(RegisterField.FIRST_NAME, R.string.auth_field_first_name)
        Field(RegisterField.LAST_NAME, R.string.auth_field_last_name)
        Field(RegisterField.EMAIL, R.string.auth_field_email, keyboardType = KeyboardType.Email)
        Field(RegisterField.PHONE, R.string.auth_field_phone, keyboardType = KeyboardType.Phone)
        Field(RegisterField.PASSWORD, R.string.auth_field_password, isPassword = true, imeAction = ImeAction.Done)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.acceptTerms,
                onCheckedChange = { onAction(RegisterAction.TermsChanged(it)) },
                enabled = !state.isLoading,
            )
            Text(stringResource(R.string.auth_register_accept_terms))
        }
        state.termsError.asMessage()?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }

        FormErrorText(state.error)
        ArizPrimaryButton(
            text = stringResource(R.string.auth_register_submit),
            onClick = { onAction(RegisterAction.Submit) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isLoading,
        )
        AuthFooterLink(
            prefix = stringResource(R.string.auth_register_have_account),
            link = stringResource(R.string.auth_register_login_link),
            onClick = onLoginClick,
        )
        AuthFooterLink(
            prefix = stringResource(R.string.auth_register_professional_prompt),
            link = stringResource(R.string.auth_register_professional_link),
            onClick = onProfessionalClick,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterContentPreview() {
    ArizTheme {
        RegisterContent(state = RegisterUiState(), onAction = {}, onLoginClick = {}, onProfessionalClick = {}, onBackClick = {})
    }
}
