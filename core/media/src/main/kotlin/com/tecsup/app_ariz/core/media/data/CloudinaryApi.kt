package com.tecsup.app_ariz.core.media.data

import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

@Serializable
internal data class SignatureRequest(val folder: String)

@Serializable
internal data class SignatureDto(
    val cloudName: String = "",
    val apiKey: String = "",
    val timestamp: Long = 0,
    val folder: String = "",
    val signature: String = "",
)

@Serializable
internal data class CloudinaryUploadDto(
    val secure_url: String = "",
    val public_id: String = "",
)

internal interface CloudinaryApi {

    @POST("media/cloudinary/signature")
    suspend fun signature(@Body body: SignatureRequest): SignatureDto

    /** La URL es absoluta (api.cloudinary.com), no la base de la API de ARIZ. */
    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part file: MultipartBody.Part,
        @Part("api_key") apiKey: RequestBody,
        @Part("timestamp") timestamp: RequestBody,
        @Part("folder") folder: RequestBody,
        @Part("signature") signature: RequestBody,
    ): CloudinaryUploadDto
}
