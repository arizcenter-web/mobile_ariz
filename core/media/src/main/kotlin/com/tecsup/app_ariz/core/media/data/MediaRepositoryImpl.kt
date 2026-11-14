package com.tecsup.app_ariz.core.media.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.media.MediaRepository
import com.tecsup.app_ariz.core.media.UploadedImage
import com.tecsup.app_ariz.core.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject

private const val MaxSide = 1600
private const val Quality = 82

internal class MediaRepositoryImpl @Inject constructor(
    private val api: CloudinaryApi,
    @ApplicationContext private val context: Context,
) : MediaRepository {

    override suspend fun uploadImage(uri: String, folder: String): AppResult<UploadedImage> {
        val bytes = withContext(Dispatchers.IO) { readCompressed(Uri.parse(uri)) }
            ?: return AppResult.Failure(AppError.Validation("No se pudo leer la imagen seleccionada"))

        val signature = when (val result = safeApiCall { api.signature(SignatureRequest(folder)) }) {
            is AppResult.Failure -> return result
            is AppResult.Success -> result.data
        }
        val text = "text/plain".toMediaType()
        val file = MultipartBody.Part.createFormData("file", "foto.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
        return safeApiCall {
            api.upload(
                url = "https://api.cloudinary.com/v1_1/${signature.cloudName}/image/upload",
                file = file,
                apiKey = signature.apiKey.toRequestBody(text),
                timestamp = signature.timestamp.toString().toRequestBody(text),
                folder = signature.folder.toRequestBody(text),
                signature = signature.signature.toRequestBody(text),
            )
        }.map { UploadedImage(url = it.secure_url, publicId = it.public_id) }
    }

    override suspend fun uploadRawFile(uri: String, folder: String, fileName: String, mimeType: String): AppResult<UploadedImage> {
        val bytes = withContext(Dispatchers.IO) {
            runCatching { context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() } }.getOrNull()
        } ?: return AppResult.Failure(AppError.Validation("No se pudo leer el archivo seleccionado"))

        val signature = when (val result = safeApiCall { api.signature(SignatureRequest(folder)) }) {
            is AppResult.Failure -> return result
            is AppResult.Success -> result.data
        }
        val text = "text/plain".toMediaType()
        val file = MultipartBody.Part.createFormData("file", fileName, bytes.toRequestBody(mimeType.toMediaType()))
        return safeApiCall {
            api.upload(
                url = "https://api.cloudinary.com/v1_1/${signature.cloudName}/raw/upload",
                file = file,
                apiKey = signature.apiKey.toRequestBody(text),
                timestamp = signature.timestamp.toString().toRequestBody(text),
                folder = signature.folder.toRequestBody(text),
                signature = signature.signature.toRequestBody(text),
            )
        }.map { UploadedImage(url = it.secure_url, publicId = it.public_id) }
    }

    /** Reduce la imagen a 1600 px y JPEG al 82 %: sube rápido y no gasta el plan de datos del usuario. */
    private fun readCompressed(uri: Uri): ByteArray? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MaxSide * 2) sample *= 2
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val scale = MaxSide.toFloat() / maxOf(bitmap.width, bitmap.height)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else {
            bitmap
        }
        ByteArrayOutputStream().also { scaled.compress(Bitmap.CompressFormat.JPEG, Quality, it) }.toByteArray()
    }.getOrNull()
}
