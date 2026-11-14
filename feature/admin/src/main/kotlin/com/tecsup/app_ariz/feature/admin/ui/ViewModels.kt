package com.tecsup.app_ariz.feature.admin.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.admin.domain.AdminAction
import com.tecsup.app_ariz.feature.admin.domain.AdminChoice
import com.tecsup.app_ariz.feature.admin.domain.FieldKind
import com.tecsup.app_ariz.feature.admin.domain.LoadAdminDetailUseCase
import com.tecsup.app_ariz.feature.admin.domain.LoadAdminOptionsUseCase
import com.tecsup.app_ariz.feature.admin.domain.AdminExport
import com.tecsup.app_ariz.feature.admin.domain.DownloadAdminExportUseCase
import com.tecsup.app_ariz.feature.admin.domain.AdminItem
import com.tecsup.app_ariz.feature.admin.domain.AdminOverview
import com.tecsup.app_ariz.feature.admin.domain.AdminSection
import com.tecsup.app_ariz.feature.admin.domain.AdminSections
import com.tecsup.app_ariz.feature.admin.domain.INVALID_PREFIX
import com.tecsup.app_ariz.feature.admin.domain.LoadAdminOverviewUseCase
import com.tecsup.app_ariz.feature.admin.domain.LoadAdminSectionUseCase
import com.tecsup.app_ariz.feature.admin.domain.RunAdminActionUseCase
import com.tecsup.app_ariz.feature.admin.domain.SaveAdminRecordUseCase
import com.tecsup.app_ariz.feature.admin.domain.UploadAdminImageUseCase
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

const val SECTION_ARG = "key"

data class OverviewUiState(val isLoading: Boolean = true, val overview: AdminOverview? = null)

@HiltViewModel
class AdminOverviewViewModel @Inject constructor(private val loadOverview: LoadAdminOverviewUseCase) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState())
    val state: StateFlow<OverviewUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            _state.update { OverviewUiState(isLoading = false, overview = loadOverview()) }
        }
    }
}

/** Resultado de la última acción para mostrarlo una vez. */
sealed interface AdminNotice {
    data object Done : AdminNotice
    /** Dato que el admin debe copiar (por ejemplo, un código que solo se muestra una vez). */
    data class Result(val title: String, val value: String) : AdminNotice
    data class Failure(val message: String) : AdminNotice
}

data class SectionUiState(
    val section: AdminSection? = null,
    val filterIndex: Int = 0,
    val isLoading: Boolean = true,
    val items: List<AdminItem> = emptyList(),
    val error: AppError? = null,
    /** Id del registro con una acción en curso (deshabilita sus botones). */
    val busyId: String? = null,
    val notice: AdminNotice? = null,
    val exporting: Boolean = false,
    /** Detalles ya cargados (por id) para "Ver detalle". */
    val details: Map<String, AdminItem> = emptyMap(),
)

@HiltViewModel
class AdminSectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadSection: LoadAdminSectionUseCase,
    private val runAction: RunAdminActionUseCase,
    private val downloadExport: DownloadAdminExportUseCase,
    private val loadDetail: LoadAdminDetailUseCase,
) : ViewModel() {
    private val _files = Channel<String>(Channel.BUFFERED)
    /** Rutas de los archivos exportados, listos para abrir o compartir. */
    val files: Flow<String> = _files.receiveAsFlow()

    private val section: AdminSection? = AdminSections.byKey(savedStateHandle.get<String>(SECTION_ARG).orEmpty())
    private val _state = MutableStateFlow(SectionUiState(section = section, isLoading = section != null))
    val state: StateFlow<SectionUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun selectFilter(index: Int) {
        if (index == _state.value.filterIndex) return
        _state.update { it.copy(filterIndex = index) }
        load()
    }

    fun load() {
        val section = section ?: return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = loadSection(section, _state.value.filterIndex)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, items = result.data) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, items = emptyList(), error = result.error) }
            }
        }
    }

    fun run(action: AdminAction, id: String, reason: String?) {
        if (_state.value.busyId != null) return
        _state.update { it.copy(busyId = id) }
        viewModelScope.launch {
            when (val result = runAction(action, id, reason)) {
                is AppResult.Success -> {
                    val shown = result.data
                    val notice = if (shown != null) AdminNotice.Result(action.resultTitle ?: action.label, shown) else AdminNotice.Done
                    // Los detalles abiertos quedaron viejos: se vuelven a pedir al mostrarse.
                    _state.update { it.copy(busyId = null, notice = notice, details = emptyMap()) }
                    load()
                }
                is AppResult.Failure -> _state.update { it.copy(busyId = null, notice = AdminNotice.Failure(result.error.message)) }
            }
        }
    }

    /** Pide el detalle completo del registro (si la sección lo declara y aún no se cargó). */
    fun expand(id: String) {
        val section = section ?: return
        if (section.detailPath == null || id in _state.value.details) return
        viewModelScope.launch {
            (loadDetail(section, id) as? AppResult.Success)?.let { r -> _state.update { it.copy(details = it.details + (id to r.data)) } }
        }
    }

    fun export(export: AdminExport) {
        if (_state.value.exporting) return
        _state.update { it.copy(exporting = true) }
        viewModelScope.launch {
            when (val result = downloadExport(export)) {
                is AppResult.Success -> {
                    _state.update { it.copy(exporting = false) }
                    _files.send(result.data)
                }
                is AppResult.Failure -> _state.update { it.copy(exporting = false, notice = AdminNotice.Failure(result.error.message)) }
            }
        }
    }

    fun noticeShown() = _state.update { it.copy(notice = null) }
}

// ---- Formulario genérico de alta y edición ----

const val ITEM_ARG = "id"

data class FormUiState(
    val section: AdminSection? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val values: Map<String, String> = emptyMap(),
    /** Claves con valor faltante o inválido. */
    val invalid: Set<String> = emptySet(),
    val uploading: Boolean = false,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    /** Opciones traídas del backend, por clave de campo. */
    val options: Map<String, List<AdminChoice>> = emptyMap(),
)

@HiltViewModel
class AdminFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadSection: LoadAdminSectionUseCase,
    private val save: SaveAdminRecordUseCase,
    private val uploadImage: UploadAdminImageUseCase,
    private val loadOptions: LoadAdminOptionsUseCase,
) : ViewModel() {
    private val section: AdminSection? = AdminSections.byKey(savedStateHandle.get<String>(SECTION_ARG).orEmpty())
    private val itemId: String? = savedStateHandle.get<String>(ITEM_ARG)?.takeIf { it.isNotBlank() }

    private val _state = MutableStateFlow(
        FormUiState(
            section = section,
            isEditing = itemId != null,
            isLoading = itemId != null,
            values = section?.form?.fields?.associate { it.key to it.default }.orEmpty(),
        ),
    )
    val state: StateFlow<FormUiState> = _state.asStateFlow()

    private val _saved = Channel<Unit>(Channel.BUFFERED)
    val saved: Flow<Unit> = _saved.receiveAsFlow()

    init {
        loadRemoteOptions()
        load()
    }

    private fun loadRemoteOptions() {
        section?.form?.fields?.filter { it.remote != null }?.forEach { field ->
            viewModelScope.launch {
                (loadOptions(field.remote!!) as? AppResult.Success)?.let { r -> _state.update { it.copy(options = it.options + (field.key to r.data)) } }
            }
        }
    }

    fun load() {
        val section = section ?: return
        val id = itemId ?: return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = loadSection(section, 0)) {
                is AppResult.Success -> {
                    // El registro puede no estar en el filtro por defecto; se busca en todos los filtros.
                    val item = result.data.firstOrNull { it.id == id } ?: findInOtherFilters(section, id)
                    _state.update {
                        if (item == null) it.copy(isLoading = false, error = AppError.NotFound())
                        else it.copy(isLoading = false, values = it.values + item.values)
                    }
                }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    private suspend fun findInOtherFilters(section: AdminSection, id: String): AdminItem? {
        for (index in 1 until section.filters.size) {
            val found = (loadSection(section, index) as? AppResult.Success)?.data?.firstOrNull { it.id == id }
            if (found != null) return found
        }
        return null
    }

    fun set(key: String, value: String) = _state.update { it.copy(values = it.values + (key to value), invalid = it.invalid - key, error = null) }

    fun imagePicked(key: String, uri: String) {
        _state.update { it.copy(uploading = true, error = null) }
        viewModelScope.launch {
            when (val result = uploadImage(uri)) {
                is AppResult.Success -> _state.update {
                    // IMAGES acumula las URLs (una por línea); IMAGE reemplaza la anterior.
                    val field = section?.form?.fields?.firstOrNull { f -> f.key == key }
                    val value = if (field?.kind == FieldKind.IMAGES) (it.values[key].orEmpty().lines().filter { l -> l.isNotBlank() } + result.data).joinToString("\n") else result.data
                    it.copy(uploading = false, values = it.values + (key to value), invalid = it.invalid - key)
                }
                is AppResult.Failure -> _state.update { it.copy(uploading = false, error = result.error) }
            }
        }
    }

    fun submit() {
        val section = section ?: return
        val current = _state.value
        if (current.isSaving || current.uploading) return
        _state.update { it.copy(isSaving = true, error = null, invalid = emptySet()) }
        viewModelScope.launch {
            when (val result = save(section, itemId, current.values)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    _saved.send(Unit)
                }
                is AppResult.Failure -> _state.update {
                    val message = (result.error as? AppError.Validation)?.message.orEmpty()
                    val keys = if (message.startsWith(INVALID_PREFIX)) message.removePrefix(INVALID_PREFIX).split(",").toSet() else emptySet()
                    it.copy(isSaving = false, invalid = keys, error = if (keys.isEmpty()) result.error else null)
                }
            }
        }
    }
}
