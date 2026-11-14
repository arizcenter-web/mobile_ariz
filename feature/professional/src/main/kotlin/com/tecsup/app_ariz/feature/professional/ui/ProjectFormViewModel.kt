package com.tecsup.app_ariz.feature.professional.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.professional.domain.CreateProjectUseCase
import com.tecsup.app_ariz.feature.professional.domain.GetProjectDraftUseCase
import com.tecsup.app_ariz.feature.professional.domain.LotDraft
import com.tecsup.app_ariz.feature.professional.domain.UpdateProjectUseCase
import com.tecsup.app_ariz.feature.professional.domain.ProjectDraft
import com.tecsup.app_ariz.feature.professional.domain.ProjectField
import com.tecsup.app_ariz.feature.professional.domain.ProjectFieldError
import com.tecsup.app_ariz.feature.professional.domain.ProjectValidator
import com.tecsup.app_ariz.feature.professional.domain.PublishProjectUseCase
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

data class ProjectFormUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    /** El administrador puede elegir el tipo (lotización o departamentos). */
    val isAdmin: Boolean = false,
    val draft: ProjectDraft = ProjectDraft(),
    val errors: Map<ProjectField, ProjectFieldError> = emptyMap(),
    val uploading: Int = 0,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    val photoError: String? = null,
)

sealed interface ProjectFormAction {
    data class Edit(val change: (ProjectDraft) -> ProjectDraft) : ProjectFormAction
    data class PhotosPicked(val uris: List<String>) : ProjectFormAction
    data class RemovePhoto(val url: String) : ProjectFormAction
    data class MakeCover(val url: String) : ProjectFormAction
    data object AddLot : ProjectFormAction
    data class EditLot(val index: Int, val change: (LotDraft) -> LotDraft) : ProjectFormAction
    data class RemoveLot(val index: Int) : ProjectFormAction
    data class Save(val publish: Boolean) : ProjectFormAction
}

sealed interface ProjectFormEvent {
    data class Saved(val published: Boolean) : ProjectFormEvent
}

@HiltViewModel
class ProjectFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createProject: CreateProjectUseCase,
    private val getDraft: GetProjectDraftUseCase,
    private val updateProject: UpdateProjectUseCase,
    private val publishProject: PublishProjectUseCase,
    private val uploadPhoto: UploadPhotoUseCase,
) : ViewModel() {
    // Argumento `id`: al editar un proyecto existente; `null` al crear.
    private val projectId: String? = savedStateHandle.get<String>("id")?.takeIf { it.isNotBlank() }

    private val _state = MutableStateFlow(ProjectFormUiState(isEditing = projectId != null, isLoading = projectId != null, isAdmin = savedStateHandle.get<Boolean>("admin") == true))
    val state: StateFlow<ProjectFormUiState> = _state.asStateFlow()

    private val _events = Channel<ProjectFormEvent>(Channel.BUFFERED)
    val events: Flow<ProjectFormEvent> = _events.receiveAsFlow()

    // Id del proyecto ya creado: si la publicación falla, un segundo intento no lo crea otra vez.
    private var createdId: String? = projectId

    // Proyecto de ARIZ creado por el administrador (endpoints propios).
    private val asAdmin: Boolean = savedStateHandle.get<Boolean>("admin") == true

    init {
        load()
    }

    fun load() {
        val id = projectId ?: return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getDraft(id, asAdmin)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, draft = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    fun onAction(action: ProjectFormAction) {
        when (action) {
            is ProjectFormAction.Edit -> _state.update { it.copy(draft = action.change(it.draft), errors = emptyMap(), error = null) }
            is ProjectFormAction.PhotosPicked -> upload(action.uris)
            is ProjectFormAction.RemovePhoto -> _state.update { it.copy(draft = it.draft.copy(photos = it.draft.photos - action.url)) }
            is ProjectFormAction.MakeCover -> _state.update { it.copy(draft = it.draft.copy(photos = listOf(action.url) + (it.draft.photos - action.url))) }
            ProjectFormAction.AddLot -> _state.update { it.copy(draft = it.draft.copy(lots = it.draft.lots + LotDraft())) }
            is ProjectFormAction.EditLot -> _state.update { s ->
                s.copy(draft = s.draft.copy(lots = s.draft.lots.mapIndexed { i, lot -> if (i == action.index) action.change(lot) else lot }), errors = emptyMap())
            }
            is ProjectFormAction.RemoveLot -> _state.update { s ->
                s.copy(draft = s.draft.copy(lots = s.draft.lots.filterIndexed { i, _ -> i != action.index }.ifEmpty { listOf(LotDraft()) }))
            }
            is ProjectFormAction.Save -> save(action.publish)
        }
    }

    private fun upload(uris: List<String>) {
        if (uris.isEmpty()) return
        _state.update { it.copy(uploading = it.uploading + uris.size, photoError = null) }
        uris.forEach { uri ->
            viewModelScope.launch {
                when (val result = uploadPhoto(uri, folder = "ariz/projects")) {
                    is AppResult.Success -> _state.update {
                        it.copy(uploading = it.uploading - 1, draft = it.draft.copy(photos = it.draft.photos + result.data.url), errors = it.errors - ProjectField.PHOTOS)
                    }
                    is AppResult.Failure -> _state.update { it.copy(uploading = it.uploading - 1, photoError = result.error.message) }
                }
            }
        }
    }

    private fun save(publish: Boolean) {
        val current = _state.value
        if (current.isSaving || current.uploading > 0) return
        val errors = ProjectValidator.validate(current.draft, requireCoordinates = publish)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            // Al editar, primero se guarda el PATCH; al crear, se crea una sola vez.
            if (projectId != null) {
                val updated = updateProject(projectId, current.draft)
                if (updated is AppResult.Failure) {
                    _state.update { it.copy(isSaving = false, error = updated.error) }
                    return@launch
                }
            }
            val id = createdId ?: when (val created = createProject(current.draft, asAdmin)) {
                is AppResult.Failure -> {
                    _state.update { it.copy(isSaving = false, error = created.error) }
                    return@launch
                }
                is AppResult.Success -> created.data.also { createdId = it }
            }
            if (!publish) {
                _state.update { it.copy(isSaving = false) }
                _events.send(ProjectFormEvent.Saved(published = false))
                return@launch
            }
            when (val published = publishProject(id, asAdmin)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(ProjectFormEvent.Saved(published = true))
                }
                is AppResult.Failure -> _state.update { it.copy(isSaving = false, error = published.error) }
            }
        }
    }
}
