package com.tecsup.app_ariz.core.leads.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.leads.domain.LeadFieldError
import com.tecsup.app_ariz.core.leads.domain.LeadRequest
import com.tecsup.app_ariz.core.leads.domain.LeadTarget
import com.tecsup.app_ariz.core.leads.domain.LeadValidator
import com.tecsup.app_ariz.core.leads.domain.LeadsRepository
import com.tecsup.app_ariz.core.session.SessionManager
import com.tecsup.app_ariz.core.session.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeadFormUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val dni: String = "",
    val message: String = "",
    val acceptTerms: Boolean = true,
    val nameError: LeadFieldError? = null,
    val emailError: LeadFieldError? = null,
    val messageError: LeadFieldError? = null,
    val termsError: LeadFieldError? = null,
    val isSending: Boolean = false,
    val error: AppError? = null,
    /** La consulta salió bien: se muestra la confirmación. */
    val sent: Boolean = false,
)

sealed interface LeadFormAction {
    data class NameChanged(val value: String) : LeadFormAction
    data class EmailChanged(val value: String) : LeadFormAction
    data class PhoneChanged(val value: String) : LeadFormAction
    data class DniChanged(val value: String) : LeadFormAction
    data class MessageChanged(val value: String) : LeadFormAction
    data class TermsChanged(val value: Boolean) : LeadFormAction
    data class Submit(val target: LeadTarget) : LeadFormAction
    data object Dismissed : LeadFormAction
}

@HiltViewModel
class LeadFormViewModel @Inject constructor(
    private val repository: LeadsRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(LeadFormUiState())
    val state: StateFlow<LeadFormUiState> = _state.asStateFlow()

    init {
        // Con sesión iniciada se adelantan el nombre y el correo, como hace el web con el usuario.
        (sessionManager.state.value as? SessionState.SignedIn)?.user?.let { user ->
            _state.update { it.copy(name = user.name, email = user.email) }
        }
    }

    /** Pone el mensaje sugerido la primera vez que se abre el formulario (si el usuario no escribió nada). */
    fun prepare(defaultMessage: String) {
        _state.update { if (it.message.isBlank() && !it.sent) it.copy(message = defaultMessage) else it }
    }

    fun onAction(action: LeadFormAction) {
        when (action) {
            is LeadFormAction.NameChanged ->
                _state.update { it.copy(name = action.value.take(LeadValidator.NAME_MAX), nameError = null, error = null) }
            is LeadFormAction.EmailChanged -> _state.update { it.copy(email = action.value, emailError = null, error = null) }
            is LeadFormAction.PhoneChanged -> _state.update { it.copy(phone = action.value.take(LeadValidator.PHONE_MAX)) }
            is LeadFormAction.DniChanged ->
                _state.update { it.copy(dni = action.value.filter(Char::isDigit).take(LeadValidator.DNI_MAX)) }
            is LeadFormAction.MessageChanged ->
                _state.update { it.copy(message = action.value.take(LeadValidator.MESSAGE_MAX), messageError = null, error = null) }
            is LeadFormAction.TermsChanged -> _state.update { it.copy(acceptTerms = action.value, termsError = null) }
            is LeadFormAction.Submit -> submit(action.target)
            LeadFormAction.Dismissed -> _state.update { it.copy(sent = false, error = null) }
        }
    }

    private fun submit(target: LeadTarget) {
        val current = _state.value
        if (current.isSending) return

        val nameError = LeadValidator.validateName(current.name)
        val emailError = LeadValidator.validateEmail(current.email)
        val messageError = LeadValidator.validateMessage(current.message)
        val termsError = LeadValidator.validateTerms(current.acceptTerms)
        if (listOf(nameError, emailError, messageError, termsError).any { it != null }) {
            _state.update {
                it.copy(nameError = nameError, emailError = emailError, messageError = messageError, termsError = termsError)
            }
            return
        }

        _state.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            val request = LeadRequest(target, current.name, current.email, current.phone, current.dni, current.message)
            when (val result = repository.send(request)) {
                // El formulario queda listo para otra consulta (el mensaje se vuelve a sugerir al abrirlo).
                is AppResult.Success -> _state.update { it.copy(isSending = false, sent = true, message = "", dni = "", phone = "") }
                is AppResult.Failure -> _state.update { it.copy(isSending = false, error = result.error) }
            }
        }
    }
}
