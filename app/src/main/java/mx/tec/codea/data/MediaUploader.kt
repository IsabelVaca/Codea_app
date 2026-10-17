package mx.tec.codea.data

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.UploadBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

// subir un archivo son tres pasos:
// 1) pedir permiso a la API  2) mandar el archivo al almacenamiento (S3)  3) avisar a la API que ya está.
// regresa el media_id, que se liga a un reporte o a un documento.
object MediaUploader {

    // sin el token de la API: el almacenamiento tiene su propia firma.
    private val storage = OkHttpClient()

    // kind = "PHOTO" o "DOCUMENT"
    suspend fun upload(bytes: ByteArray, contentType: String, kind: String): Int {
        val permiso = ApiClient.api.requestUpload(UploadBody(contentType, bytes.size, kind))

        withContext(Dispatchers.IO) {
            val form = MultipartBody.Builder().setType(MultipartBody.FORM)
            permiso.fields.forEach { (campo, valor) -> form.addFormDataPart(campo, valor) }
            // el archivo va al final, como pide S3.
            form.addFormDataPart("file", "archivo", bytes.toRequestBody(contentType.toMediaType()))
            val peticion = Request.Builder().url(permiso.url).post(form.build()).build()
            storage.newCall(peticion).execute().use { respuesta ->
                if (!respuesta.isSuccessful) {
                    throw IOException("No se pudo subir el archivo (${respuesta.code})")
                }
            }
        }

        ApiClient.api.completeMedia(permiso.mediaId)
        return permiso.mediaId
    }

    suspend fun uploadPhoto(bitmap: Bitmap): Int {
        val bytes = withContext(Dispatchers.Default) {
            ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }.toByteArray()
        }
        return upload(bytes, "image/jpeg", "PHOTO")
    }
}
