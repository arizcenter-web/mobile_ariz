package com.tecsup.app_ariz.feature.info.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.info.domain.ContactFieldError
import com.tecsup.app_ariz.feature.info.domain.ContactReport
import com.tecsup.app_ariz.feature.info.domain.ContactRepository
import com.tecsup.app_ariz.feature.info.domain.ContactValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Temas del formulario de contacto, los mismos del web. */
enum class ContactTopic { INFORMATION, BUY, SELL, INVEST, PUBLISH, SUPPORT }

data class ContactUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val topic: ContactTopic = ContactTopic.INFORMATION,
    val message: String = "",
    val nameError: ContactFieldError? = null,
    val emailError: ContactFieldError? = null,
    val phoneError: ContactFieldError? = null,
    val messageError: ContactFieldError? = null,
    val isSending: Boolean = false,
    val error: AppError? = null,
    val sent: Boolean = false,
)

sealed interface ContactAction {
    data class NameChanged(val value: String) : ContactAction
    data class EmailChanged(val value: String) : ContactAction
    data class PhoneChanged(val value: String) : ContactAction
    data class TopicChanged(val value: ContactTopic) : ContactAction
    data class MessageChanged(val value: String) : ContactAction

    /** [topicLabel] es el texto del tema ya traducido: es lo que recibe el backend. */
    data class Submit(val topicLabel: String) : ContactAction
    data object SentDismissed : ContactAction
}

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val repository: ContactRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ContactUiState())
    val state: StateFlow<ContactUiState> = _state.asStateFlow()

    fun onAction(action: ContactAction) {
        when (action) {
            is ContactAction.NameChanged ->
                _state.update { it.copy(name = action.value.take(ContactValidator.NAME_MAX), nameError = null, error = null) }
            is ContactAction.EmailChanged -> _state.update { it.copy(email = action.value, emailError = null, error = null) }
            is ContactAction.PhoneChanged ->
                _state.update { it.copy(phone = action.value.filter(Char::isDigit).take(9), phoneError = null, error = null) }
            is ContactAction.TopicChanged -> _state.update { it.copy(topic = action.value) }
            is ContactAction.MessageChanged ->
                _state.update { it.copy(message = action.value.take(ContactValidator.MESSAGE_MAX), messageError = null, error = null) }
            is ContactAction.Submit -> submit(action.topicLabel)
            ContactAction.SentDismissed -> _state.update { it.copy(sent = false) }
        }
    }

    private fun submit(topicLabel: String) {
        val current = _state.value
        if (current.isSending) return

        val nameError = ContactValidator.validateName(current.name)
        val emailError = ContactValidator.validateEmail(current.email)
        val phoneError = ContactValidator.validatePhone(current.phone)
        val messageError = ContactValidator.validateMessage(current.message)
        if (listOf(nameError, emailError, phoneError, messageError).any { it != null }) {
            _state.update { it.copy(nameError = nameError, emailError = emailError, phoneError = phoneError, messageError = messageError) }
            return
        }

        _state.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            val report = ContactReport(current.name, current.email, current.phone, topicLabel, current.message)
            when (val result = repository.send(report)) {
                // El formulario queda limpio y listo para otra consulta, como en el web.
                is AppResult.Success -> _state.update { ContactUiState(sent = true) }
                is AppResult.Failure -> _state.update { it.copy(isSending = false, error = result.error) }
            }
        }
    }
}
