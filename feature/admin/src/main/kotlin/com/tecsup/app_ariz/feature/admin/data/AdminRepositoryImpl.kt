package com.tecsup.app_ariz.feature.admin.data

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.media.MediaRepository
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.feature.admin.domain.AdminAction
import com.tecsup.app_ariz.feature.admin.domain.AdminItem
import com.tecsup.app_ariz.feature.admin.domain.AdminOverview
import com.tecsup.app_ariz.feature.admin.domain.AdminRepository
import com.tecsup.app_ariz.feature.admin.domain.AdminSection
import com.tecsup.app_ariz.feature.admin.domain.AdminSections
import com.tecsup.app_ariz.feature.admin.domain.HttpVerb
import com.tecsup.app_ariz.feature.admin.domain.INVALID_PREFIX
import com.tecsup.app_ariz.feature.admin.domain.SingletonSpec
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import android.content.Context
import com.tecsup.app_ariz.feature.admin.domain.AdminChoice
import com.tecsup.app_ariz.feature.admin.domain.AdminExport
import com.tecsup.app_ariz.feature.admin.domain.RemoteOptions
import com.tecsup.app_ariz.feature.admin.domain.AnalyticsRaw
import com.tecsup.app_ariz.feature.admin.domain.RenderedPage
import com.tecsup.app_ariz.feature.admin.domain.SitePlanDoc
import com.tecsup.app_ariz.feature.admin.domain.SitePlanSaved
import com.tecsup.app_ariz.feature.admin.domain.SizedImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

internal class AdminRepositoryImpl @Inject constructor(
    private val api: AdminApi,
    private val json: Json,
    private val media: MediaRepository,
    @ApplicationContext private val context: Context,
) : AdminRepository {

    override suspend fun load(section: AdminSection, filterIndex: Int): AppResult<List<AdminItem>> {
        val filter = section.filters.getOrNull(filterIndex)
        // El tamaño de página evita que el backend corte la lista en 9-10 registros; los endpoints sin paginación lo ignoran.
        val query = mapOf("page" to "1", "pageSize" to PAGE_SIZE.toString()) + filter?.query.orEmpty()
        val path = filter?.path ?: section.path
        return safeApiCall { api.list(path, query).string() }.map { body ->
            val single = section.singleton
            if (single != null) listOfNotNull(singletonRow(body, single)?.toAdminItem(section))
            else parseRows(body, json).mapNotNull { it.toAdminItem(section) }
        }
    }

    /** Convierte `{ rowsKey: [{status, points}] }` en una sola fila `{ id: config, CONTACTED: 5, ... }`. */
    private fun singletonRow(body: String, spec: SingletonSpec): JsonObject? {
        val root = json.parseToJsonElement(body) as? JsonObject ?: return null
        val rows = (root[spec.rowsKey] as? JsonArray)?.filterIsInstance<JsonObject>() ?: return null
        return buildJsonObject {
            put("id", "config")
            put("title", "Valores generales")
            rows.forEach { row ->
                val key = (row[spec.keyPath] as? JsonPrimitive)?.content ?: return@forEach
                row[spec.valuePath]?.let { put(key, it) }
            }
        }
    }

    override suspend fun run(action: AdminAction, id: String, reason: String?): AppResult<String?> {
        val path = action.pathFor(id)
        val body = action.body.toJson(action.reasonKey, reason)
        return safeApiCall {
            when (action.verb) {
                HttpVerb.PATCH -> api.patch(path, body)
                HttpVerb.POST -> api.post(path, body)
                HttpVerb.DELETE -> api.delete(path)
                HttpVerb.GET -> api.list(path, emptyMap())
            }.string()
        }.map { raw -> action.resultKey?.let { key -> resultValue(raw, key) } }
    }

    private fun resultValue(raw: String, key: String): String? = runCatching {
        (json.parseToJsonElement(raw) as? JsonObject)?.get(key)?.let { (it as? JsonPrimitive)?.content }
    }.getOrNull()

    override suspend fun save(section: AdminSection, id: String?, values: Map<String, String>): AppResult<Unit> {
        val spec = section.form ?: return AppResult.Failure(AppError.Unknown())
        val (body, invalid) = buildFormBody(spec, values, editing = id != null)
        if (invalid.isNotEmpty()) return AppResult.Failure(AppError.Validation(INVALID_PREFIX + invalid.joinToString(",")))
        return safeApiCall {
            if (section.formIdless) api.patch(section.formPath ?: section.path, body)
            else if (id == null) api.post(section.formPath ?: section.path, body) else api.patch("${section.formPath ?: section.path}/$id${section.formSuffix}", body)
        }.map { }
    }

    override suspend fun loadSitePlan(projectId: String): AppResult<SitePlanDoc> =
        safeApiCall { api.list("admin/projects/$projectId/site-plan", emptyMap()).string() }.let { result ->
            when (result) {
                is AppResult.Failure -> result
                is AppResult.Success -> runCatching { parseSitePlan(result.data, projectId) }.fold(
                    onSuccess = { AppResult.Success(it) },
                    onFailure = { AppResult.Failure(AppError.Unknown()) },
                )
            }
        }

    private fun parseSitePlan(body: String, projectId: String): SitePlanDoc {
        val root = json.parseToJsonElement(body) as JsonObject
        val project = root["project"] as? JsonObject
        val location = project?.get("location") as? JsonObject
        fun text(obj: JsonObject?, key: String) = (obj?.get(key) as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content
        return SitePlanDoc(
            projectId = projectId,
            projectName = text(project, "name").orEmpty(),
            category = text(project, "category"),
            projectStatus = text(project, "status"),
            currency = text(project, "currency") ?: "PEN",
            latitude = text(location, "latitude")?.toDoubleOrNull(),
            longitude = text(location, "longitude")?.toDoubleOrNull(),
            revision = text(root, "revision")?.toIntOrNull() ?: 0,
            publishedAt = text(root, "publishedAt"),
            plan = root["plan"] as? JsonObject ?: JsonObject(emptyMap()),
        )
    }

    override suspend fun saveSitePlan(projectId: String, revision: Int, plan: JsonObject, publish: Boolean): AppResult<SitePlanSaved> {
        val body = buildJsonObject {
            put("revision", revision)
            put("plan", plan)
            put("publish", publish)
        }
        return safeApiCall { api.put("admin/projects/$projectId/site-plan", body).string() }.map { raw ->
            val obj = runCatching { json.parseToJsonElement(raw) as? JsonObject }.getOrNull()
            SitePlanSaved(
                revision = (obj?.get("revision") as? JsonPrimitive)?.content?.toIntOrNull() ?: (revision + 1),
                publishedAt = (obj?.get("publishedAt") as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content,
            )
        }
    }

    override suspend fun importLotsExcel(projectId: String, uri: String): AppResult<List<Map<String, String>>> {
        val parsed = withContext(Dispatchers.IO) {
            runCatching {
                val resolver = context.contentResolver
                val target = android.net.Uri.parse(uri)
                val name = resolver.query(target, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                    if (c.moveToFirst()) c.getString(0) else null
                }?.takeIf { it.lowercase().endsWith(".xlsx") } ?: "lotes.xlsx"
                val bytes = resolver.openInputStream(target)?.use { it.readBytes() } ?: error("sin archivo")
                name to bytes
            }.getOrNull()
        } ?: return AppResult.Failure(AppError.Validation("No se pudo leer el archivo."))
        val part = okhttp3.MultipartBody.Part.createFormData(
            "file", parsed.first,
            parsed.second.toRequestBody("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".toMediaType()),
        )
        return safeApiCall { api.upload("admin/projects/$projectId/site-plan/import", part).string() }.map { raw ->
            val rows = (json.parseToJsonElement(raw) as? JsonObject)?.get("rows") as? JsonArray
            rows.orEmpty().filterIsInstance<JsonObject>().map { row ->
                row.mapValues { (_, v) -> (v as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content.orEmpty() }
            }
        }
    }

    override suspend fun renderPdfPage(uri: String, page: Int): AppResult<RenderedPage> = withContext(Dispatchers.IO) {
        runCatching {
            val descriptor = context.contentResolver.openFileDescriptor(android.net.Uri.parse(uri), "r") ?: error("sin archivo")
            descriptor.use { fd ->
                android.graphics.pdf.PdfRenderer(fd).use { renderer ->
                    val count = renderer.pageCount
                    if (page < 1 || page > count) return@withContext AppResult.Failure(AppError.Validation("El PDF tiene $count páginas: elige entre 1 y $count."))
                    renderer.openPage(page - 1).use { p ->
                        // Hasta 2400 px del lado largo: suficiente para ver el plano sin gastar memoria.
                        val scale = 2400f / maxOf(p.width, p.height)
                        val bitmap = android.graphics.Bitmap.createBitmap((p.width * scale).toInt().coerceAtLeast(1), (p.height * scale).toInt().coerceAtLeast(1), android.graphics.Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        p.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        val file = File(File(context.cacheDir, "exports").apply { mkdirs() }, "plano-pdf-$page.png")
                        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                        bitmap.recycle()
                        AppResult.Success(RenderedPage(android.net.Uri.fromFile(file).toString(), count))
                    }
                }
            }
        }.getOrElse { AppResult.Failure(AppError.Validation("No se pudo leer el PDF.")) }
    }

    override suspend fun uploadSizedImage(uri: String): AppResult<SizedImage> {
        val size = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(android.net.Uri.parse(uri))?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
                bounds.outWidth to bounds.outHeight
            }.getOrNull()
        }
        return media.uploadImage(uri, folder = "ariz/admin").map { SizedImage(it.url, size?.first?.takeIf { w -> w > 0 } ?: 1000, size?.second?.takeIf { h -> h > 0 } ?: 1000) }
    }

    override suspend fun detail(section: AdminSection, id: String): AppResult<AdminItem> {
        val path = section.detailPath?.replace("{id}", id) ?: return AppResult.Failure(AppError.NotFound())
        return safeApiCall { api.list(path, emptyMap()).string() }.let { result ->
            when (result) {
                is AppResult.Failure -> result
                is AppResult.Success -> {
                    val obj = runCatching { json.parseToJsonElement(result.data) as? JsonObject }.getOrNull()
                    // Algunos detalles no repiten el id: se inyecta para armar el registro.
                    val withId = obj?.let { JsonObject(it + ("id" to JsonPrimitive(id))) }
                    val item = withId?.toAdminItem(section, section.detailChildren)
                    if (item != null) AppResult.Success(item) else AppResult.Failure(AppError.Unknown())
                }
            }
        }
    }

    override suspend fun options(remote: RemoteOptions): AppResult<List<AdminChoice>> =
        safeApiCall { api.list(remote.path, emptyMap()).string() }.map { body ->
            parseRows(body, json).mapNotNull { row ->
                val value = row.firstText(listOf(remote.valueKey)) ?: return@mapNotNull null
                AdminChoice(value, row.firstText(listOf(remote.labelKey)) ?: value)
            }
        }

    override suspend fun analytics(): AppResult<AnalyticsRaw> = coroutineScope {
        suspend fun fetch(path: String, query: Map<String, String> = emptyMap()) = safeApiCall { api.list(path, query).string() }
        val pro = async { fetch("admin/professionals", mapOf("page" to "1", "pageSize" to "200")) }
        val props = async { fetch("properties", mapOf("sort" to "recent", "page" to "1", "pageSize" to "200")) }
        val projs = async { fetch("projects", mapOf("page" to "1", "pageSize" to "200")) }
        val subs = async { fetch("admin/subscriptions", mapOf("page" to "1", "pageSize" to "200")) }
        val dist = async { fetch("admin/professionals/distribution") }
        val results = listOf(pro.await(), props.await(), projs.await(), subs.await(), dist.await())
        fun rows(result: AppResult<String>) = (result as? AppResult.Success)?.data?.let { runCatching { parseRows(it, json) }.getOrNull() }.orEmpty()
        if (results.all { it is AppResult.Failure }) return@coroutineScope (results.first() as AppResult.Failure)
        AppResult.Success(
            AnalyticsRaw(
                professionals = rows(results[0]).map { it.toAnalyticsRow(listOf("type")) },
                properties = rows(results[1]).map { it.toAnalyticsRow(listOf("agent.type")) },
                projects = rows(results[2]).map { it.toAnalyticsProject() },
                subscriptions = rows(results[3]).map { it.toAnalyticsRow(listOf("agentProfile.type")) },
                distribution = (results[4] as? AppResult.Success)?.data?.let { runCatching { parseDistribution(it, json) }.getOrNull() }.orEmpty(),
            ),
        )
    }

    override suspend fun download(export: AdminExport): AppResult<String> = safeApiCall {
        withContext(Dispatchers.IO) {
            val body = api.download(export.path)
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(dir, export.fileName)
            body.byteStream().use { input -> file.outputStream().use { input.copyTo(it) } }
            file.absolutePath
        }
    }

    override suspend fun uploadPdf(uri: String, fileName: String): AppResult<String> =
        media.uploadRawFile(uri, folder = "ariz/projects", fileName = fileName, mimeType = "application/pdf").map { it.url }

    override suspend fun uploadImage(uri: String): AppResult<String> =media.uploadImage(uri, folder = "ariz/admin").map { it.url }

    override suspend fun overview(): AdminOverview {
        suspend fun count(key: String, filter: Int = 0): Int? =
            AdminSections.byKey(key)?.let { (load(it, filter) as? AppResult.Success)?.data?.size }
        return coroutineScope {
            val pendingProfessionals = async { count("professionals", 0) }
            val pendingSubscriptions = async { count("subscriptions") }
            val pendingRedemptions = async { count("redemptions", 0) }
            val professionals = async { count("professionals", 3) }
            val projects = async { count("projects") }
            val claims = async { count("claims") }
            AdminOverview(
                pendingProfessionals = pendingProfessionals.await(),
                pendingSubscriptions = pendingSubscriptions.await(),
                pendingRedemptions = pendingRedemptions.await(),
                professionals = professionals.await(),
                projects = projects.await(),
                claims = claims.await(),
            )
        }
    }

    private companion object {
        const val PAGE_SIZE = 50
    }
}
