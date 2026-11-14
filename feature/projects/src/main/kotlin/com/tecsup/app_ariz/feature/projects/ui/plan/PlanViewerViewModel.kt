package com.tecsup.app_ariz.feature.projects.ui.plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.projects.domain.model.PublicPlan
import com.tecsup.app_ariz.feature.projects.domain.usecase.DownloadSheetUseCase
import com.tecsup.app_ariz.feature.projects.domain.usecase.GetSitePlanUseCase
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

/** Largo de los códigos de acceso que genera el backend (18 caracteres hexadecimales). */
const val AccessCodeLength = 18

data class PlanViewerUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val plan: PublicPlan? = null,
    val error: AppError? = null,
    /** El proyecto no tiene plano publicado (la respuesta llegó vacía). */
    val notPublished: Boolean = false,
    /** Estado elegido en el filtro (`null` = todos). */
    val filter: String? = null,
    /** Id del lote/departamento cuya ficha se está descargando. */
    val downloadingId: String? = null,
)

@HiltViewModel
class PlanViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlan: GetSitePlanUseCase,
    private val downloadSheet: DownloadSheetUseCase,
) : ViewModel() {
    private val slug: String = savedStateHandle.get<String>("slug").orEmpty()

    private val _state = MutableStateFlow(PlanViewerUiState())
    val state: StateFlow<PlanViewerUiState> = _state.asStateFlow()

    private val _files = Channel<String>(Channel.BUFFERED)
    /** Fichas PDF descargadas, listas para abrir. */
    val files: Flow<String> = _files.receiveAsFlow()

    fun onCodeChange(value: String) = _state.update {
        it.copy(code = value.uppercase().filter { c -> c.isLetterOrDigit() }.take(AccessCodeLength), error = null, notPublished = false)
    }

    fun load() {
        val current = _state.value
        if (current.isLoading) return
        _state.update { it.copy(isLoading = true, error = null, notPublished = false) }
        viewModelScope.launch {
            when (val result = getPlan(slug, current.code)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, plan = result.data, notPublished = result.data == null) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    fun setFilter(status: String?) = _state.update { it.copy(filter = status) }

    fun lock() = _state.update { PlanViewerUiState() }

    fun downloadSheet(id: String, apartment: Boolean, label: String) {
        val code = _state.value.code
        if (_state.value.downloadingId != null) return
        _state.update { it.copy(downloadingId = id) }
        viewModelScope.launch {
            val safe = label.filter { c -> c.isLetterOrDigit() || c == '-' }.ifBlank { id.take(8) }
            when (val result = downloadSheet(slug, id, apartment, code, "ficha-$safe.pdf")) {
                is AppResult.Success -> {
                    _state.update { it.copy(downloadingId = null) }
                    _files.send(result.data)
                }
                is AppResult.Failure -> _state.update { it.copy(downloadingId = null, error = result.error) }
            }
        }
    }
}
