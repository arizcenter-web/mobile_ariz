package com.tecsup.app_ariz.feature.admin.data

import kotlinx.serialization.json.JsonObject
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.QueryMap
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Cliente genérico del panel de administración: cada sección declara su ruta (ver `AdminSections`), así que un solo
 * par de métodos cubre todos los listados y acciones. Todas las rutas son relativas a la base de la API.
 */
internal interface AdminApi {
    @GET
    suspend fun list(@Url path: String, @QueryMap query: Map<String, String>): ResponseBody

    @HTTP(method = "PATCH", hasBody = true)
    suspend fun patch(@Url path: String, @Body body: JsonObject): ResponseBody

    @Multipart
    @POST
    suspend fun upload(@Url path: String, @Part file: MultipartBody.Part): ResponseBody

    @HTTP(method = "PUT", hasBody = true)
    suspend fun put(@Url path: String, @Body body: JsonObject): ResponseBody

    @HTTP(method = "POST", hasBody = true)
    suspend fun post(@Url path: String, @Body body: JsonObject): ResponseBody

    /** Archivos grandes (Excel/PDF): se leen en flujo, no en memoria. */
    @Streaming
    @GET
    suspend fun download(@Url path: String): ResponseBody

    @HTTP(method = "DELETE")
    suspend fun delete(@Url path: String): ResponseBody
}
