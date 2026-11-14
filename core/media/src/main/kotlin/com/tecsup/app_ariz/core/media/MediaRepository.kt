package com.tecsup.app_ariz.core.media

import com.tecsup.app_ariz.core.common.AppResult

/** Foto ya subida a Cloudinary. */
data class UploadedImage(val url: String, val publicId: String)

/**
 * Subida de imágenes del usuario (fotos de propiedades, comprobantes de pago...). El backend firma la
 * subida (`POST /media/cloudinary/signature`) y la imagen va directo a Cloudinary.
 */
interface MediaRepository {
    /** Sube la imagen de [uri] (contenido de la galería) a [folder] y devuelve su URL pública. */
    suspend fun uploadImage(uri: String, folder: String): AppResult<UploadedImage>

    /** Sube el archivo de [uri] tal cual (p. ej. un PDF) como recurso "raw" de Cloudinary. */
    suspend fun uploadRawFile(uri: String, folder: String, fileName: String, mimeType: String): AppResult<UploadedImage>
}
