package com.example.myvoronesh.features.album

import android.content.Context
import android.net.Uri
import com.example.myvoronesh.data.remote.ApiClient
import com.example.myvoronesh.data.remote.LocationPhotosDto
import com.example.myvoronesh.data.remote.PhotoDto
import com.example.myvoronesh.data.remote.PhotoUploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class PhotoRepository(private val context: Context) {

    init {
        ApiClient.init(context)
    }

    suspend fun uploadPhoto(
        imageUri: Uri,
        pointId: String,
        description: String = ""
    ): Result<PhotoUploadResponse> {
        return try {
            val tempFile = createTempFileFromUri(imageUri)
                ?: return Result.failure(Exception("Не удалось прочитать файл"))

            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("photo", tempFile.name, requestFile)

            val response = ApiClient.apiService.uploadPhoto(
                pointId = pointId.toRequestBody("text/plain".toMediaTypeOrNull()),
                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                photo = photoPart
            )

            tempFile.delete()

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Ошибка загрузки"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyPhotos(): Result<List<LocationPhotosDto>> {
        return try {
            val response = ApiClient.apiService.getMyPhotos()
            if (response.success && response.data != null) {
                Result.success(response.data.data)
            } else {
                Result.failure(Exception(response.message ?: "Ошибка загрузки фото"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePhoto(photoId: String): Result<Boolean> {
        return try {
            val response = ApiClient.apiService.deletePhoto(photoId)

            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Ошибка удаления"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPhotosForPoint(pointId: String): Result<List<PhotoDto>> {
        return try {
            val response = ApiClient.apiService.getPointPhotos(pointId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Ошибка загрузки фото"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            val mimeType = context.contentResolver.getType(uri)
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/gif" -> "gif"
                else -> "jpg"
            }

            val tempFile = File.createTempFile("upload_", ".$extension", context.cacheDir)

            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
