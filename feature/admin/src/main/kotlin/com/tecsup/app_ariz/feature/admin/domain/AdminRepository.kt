package com.tecsup.app_ariz.feature.admin.domain

import com.tecsup.app_ariz.core.common.AppResult
import kotlinx.serialization.json.JsonObject

interface AdminRepository {
    /** Lista los registros de una sección con el filtro elegido (índice en `section.filters`). */
    suspend fun load(section: AdminSection, filterIndex: Int = 0): AppResult<List<AdminItem>>

    /** Ejecuta una acción sobre un registro; `reason` va al cuerpo si la acción lo pide. */
    suspend fun run(action: AdminAction, id: String, reason: String? = null): AppResult<String?>

    /** Descarga un archivo exportado y devuelve su ruta local (en la caché de la app). */
    suspend fun download(export: AdminExport): AppResult<String>

    /** Datos de los gráficos del dashboard; las partes que fallen quedan vacías (falla solo si ninguna cargó). */
    suspend fun analytics(): AppResult<AnalyticsRaw>

    /** Opciones de un campo que depende de datos del backend. */
    suspend fun options(remote: RemoteOptions): AppResult<List<AdminChoice>>

    /** Detalle completo de un registro (la sección debe declarar `detailPath`). */
    suspend fun detail(section: AdminSection, id: String): AppResult<AdminItem>

    /** Plano del proyecto (`admin/projects/{id}/site-plan`). */
    suspend fun loadSitePlan(projectId: String): AppResult<SitePlanDoc>

    /** Guarda el plano completo; `revision` evita pisar cambios de otro administrador. */
    suspend fun saveSitePlan(projectId: String, revision: Int, plan: JsonObject, publish: Boolean): AppResult<SitePlanSaved>

    /** Lee un Excel de lotes en el backend y devuelve sus filas (columnas de la plantilla). */
    suspend fun importLotsExcel(projectId: String, uri: String): AppResult<List<Map<String, String>>>

    /** Convierte una página de un PDF en imagen (PNG en la caché) para usarla como plano. */
    suspend fun renderPdfPage(uri: String, page: Int): AppResult<RenderedPage>

    /** Sube una imagen y devuelve su URL con el ancho y alto (el plano de un piso los exige). */
    suspend fun uploadSizedImage(uri: String): AppResult<SizedImage>

    suspend fun overview(): AdminOverview

    /** Crea (`id == null`) o edita un registro con los valores del formulario. */
    suspend fun save(section: AdminSection, id: String?, values: Map<String, String>): AppResult<Unit>

    /** Sube una imagen elegida del dispositivo y devuelve su URL pública. */
    suspend fun uploadImage(uri: String): AppResult<String>

    /** Sube el PDF original del plano a la biblioteca del proyecto y devuelve su URL. */
    suspend fun uploadPdf(uri: String, fileName: String): AppResult<String>
}
