package com.tecsup.app_ariz.feature.admin.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.admin.domain.AdminExport
import com.tecsup.app_ariz.feature.admin.domain.DownloadAdminExportUseCase
import com.tecsup.app_ariz.feature.admin.domain.ImportLotsExcelUseCase
import com.tecsup.app_ariz.feature.admin.domain.LoadSitePlanUseCase
import com.tecsup.app_ariz.feature.admin.domain.PlanImage
import com.tecsup.app_ariz.feature.admin.domain.PlanMode
import com.tecsup.app_ariz.feature.admin.domain.RenderPdfPageUseCase
import com.tecsup.app_ariz.feature.admin.domain.SavePlanOutcome
import com.tecsup.app_ariz.feature.admin.domain.SitePlanDoc
import com.tecsup.app_ariz.feature.admin.domain.SaveSitePlanUseCase
import com.tecsup.app_ariz.feature.admin.domain.SitePlans
import com.tecsup.app_ariz.feature.admin.domain.UploadSizedImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

data class SitePlanUiState(
    val isLoading: Boolean = true,
    val doc: SitePlanDoc? = null,
    /** Copia de trabajo del plano (lo que se envía al guardar). */
    val plan: JsonObject = JsonObject(emptyMap()),
    val mode: PlanMode = PlanMode.LOTS,
    val dirty: Boolean = false,
    val isSaving: Boolean = false,
    val uploading: Boolean = false,
    val error: AppError? = null,
    val outcome: SavePlanOutcome? = null,
    val failure: String? = null,
    /** Lotes sumados desde un Excel en la última importación (para avisar una vez). */
    val imported: Int? = null,
)

@HiltViewModel
class SitePlanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadPlan: LoadSitePlanUseCase,
    private val savePlan: SaveSitePlanUseCase,
    private val uploadImage: UploadSizedImageUseCase,
    private val importExcel: ImportLotsExcelUseCase,
    private val renderPdf: RenderPdfPageUseCase,
    private val uploadPdf: com.tecsup.app_ariz.feature.admin.domain.UploadPlanPdfUseCase,
    private val downloadExport: DownloadAdminExportUseCase,
) : ViewModel() {
    private val _files = Channel<String>(Channel.BUFFERED)
    /** Archivos descargados (la plantilla de Excel), listos para abrir. */
    val files: Flow<String> = _files.receiveAsFlow()

    private val projectId: String = savedStateHandle.get<String>("id").orEmpty()

    private val _state = MutableStateFlow(SitePlanUiState())
    val state: StateFlow<SitePlanUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null, failure = null) }
        viewModelScope.launch {
            when (val result = loadPlan(projectId)) {
                is AppResult.Success -> {
                    val doc = result.data
                    val mode = SitePlans.mode(doc.plan, doc.category)
                    _state.update {
                        it.copy(isLoading = false, doc = doc, mode = mode, plan = SitePlans.ensure(doc.plan, mode), dirty = false)
                    }
                }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    /** Aplica un cambio al plano de trabajo (queda sin guardar hasta [save]). */
    fun edit(change: (JsonObject) -> JsonObject) = _state.update { it.copy(plan = change(it.plan), dirty = true, outcome = null, failure = null) }

    fun uploadFloorImage(buildingId: String, floorId: String, uri: String) {
        _state.update { it.copy(uploading = true, failure = null) }
        viewModelScope.launch {
            when (val result = uploadImage(uri)) {
                is AppResult.Success -> {
                    val image = result.data
                    _state.update {
                        it.copy(uploading = false, plan = SitePlans.setFloorImage(it.plan, buildingId, floorId, image.url, image.width, image.height), dirty = true)
                    }
                }
                is AppResult.Failure -> _state.update { it.copy(uploading = false, failure = result.error.message) }
            }
        }
    }

    /** Sube la imagen del plano y la coloca centrada en ([lat], [lng]) con un tamaño inicial de 200 m de ancho. */
    fun placeImage(uri: String, lat: Double, lng: Double) {
        _state.update { it.copy(uploading = true, failure = null) }
        viewModelScope.launch { setPlanImage(uri, lat, lng) }
    }

    /** Igual que [placeImage], pero con una página de un PDF convertida a imagen. */
    fun placePdf(uri: String, page: Int, lat: Double, lng: Double) {
        _state.update { it.copy(uploading = true, failure = null) }
        viewModelScope.launch {
            when (val rendered = renderPdf(uri, page)) {
                is AppResult.Failure -> _state.update { it.copy(uploading = false, failure = rendered.error.message) }
                is AppResult.Success -> when (val original = uploadPdf(uri, "plano.pdf")) {
                    is AppResult.Failure -> _state.update { it.copy(uploading = false, failure = original.error.message) }
                    is AppResult.Success -> setPlanImage(rendered.data.uri, lat, lng, sourcePdf = original.data to page)
                }
            }
        }
    }

    private suspend fun setPlanImage(uri: String, lat: Double, lng: Double, sourcePdf: Pair<String, Int>? = null) {
        when (val result = uploadImage(uri)) {
            is AppResult.Failure -> _state.update { it.copy(uploading = false, failure = result.error.message) }
            is AppResult.Success -> {
                val image = result.data
                val widthM = 200.0
                val heightM = widthM * image.height / image.width
                _state.update {
                    val placed = SitePlans.replaceImage(it.plan, PlanImage(image.url, lat, lng, widthM, heightM, 0.0, 0.7))
                    val plan = sourcePdf?.let { (pdfUrl, page) -> SitePlans.withSourcePdf(placed, pdfUrl, page, "plano.pdf") } ?: placed
                    it.copy(uploading = false, dirty = true, plan = plan)
                }
            }
        }
    }

    /** Mueve, gira o cambia el tamaño/opacidad de la imagen del plano. */
    fun adjustImage(change: (PlanImage) -> PlanImage) = edit { plan -> SitePlans.image(plan)?.let { SitePlans.withImage(plan, change(it)) } ?: plan }

    /** Recorta la hoja a esa parte de lo visible (0..1); false si el área no es válida. */
    fun cropImage(left: Double, top: Double, right: Double, bottom: Double): Boolean {
        val next = SitePlans.cropImage(_state.value.plan, left, top, right, bottom) ?: return false
        edit { next }
        return true
    }

    /** Vuelve a mostrar la hoja completa; false si no se puede (por tamaño). */
    fun restoreCrop(): Boolean {
        val next = SitePlans.restoreCrop(_state.value.plan) ?: return false
        edit { next }
        return true
    }

    /** Lee el Excel en el backend y suma sus lotes al plano (sin dibujo, como en el web). */
    fun importLots(uri: String) {
        _state.update { it.copy(uploading = true, failure = null) }
        viewModelScope.launch {
            when (val result = importExcel(projectId, uri)) {
                is AppResult.Failure -> _state.update { it.copy(uploading = false, failure = result.error.message) }
                is AppResult.Success -> {
                    val (lots, problem) = SitePlans.lotsFromExcelRows(result.data, SitePlans.lots(_state.value.plan))
                    _state.update {
                        if (problem != null) it.copy(uploading = false, failure = problem)
                        else it.copy(uploading = false, plan = SitePlans.addLots(it.plan, lots), dirty = true, imported = lots.size)
                    }
                }
            }
        }
    }

    fun downloadTemplate() {
        viewModelScope.launch {
            when (val result = downloadExport(AdminExport("Plantilla", "admin/projects/$projectId/site-plan/template", "plantilla-lotes.xlsx"))) {
                is AppResult.Success -> _files.send(result.data)
                is AppResult.Failure -> _state.update { it.copy(failure = result.error.message) }
            }
        }
    }

    fun save(publish: Boolean) {
        val current = _state.value
        val doc = current.doc ?: return
        if (current.isSaving || current.uploading) return
        _state.update { it.copy(isSaving = true, failure = null, outcome = null) }
        viewModelScope.launch {
            when (val result = savePlan(projectId, doc.revision, current.plan, publish)) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isSaving = false,
                        dirty = false,
                        doc = doc.copy(revision = result.data.revision, publishedAt = if (publish) result.data.publishedAt else doc.publishedAt),
                        outcome = if (publish) SavePlanOutcome.PUBLISHED else SavePlanOutcome.SAVED,
                    )
                }
                // Conflicto de revisión u otra validación del backend: el mensaje explica qué corregir.
                is AppResult.Failure -> _state.update { it.copy(isSaving = false, failure = result.error.message) }
            }
        }
    }

    fun messageShown() = _state.update { it.copy(outcome = null, failure = null, imported = null) }
}
