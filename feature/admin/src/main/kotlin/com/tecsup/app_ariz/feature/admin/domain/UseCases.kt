package com.tecsup.app_ariz.feature.admin.domain

import com.tecsup.app_ariz.core.common.AppResult
import javax.inject.Inject

class LoadAdminSectionUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(section: AdminSection, filterIndex: Int = 0): AppResult<List<AdminItem>> =
        repository.load(section, filterIndex)
}

class RunAdminActionUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(action: AdminAction, id: String, reason: String? = null): AppResult<String?> =
        repository.run(action, id, reason)
}

class DownloadAdminExportUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(export: AdminExport): AppResult<String> = repository.download(export)
}

class LoadAdminAnalyticsUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(): AppResult<AnalyticsRaw> = repository.analytics()
}

class LoadAdminOptionsUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(remote: RemoteOptions): AppResult<List<AdminChoice>> = repository.options(remote)
}

class LoadAdminDetailUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(section: AdminSection, id: String): AppResult<AdminItem> = repository.detail(section, id)
}

class LoadSitePlanUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(projectId: String): AppResult<SitePlanDoc> = repository.loadSitePlan(projectId)
}

class SaveSitePlanUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(projectId: String, revision: Int, plan: kotlinx.serialization.json.JsonObject, publish: Boolean): AppResult<SitePlanSaved> =
        repository.saveSitePlan(projectId, revision, plan, publish)
}

class ImportLotsExcelUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(projectId: String, uri: String): AppResult<List<Map<String, String>>> = repository.importLotsExcel(projectId, uri)
}

class RenderPdfPageUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(uri: String, page: Int): AppResult<RenderedPage> = repository.renderPdfPage(uri, page)
}

class UploadSizedImageUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(uri: String): AppResult<SizedImage> = repository.uploadSizedImage(uri)
}

class UploadPlanPdfUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(uri: String, fileName: String): AppResult<String> = repository.uploadPdf(uri, fileName)
}

class LoadAdminOverviewUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(): AdminOverview = repository.overview()
}

class SaveAdminRecordUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(section: AdminSection, id: String?, values: Map<String, String>): AppResult<Unit> =
        repository.save(section, id, values)
}

class UploadAdminImageUseCase @Inject constructor(private val repository: AdminRepository) {
    suspend operator fun invoke(uri: String): AppResult<String> = repository.uploadImage(uri)
}
