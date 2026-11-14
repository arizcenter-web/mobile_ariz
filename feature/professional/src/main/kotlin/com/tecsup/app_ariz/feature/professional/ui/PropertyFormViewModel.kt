package com.tecsup.app_ariz.feature.professional.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.professional.domain.GetPropertyDraftUseCase
import com.tecsup.app_ariz.feature.professional.domain.PropertyDraft
import com.tecsup.app_ariz.feature.professional.domain.PropertyField
import com.tecsup.app_ariz.feature.professional.domain.PropertyFieldError
import com.tecsup.app_ariz.feature.professional.domain.PropertyValidator
import com.tecsup.app_ariz.feature.professional.domain.PublishPropertyUseCase
import com.tecsup.app_ariz.feature.professional.domain.SavePropertyUseCase
import com.tecsup.app_ariz.feature.professional.domain.UploadPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PropertyFormUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val draft: PropertyDraft = PropertyDraft(),
    val errors: Map<PropertyField, PropertyFieldError> = emptyMap(),
    /** Fotos que se están subiendo a Cloudinary. */
    val uploading: Int = 0,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    val photoError: String? = null,
)

sealed interface PropertyFormAction {
    data class Edit(val change: (PropertyDraft) -> PropertyDraft) : PropertyFormAction
    data class PhotosPicked(val uris: List<String>) : PropertyFormAction
    data class RemovePhoto(val url: String) : PropertyFormAction
    data class MakeCover(val url: String) : PropertyFormAction
    data class Save(val publish: Boolean) : PropertyFormAction
    data object Retry : PropertyFormAction
}

sealed interface PropertyFormEvent {
    /** Guardada (y publicada si se pidió): volver a la lista. */
    data class Saved(val published: Boolean) : PropertyFormEvent
}

@HiltViewModel
class PropertyFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDraft: GetPropertyDraftUseCase,
    private val saveProperty: SavePropertyUseCase,
    private val publishProperty: PublishPropertyUseCase,
    private val uploadPhoto: UploadPhotoUseCase,
) : ViewModel() {

    // Argumento `id` de PropertyFormRoute: `null` al crear, el id al editar.
    private val propertyId: String? = savedStateHandle.get<String>(ID_ARG)?.takeIf { it.isNotBlank() }

    // Id con el que se guarda: tras crear una propiedad nueva pasa a ser el suyo, así un segundo intento (por ejemplo
    // si falló la publicación) edita la misma en vez de crear un duplicado.
    private var savedId: String? = propertyId

    // Propiedad de ARIZ creada por el administrador (endpoint propio, sin plan).
    private val asAdmin: Boolean = savedStateHandle.get<Boolean>(ADMIN_ARG) == true

    private val _state = MutableStateFlow(PropertyFormUiState(isEditing = propertyId != null, isLoading = propertyId != null))
    val state: StateFlow<PropertyFormUiState> = _state.asStateFlow()

    private val _events = Channel<PropertyFormEvent>(Channel.BUFFERED)
    val events: Flow<PropertyFormEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: PropertyFormAction) {
        when (action) {
            is PropertyFormAction.Edit -> _state.update { it.copy(draft = action.change(it.draft), errors = emptyMap(), error = null) }
            is PropertyFormAction.PhotosPicked -> upload(action.uris)
            is PropertyFormAction.RemovePhoto -> _state.update { it.copy(draft = it.draft.copy(photos = it.draft.photos - action.url)) }
            is PropertyFormAction.MakeCover -> _state.update {
                it.copy(draft = it.draft.copy(photos = listOf(action.url) + (it.draft.photos - action.url)))
            }
            is PropertyFormAction.Save -> save(action.publish)
            PropertyFormAction.Retry -> load()
        }
    }

    private fun load() {
        val id = propertyId ?: return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getDraft(id)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, draft = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private fun upload(uris: List<String>) {
        if (uris.isEmpty()) return
        _state.update { it.copy(uploading = it.uploading + uris.size, photoError = null) }
        uris.forEach { uri ->
            viewModelScope.launch {
                when (val result = uploadPhoto(uri)) {
                    is AppResult.Success -> _state.update {
                        it.copy(uploading = it.uploading - 1, draft = it.draft.copy(photos = it.draft.photos + result.data.url), errors = it.errors - PropertyField.PHOTOS)
                    }
                    is AppResult.Failure -> _state.update { it.copy(uploading = it.uploading - 1, photoError = result.error.message) }
                }
            }
        }
    }

    private fun save(publish: Boolean) {
        val current = _state.value
        if (current.isSaving || current.uploading > 0) return
        val errors = PropertyValidator.validate(current.draft)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            when (val saved = saveProperty(savedId, current.draft, asAdmin)) {
                is AppResult.Failure -> _state.update { it.copy(isSaving = false, error = saved.error) }
                is AppResult.Success -> {
                    savedId = saved.data.id
                    if (!publish) {
                        _state.update { it.copy(isSaving = false) }
                        _events.send(PropertyFormEvent.Saved(published = false))
                        return@launch
                    }
                    when (val published = publishProperty(saved.data.id)) {
                        is AppResult.Success -> {
                            _state.update { it.copy(isSaving = false) }
                            _events.send(PropertyFormEvent.Saved(published = true))
                        }
                        // Quedó guardada como borrador; se muestra por qué no se pudo publicar (plan, perfil...).
                        is AppResult.Failure -> _state.update { it.copy(isSaving = false, error = published.error) }
                    }
                }
            }
        }
    }

    companion object {
        const val ID_ARG = "id"
        const val ADMIN_ARG = "admin"
    }
}
