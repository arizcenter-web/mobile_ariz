package com.tecsup.app_ariz.feature.professional.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.professional.domain.DeleteProjectUseCase
import com.tecsup.app_ariz.feature.professional.domain.DeletePropertyUseCase
import com.tecsup.app_ariz.feature.professional.domain.GetMyLeadsUseCase
import com.tecsup.app_ariz.feature.professional.domain.GetMyProjectsUseCase
import com.tecsup.app_ariz.feature.professional.domain.GetMyPropertiesUseCase
import com.tecsup.app_ariz.feature.professional.domain.LoadPanelUseCase
import com.tecsup.app_ariz.feature.professional.domain.OwnerLead
import com.tecsup.app_ariz.feature.professional.domain.OwnerProject
import com.tecsup.app_ariz.feature.professional.domain.OwnerProperty
import com.tecsup.app_ariz.feature.professional.domain.PanelData
import com.tecsup.app_ariz.feature.professional.domain.PublishProjectUseCase
import com.tecsup.app_ariz.feature.professional.domain.PublishPropertyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---- Dashboard y estadísticas (misma carga) ----

data class PanelUiState(
    val isLoading: Boolean = true,
    val data: PanelData? = null,
    val error: AppError? = null,
)

/** Carga el panel completo: sirve al dashboard y a la pantalla de estadísticas. */
@HiltViewModel
class PanelViewModel @Inject constructor(
    private val loadPanel: LoadPanelUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(PanelUiState())
    val state: StateFlow<PanelUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = loadPanel()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, data = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }
}

// ---- Mis propiedades ----

data class PropertiesUiState(
    val isLoading: Boolean = true,
    val items: List<OwnerProperty> = emptyList(),
    val error: AppError? = null,
    /** Propiedad con una acción en curso (publicar o eliminar). */
    val busyId: String? = null,
    /** Resultado de la última acción, para avisar con un mensaje. */
    val notice: PanelNotice? = null,
)

/** Aviso de una acción: éxito o el texto del error del backend. */
sealed interface PanelNotice {
    data class Success(val message: String) : PanelNotice
    data class Failure(val message: String) : PanelNotice
}

sealed interface PropertiesAction {
    data object Refresh : PropertiesAction
    data class Publish(val id: String) : PropertiesAction
    data class Delete(val id: String) : PropertiesAction
    data object NoticeShown : PropertiesAction
}

@HiltViewModel
class PropertiesViewModel @Inject constructor(
    private val getProperties: GetMyPropertiesUseCase,
    private val publish: PublishPropertyUseCase,
    private val delete: DeletePropertyUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(PropertiesUiState())
    val state: StateFlow<PropertiesUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun onAction(action: PropertiesAction) {
        when (action) {
            PropertiesAction.Refresh -> load()
            is PropertiesAction.Publish -> run(action.id, "Enviada: tu propiedad quedó publicada o en revisión.") { publish(action.id) }
            is PropertiesAction.Delete -> run(action.id, "Propiedad eliminada.") { delete(action.id) }
            PropertiesAction.NoticeShown -> _state.update { it.copy(notice = null) }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getProperties()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, items = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private fun run(id: String, success: String, block: suspend () -> AppResult<*>) {
        if (_state.value.busyId != null) return
        _state.update { it.copy(busyId = id) }
        viewModelScope.launch {
            val result = block()
            _state.update {
                it.copy(
                    busyId = null,
                    notice = when (result) {
                        is AppResult.Success -> PanelNotice.Success(success)
                        is AppResult.Failure -> PanelNotice.Failure(result.error.message)
                    },
                )
            }
            if (result is AppResult.Success) load()
        }
    }
}

// ---- Mis proyectos ----

data class ProjectsUiState(
    val isLoading: Boolean = true,
    val items: List<OwnerProject> = emptyList(),
    val error: AppError? = null,
    val busyId: String? = null,
    val notice: PanelNotice? = null,
)

sealed interface ProjectsAction {
    data object Refresh : ProjectsAction
    data class Publish(val id: String) : ProjectsAction
    data class Delete(val id: String) : ProjectsAction
    data object NoticeShown : ProjectsAction
}

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val getProjects: GetMyProjectsUseCase,
    private val publish: PublishProjectUseCase,
    private val delete: DeleteProjectUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(ProjectsUiState())
    val state: StateFlow<ProjectsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun onAction(action: ProjectsAction) {
        when (action) {
            ProjectsAction.Refresh -> load()
            is ProjectsAction.Publish -> run(action.id, "Proyecto enviado a revisión o publicado.") { publish(action.id) }
            is ProjectsAction.Delete -> run(action.id, "Proyecto eliminado.") { delete(action.id) }
            ProjectsAction.NoticeShown -> _state.update { it.copy(notice = null) }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getProjects()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, items = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private fun run(id: String, success: String, block: suspend () -> AppResult<*>) {
        if (_state.value.busyId != null) return
        _state.update { it.copy(busyId = id) }
        viewModelScope.launch {
            val result = block()
            _state.update {
                it.copy(
                    busyId = null,
                    notice = when (result) {
                        is AppResult.Success -> PanelNotice.Success(success)
                        is AppResult.Failure -> PanelNotice.Failure(result.error.message)
                    },
                )
            }
            if (result is AppResult.Success) load()
        }
    }
}

// ---- Consultas recibidas ----

data class LeadsUiState(
    val isLoading: Boolean = true,
    val items: List<OwnerLead> = emptyList(),
    val error: AppError? = null,
)

@HiltViewModel
class LeadsViewModel @Inject constructor(
    private val getLeads: GetMyLeadsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(LeadsUiState())
    val state: StateFlow<LeadsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getLeads()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, items = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }
}
